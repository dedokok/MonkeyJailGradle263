package me.dedushka.monkeyJailGradle263.Listeners;

//import io.canvasmc.canvas.event.world.WorldUnloadAsyncEvent;
import me.dedushka.monkeyJailGradle263.*;
import me.dedushka.monkeyJailGradle263.Classes.JailProcessClass;
import me.dedushka.monkeyJailGradle263.Classes.ShreakPlayersClass;
import me.dedushka.monkeyJailGradle263.Commands.JailCommandsExecutors;
import me.dedushka.monkeyJailGradle263.Config.Config;
import me.dedushka.monkeyJailGradle263.Util.JailLogic;
import me.dedushka.monkeyJailGradle263.Util.GenerateWorld.OneChunkWorldManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.server.level.ServerPlayer;
//import org.bukkit.entity.Enderman; // Интерфейс Bukkit из параметров/события
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.entity.CraftPlayer;
//import org.bukkit.entity.Enderman;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import static org.bukkit.Bukkit.getLogger;

public class EventListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        //установить скин обезьяны
        if(JailLogic.monkeyList.containsKey(player.getName())){
            try {
                JailCommandsExecutors.setSkinFromUrl(Bukkit.getPlayer(player.getName()), Config.defaultMonkeySkinURL);
            }
            catch(Exception e ){
                getLogger().warning("Не удалось получить дефолтный скин");
            }
        }
        //если зашедший игрок находится на траходроме, но не должен, его тп на место спавна
        if(player.getWorld().getName().equals("trahodrom")){
            if(!JailLogic.shreakPlayers.containsKey(player.getName())){
                Bukkit.getGlobalRegionScheduler().run(MonkeyJailGradle263.instance, task -> {
                    player.teleportAsync(Bukkit.getWorld("world").getSpawnLocation());
                });
            }
            else{
                JailLogic.hideShreakPlayers(player);
                player.showBossBar(JailLogic.shreakPlayers.get(player.getName()).halfBar);
            }
        }
        if(JailCommandsExecutors.jailsCreationProcesses.containsKey(player.getName())){
            player.sendMessage("У вас имеется процесс создания/редактирования клетки.");
            JailProcessClass jailProcess = JailCommandsExecutors.jailsCreationProcesses.get(player.getName());
            if(jailProcess.isShow){
                JailCommandsExecutors.showJailBorder(jailProcess,player);
            }
        }
    }


