package com.stanleyidesis.livewallpaperquotes.api.service;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.service.wallpaper.WallpaperService;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.stanleyidesis.livewallpaperquotes.LWQApplication;
import com.stanleyidesis.livewallpaperquotes.LWQPreferences;
import com.stanleyidesis.livewallpaperquotes.R;
import com.stanleyidesis.livewallpaperquotes.api.controller.LWQAlarmController;
import com.stanleyidesis.livewallpaperquotes.api.controller.LWQWallpaperController;
import com.stanleyidesis.livewallpaperquotes.api.drawing.LWQSurfaceHolderDrawScript;
import com.stanleyidesis.livewallpaperquotes.api.event.PreferenceUpdateEvent;
import com.stanleyidesis.livewallpaperquotes.api.event.WallpaperRetrievedEvent;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

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
 * LWQWallpaperService.java
 * @author Stanley Idesis
 *
 * From Live-Wallpaper-Quotes
 * https://github.com/stanidesis/live-wallpaper-quotes
 *
 * Please report any issues
 * https://github.com/stanidesis/live-wallpaper-quotes/issues
 *
 * Date: 07/11/2015
 */
public class LWQWallpaperService extends WallpaperService {

    public class LWQWallpaperEngine extends Engine implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

        private LWQSurfaceHolderDrawScript drawScript;
        private Executor executor;

        private GestureDetectorCompat gestureDetectorCompat;

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            drawScript = new LWQSurfaceHolderDrawScript(surfaceHolder);
            LWQAlarmController.resetAlarm();
            gestureDetectorCompat = new GestureDetectorCompat(LWQWallpaperService.this, this);
            gestureDetectorCompat.setOnDoubleTapListener(this);
            executor = Executors.newSingleThreadExecutor();
            EventBus.getDefault().register(this);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            EventBus.getDefault().unregister(this);
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            asyncSetHolder(holder);
            asyncDraw();
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
            asyncSetHolder(holder);
        }

        @Override
        public void onSurfaceRedrawNeeded(SurfaceHolder holder) {
            super.onSurfaceRedrawNeeded(holder);
            asyncSetHolder(holder);
            asyncDraw();
        }

        @Override
        public void onTouchEvent(MotionEvent event) {
            super.onTouchEvent(event);
            if (gestureDetectorCompat != null) {
                gestureDetectorCompat.onTouchEvent(event);
            }
            Log.v(getClass().getSimpleName(), null, new Throwable());
        }

        public void onEvent(WallpaperRetrievedEvent wallpaperRetrievedEvent) {
            if (wallpaperRetrievedEvent.didFail()) {
                return;
            }
            asyncDraw();
        }

        public void onEvent(PreferenceUpdateEvent preferenceUpdateEvent) {
            if (preferenceUpdateEvent.getPreferenceKeyId() == R.string.preference_key_blur ||
                    preferenceUpdateEvent.getPreferenceKeyId() == R.string.preference_key_dim) {
                asyncDraw();
            }
        }

        void asyncDraw() {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        final LWQWallpaperController wallpaperController =
                                LWQApplication.getWallpaperController();
                        if (!wallpaperController.activeWallpaperLoaded()) {
                            wallpaperController.retrieveActiveWallpaper();
                            return;
                        }
                        drawScript.draw();
                    } catch (Exception e) {
                        Log.e(getClass().getSimpleName(), "Failure to draw", e);
                    }
                }
            });
        }

        void asyncSetHolder(final SurfaceHolder surfaceHolder) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        drawScript.setSurfaceHolder(surfaceHolder);
                    } catch (Exception e) {
                        Log.e(getClass().getSimpleName(), "Failure to set SurfaceHolder", e);
                    }
                }
            });
        }

        /*
         * Gesture detection
         */

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (LWQPreferences.isDoubleTapEnabled()) {
                final PackageManager packageManager = getPackageManager();
                final Intent launchIntentForPackage = packageManager.getLaunchIntentForPackage(getPackageName());
                startActivity(launchIntentForPackage);
            }
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
    }

    @Override
    public Engine onCreateEngine() {
        return new LWQWallpaperEngine();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LWQApplication.getWallpaperController().discardActiveWallpaper();
    }
}
