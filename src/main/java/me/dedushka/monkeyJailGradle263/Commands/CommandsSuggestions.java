package me.dedushka.monkeyJailGradle263.Commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import me.dedushka.monkeyJailGradle263.Classes.MonkeyClass;
import me.dedushka.monkeyJailGradle263.Config.Config;
import me.dedushka.monkeyJailGradle263.Util.JailLogic;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandsSuggestions {

    public static CompletableFuture<Suggestions> onlinePlayersSuggestion(final CommandContext<CommandSourceStack> ctx, final SuggestionsBuilder builder){
        String input = builder.getRemaining();
        for(Player player : Bukkit.getOnlinePlayers()){
            if(player.getName().startsWith(input)) {
                builder.suggest(player.getName());
            }
        }
        return builder.buildFuture();
    }


    public static CompletableFuture<Suggestions> timeSuggest(final CommandContext<CommandSourceStack> ctx, final SuggestionsBuilder builder){


        String text = builder.getRemaining();
        Pattern pattern = Pattern.compile("^\\d+$");
        Matcher matcher = pattern.matcher(text);
        String permanent = "permanent";

        String[] suggests = {"s","m","h","d","w","M","y"};
        if (matcher.find()) {
            for(String suggest : suggests){
                //if(suggest.startsWith(text)) {
                    builder.suggest(text+suggest);
                    //}
            }
        }
        else if(permanent.startsWith(text)){
            builder.suggest(permanent);
        }
//        else{
//            Pattern pattern_2 = Pattern.compile("\\d+[a-zA-Z]");
//            Matcher matcher_2 = pattern.matcher(text);
//            if(matcher.find())
//        }


        return builder.buildFuture();
    }


    public static CompletableFuture<Suggestions> monkeyListSuggestion(final CommandContext<CommandSourceStack> ctx, final SuggestionsBuilder builder){
        String input = builder.getRemaining();
        for(MonkeyClass monkey : JailLogic.monkeyList.values()){
            if(monkey.time_left<=0)continue;
            if(monkey.username.startsWith(input)) {
                builder.suggest(monkey.username);
            }
        }
        return builder.buildFuture();
    }

    public static CompletableFuture<Suggestions> onlineMonkeyListSuggestion(final CommandContext<CommandSourceStack> ctx, final SuggestionsBuilder builder){
        String input = builder.getRemaining();

        for(MonkeyClass monkey : JailLogic.monkeyList.values()){
            if(monkey.time_left<=0)continue;

            if(Bukkit.getPlayer(monkey.username)!=null) {
                if(monkey.username.startsWith(input)) {
                    builder.suggest(monkey.username);
                }
            }
        }
        return builder.buildFuture();
    }

    public static CompletableFuture<Suggestions> onlineMonkeysInSurvListSuggestion(final CommandContext<CommandSourceStack> ctx, final SuggestionsBuilder builder){
        String input = builder.getRemaining();

        for(MonkeyClass monkey : JailLogic.monkeyList.values()){
            if(monkey.time_left<=0)continue;
            Player monkey_player = Bukkit.getPlayer(monkey.username);
            if(monkey_player!=null && monkey_player.getWorld().getName().equals(Config.monkeySurvWorldName)) {
                if(monkey.username.startsWith(input)) {
                    builder.suggest(monkey.username);
                }
            }
        }
        return builder.buildFuture();
    }


    public static CompletableFuture<Suggestions> jailListSuggestion(final CommandContext<CommandSourceStack> ctx, final SuggestionsBuilder builder){
        String input = builder.getRemaining();

        for(String jail_name : JailLogic.jails.keySet()){
            if(jail_name.startsWith(input)) {
                builder.suggest(jail_name);
            }

        }
        return builder.buildFuture();
    }
}