//    @EventHandler
//    public void onUseEvent(PlayerInteractEvent event){//запретить обезьянам использовать
//        if(JailLogic.monkeyList.containsKey(event.getPlayer().getName())){
//            event.setCancelled(true);
//        }
//    }
//    @EventHandler
//    public void onPlaceEvent(BlockPlaceEvent event){//запретить обезьянам ставить
//        if(JailLogic.monkeyList.containsKey(event.getPlayer().getName())){
//            event.setCancelled(true);
//        }
//    }

    @EventHandler
    public void playerDeathEvent(PlayerDeathEvent event) {//запретить обезьянам терять вещи при смерти
        Player player = event.getPlayer();
        if (player.getWorld().getName().equals(Config.monkeySurvWorldName)) return;
        if (JailLogic.monkeyList.containsKey(player.getName()) || JailLogic.shreakPlayers.containsKey(player.getName())) {
            processDeath(event);
        }
        if (JailLogic.shreakPlayers.containsKey(player.getName())) {
            ShreakPlayersClass shreaking_player = JailLogic.shreakPlayers.get(player.getName());
            shreaking_player.time_left = 0;
            getLogger().info("123");

            JailLogic.shreakPlayers.get(shreaking_player.partnerUsername).time_left = 0;
            io.papermc.paper.threadedregions.scheduler.ScheduledTask task =
                    Bukkit.getRegionScheduler().run(MonkeyJailGradle263.instance, player.getLocation(), scheduledTask -> {
                        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
                        net.minecraft.server.MinecraftServer.getServer().getPlayerList().respawn(
                                serverPlayer,
                                false,
                                net.minecraft.world.entity.Entity.RemovalReason.KILLED,
                                PlayerRespawnEvent.RespawnReason.DEATH
                        );
                    });

        }


//        for(Entity entity : Bukkit.getWorld("world_the_end").getEntities()){
//            if(entity instanceof net.minecraft.world.entity.monster.EnderMan enderman){
//
//                if(!entity.getWorld().getName().equals("world_the_end"))continue;
//                Random rand = new Random();
//                Collection<? extends Player> listCollection = Bukkit.getOnlinePlayers();
//                Player[] players = listCollection.toArray(new Player[0]);
//                int randomIndex = rand.nextInt(players.length);
//                Player randPlayer = players[randomIndex];
//
//                enderman.setScreaming(true);
//                enderman.isAngryAtAllPlayers()
//            }
//        }


    }
    void processDeath(PlayerDeathEvent event) {
        event.setKeepInventory(true);
        event.setKeepLevel(true);
        event.getDrops().clear();
        event.setDeathMessage(null);
        if (JailLogic.monkeyList.containsKey(event.getPlayer().getName())) {
            event.getPlayer().sendRichMessage("<red>Пока вы в стадии смерти, вам не засчитывается время");
        }
    }




    @EventHandler
    public void onHitEvent(EntityDamageByEntityEvent event){//запретить обезьянам бить
        Entity damager = event.getDamager();
        Entity victim = event.getEntity();
        if (damager instanceof Player && victim instanceof Player) {
            Player attacker = (Player) damager;
            Player target = (Player) victim;
            if(attacker.getWorld().getName().equals(Config.monkeySurvWorldName))return;
            if(JailLogic.monkeyList.containsKey(attacker.getName())){
                if(!JailLogic.monkeyList.containsKey(target.getName())){
                    event.setCancelled(true);
                }
            }
        }



    }

    @EventHandler
    public void onServerEnable(ServerLoadEvent event){
        //создание/загрузка мира траходрома
        boolean isTrahodromExists = worldFolderExists(Config.trahodromName);
        //я ебал эти шедулеры. Изза их логики тут будет пизда
        Bukkit.getRegionScheduler().run(MonkeyJailGradle263.instance, Bukkit.getWorld("world"), 0, 0, task -> {
            OneChunkWorldManager worldManager = new OneChunkWorldManager(MonkeyJailGradle263.instance);
            worldManager.createWorld(Config.trahodromName, "shreakmachine", false,world -> {
                if (world == null) {
                    Bukkit.getLogger().warning("Не удалось создать мир "+Config.trahodromName+"!");
                    return;
                }
                MonkeyJailGradle263.trahodromWorld=world;

                //тут делаю так, чтобы при создании мира активировался поршень
                if(!isTrahodromExists) {
                    int x = 2, y = 64, z = 8;
                    Bukkit.getRegionScheduler().execute(MonkeyJailGradle263.instance, world, x >> 4, z >> 4, () -> {
                        world.getBlockAt(x, y, z).setType(Material.REDSTONE_BLOCK);

                        Bukkit.getRegionScheduler().runDelayed(MonkeyJailGradle263.instance, world, x >> 4, z >> 4, task2 -> {
                            world.getBlockAt(x, y, z).setType(Material.AIR);
                        }, 2);
                    });
                }
            });
        });


        //создание/загрузка мира выживания обезьян
        Bukkit.getRegionScheduler().run(MonkeyJailGradle263.instance, Bukkit.getWorld("world"), 0, 0, task -> {
            OneChunkWorldManager worldManager = new OneChunkWorldManager(MonkeyJailGradle263.instance);
            worldManager.createWorld(Config.monkeySurvWorldName, "null", true,world -> {
                if (world == null) {
                    Bukkit.getLogger().warning("Не удалось создать мир "+Config.monkeySurvWorldName+"!");
                    return;
                }
                MonkeyJailGradle263.monkeySurvWorld=world;
            });
        });




    }
    public boolean worldFolderExists(String worldName) {
        File folder = new File(Bukkit.getWorldContainer(), worldName);
        return folder.exists() && folder.isDirectory();
    }

    @EventHandler
    public void worldUnloadEvent(WorldUnloadEvent event){
        JailCommandsExecutors.stopProcess(null);
    }

