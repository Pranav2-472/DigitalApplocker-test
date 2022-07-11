package com.digitalapps.digitalapplocker;

/*
* This is an App object. In service thread and config object, apps info is handled as instances of this.
* This is used to store information about an app - which is the package name, starting time to be locked and ending time until app is locked.
* Completely custom implementation, no further documentation provided.
*/

public class App {
    public static class TimeSet {
        int hr;
        int min;
        int sec;
        private TimeSet(int hr, int min, int sec) {
            this.hr=hr;
            this.min=min;
            this.sec=sec;

        }
        public TimeSet(long t) {
            this.setTimeAsLong(t);
        }
        private long getTimeAsLong() {
            return sec+(min*60)+(hr*60*60);
        }
        private void setTimeAsLong(long t) {
            sec= Math.toIntExact(t % 60);
            min=Math.toIntExact((t/60)%60);
            hr=Math.toIntExact(t/(60*60));
        }
        public int getHr(){
            return hr;
        }
        public int getMin(){
            return min;
        }
        public int getSec(){
            return sec;
        }
    }
    private final String name;
    private TimeSet startTime;
    private TimeSet endTime;
    private String passwd;
    private int EmergencyTimeLimit;
    public App(String name, TimeSet startTime, TimeSet endTime, int Emergency, String passwd) {
        this.name=name;
        this.startTime=startTime;
        this.endTime=endTime;
        this.EmergencyTimeLimit=Emergency;
        this.passwd=passwd;
    }
    public TimeSet[] getTimeSets() {
        TimeSet[] sets = new TimeSet[2];
        sets[0]=startTime;
        sets[1]=endTime;
        return sets;
    }
    
    public int getEmergencyTimeLimit() {
        return EmergencyTimeLimit;
    }
    public void setEmergencyTimeLimit(int limit) {
        EmergencyTimeLimit=limit;
    }
    public void setTimeFromLong(int index, long value) {
        switch(index) {
            case 1 :if(startTime==null)
                        startTime=new TimeSet(value);
                    else
                        startTime.setTimeAsLong(value);
                    break;
            case 2 : if(endTime==null)
                endTime=new TimeSet(value);
            else
                endTime.setTimeAsLong(value);
                break;
        }
    }
    public void setTime(int index,int hr, int min, int sec) {
        switch(index) {
            case 1 :startTime=new TimeSet(hr,min,sec);
                break;
            case 2 : endTime=new TimeSet(hr,min,sec);
                break;
        }
    }
    public String getTimeAsString(int index) {
        String result=new String();
        switch(index) {
            case 1 :result=result+startTime.getHr()+":"+startTime.getMin()+":"+startTime.getSec();
                break;
            case 2 : result=result+endTime.getHr()+":"+endTime.getMin()+":"+endTime.getSec();
                break;
        }
        return result;
    }
    public long getTimeAsLong(int index) {
        switch(index) {
            case 1 : return startTime.getTimeAsLong();
            case 2 : return endTime.getTimeAsLong();
            default: return -1;
        }
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    public String name() {
        return name;
    }
}