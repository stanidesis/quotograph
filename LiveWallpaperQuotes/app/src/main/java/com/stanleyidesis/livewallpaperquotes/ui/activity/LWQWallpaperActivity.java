package com.stanleyidesis.livewallpaperquotes.ui.activity;

import android.animation.ObjectAnimator;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.stanleyidesis.livewallpaperquotes.LWQApplication;
import com.stanleyidesis.livewallpaperquotes.R;
import com.stanleyidesis.livewallpaperquotes.api.drawing.LWQSurfaceHolderDrawScript;
import com.stanleyidesis.livewallpaperquotes.api.event.NewWallpaperEvent;
import com.stanleyidesis.livewallpaperquotes.api.event.PreferenceUpdateEvent;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import de.greenrobot.event.EventBus;

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
        DEFAULT(1f, .7f),
        REVEAL(.1f, 0f);

        float contentAlpha;
        float screenAlpha;

        SilkScreenState(float contentAlpha, float screenAlpha) {
            this.contentAlpha = contentAlpha;
            this.screenAlpha = screenAlpha;
        }
    }

    ScheduledExecutorService scheduledExecutorService;
    LWQSurfaceHolderDrawScript drawScript;
    SurfaceView surfaceView;
    View silkScreen;

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
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!LWQApplication.getWallpaperController().activeWallpaperLoaded()) {
            LWQApplication.getWallpaperController().retrieveActiveWallpaper();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (!LWQApplication.getWallpaperController().activeWallpaperLoaded()) {
            return;
        }
        draw();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {}

    public void onEvent(NewWallpaperEvent newWallpaperEvent) {
        if (newWallpaperEvent.loaded) {
            draw();
        }
    }

    public void onEvent(PreferenceUpdateEvent preferenceUpdateEvent) {
        if (preferenceUpdateEvent.getPreferenceKeyId() == R.string.preference_key_blur ||
                preferenceUpdateEvent.getPreferenceKeyId() == R.string.preference_key_dim) {
            draw();
        }
    }

    void switchToSilkScreen(SilkScreenState state) {
        switchToSilkScreen(state, null);
    }

    void switchToSilkScreen(SilkScreenState state, View content) {
        silkScreen.setAlpha(state.screenAlpha);
        if (content != null) {
            content.setAlpha(state.contentAlpha);
        }
    }

    void animateSilkScreen(SilkScreenState state, View content) {
        ObjectAnimator silkScreenAnimator = ObjectAnimator.ofFloat(silkScreen, "alpha", silkScreen.getAlpha(), state.screenAlpha);
        silkScreenAnimator.setDuration(300);
        silkScreenAnimator.setInterpolator(new LinearInterpolator());
        silkScreenAnimator.start();
        if (content != null) {
            ObjectAnimator contentAnimator = ObjectAnimator.ofFloat(content, "alpha", content.getAlpha(), state.contentAlpha);
            contentAnimator.setDuration(300);
            contentAnimator.setInterpolator(new LinearInterpolator());
            contentAnimator.start();
        }
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
            drawScript = new LWQSurfaceHolderDrawScript(surfaceView.getHolder());
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
