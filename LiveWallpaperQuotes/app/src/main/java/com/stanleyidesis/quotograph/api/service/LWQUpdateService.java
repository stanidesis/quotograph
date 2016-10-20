package com.stanleyidesis.quotograph.api.service;

import android.app.IntentService;
import android.content.Intent;

import org.greenrobot.eventbus.Subscribe;
import com.stanleyidesis.quotograph.api.controller.LWQWallpaperControllerHelper;
import com.stanleyidesis.quotograph.api.event.WallpaperEvent;
import com.stanleyidesis.quotograph.api.receiver.LWQReceiver;

import org.greenrobot.eventbus.EventBus;

/**
 * Copyright (c) 2016 Stanley Idesis
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
 * From Quotograph
 * https://github.com/stanidesis/quotograph
 *
 * Please report any issues
 * https://github.com/stanidesis/quotograph/issues
 *
 * Date: 08/14/2015
 */
public class LWQUpdateService extends IntentService {

    final int MAX_ATTEMPTS = 3;
    int attempt;
    Intent intent;

    public LWQUpdateService() {
        super("LWQUpdateService");
    }

    @Override
    public void onCreate() {
        EventBus.getDefault().register(this);
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        this.intent = intent;
        this.attempt = 0;
        attempt();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Subscribe
    public void onEvent(WallpaperEvent wallpaperEvent) {
        if (wallpaperEvent.didFail()) {
            attempt();
        } else if (wallpaperEvent.getStatus() == WallpaperEvent.Status.RETRIEVED_WALLPAPER) {
            LWQReceiver.completeWakefulIntent(intent);
        }
    }

    void attempt() {
        attempt++;
        if (attempt > MAX_ATTEMPTS) {
            LWQReceiver.completeWakefulIntent(intent);
            return;
        }
        LWQWallpaperControllerHelper.get().generateNewWallpaper();
    }
}
