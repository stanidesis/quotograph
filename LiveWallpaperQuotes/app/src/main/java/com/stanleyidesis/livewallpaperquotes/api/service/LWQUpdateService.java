package com.stanleyidesis.livewallpaperquotes.api.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.stanleyidesis.livewallpaperquotes.LWQApplication;
import com.stanleyidesis.livewallpaperquotes.api.Callback;
import com.stanleyidesis.livewallpaperquotes.api.receiver.LWQReceiver;

/**
 * Created by stanleyidesis on 8/14/15.
 */
public class LWQUpdateService extends IntentService {

    public LWQUpdateService() {
        super("LWQUpdateService");
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        LWQApplication.getWallpaperController().generateNewWallpaper(new Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                Log.v(getClass().getSimpleName(), "Updated Wallpaper Automagically", new Throwable());
                LWQReceiver.completeWakefulIntent(intent);
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(getClass().getSimpleName(), errorMessage, new Throwable());
                // TODO try again?
                LWQReceiver.completeWakefulIntent(intent);
            }
        });
    }
}
