package com.digitalapps.digitalapplocker;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.XmlResourceParser;
import android.renderscript.RenderScript;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ServiceThread extends Thread{
    private Config config=null;
    public static boolean LOCK_SCREEN_ACTIVE=false;
    public static String currentlyUnlockedApp=null;
    private final Context context;
    public ServiceThread(Context context) {
        this.context=context;
    }

    public Config getConfig() {
        return config;
    }
    @Override
    public void run() {
        boolean newfile = false;
        long currentTime;
        UsageStatsManager manager = (UsageStatsManager)context.getSystemService(Context.USAGE_STATS_SERVICE);
        String pathname = context.getExternalFilesDir(null)+"/com.digitalapps.digitalapplocker.config.txt";
        ConfigParser parser =new ConfigParser(pathname);
        config = parser.readConfig();
        while(true) {
            currentTime = System.currentTimeMillis();
            List<UsageStats> usageStats = manager.queryUsageStats(UsageStatsManager.INTERVAL_BEST,currentTime-30000,currentTime);

            for(UsageStats stats: usageStats) {

                String pack=stats.getPackageName();
                long time = (System.currentTimeMillis()%(24*60*60*1000))/1000;

                if(currentTime - stats.getLastTimeUsed()< 1500) {
                    if(stats.getPackageName().contains("launcher")) {
                        MainActivity.UNLOCKED=false;
                        currentlyUnlockedApp=null;
                        continue;
                    }
                    for(App app : config.getApps()) {
                        String appname = app.name();
                        if(LOCK_SCREEN_ACTIVE) {
                            break;
                        }
                            if(app.name().equals(pack) && app.getTimeAsLong(1)<time && app.getTimeAsLong(2)>time) {
                                /*if (app.name().equals("com.android.launcher3")) {
                                    currentlyUnlockedApp = null;
                                    break;
                                }*/
                                if (!app.name().equals(currentlyUnlockedApp)) {
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
