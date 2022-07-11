package com.digitalapps.digitalapplocker;
/*
*
* This is a Service class, it gets started as a foreground service that runs on android forever.
* It monitors applock monitoring and communication to User Activities.
* It uses a ServiceThread for continuous monitoring and AppLocking
* */

//imports
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
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
    //The context and ServiceThread variables to pass the application context and manage the Service Thread object
    private Context context;
    private ServiceThread serviceThread;

    //Supported commands. These commands can be sent from User Activities to the Service
    public static final int SEND_PING = 0;
    public static final int PROCESS_LIST=1;
    public static final int GET_CONFIG=8;
    public static final int GET_SERVICE=9;

    private List<UsageStats> processInfoList;
    private long currentTime;

    private AppLockerInterface currentActivity;


    //The Communications handler class. This handles all communications from User Activities to this Service.
    public class CommsHandler extends Binder {

        public int command(int cmd) {
            switch (cmd) {
                case SEND_PING: AppLockerService.this.ping();
                                return 0;
                case PROCESS_LIST:AppLockerService.this.listProcess();
                                return 0;


                case GET_SERVICE : currentActivity.serviceReply(AppLockerService.this);     //Give user activity an instance of this Service.
                                return 0;
                case GET_CONFIG : currentActivity.serviceReply(serviceThread.getConfig());         //Give user Activity Config object - Config.java
                                return 0;
            }
            return -1;
        }

        //The Activity needs to provide the Service it's instance, if a command requires a callback with a result
        public void setActivity(AppLockerInterface appLockerInterface) {
            AppLockerService.this.currentActivity=appLockerInterface;
        }


    }


    @Override
    public int onStartCommand(Intent intent,int flags, int startID) {
        return Service.START_STICKY;
    }


    //Use android's onBind functionality to give the Communications handler to binding User Activity.
    @Override
    public IBinder onBind(Intent intent){
        return new CommsHandler();
    }



    //In onCreate the the service creates a notification and registers itself as Foreground Service. See Foreground Services in Android Docs.
    @Override
    public void onCreate() {


        context = getApplicationContext();

        /*
        * From Android 8.0, notifications need to be pushed through channels. But Android 6.0 doesn't support channels.
        * This code checks android version and deals with notification accordingly.
        */
        //Create channel - for android 8.0+
        NotificationChannel channel=null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channel = new NotificationChannel(
                    "service",
                    "My Foreground Service",
                    NotificationManager.IMPORTANCE_HIGH);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        //Create notification - android 8.0+
        Notification serviceNotification = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            serviceNotification = new Notification.Builder(context,"service")
                    .setSmallIcon(com.google.android.material.R.drawable.notification_icon_background)
                    .setContentTitle("Digital AppLocker")
                    .setContentText("Digital AppLocker is monitoring this device")
                    .setPriority(Notification.PRIORITY_DEFAULT)
                    .build();

        }


        // Create notification without channel - android < 8.0
        else
        {
            serviceNotification = new Notification.Builder(context)
                    .setSmallIcon(com.google.android.material.R.drawable.notification_icon_background)
                    .setContentTitle("Digital AppLocker")
                    .setContentText("Digital AppLocker is monitoring this device")
                    .setPriority(Notification.PRIORITY_DEFAULT)
                    .build();

        }

        //push the notification and set service to Foreground.
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.notify(1,serviceNotification);
        startForeground(1, serviceNotification);

        //Service thread is what does the MONITORING and APPLOCKING. Start the service thread.
        serviceThread = new ServiceThread(getApplicationContext());
        serviceThread.start();

    }

    //ping - disabled
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

    //list process - debug
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
