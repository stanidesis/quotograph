package com.stanleyidesis.quotograph.api.controller;

/**
 * Created by Stanley Idesis on 10/12/2016.
 */

public class LWQQuoteControllerHelper {
    static LWQQuoteController lwqQuoteController;

    public static LWQQuoteController get() {
        if (lwqQuoteController != null) {
            return lwqQuoteController;
        }
        lwqQuoteController = new LWQQuoteControllerBrainyQuoteImpl();
        return get();
    }
}
