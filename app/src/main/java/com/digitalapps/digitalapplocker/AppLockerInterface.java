package com.digitalapps.digitalapplocker;

/*
* Helps interface Activities to the service.
* Provides a single function that the activities can override.
* The service calls it if a result is to be returned.
*/

public interface AppLockerInterface {

    public void serviceReply(Object results);
}
