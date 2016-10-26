package com.stanleyidesis.quotograph.api.controller;

/**
 * Created by Stanley Idesis on 10/12/2016.
 */

public class LWQImageControllerHelper {
    static LWQImageController lwqImageController;

    public static LWQImageController get() {
        if (lwqImageController != null) {
            return lwqImageController;
        }
        lwqImageController = new LWQImageControllerUIL();
        return get();
    }
}
