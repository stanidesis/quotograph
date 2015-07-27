package com.stanleyidesis.livewallpaperquotes.api.service;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
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

import com.stanleyidesis.livewallpaperquotes.R;
import com.stanleyidesis.livewallpaperquotes.api.db.Quote;
import com.stanleyidesis.livewallpaperquotes.ui.Fonts;

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
            Log.v(getClass().getSimpleName(), "Visible: " + visible, new Throwable());
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

            final int horizontalPadding = (int) (screenWidth * .07);
            final int verticalPadding = (int) (screenHeight * .07);

            int googleBarOffset = 0;

            Rect drawingArea = new Rect(horizontalPadding, verticalPadding, screenWidth - horizontalPadding, screenHeight - verticalPadding);

            // Google Now Search Offset
            int currentAPIVersion = android.os.Build.VERSION.SDK_INT;
            if (currentAPIVersion >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                // There's a good chance the Google Search Bar is there, I'm going to assume
                // it is for ICS+ installs, and just offset the top
                final TypedArray styledAttributes = getTheme().obtainStyledAttributes(
                        new int[] { android.R.attr.actionBarSize });
                int actionBarSize = (int) styledAttributes.getDimension(0, 0);
                styledAttributes.recycle();
                googleBarOffset = actionBarSize;
            }

            canvas.clipRect(drawingArea, Region.Op.REPLACE);
//            canvas.drawColor(getResources().getColor(android.R.color.darker_gray));

            // TODO use Palette class's swatch abilities to get title/text colors see: https://www.bignerdranch.com/blog/extracting-colors-to-a-palette-with-android-lollipop/

            // Setup Quote text
            TextPaint quoteTextPaint = new TextPaint();
            quoteTextPaint.setTextAlign(Paint.Align.LEFT);
            quoteTextPaint.setColor(getResources().getColor(android.R.color.black));
            quoteTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
            quoteTextPaint.setTypeface(Fonts.JOSEFIN_LIGHT.load(LWQWallpaperService.this));
            quoteTextPaint.setTextSize(145f);
            quoteTextPaint.setStyle(Paint.Style.FILL);

            // Setup Author Text
            TextPaint authorTextPaint = new TextPaint(quoteTextPaint);
            authorTextPaint.setTextSize(100f);
            authorTextPaint.setTextAlign(Paint.Align.RIGHT);
            authorTextPaint.setTypeface(Fonts.DAWNING_OF_A_NEW_DAY.load(LWQWallpaperService.this));
            String author = getString(R.string.unknown);
            if (activeQuote.author != null && activeQuote.author.name != null && !activeQuote.author.name.isEmpty()) {
                author = activeQuote.author.name;
            }
            StaticLayout quoteLayout = new StaticLayout(activeQuote.text.toUpperCase(), quoteTextPaint,
                    drawingArea.width(), Layout.Alignment.ALIGN_NORMAL, 1, 0, true);
            StaticLayout authorLayout = new StaticLayout(author, authorTextPaint,
                    drawingArea.width(), Layout.Alignment.ALIGN_NORMAL, 1, 0, true);

            // Correct the quote height, if necessary
            quoteLayout = correctFontSize(quoteLayout, drawingArea.height() - authorLayout.getHeight());

            int centerQuoteOffset = (int)(.5 * (drawingArea.height() - quoteLayout.getHeight()));
            if (drawingArea.top + centerQuoteOffset < googleBarOffset) {
                quoteLayout = correctFontSize(quoteLayout, drawingArea.height() - authorLayout.getHeight() - googleBarOffset);
                centerQuoteOffset = (int)(.5 * (drawingArea.height() - quoteLayout.getHeight()));
            }

            // Draw the quote centered vertically
            canvas.translate(drawingArea.left, drawingArea.top + centerQuoteOffset);
            quoteLayout.draw(canvas);
            canvas.translate(drawingArea.width(), quoteLayout.getHeight());
            authorLayout.draw(canvas);

            canvas.restore();
            holder.unlockCanvasAndPost(canvas);
        }

        StaticLayout correctFontSize(StaticLayout staticLayout, int maxHeight) {
            final TextPaint textPaint = staticLayout.getPaint();
            while (staticLayout.getHeight() > maxHeight) {
                textPaint.setTextSize(textPaint.getTextSize() * .95f);
                staticLayout = new StaticLayout(staticLayout.getText(), textPaint,
                        staticLayout.getWidth(), staticLayout.getAlignment(), staticLayout.getSpacingMultiplier(),
                        staticLayout.getSpacingAdd(), true);
            }
            return staticLayout;
        }

        void strokeText(StaticLayout staticLayout, Canvas canvas) {
            TextPaint strokePaint = new TextPaint(staticLayout.getPaint());
            strokePaint.setColor(getResources().getColor(android.R.color.holo_orange_light));
            strokePaint.setStyle(Paint.Style.STROKE);
            strokePaint.setStrokeWidth(5f);
            strokePaint.setStrokeJoin(Paint.Join.MITER);
            StaticLayout strokeLayout = new StaticLayout(staticLayout.getText(), strokePaint,
                    staticLayout.getWidth(), staticLayout.getAlignment(), staticLayout.getSpacingMultiplier(),
                    staticLayout.getSpacingAdd(), true);
            strokeLayout.draw(canvas);
        }
    }

    @Override
    public Engine onCreateEngine() {
        return new LWQWallpaperEngine();
    }

}