//    @EventHandler
//    public void playerQuitEvent(PlayerQuitEvent event){
//        Player player = event.getPlayer();
//        JailLogic.showJails.remove(player.getName());
//    }




    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {//запретить обезьяне на траходроме двигаться (можно немного ворочать камерой)
        ShreakPlayersClass shreakPlayer = JailLogic.shreakPlayers.get(event.getPlayer().getName());
        if(shreakPlayer!=null && shreakPlayer.isMonkey){
            Location from = event.getFrom();
            Location to = event.getTo();

            if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) {
                event.setCancelled(true);
                return;
            }
            if (to.getYaw()<-140 || to.getYaw()>-40) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {//запретить обезьяне на траходроме слазить с шифта
        //ShreakPlayersClass shreakPlayer = JailLogic.shreakPlayers.get(event.getPlayer().getName());
        Player player = event.getPlayer();

        if(!player.getWorld().getName().equals(Config.trahodromName))return;

        if (JailLogic.monkeyList.containsKey(player.getName())) {
            event.setCancelled(true);
            event.getPlayer().setSneaking(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){//запретить игрокам на траходроме ломать блоки
        Player player = event.getPlayer();

        if (player.getWorld().getName().equals(Config.trahodromName)) {
            boolean canBreak = player.hasPermission("monkeyjail.canbreaktrah");
            if (!canBreak) {
                event.setCancelled(true);
            }
            return;
        }
        if (JailLogic.monkeyList.containsKey(player.getName())) {
            if (!player.getWorld().getName().equals(Config.monkeySurvWorldName)) {
                event.setCancelled(true);
            }
        }
    }
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {//запретить игрокам на траходроме ставить блоки
        Player player = event.getPlayer();

        if (player.getWorld().getName().equals(Config.trahodromName)) {
            boolean canPlace = player.hasPermission("monkeyjail.canplacetrah");
            if (!canPlace) {
                event.setCancelled(true);
            }
            return;
        }
        if (JailLogic.monkeyList.containsKey(player.getName())) {
            if (!player.getWorld().getName().equals(Config.monkeySurvWorldName)) {
                event.setCancelled(true);
            }
        }

    }
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event){//запретить игрокам на траходроме использовать предметы
        Player player = event.getPlayer();
        //if(!player.getWorld().getName().equals(Config.trahodromName))return;
        if(player.getWorld().getName().equals(Config.trahodromName)) {
            boolean canUse = player.hasPermission("monkeyjail.canusetrah");
            if (!canUse) {
                event.setCancelled(true);
            }
            return;
        }
        if(JailLogic.monkeyList.containsKey(player.getName()) && !player.getWorld().getName().equals(Config.monkeySurvWorldName)){
            if (!player.getWorld().getName().equals(Config.monkeySurvWorldName)) {
                event.setCancelled(true);
            }
        }
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {//чтобы при выходе обезьяны траходром сбрасывался, а при выходе игрока нет
        Player player = event.getPlayer();
        JailLogic.showJails.remove(player.getName());

        if (!player.getWorld().getName().equals(Config.trahodromName)) return;

        //if(JailLogic.shreakPlayers.containsKey(event.getPlayer().getName())) {
        if (JailLogic.monkeyList.containsKey(player.getName())) {
            JailLogic.monkeyList.get(player.getName()).time_left += Config.quietMonkeyTickIncrease;
            Bukkit.broadcast(MiniMessage.miniMessage().deserialize("<red>Обезьяна " + event.getPlayer().getName() + " вышла из игры на траходроме и получила штрафное время в зоопарке!"));
            String partnerName = JailLogic.shreakPlayers.get(event.getPlayer().getName()).partnerUsername;
            if (JailLogic.shreakPlayers.containsKey(partnerName) && Bukkit.getPlayer(partnerName) != null) {
                Bukkit.getPlayer(partnerName).teleportAsync(JailLogic.shreakPlayers.get(partnerName).location);
                Bukkit.getPlayer(partnerName).hideBossBar(JailLogic.shreakPlayers.get(partnerName).halfBar);
                JailLogic.shreakPlayers.remove(partnerName);
            }
            event.getPlayer().teleportAsync(JailLogic.shreakPlayers.get(event.getPlayer().getName()).location);
            JailLogic.shreakPlayers.remove(event.getPlayer().getName());
        }
        //сделал так, чтобы обычный игрок при выходе не удалялся из траходрома, т.к. он мог бы сразу другую обезьяну отрправить, опять перезайти и отправить ещё обезьян
        //}
    }



}