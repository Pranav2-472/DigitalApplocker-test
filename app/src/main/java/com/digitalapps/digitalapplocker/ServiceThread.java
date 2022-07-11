package com.digitalapps.digitalapplocker;
/*
*
* This is the Service Thread. The Foreground service starts this thread on creation.
* This thread is supposed to be running forever.
* It does the complete monitoring and applocking.
* Further explanation is done below.
* */

//imports
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import java.util.List;

//This uses Java Thread class. run() is run on a separate thread when object.start() is called from main thread.
public class ServiceThread extends Thread{

    //The config object global - Config.java
    private Config config=null;

    //Value to check if locking screen is active
    public static boolean LOCK_SCREEN_ACTIVE=false;

    //Variable to hold the currently unlocked app(if unlocking is allowed)
    public static String currentlyUnlockedApp=null;

    private final Context context;
    public ServiceThread(Context context) {
        this.context=context;
    }

    //The function used by Foreground Service to get Config object.
    public Config getConfig() {
        return config;
    }


    @Override
    public void run() {

        //newfile checks if config.xml exists or not. currentTime holds the current time in millis.
        boolean newfile = false;
        long currentTime;

        //UsageStatsManager is used to get real time usage statistics of device. See android docs.
        UsageStatsManager manager = (UsageStatsManager)context.getSystemService(Context.USAGE_STATS_SERVICE);

        //Set the file location, create a configuration parser object and create/read the config file.
        String pathname = context.getExternalFilesDir(null)+"/com.digitalapps.digitalapplocker.config.txt";
        ConfigParser parser =new ConfigParser(pathname);
        config = parser.readConfig();

        //forever
        while(true) {

            /*
            * This code here is the main monitoring and applocking code.
            * The code requires usage stats permission to be enabled(See AndroidManifest.xml)
            * It also requires App Usage Stats to be enabled for this app from device settings
            * Otherwise an empty list is returned.
            */
            currentTime = System.currentTimeMillis();

            //get the list of usage statistics using android API. See android docs.
            List<UsageStats> usageStats = manager.queryUsageStats(UsageStatsManager.INTERVAL_BEST,currentTime-30000,currentTime);

            //loop through each app in the list
            for(UsageStats stats: usageStats) {

                String pack=stats.getPackageName();     //get the package namme
                long time = (System.currentTimeMillis()%(24*60*60*1000))/1000;  //get today's time elapsed in seconds

                //if the app is not opened right now. It calculates it using the getLastTimeUsed() function and current time. We give a 1.5sec tolerance
                //to account for any slowness by system. If value is too low, no app gets detected. If value is too high, false positives occur.
                if(currentTime - stats.getLastTimeUsed()< 1500) {

                    //check if current activity is launcher. It is the home of android. If that is 'opened', it means all other applications are closed.
                    //That means the unlocked app too. So we reset and get ready to monitor again.
                    if(stats.getPackageName().contains("launcher")) {
                        MainActivity.UNLOCKED=false;
                        currentlyUnlockedApp=null;
                        continue;
                    }

                    //Get list of apps to be locked from Config object and loop through them
                    for(App app : config.getApps()) {
                        String appname = app.name();

                        //if lock screen is already displayed, nothing to do.
                        if(LOCK_SCREEN_ACTIVE) {
                            break;
                        }
                            //Checks if the appname from usage statistics is the app that is on hand. If yes, checks if system is is inside the
                            //timespan for which the app is to be locked
                            if(app.name().equals(pack) && app.getTimeAsLong(1)<time && app.getTimeAsLong(2)>time) {

                                //Checks if the user haven't unlocked the app using password
                                if (!app.name().equals(currentlyUnlockedApp)) {

                                    /*
                                    * Control landing here means the currently running app must be blocked.
                                    * We simply create an intent and give it any password if password unlocking is available.
                                    * Then we draw it over the app that the app is blocked.
                                    * The LockScreenActivity is the locking screen. It handles the rest from here.
                                    * We wait half a second for preventing too much hits and go back to monitoring mode.
                                    */

                                    Intent lockIntent = new Intent(context, LockScreenActivity.class);
                                    if (app.getPasswd() != null)
                                        lockIntent.putExtra("PwdAvailable", 1);
                                    else
                                        lockIntent.putExtra("PwdAvailable", 0);
                                    lockIntent.putExtra("packagename", app.name());
                                    if (app.getPasswd() != null)
                                        lockIntent.putExtra("password", app.getPasswd());
                                    lockIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    context.startActivity(lockIntent);
                                    try {
                                        Thread.sleep(500);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }

                                }
                            }
                    }
                }

            }
            try{
                Thread.sleep(100);
            }
            catch (Exception e) {

            }
        }
    }

}
