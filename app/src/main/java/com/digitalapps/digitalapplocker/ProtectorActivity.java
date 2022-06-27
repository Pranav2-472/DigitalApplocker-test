package com.digitalapps.digitalapplocker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.EditText;

public class ProtectorActivity extends AppCompatActivity implements AppLockerInterface{
    ServiceConnection connection;
    Config config;
    private boolean unlocked=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_protector);
        Intent intent = new Intent(this, AppLockerService.class);
        serviceConnect(intent);
    }
    private void serviceConnect(Intent intent) {
        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                AppLockerService.CommsHandler commsHandler=(AppLockerService.CommsHandler) iBinder;
                commsHandler.setActivity(ProtectorActivity.this);
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
        new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            runOnUiThread(() -> {
                String mpassword = config.getMasterpwd();
                if(mpassword==null || mpassword.equals("")) {
                    unlocked=true;
                    MainActivity.UNLOCKED=true;
                    this.finish();
                }
            });
        }).start();
        super.onStart();
    }

    public void check(View v) {
        EditText PwdBox = (EditText) findViewById(R.id.MPwdCheckBox);
        String password = PwdBox.getText().toString();
        if(password!=null && !password.equals("") && password.equals(config.getMasterpwd())) {
            MainActivity.UNLOCKED=true;
            unlocked=true;
            this.finish();
        }
    }
    @Override
    protected void onPause() {
        if(!unlocked) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

        }
        //unbindService(connection);
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