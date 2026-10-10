package me.dedushka.monkeyJailGradle263.Util;

import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import me.dedushka.monkeyJailGradle263.Classes.BlockPosClass;
import me.dedushka.monkeyJailGradle263.Classes.JailClass;
import me.dedushka.monkeyJailGradle263.Classes.MonkeyClass;
import me.dedushka.monkeyJailGradle263.Classes.ShreakPlayersClass;
import me.dedushka.monkeyJailGradle263.Commands.JailCommandsExecutors;
import me.dedushka.monkeyJailGradle263.Config.Config;
import me.dedushka.monkeyJailGradle263.Listeners.EventListener;
import me.dedushka.monkeyJailGradle263.Listeners.ShreakingListener;
import me.dedushka.monkeyJailGradle263.MonkeyJailGradle263;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.skinsrestorer.api.property.SkinApplier;
import net.skinsrestorer.api.storage.PlayerStorage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
//import me.dedushka.monkeyJailGradle263.Util.DataBaseManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static org.bukkit.Bukkit.getLogger;
import me.dedushka.monkeyJailGradle263.Util.DiscordSRVManager;

public class JailLogic {
    public static DataBaseManager DBM = new DataBaseManager();
    public static HashMap<String, MonkeyClass> monkeyList = DBM.getAllMonkeys(-1, -1);
    public static HashMap<String, MonkeyClass> updatedTimeMonkeys = new HashMap<>();
    public static HashMap<String, JailClass> jails;
    public static MonkeyJailGradle263 MJ;
    public static long ticks = 0;
    public static boolean isTimeLeftUpdated = false;
    public static HashMap<String, ShreakPlayersClass> shreakPlayers = new HashMap<>();
    public static ArrayList<MonkeyClass> monkeysToRemove = new ArrayList<>();
    public static HashMap<String, HashMap<String, ArrayList<Integer>>> showJails = new HashMap<>();
    public static DiscordSRVManager discordSRVManager = new DiscordSRVManager();
    public static ShreakingListener shreakingListener = new ShreakingListener();


    public JailLogic(MonkeyJailGradle263 MJ) {
        this.MJ = MJ;
        updateMonkeyList();
        updateJailList();
    }

    public JailLogic() {
    }

    public static void updateMonkeyList() {
        monkeyList = DBM.getAllMonkeys(-1, -1);
    }

    public static void updateJailList() {
        jails = DBM.loadAllJails();
    }


    public void startJail() {
        GlobalRegionScheduler globalScheduler = Bukkit.getServer().getGlobalRegionScheduler();
        globalScheduler.runAtFixedRate(MJ, task -> {
            ticks += Config.globalTimerDelay;
            if (ticks % Config.doEveryLoopDelay == 0) {
                iterateMonkeysToRemove();
                doEveryLoop(ticks);
            }
            //каждую секунду
            if (ticks % Config.shreakTimerDelay == 0) {
                doGlobalTimerShreaker();
            }
            if (ticks % 1200 == 0 && isTimeLeftUpdated) {
                isTimeLeftUpdated = false;
                DBM.updateMonkeyTable(updatedTimeMonkeys);
            }
        }, 1L, Config.globalTimerDelay);
    }

    public static void reloadPlugin(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().getSender().sendRichMessage("<green>Начинаю перезагрузку плагина...");
        MJ.reloadConfig();
        tpAllFromShreakingMachine();
        updateJailList();
        updateMonkeyList();
        Config.updateConfigValues();

        HandlerList.unregisterAll(MonkeyJailGradle263.instance);

        MJ.checkDataBase();
        discordSRVManager.updateValues();
        MJ.checkSkinsRestorer();

        JailCommandsExecutors.response = null;

        MonkeyJailGradle263.eventListener = new EventListener();
        MJ.getServer().getPluginManager().registerEvents(MonkeyJailGradle263.eventListener, MJ);
        ctx.getSource().getSender().sendRichMessage("<green>Плагин перезагружен");
    }

    public static void iterateMonkeysToRemove() {
        for (MonkeyClass monkey : monkeysToRemove) {
            Player player = Bukkit.getPlayer(monkey.username);
            if (player != null) {
                player.getScheduler().run(MonkeyJailGradle263.instance, task -> {
                    removeFromMonkeys(monkey.username);
                }, null);
            }
        }
        monkeysToRemove.clear();
    }


