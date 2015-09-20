package com.stanleyidesis.livewallpaperquotes.api.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.stanleyidesis.livewallpaperquotes.LWQApplication;
import com.stanleyidesis.livewallpaperquotes.api.Callback;
import com.stanleyidesis.livewallpaperquotes.api.receiver.LWQReceiver;

/**
 * Copyright (c) 2015 Stanley Idesis
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 *
 * LWQUpdateService.java
 * @author Stanley Idesis
 *
 * From Live-Wallpaper-Quotes
 * https://github.com/stanidesis/live-wallpaper-quotes
 *
 * Please report any issues
 * https://github.com/stanidesis/live-wallpaper-quotes/issues
 *
 * Date: 08/14/2015
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
                if (!aBoolean) {
                    LWQApplication.getWallpaperController().retrieveActiveWallpaper(new Callback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean aBoolean) {
                            LWQApplication.getNotificationController().postNewWallpaperNotification();
                            LWQReceiver.completeWakefulIntent(intent);
                        }

                        @Override
                        public void onError(String errorMessage) {
                            // Boo! TODO ummm?
                            LWQReceiver.completeWakefulIntent(intent);
                        }
                    }, false);
                } else {
                    LWQApplication.getNotificationController().postNewWallpaperNotification();
                    LWQReceiver.completeWakefulIntent(intent);
                }
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
