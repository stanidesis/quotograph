package com.stanleyidesis.livewallpaperquotes.api.service;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.service.wallpaper.WallpaperService;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import com.stanleyidesis.livewallpaperquotes.api.db.Quote;

/**
 * Created by stanleyidesis on 7/11/15.
 */
public class LWQWallpaperService extends WallpaperService {

    public class LWQWallpaperEngine extends Engine {

        private Quote activeQuote;
        private float xOffset;
        private float yOffset;
        private float xOffsetStep;
        private float yOffsetStep;
        private int xPixelOffset;
        private int yPixelOffset;
        private int format;
        private int width;
        private int height;

        @Override
        public Bundle onCommand(String action, int x, int y, int z, Bundle extras, boolean resultRequested) {
            Log.v(getClass().getSimpleName(), null, new Throwable());
            return super.onCommand(action, x, y, z, extras, resultRequested);
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            setOffsetNotificationsEnabled(true);
            Log.v(getClass().getSimpleName(), null, new Throwable());
            activeQuote = Quote.active();
            if (activeQuote == null) {
                activeQuote = Quote.random();
                Log.v(getClass().getSimpleName(), "Active quote recovered: " + activeQuote);
//                activeQuote.active = true;
//                activeQuote.save();
            } else {
                Log.v(getClass().getSimpleName(), "Active quote found: " + activeQuote);
            }
        }

        @Override
        public void onDesiredSizeChanged(int desiredWidth, int desiredHeight) {
            super.onDesiredSizeChanged(desiredWidth, desiredHeight);
            Log.v(getClass().getSimpleName(), null, new Throwable());
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            Log.v(getClass().getSimpleName(), null, new Throwable());
        }

        @Override
        public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {
            super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep, xPixelOffset, yPixelOffset);
            Log.v(getClass().getSimpleName(), null, new Throwable());
            this.xOffset = xOffset;
            this.yOffset = yOffset;
            this.xOffsetStep = xOffsetStep;
            this.yOffsetStep = yOffsetStep;
            this.xPixelOffset = xPixelOffset;
            this.yPixelOffset = yPixelOffset;
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            Log.v(getClass().getSimpleName(), null, new Throwable());
            this.format = format;
            this.width = width;
            this.height = height;
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
            Log.v(getClass().getSimpleName(), null, new Throwable());
        }

        @Override
        public void onSurfaceRedrawNeeded(SurfaceHolder holder) {
            super.onSurfaceRedrawNeeded(holder);
            Log.v(getClass().getSimpleName(), null, new Throwable());
            draw(holder);
        }

        @Override
        public void onTouchEvent(MotionEvent event) {
            super.onTouchEvent(event);
            Log.v(getClass().getSimpleName(), null, new Throwable());
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            Log.v(getClass().getSimpleName(), null, new Throwable());
        }

        void draw(SurfaceHolder holder) {
            final Canvas canvas = holder.lockCanvas();
            canvas.drawColor(getResources().getColor(android.R.color.white));
            canvas.save();

            // Get screen width/height
            WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            final Display defaultDisplay = windowManager.getDefaultDisplay();
            final Point size = new Point();
            defaultDisplay.getSize(size);
            final int screenWidth = size.x;
            final int screenHeight = size.y;

            final int desiredMinimumHeight = getDesiredMinimumHeight();
            final int desiredMinimumWidth = getDesiredMinimumWidth();

            final int maxQuoteWidth = Math.min(screenWidth, desiredMinimumWidth);
            final int maxQuoteHeight = Math.max(screenHeight, desiredMinimumHeight);

            final int horizontalPadding = (int) (maxQuoteWidth * .07);
            final int verticalPadding = (int) (maxQuoteHeight * .07);

            Rect clipRect = new Rect(horizontalPadding, verticalPadding, maxQuoteWidth - horizontalPadding, maxQuoteHeight - verticalPadding);

            // Google Now Search Offset
            int currentAPIVersion = android.os.Build.VERSION.SDK_INT;
            if (currentAPIVersion >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                // There's a good chance the Google Search Bar is there, I'm going to assume
                // it is for ICS+ installs, and just offset the top
                final TypedArray styledAttributes = getTheme().obtainStyledAttributes(
                        new int[] { android.R.attr.actionBarSize });
                int actionBarSize = (int) styledAttributes.getDimension(0, 0);
                styledAttributes.recycle();
                clipRect.top += actionBarSize;
            }

            canvas.clipRect(clipRect, Region.Op.REPLACE);

            // Test clip
            canvas.drawColor(getResources().getColor(android.R.color.holo_orange_light));

            TextPaint textPaint = new TextPaint();
            textPaint.setTextAlign(Paint.Align.LEFT);
            textPaint.setColor(getResources().getColor(android.R.color.black));
            textPaint.setFlags(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
            textPaint.setTypeface(Typeface.DEFAULT_BOLD);
            textPaint.setTextSize(85f);
            textPaint.setStyle(Paint.Style.FILL);

            StaticLayout staticLayout = new StaticLayout(activeQuote.text, textPaint,
                    clipRect.width(), Layout.Alignment.ALIGN_NORMAL, 1, 0, true);
            canvas.translate(clipRect.left, clipRect.top);
            staticLayout.draw(canvas);

            final float quoteHeight = staticLayout.getHeight();
            textPaint.setTextSize(55f);
            textPaint.setTypeface(Typeface.DEFAULT);
            staticLayout = new StaticLayout(activeQuote.author.name, textPaint,
                    clipRect.width(), Layout.Alignment.ALIGN_NORMAL, 1, 0, true);
            canvas.translate(0, quoteHeight);
            staticLayout.draw(canvas);

            canvas.restore();
            holder.unlockCanvasAndPost(canvas);
        }

    }

    @Override
    public Engine onCreateEngine() {
        return new LWQWallpaperEngine();
    }

}
