package com.digitalapps.digitalapplocker;

/*
* Shows process list using usage statistics. Debug purposes only.
* To be removed
*/
import androidx.appcompat.app.AppCompatActivity;

import android.app.usage.UsageStats;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class ProcessListActivity extends AppCompatActivity implements AppLockerInterface {

    private AppLockerService.CommsHandler commsHandler;
    private List<UsageStats> processes;
    private Button refreshbtn;
    private static Thread servicePoll;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_list);
        refreshbtn=(Button)findViewById(R.id.refreshProcList);
        ServiceConnection connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                commsHandler=(AppLockerService.CommsHandler) iBinder;

            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                commsHandler=null;
            }
        };
        bindService(new Intent(getApplicationContext(),AppLockerService.class),connection,BIND_AUTO_CREATE);
         if(servicePoll==null) {
             servicePoll = new Thread() {
                 @Override
                 public void run() {
                     try {
                         Thread.sleep(500);
                     } catch (InterruptedException e) {
                         e.printStackTrace();
                     }
                     commsHandler.setActivity(ProcessListActivity.this);
                     commsHandler.command(AppLockerService.PROCESS_LIST);
                 }
             };
             servicePoll.start();
         }

    }

    @Override
    public void serviceReply(Object results) {
        processes = (List<UsageStats>) results;
    }
    public void ListProcesses(View v) {
        LinearLayout layout = (LinearLayout) findViewById(R.id.ListProcLayout);
        layout.removeAllViews();
        layout.addView(refreshbtn,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,100));

        for(UsageStats ps : processes) {
            TextView tv = new TextView(this);
            tv.setTextSize(12);
            tv.setText(ps.getPackageName()+"\t");//+Long.toString(System.currentTimeMillis()-ps.getLastTimeUsed()));

            layout.addView(tv,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,100));
        }
    }


}