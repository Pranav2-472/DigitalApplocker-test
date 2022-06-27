package com.digitalapps.digitalapplocker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.EditText;

public class MainSettingsActivity extends AppCompatActivity implements AppLockerInterface{
    private Intent intent;
    private Config config;
    private ServiceConnection connection;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_settings);
        Intent intent = new Intent(this, AppLockerService.class);
        serviceConnect(intent);
    }
    private void serviceConnect(Intent intent) {
        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                AppLockerService.CommsHandler commsHandler=(AppLockerService.CommsHandler) iBinder;
                commsHandler.setActivity(MainSettingsActivity.this);
                commsHandler.command(AppLockerService.GET_CONFIG);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };
        bindService(intent,connection,BIND_IMPORTANT);
    }
    @Override
    protected void onStart() {
        new Thread() {
            @Override
            public void run() {
                try{
                    Thread.sleep(100);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                runOnUiThread(()-> {
                    EditText MPwdBox = (EditText) findViewById(R.id.MasterPwdBox);
                    MPwdBox.setText(config.getMasterpwd());
                });
            }
        }.start();
        super.onStart();
    }
    @Override
    protected void onPause() {
        EditText MPwdBox = (EditText) findViewById(R.id.MasterPwdBox);
        String mpasswd = MPwdBox.getText().toString();
        if(mpasswd!=null && !mpasswd.equals(""))
            config.setMasterpwd(mpasswd);
        else
            config.setMasterpwd(null);
        config.sync();
        super.onPause();
    }
    @Override
    protected void onDestroy() {
        //unbindService(connection);
        super.onDestroy();
    }
    @Override
    public void serviceReply(Object result) {
        config=(Config) result;
    }
}