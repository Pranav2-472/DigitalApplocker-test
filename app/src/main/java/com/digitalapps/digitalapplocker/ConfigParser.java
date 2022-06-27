package com.digitalapps.digitalapplocker;


import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

public class ConfigParser {
    private final File configFile;
    public static ConfigParser currentInstance;
    public ConfigParser(String filename) {
        currentInstance=this;
        configFile=new File(filename);

        try {
            if(!configFile.exists()) {
                init();
            }
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new FileReader(configFile));
            parser.next();
            try {
                if(!parser.getName().equals("root")) {
                    configFile.delete();
                    init();
                }
            }
            catch (Exception e) {
                configFile.delete();
                init();
                e.printStackTrace();
            }
        }
        catch (Exception e) {
            currentInstance=null;
        }
    }
    private void init() {

        try {
            FileWriter writer=new FileWriter(configFile);
            XmlSerializer serializer=Xml.newSerializer();
            serializer.setOutput(writer);
            serializer.startDocument("utf-8",true);
            serializer.startTag(null,"root");
            serializer.endTag(null,"root");
            serializer.endDocument();
            serializer.flush();
            writer.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }
    public void writeConfig(Config config) {
        try {
            FileWriter writer = new FileWriter(configFile,false);
            XmlSerializer serializer = Xml.newSerializer();
            serializer.setOutput(writer);
            serializer.startDocument("utf-8",true);
            serializer.startTag(null,"root");
            serializer.startTag(null,"master-password");
            serializer.text(config.getMasterpwd());
            serializer.endTag(null,"master-password");
            for ( App app : config.getApps() ) {
                try{
                    String appname = app.name();
                    String startTime = Long.toString(app.getTimeAsLong(1));
                    String endTime = Long.toString(app.getTimeAsLong(2));
                    String emergency = Integer.toString(app.getEmergencyTimeLimit());
                    String passwd = app.getPasswd();
                    if(startTime.equals("0") || endTime.equals("0"))
                        throw new Exception();
                    serializer.startTag(null, "app");
                    serializer.attribute(null, "name",appname);
                    serializer.attribute(null,"startTime",startTime);
                    serializer.attribute(null,"endTime",endTime);
                    serializer.attribute(null,"emergencyTime",emergency);
                    if(passwd!=null)
                        serializer.attribute(null,"password",passwd);
                    serializer.endTag(null,"app");
                }
                catch (Exception e ) {

                }
            }
            serializer.endTag(null,"root");
            serializer.endDocument();
            serializer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public Config readConfig() {
        XmlPullParser parser = Xml.newPullParser();
        int event = 0;
        String masterpwd = null;
        ArrayList<App> apps = new ArrayList<>();
        FileReader reader;
        try {
            reader = new FileReader(configFile);
            parser.setInput(reader);
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES,false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            event=parser.getEventType();
            if(event!=XmlPullParser.START_DOCUMENT) {
                return null;
            }
            event=parser.next();
            while(event!=XmlPullParser.END_DOCUMENT)
            {
                if(event==XmlPullParser.END_TAG) {
                    event=parser.next();
                    continue;
                }

                switch(parser.getName()) {
                    case "master-password": masterpwd=parser.nextText();
                                            break;
                    case "app": String AppName = parser.getAttributeValue(XmlPullParser.NO_NAMESPACE,"name");
                                long startTime = Long.parseLong(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE,"startTime"));
                                long endTime = Long.parseLong(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE,"endTime"));
                                int EmergencyTime = Integer.parseInt(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE,"emergencyTime"));
                                String passwd = parser.getAttributeValue(XmlPullParser.NO_NAMESPACE,"password");
                                apps.add(new App(AppName,new App.TimeSet(startTime),new App.TimeSet(endTime),EmergencyTime,passwd));
                                break;
                    default: break;
                }
                event=parser.next();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return new Config(apps,masterpwd);
    }
}
