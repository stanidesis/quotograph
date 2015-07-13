package com.stanleyidesis.livewallpaperquotes.api.service;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.Typeface;
import android.os.Bundle;
import android.service.wallpaper.WallpaperService;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

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

            final Rect clipBounds = canvas.getClipBounds();

            final int desiredMinimumHeight = getDesiredMinimumHeight();
            final int desiredMinimumWidth = getDesiredMinimumWidth();
            final int horizontalPadding = (int) (desiredMinimumWidth * .07);
            final int verticalPadding = (int) (desiredMinimumHeight * .07);

            Rect clipRect = new Rect(horizontalPadding, verticalPadding, desiredMinimumWidth - horizontalPadding, desiredMinimumHeight - verticalPadding);
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
            canvas.translate(horizontalPadding, verticalPadding);
            staticLayout.draw(canvas);

            final float quoteHeight = getTextHeight(activeQuote.text, textPaint);
            textPaint.setTextSize(55f);
            textPaint.setTypeface(Typeface.DEFAULT);
            staticLayout = new StaticLayout(activeQuote.author.name, textPaint,
                    clipRect.width(), Layout.Alignment.ALIGN_NORMAL, 1, 0, true);
            canvas.translate(0, quoteHeight + verticalPadding);
            staticLayout.draw(canvas);

            canvas.restore();
            holder.unlockCanvasAndPost(canvas);
        }

        /**
         * @return text height
         */
        private float getTextHeight(String text, Paint paint) {

            Rect rect = new Rect();
            paint.getTextBounds(text, 0, text.length(), rect);
            return rect.height();
        }

    }

    @Override
    public Engine onCreateEngine() {
        return new LWQWallpaperEngine();
    }

}
