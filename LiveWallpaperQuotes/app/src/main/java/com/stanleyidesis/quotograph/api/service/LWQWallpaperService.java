package com.stanleyidesis.quotograph.api.service;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.service.wallpaper.WallpaperService;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.stanleyidesis.quotograph.LWQApplication;
import com.stanleyidesis.quotograph.LWQPreferences;
import com.stanleyidesis.quotograph.R;
import com.stanleyidesis.quotograph.api.BaseCallback;
import com.stanleyidesis.quotograph.api.controller.LWQWallpaperController;
import com.stanleyidesis.quotograph.api.drawing.LWQSurfaceHolderDrawScript;
import com.stanleyidesis.quotograph.api.event.PreferenceUpdateEvent;
import com.stanleyidesis.quotograph.api.event.WallpaperEvent;

import de.greenrobot.event.EventBus;

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
 * LWQWallpaperService.java
 * @author Stanley Idesis
 *
 * From Quotograph
 * https://github.com/stanidesis/quotograph
 *
 * Please report any issues
 * https://github.com/stanidesis/quotograph/issues
 *
 * Date: 07/11/2015
 */
public class LWQWallpaperService extends WallpaperService {

    public class LWQWallpaperEngine extends Engine implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

        LWQSurfaceHolderDrawScript drawScript;
        GestureDetectorCompat gestureDetectorCompat;
        Intent launchIntent;
        BaseCallback<Boolean> eventCallback = new BaseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                EventBus.getDefault().post(WallpaperEvent.withStatus(WallpaperEvent.Status.RENDERED_WALLPAPER));
            }
        };

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            drawScript = new LWQSurfaceHolderDrawScript(surfaceHolder);
            gestureDetectorCompat = new GestureDetectorCompat(LWQWallpaperService.this, this);
            gestureDetectorCompat.setOnDoubleTapListener(this);
            EventBus.getDefault().register(this);
            LWQWallpaperController wallpaperController = LWQApplication.getWallpaperController();
            if (!wallpaperController.activeWallpaperLoaded()) {
                wallpaperController.retrieveActiveWallpaper();
            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            EventBus.getDefault().unregister(this);
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            drawScript.setSurfaceHolder(holder);
            drawScript.requestDraw(eventCallback);
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
            drawScript.setSurfaceHolder(holder);
        }

        @Override
        public void onSurfaceRedrawNeeded(SurfaceHolder holder) {
            super.onSurfaceRedrawNeeded(holder);
            drawScript.setSurfaceHolder(holder);
            drawScript.requestDraw(eventCallback);
        }

        @Override
        public void onTouchEvent(MotionEvent event) {
            super.onTouchEvent(event);
            if (gestureDetectorCompat != null) {
                gestureDetectorCompat.onTouchEvent(event);
            }
        }

        public void onEvent(WallpaperEvent wallpaperEvent) {
            if (wallpaperEvent.didFail()) {
                return;
            }
            if (wallpaperEvent.getStatus() == WallpaperEvent.Status.RETRIEVED_WALLPAPER) {
                drawScript.requestDraw(eventCallback);
            }
        }

        public void onEvent(PreferenceUpdateEvent preferenceUpdateEvent) {
            if (preferenceUpdateEvent.getPreferenceKeyId() == R.string.preference_key_blur ||
                    preferenceUpdateEvent.getPreferenceKeyId() == R.string.preference_key_dim) {
                drawScript.requestDraw(eventCallback);
            }
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
            // TODO analytics and hard-code the Intent to test for speed
            if (LWQPreferences.isDoubleTapEnabled()) {
                if (launchIntent == null) {
                    final PackageManager packageManager = getPackageManager();
                    launchIntent = packageManager.getLaunchIntentForPackage(getPackageName());
                }
                startActivity(launchIntent);
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
