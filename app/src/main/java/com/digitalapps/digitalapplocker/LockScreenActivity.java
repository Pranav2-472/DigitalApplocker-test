package com.digitalapps.digitalapplocker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LockScreenActivity extends AppCompatActivity implements AppLockerInterface {
    AppLockerService.CommsHandler commsHandler = null;
    ServiceConnection connection;
    String password=null;
    private int ALLOWED=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_screen);
        serviceConnect(new Intent(this,AppLockerService.class));
        ServiceThread.LOCK_SCREEN_ACTIVE=true;
        Intent i = getIntent();
        int pwdAvailable = (int) i.getExtras().get("PwdAvailable");

        if(pwdAvailable==0) {
            EditText pwdbox = (EditText) findViewById(R.id.UnlockPassword);
            pwdbox.setEnabled(false);
            Button UnlockButton = (Button) findViewById(R.id.UnlockButton);
            UnlockButton.setEnabled(false);
            TextView status = (TextView) findViewById(R.id.PasswordStatus);
            status.setText("(password not allowed)");
        }
        else {
            password= (String) i.getExtras().get("password");
            TextView status = (TextView) findViewById(R.id.PasswordStatus);
            status.setText("(password allowed)");
        }
    }

    public void unlock(View v) {
        EditText pwdbox = findViewById(R.id.UnlockPassword);
        String password = pwdbox.getText().toString();
        if(password.equals(this.password)) {
            ServiceThread.currentlyUnlockedApp=(String) getIntent().getExtras().get("packagename");
            ALLOWED=1;
            this.finish();
        }


    }

    @Override
    public void serviceReply(Object obj) {
    }


    private void serviceConnect(Intent intent) {
        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                commsHandler=(AppLockerService.CommsHandler) iBinder;
                commsHandler.setActivity(LockScreenActivity.this);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                commsHandler=null;
            }
        };
        bindService(intent,connection,BIND_IMPORTANT);

    }

    @Override
    protected void onPause() {
        if(ALLOWED==0) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

        }
        unbindService(connection);
        ServiceThread.LOCK_SCREEN_ACTIVE=false;
        super.onPause();
    }
}