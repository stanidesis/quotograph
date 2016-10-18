package com.stanleyidesis.quotograph.api.controller;

import com.stanleyidesis.quotograph.BuildConfig;

/**
 * Created by Stanley Idesis on 10/12/2016.
 */

public class LWQLoggerHelper {

    static LWQLogger logger;

    public static LWQLogger get() {
        if (logger != null) {
            return logger;
        }
        logger = BuildConfig.DEBUG ? new LWQDebugLogger() : new LWQLoggerImpl();
        return get();
    }
}
