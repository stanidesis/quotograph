package com.stanleyidesis.livewallpaperquotes.api.service;

import android.os.Bundle;
import android.service.wallpaper.WallpaperService;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.stanleyidesis.livewallpaperquotes.BuildConfig;
import com.stanleyidesis.livewallpaperquotes.LWQApplication;
import com.stanleyidesis.livewallpaperquotes.R;
import com.stanleyidesis.livewallpaperquotes.api.controller.LWQAlarmController;
import com.stanleyidesis.livewallpaperquotes.api.controller.LWQWallpaperController;
import com.stanleyidesis.livewallpaperquotes.api.drawing.LWQSurfaceHolderDrawScript;
import com.stanleyidesis.livewallpaperquotes.api.event.NewWallpaperEvent;
import com.stanleyidesis.livewallpaperquotes.api.event.PreferenceUpdateEvent;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import de.greenrobot.event.EventBus;

/**
 * Created by stanleyidesis on 7/11/15.
 */
public class LWQWallpaperService extends WallpaperService {

    public class LWQWallpaperEngine extends Engine implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

        private LWQSurfaceHolderDrawScript drawScript;
        private Executor executor;

        private GestureDetectorCompat gestureDetectorCompat;

        @Override
        public Bundle onCommand(String action, int x, int y, int z, Bundle extras, boolean resultRequested) {
            Log.v(getClass().getSimpleName(), null, new Throwable());
            return super.onCommand(action, x, y, z, extras, resultRequested);
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            drawScript = new LWQSurfaceHolderDrawScript(surfaceHolder);
            // setOffsetNotificationsEnabled(true);
            // TODO better place for this? Maybe not.
            LWQAlarmController.resetAlarm();
            if (BuildConfig.DEBUG) {
                gestureDetectorCompat = new GestureDetectorCompat(LWQWallpaperService.this, this);
                gestureDetectorCompat.setOnDoubleTapListener(this);
            }
            executor = Executors.newSingleThreadExecutor();
            EventBus.getDefault().register(this);
            Log.v(getClass().getSimpleName(), null, new Throwable());
        }

        @Override
        public void onDesiredSizeChanged(int desiredWidth, int desiredHeight) {
            super.onDesiredSizeChanged(desiredWidth, desiredHeight);
            Log.v(getClass().getSimpleName(), null, new Throwable());
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            EventBus.getDefault().unregister(this);
            Log.v(getClass().getSimpleName(), null, new Throwable());
        }

        @Override
        public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {
            super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep, xPixelOffset, yPixelOffset);
            Log.v(getClass().getSimpleName(), null, new Throwable());
            /*
            Log.v(getClass().getSimpleName(), "xOffset: " + xOffset);
            Log.v(getClass().getSimpleName(), "yOffset: " + yOffset);
            Log.v(getClass().getSimpleName(), "xOffsetStep: " + xOffsetStep);
            Log.v(getClass().getSimpleName(), "yOffsetStep: " + yOffsetStep);
            Log.v(getClass().getSimpleName(), "xPixelOffset: " + xPixelOffset);
            Log.v(getClass().getSimpleName(), "yPixelOffset: " + yPixelOffset);
            */
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            Log.v(getClass().getSimpleName(), null, new Throwable());
            asyncSetHolder(holder);
            asyncDraw();
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
            asyncSetHolder(holder);
            Log.v(getClass().getSimpleName(), null, new Throwable());
        }

        @Override
        public void onSurfaceRedrawNeeded(SurfaceHolder holder) {
            super.onSurfaceRedrawNeeded(holder);
            Log.v(getClass().getSimpleName(), null, new Throwable());
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

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            Log.v(getClass().getSimpleName(), "Visible: " + visible, new Throwable());
        }

        public void onEvent(NewWallpaperEvent newWallpaperEvent) {
            if (newWallpaperEvent.loaded) {
                asyncDraw();
            } else if (!newWallpaperEvent.didFail()){
                LWQApplication.getWallpaperController().retrieveActiveWallpaper();
            }
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
//            if (BuildConfig.DEBUG) {
//                drawScript.changeSwatch();
//                asyncDraw();
//            }
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (BuildConfig.DEBUG) {
                LWQApplication.getWallpaperController().generateNewWallpaper();
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
        Log.v(getClass().getSimpleName(), null, new Throwable());
        super.onDestroy();
        LWQApplication.getWallpaperController().discardActiveWallpaper();
    }
}
