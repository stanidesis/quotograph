package com.stanleyidesis.quotograph.api.controller;

/**
 * Created by Stanley Idesis on 10/12/2016.
 */

public class LWQNotificationControllerHelper {
    static LWQNotificationController lwqNotificationController;

    public static LWQNotificationController get() {
        if (lwqNotificationController != null) {
            return lwqNotificationController;
        }
        lwqNotificationController = new LWQNotificationControllerImpl();
        return get();
    }
}
