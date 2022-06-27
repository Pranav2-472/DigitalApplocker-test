package com.digitalapps.digitalapplocker;

import androidx.appcompat.app.AppCompatActivity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import java.util.List;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    AppLockerService.CommsHandler commsHandler = null;
    ServiceConnection connection;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        PackageManager manager = getPackageManager();
        List<ApplicationInfo> applist = manager.getInstalledApplications(PackageManager.GET_META_DATA);

        //placeholder code
        int i=0;
        LinearLayout layout = (LinearLayout) findViewById(R.id.mainlayout);
        while(i<applist.size()) {
            TextView tv = new TextView(this);
            tv.setTextSize(18);
            tv.setText(manager.getApplicationLabel(applist.get(i)));
            tv.setGravity(Gravity.CENTER_VERTICAL);
            int finalI = i;
            tv.setOnClickListener(view -> {
                Intent AppSettingsIntent = new Intent(this,AppSettings.class);
                AppSettingsIntent.putExtra("PACKAGE",applist.get(finalI).packageName);
                startActivity(AppSettingsIntent);
                Toast t = Toast.makeText(getApplicationContext(),"Clicked "+((TextView)view).getText(),Toast.LENGTH_SHORT);
                t.show();
            });
            if(i%2==0)
                tv.setBackgroundColor(0xFFE7F0EA);
            layout.addView(tv,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,150));
            i++;
        }
        Button btn = new Button(this);
        btn.setText("Click to ping");
        btn.setOnClickListener(v -> {
                ping(v);
        });
        layout.addView(btn,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,100));
        Button btn2 = new Button(this);
        btn2.setText("Click to view running process");
        btn2.setOnClickListener(v -> {
            startActivity(new Intent(this,ProcessListActivity.class));
        });
        layout.addView(btn2,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,100));
        //make sure to check if service already exist
        Intent intent = new Intent(this,AppLockerService.class);
        startService(intent);
    }
    @Override
    public void onStart() {
        Intent intent = new Intent(this, AppLockerService.class);
        serviceConnect(intent);
        super.onStart();
    }

    private void serviceConnect(Intent intent) {
        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                commsHandler=(AppLockerService.CommsHandler) iBinder;
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                commsHandler=null;
            }
        };
        bindService(intent,connection,BIND_IMPORTANT);
    }

    public void ping(View v) {
        if(commsHandler!=null) {
            commsHandler.command(AppLockerService.SEND_PING);
        }
    }
    @Override
    protected void onStop() {
        unbindService(connection);
        super.onStop();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}