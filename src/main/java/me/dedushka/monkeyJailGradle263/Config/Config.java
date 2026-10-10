package me.dedushka.monkeyJailGradle263.Config;

import me.dedushka.monkeyJailGradle263.MonkeyJailGradle263;

public class Config {
    public static MonkeyJailGradle263 MJ;

    public static int globalTimerDelay = 1;
    public static int timeInShreakingMachine=200;
    public static int doEveryLoopDelay = 4;
    public static int quietMonkeyTickIncrease = 10*20*60; //10 минут
    public static int shreakTimerDelay = 20; //1 секунда
    public static String trahodromName = "TrahodromWorld";
    public static String monkeySurvWorldName = "MonkeySurvWorld";
    public static int updateMonkeysTimeDelay = 1200; //1 минута
    public static int fuckMonkeyDistance = 10;
    public static String defaultMonkeySkinURL;
    public static String discordHistoryChannelId = "xxx";
    public static int monkeyTimeToSurvAfterGetMonkey = 5*60*20; //5 минут
    public static int survWorldTimerSpeed = 2;


    public static void updateConfigValues(){
        MJ = MonkeyJailGradle263.instance;
        Config.globalTimerDelay = MJ.getConfig().getInt("globalTimerDelay");
        Config.timeInShreakingMachine = MJ.getConfig().getInt("timeInShreakingMachine");
        Config.doEveryLoopDelay = MJ.getConfig().getInt("doEveryLoopDelay");
        Config.quietMonkeyTickIncrease = MJ.getConfig().getInt("quietMonkeyTickIncrease");
        Config.trahodromName = MJ.getConfig().getString("trahodromWorldName");
        Config.monkeySurvWorldName = "monkeySurvWorld";
        Config.updateMonkeysTimeDelay = MJ.getConfig().getInt("updateMonkeysTimeDelay");
        Config.fuckMonkeyDistance = MJ.getConfig().getInt("fuckMonkeyDistance");
        Config.defaultMonkeySkinURL = MJ.getConfig().getString("defaultMonkeySkinURL");
        Config.discordHistoryChannelId = MJ.getConfig().getString("history_discord_channel_id");
        Config.monkeyTimeToSurvAfterGetMonkey = MJ.getConfig().getInt("monkeyTimeToSurvAfterGetMonkey");
        Config.survWorldTimerSpeed = MJ.getConfig().getInt("survWorldTimerSpeed");
    }
}
