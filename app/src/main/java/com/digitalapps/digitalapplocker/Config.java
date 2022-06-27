package com.digitalapps.digitalapplocker;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Config {
    private ArrayList<App> apps;
    private String masterpwd;
    public Config(ArrayList<App> apps, String masterpwd) {
        this.apps=apps;
        this.masterpwd=masterpwd;
    }

    public String getMasterpwd() {
        return ""+masterpwd;
    }

    public ArrayList<App> getApps() {
        return apps;
    }
    public App getApp(String name) {
        for(App app : apps) {
            if(app.name().equals(name))
                return app;
        }
        return null;
    }
    public void removeApp(String name) {
        for(App app : apps) {
            if(app.name().equals(name)) {
                apps.remove(app);
                ConfigParser.currentInstance.writeConfig(this);
                return;
            }
        }
    }

    public App createNewAppConfig(String packagename) {
        for(App app : apps) {
            if(app.name().equals(packagename))
                return null;
        }
        App app = new App(packagename,new App.TimeSet(0),new App.TimeSet(0), 0,null);
        apps.add(app);
        ConfigParser.currentInstance.writeConfig(this);
        return app;
    }
    public void sync() {
        ConfigParser.currentInstance.writeConfig(this);
    }
}
