package com.stanleyidesis.livewallpaperquotes.api.network;

import android.util.Log;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by stanleyidesis on 8/14/15.
 */
public class BrainyQuoteManager {

    private interface Endpoints {
        String BRAINY_QUOTE_TOPICS = "https://www.brainyquote.com/quotes/topics.html";
        String BRAINY_QUOTE_TOPIC_PAGE_1 = "https://www.brainyquote.com/quotes/topics/%s.html";
        String BRAINY_QUOTE_TOPIC_PAGE_NUMBER = "https://www.brainyquote.com/quotes/topics/%s%d.html";
    }

    private ScheduledExecutorService scheduledExecutorService;

    private void submit(final Runnable runnable) {
        scheduledExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } catch (Exception e) {
                    Log.e(getClass().getSimpleName(), "Error in executor service task", e);
                }
            }
        });
    }

    public BrainyQuoteManager() {
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    }

    public class BrainyQuote {
        public String author;
        public String quote;
    }
}