    //что делать каждую итерацию
    public void doEveryLoop(long ticks) {

        for (MonkeyClass monkey : monkeyList.values()) {
            if (shreakPlayers.containsKey(monkey.username)) {
                continue;
            }
            Player player = Bukkit.getPlayer(monkey.username);
            if (player == null || player.isDead()) {
                continue;
            }
            if (monkey.time_left <= 0) {
                if (!monkeysToRemove.contains(monkey)) {
                    monkeysToRemove.add(monkey);
                }
                continue;
            }


            if(player.getWorld().getName().equals(Config.monkeySurvWorldName)){
                if(ticks%((long) Config.doEveryLoopDelay *Config.survWorldTimerSpeed)==0){
                    monkey.time_left -= Config.doEveryLoopDelay;
                }
            }
            else {
                monkey.time_left -= Config.doEveryLoopDelay;
            }

            if (monkey.time_to_surv > 0) {
                monkey.time_to_surv -= Config.doEveryLoopDelay;
            }

            updatedTimeMonkeys.put(monkey.username, monkey);
            isTimeLeftUpdated = true;

            if (player.getWorld().getName().equals(Config.monkeySurvWorldName)) continue;

            Location pL = player.getLocation();
            int x = (int) Math.floor(pL.getX());
            int y = (int) Math.floor(pL.getY());
            int z = (int) Math.floor(pL.getZ());
            BlockPosClass playerPos = new BlockPosClass(x, y, z);
            JailClass jail = jails.get(monkey.jail_name);
            if (jail != null && !jail.blocks.contains(playerPos)) {
                if(player.isInsideVehicle()){
                    player.leaveVehicle();
                }
                player.getScheduler().run(MJ, task -> {
                    player.teleportAsync(new Location(
                            Bukkit.getWorld(jail.world),
                            jail.spawnBlock.x,
                            jail.spawnBlock.y,
                            jail.spawnBlock.z
                    ));
                }, null);
            }


        }

    }

    //процесс удаления обезьяны из тюрьмы
    public static void removeFromMonkeys(String username) {
        if (Bukkit.getPlayer(username) == null) {
            return;
        }
        DBM.removeMonkey(username);
        monkeyList.remove(username);

        Location location;
        try {
            location = Bukkit.getPlayer(username).getRespawnLocation();
        } catch (Exception e) {
            location = Bukkit.getPlayer(username).getWorld().getSpawnLocation();
        }
        if (location == null) {
            location = Bukkit.getPlayer(username).getWorld().getSpawnLocation();
        }
        Bukkit.getPlayer(username).teleportAsync(location);


        if (MJ.skinsRestorerAPI != null) {
            try {
                PlayerStorage playerStorage = MonkeyJailGradle263.skinsRestorerAPI.getPlayerStorage();
                SkinApplier<Player> applier = MonkeyJailGradle263.skinsRestorerAPI.getSkinApplier(Player.class);
                playerStorage.removeSkinIdOfPlayer(Bukkit.getPlayer(username).getUniqueId());
                applier.applySkin(Bukkit.getPlayer(username));
            } catch (Exception e) {
                getLogger().info("Не удалось установить скин игроку " + username);
            }
        }

    }


    public static void createShreakProcess(Player player, Player player_monkey) {
        Bukkit.getGlobalRegionScheduler().execute(MJ, () -> {
            if (MonkeyJailGradle263.trahodromWorld == null) {
                player.sendMessage("§cНе удалось создать или загрузить мир!");
                return;
            }

            Location teleportLocation = new Location(MJ.trahodromWorld, 8.5, 64, 8.5, -90, 0);
            PluginManager pM = MJ.getInstance().getServer().getPluginManager();

            if (!ShreakingListener.isActive) {
                pM.registerEvents(shreakingListener, MJ.getInstance());
                ShreakingListener.isActive = true;
            }

            Location playerLocation = player.getLocation();
            Location monkeyLocation = player_monkey.getLocation();

            BossBar halfBar = BossBar.bossBar(Component.text("Осталось 10 секунд"), 1f, BossBar.Color.GREEN, BossBar.Overlay.NOTCHED_10);
            player_monkey.showBossBar(halfBar);
            player.showBossBar(halfBar);

            ShreakPlayersClass shreakMonkeyObject = new ShreakPlayersClass(Config.timeInShreakingMachine, player_monkey.getName(), monkeyLocation, true, halfBar, player.getName(), new HashSet<>());
            ShreakPlayersClass shreakPlayerObject = new ShreakPlayersClass(Config.timeInShreakingMachine, player.getName(), playerLocation, false, halfBar, player_monkey.getName(), new HashSet<>());

            JailLogic.shreakPlayers.put(player_monkey.getName(), shreakMonkeyObject);
            JailLogic.shreakPlayers.put(player.getName(), shreakPlayerObject);

            player_monkey.teleportAsync(teleportLocation);
            player.teleportAsync(new Location(MonkeyJailGradle263.trahodromWorld, 7, 64, 4, 0, 0));

            player_monkey.setSneaking(true);

            hideShreakPlayers(player);
            hideShreakPlayers(player_monkey);

        });


    }

