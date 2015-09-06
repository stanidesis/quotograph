package com.stanleyidesis.livewallpaperquotes.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.stanleyidesis.livewallpaperquotes.LWQApplication;
import com.stanleyidesis.livewallpaperquotes.LWQPreferences;
import com.stanleyidesis.livewallpaperquotes.R;
import com.stanleyidesis.livewallpaperquotes.api.Callback;
import com.stanleyidesis.livewallpaperquotes.api.LWQDrawScript;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

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
 * LWQWallpaperActivity.java
 * @author Stanley Idesis
 *
 * From Live-Wallpaper-Quotes
 * https://github.com/stanidesis/live-wallpaper-quotes
 *
 * Please report any issues
 * https://github.com/stanidesis/live-wallpaper-quotes/issues
 *
 * Date: 09/06/2015
 */
public abstract class LWQWallpaperActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    enum SilkScreenState {
        DEFAULT(1f, .6f),
        REVEAL(.2f, 0f);

        float contentAlpha;
        float screenAlpha;

        SilkScreenState(float contentAlpha, float screenAlpha) {
            this.contentAlpha = contentAlpha;
            this.screenAlpha = screenAlpha;
        }
    }

    ScheduledExecutorService scheduledExecutorService;
    SurfaceView surfaceView;
    View silkScreen;
    LWQDrawScript drawScript;
    BroadcastReceiver newWallpaperBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (drawScript != null) {
                drawScript.draw();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fullScreenIfPossible();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        silkScreen = findViewById(R.id.view_screen_lwq_wallpaper);
        surfaceView = (SurfaceView) findViewById(R.id.surface_lwq_wallpaper);
        surfaceView.getHolder().addCallback(this);
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(getString(R.string.broadcast_new_wallpaper_available));
        registerReceiver(newWallpaperBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(newWallpaperBroadcastReceiver);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (LWQPreferences.isFirstLaunch()) {
            return;
        }
        LWQApplication.getWallpaperController().retrieveActiveWallpaper(new Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                draw();
            }

            @Override
            public void onError(String errorMessage) {

            }
        }, true);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (LWQPreferences.isFirstLaunch()) {
            return;
        }
        draw();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    void animateSilkScreen(SilkScreenState state) {
        silkScreen.animate().alpha(state.screenAlpha).setDuration(300).setInterpolator(new AccelerateDecelerateInterpolator()).start();
        // TODO fade out container
//        coordinator.animate().alpha(state.contentAlpha).setDuration(300).setInterpolator(new AccelerateDecelerateInterpolator()).start();
    }

    void fullScreenIfPossible() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }

    void draw() {
        if (drawScript == null) {
            drawScript = new LWQDrawScript(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(Palette palette) {
                    draw();
                }
            }, surfaceView.getHolder());
        } else {
            drawScript.setSurfaceHolder(surfaceView.getHolder());
        }
        scheduledExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    drawScript.draw();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}