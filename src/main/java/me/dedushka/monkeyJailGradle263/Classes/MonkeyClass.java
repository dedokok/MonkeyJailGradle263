package me.dedushka.monkeyJailGradle263.Classes;

public class MonkeyClass {
    public String jail_name;
    public String username;
    public long time_left;
    public String admin_username;
    public String reason;
    public int time_to_surv;
    MonkeyClass(){};
    public MonkeyClass(String jail_name,String username, long time_left, String admin_username, String reason, int time_to_surv){
        this.jail_name=jail_name;
        this.username = username;
        this.time_left=time_left;
        this.admin_username=admin_username;
        this.reason=reason;
        this.time_to_surv=time_to_surv;
    }
}