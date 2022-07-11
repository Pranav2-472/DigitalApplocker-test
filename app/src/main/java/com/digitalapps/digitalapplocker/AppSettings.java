package com.digitalapps.digitalapplocker;

/*
* This is the AppSettings(activity). This simply gives user an interface to edit the app settings.
* It uses the config object to get or set the current values.
* When Config object is updated, service thread automatically uses the new values.
* sync() is called to write the updates to XML file.
*/

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;

import java.util.ArrayList;

public class AppSettings extends AppCompatActivity implements AppLockerInterface{
    ServiceConnection connection;
    AppLockerService.CommsHandler commsHandler;
    Config currentconfig;
    String packagename;
    boolean enabled;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_settings);

        //get the package name from intent and connect to foreground service.
        Intent myIntent = this.getIntent();
        packagename = (String) myIntent.getExtras().get("PACKAGE");
        Intent intent = new Intent(this, AppLockerService.class);
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
        Switch appLockSwitch = (Switch) findViewById(R.id.AppLockSwitch);
        appLockSwitch.setOnCheckedChangeListener((switchbtn,isChecked) -> {
            enabled=isChecked;
        });
    }

    //Simply gets the app data. Uses the communication handler to get the config object(See AppLockerService.java)
    //gets all values from config objects and sets the UI objects accordingly.
    @Override
    public void onStart() {
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(() -> {
                    commsHandler.setActivity(AppSettings.this);
                    commsHandler.command(AppLockerService.GET_CONFIG);
                    App app = currentconfig.getApp(packagename);
                    Switch appLockSwitch = (Switch) findViewById(R.id.AppLockSwitch);
                    EditText startTimeBox = (EditText) findViewById(R.id.startTimeBox);
                    EditText endTimeBox = (EditText) findViewById(R.id.endTimeBox);
                    EditText passwdBox = (EditText) findViewById(R.id.passwd);
                    Spinner emergencyTimeSpinner = (Spinner) findViewById(R.id.EmergencyTimeBox);

                    if(app==null) {
                        appLockSwitch.setChecked(false);
                        return;
                    }
                    appLockSwitch.setChecked(true);
                    startTimeBox.setText(app.getTimeAsString(1));
                    endTimeBox.setText(app.getTimeAsString(2));
                    passwdBox.setText(app.getPasswd());
                    emergencyTimeSpinner.setSelection(app.getEmergencyTimeLimit());
                });

            }
        }.start();

        super.onStart();
    }
    //gets the current positions of UI objects on pause. Write them to the config object.
    @Override
    protected void onPause() {

        //If applock switch is off, simply remove app from config.
        Switch appLockSwitch = (Switch) findViewById(R.id.AppLockSwitch);
        if(!appLockSwitch.isChecked()) {
            currentconfig.removeApp(packagename);
            super.onPause();
            return;
        }

        //gets values of all UI handles
        boolean NumberFormatError=true;
        EditText startTimeBox = (EditText) findViewById(R.id.startTimeBox);
        String[] startTime = startTimeBox.getText().toString().split(":");

        EditText endTimeBox = (EditText) findViewById(R.id.endTimeBox);
        String[] endTime = endTimeBox.getText().toString().split(":");
        EditText passwdBox = (EditText) findViewById(R.id.passwd);
        Spinner emergencyTimeSpinner = (Spinner) findViewById(R.id.EmergencyTimeBox);

        //parses the time to internally used format.
        for(String element : startTime) {
            if(Integer.parseInt(element)>0) {
                NumberFormatError=false;
                break;
            }
        }
        if(NumberFormatError) {
            super.onPause();
            return;
        }
        NumberFormatError=true;
        for(String element : endTime) {
            if(Integer.parseInt(element)>0) {
                NumberFormatError=false;
            }
        }
        if(NumberFormatError) {
            super.onPause();
            return;
        }

        //uses communication handler and get the app object from config.
        commsHandler.setActivity(this);
        commsHandler.command(AppLockerService.GET_CONFIG);
        App thisapp = currentconfig.getApp(packagename);

        //if app object doesn't exist(App wasn't locked), create it. Update the values of app objects.
        if(thisapp==null)
            thisapp = currentconfig.createNewAppConfig(packagename);
        try {
            thisapp.setTime(1, Integer.parseInt(startTime[0]),Integer.parseInt(startTime[1]),Integer.parseInt(startTime[2]));
            thisapp.setTime(2, Integer.parseInt(endTime[0]),Integer.parseInt(endTime[1]),Integer.parseInt(endTime[2]));
            thisapp.setEmergencyTimeLimit((int) emergencyTimeSpinner.getSelectedItemId());
            String password = passwdBox.getText().toString();
            if(password!=null && !password.equals(""))
                thisapp.setPasswd(passwdBox.getText().toString());
            else
                thisapp.setPasswd(null);

            //perform sync to write to the XML file.
            currentconfig.sync();
        }
        catch (NumberFormatException e) {
            //TODO: show error message
        }
        super.onPause();
    }

    @Override
    public void serviceReply(Object result) {
        currentconfig=(Config) result;
    }
}