    //спрятать игроков, которые уже находятся на траходроме(траходром общий для всех)
    public static void hideShreakPlayers(Player player) {
        {
            if (player == null) return;
            String partnerUsername = shreakPlayers.get(player.getName()).partnerUsername;
            for (ShreakPlayersClass shreakPlayer : shreakPlayers.values()) {
                if (shreakPlayer.username.equals(partnerUsername)) continue;
                if (shreakPlayer.username.equals(player.getName())) continue;

                Player shreakPlayer_player = Bukkit.getPlayer(shreakPlayer.username);
                if (shreakPlayer_player != null) {
                    shreakPlayer_player.hidePlayer(player);
                    shreakPlayer.hiddenPlayers.add(player.getName());

                    player.hidePlayer(shreakPlayer_player);
                    shreakPlayers.get(player.getName()).hiddenPlayers.add(shreakPlayer.username);
                }

            }
        }


//        for(ShreakPlayersClass shreakPlayer : shreakPlayers.values()){
//            String partnerUsername = shreakPlayer.partnerUsername;
//            if(Bukkit.getPlayer(partnerUsername)!=null) {
//                if (Bukkit.getPlayer(shreakPlayer.username) != null) {
//                    if (!shreakPlayer.username.equals(player.getName()) && !shreakPlayer.username.equals(partnerUsername)) {
//                        if(player!=null) {
//                            player.hidePlayer(Bukkit.getPlayer(shreakPlayer.username));
//                            shreakPlayers.get(player.getName()).hiddenPlayers.add(shreakPlayer.username);
//                        }
//
//                        Player partnerPlayer = Bukkit.getPlayer(partnerUsername);
//                        partnerPlayer.hidePlayer(Bukkit.getPlayer(shreakPlayer.username));
//                        shreakPlayers.get(partnerPlayer).hiddenPlayers.add(shreakPlayer.username);
//
//                        Bukkit.getPlayer(shreakPlayer.username).hidePlayer(player);
//                        Bukkit.getPlayer(shreakPlayer.username).hidePlayer(partnerPlayer);
//
//                        shreakPlayer.hiddenPlayers.add(player.getName());
//                        shreakPlayer.hiddenPlayers.add(partnerUsername);
//                    }
//                }
//            }
    }





    public void doGlobalTimerShreaker(){
        for(ShreakPlayersClass shreakPlayer : shreakPlayers.values()) {
            if (shreakPlayer.time_left <= 0) {
                Player player = Bukkit.getPlayer(shreakPlayer.username);
                if(player!=null) {
                    player.sendMessage("§aВремя вышло!");


                    player.setSneaking(false);
                    player.teleportAsync(shreakPlayer.location);
                    player.hideBossBar(shreakPlayer.halfBar);
                    if (!shreakPlayer.hiddenPlayers.isEmpty()) {
                        //показать всех игроков обратно
                        for (String hiddenUsername : shreakPlayer.hiddenPlayers) {
                            if (Bukkit.getPlayer(hiddenUsername) != null) {
                                player.showPlayer(Bukkit.getPlayer(hiddenUsername));
                            }
                        }
                    }
                }

                shreakPlayers.remove(shreakPlayer.username);
                if(shreakPlayers.isEmpty()){
                    HandlerList.unregisterAll(shreakingListener);
                    ShreakingListener.isActive=false;
                }

                continue;
            }
            shreakPlayer.time_left-=Config.shreakTimerDelay;
            if(Bukkit.getPlayer(shreakPlayer.username)!=null) {
                if(shreakPlayer.isMonkey) {
                    Bukkit.getPlayer(shreakPlayer.username).setSneaking(true);
                }

                shreakPlayer.halfBar.progress((float) shreakPlayer.time_left / Config.timeInShreakingMachine);
                shreakPlayer.halfBar.name(Component.text("Осталось " + shreakPlayer.time_left / 20+ " секунд"));
            }

        }
    }

    public static void tpAllFromShreakingMachine(){
        for(String username : shreakPlayers.keySet()){
            if(Bukkit.getPlayer(username)!=null) {
                Bukkit.getPlayer(username).teleportAsync(shreakPlayers.get(username).location);
            }
        }
    }
}
