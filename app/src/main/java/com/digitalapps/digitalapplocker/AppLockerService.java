package com.digitalapps.digitalapplocker;

import android.app.Notification;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import androidx.core.app.NotificationManagerCompat;

import java.util.List;

public class AppLockerService extends Service {

    private Context context;
    private ServiceThread serviceThread;
    public static final int SEND_PING = 0;
    public static final int PROCESS_LIST=1;
    public static final int GET_CONFIG=8;
    public static final int GET_SERVICE=9;

    private List<UsageStats> processInfoList;
    private long currentTime;
    private AppLockerInterface currentActivity;

    public class CommsHandler extends Binder {
        public int command(int cmd) {
            switch (cmd) {
                case SEND_PING: AppLockerService.this.ping();
                                return 0;
                case PROCESS_LIST:AppLockerService.this.listProcess();
                                return 0;


                case GET_SERVICE : currentActivity.serviceReply(AppLockerService.this);
                                return 0;
                case GET_CONFIG : currentActivity.serviceReply(serviceThread.getConfig());
                                return 0;
            }
            return -1;
        }
        public void setActivity(AppLockerInterface appLockerInterface) {
            AppLockerService.this.currentActivity=appLockerInterface;
        }


    }


    @Override
    public int onStartCommand(Intent intent,int flags, int startID) {
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent){
        return new CommsHandler();
    }

    @Override
    public void onCreate() {
        context = getApplicationContext();
        Notification serviceNotification = new Notification.Builder(context)
                .setSmallIcon(com.google.android.material.R.drawable.notification_icon_background)
                .setContentTitle("Digital AppLocker")
                .setContentText("Digital AppLocker is monitoring this device")
                .setPriority(Notification.PRIORITY_DEFAULT)
                .build();
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.notify(1,serviceNotification);
        startForeground(1, serviceNotification);
        serviceThread = new ServiceThread(getApplicationContext());
        serviceThread.start();

    }
    public void ping () {
        Notification serviceNotification = new Notification.Builder(context)
                .setSmallIcon(com.google.android.material.R.drawable.notification_icon_background)
                .setContentTitle("Received Ping - Service")
                .setContentText("Digital AppLocker is monitoring this device")
                .setPriority(Notification.PRIORITY_DEFAULT)
                .build();
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.notify(2,serviceNotification);
    }
    public void listProcess() {
        currentTime = System.currentTimeMillis();
        Thread t = new Thread() {
            @Override
            public void run() {
                while(true) {
                    currentTime = System.currentTimeMillis();
                    UsageStatsManager manager = (UsageStatsManager) getSystemService(USAGE_STATS_SERVICE);

                    processInfoList=manager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,currentTime-1000*70, currentTime);
                    currentActivity.serviceReply(processInfoList);
                    /*if(INTERNAL_APP_LOCK_SCREEN_ON!=1) {
                        for(UsageStats ps : processInfoList) {

                            if (ps.getPackageName().equals("com.android.gallery") && currentTime - ps.getLastTimeUsed() < 1500) {
                                if(!ps.getPackageName().equals(currentlyUnlockedApp)) {
                                    currentlyUnlockedApp=null;
                                    Intent lockIntent = new Intent(AppLockerService.this, LockScreenActivity.class);
                                    lockIntent.putExtra("PwdAvailable", 1);
                                    lockIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(lockIntent);
                                }
                            }
                        }
                    }*/
                    try{
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        t.start();

    }
}
