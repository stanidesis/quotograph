package com.stanleyidesis.livewallpaperquotes.api.service;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.service.wallpaper.WallpaperService;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.graphics.Palette;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import com.stanleyidesis.livewallpaperquotes.BuildConfig;
import com.stanleyidesis.livewallpaperquotes.LWQApplication;
import com.stanleyidesis.livewallpaperquotes.R;
import com.stanleyidesis.livewallpaperquotes.api.Callback;
import com.stanleyidesis.livewallpaperquotes.api.LWQWallpaperController;
import com.stanleyidesis.livewallpaperquotes.ui.Fonts;

/**
 * Created by stanleyidesis on 7/11/15.
 */
public class LWQWallpaperService extends WallpaperService {

    public class LWQWallpaperEngine extends Engine implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

        private float xOffset;
        private float yOffset;
        private float xOffsetStep;
        private float yOffsetStep;
        private int xPixelOffset;
        private int yPixelOffset;
        private int format;
        private int width;
        private int height;
        private GestureDetectorCompat gestureDetectorCompat;
        private Palette palette;
        private Callback<Boolean> callback = new Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean loaded) {
                if (loaded || LWQApplication.getWallpaperController().activeWallpaperLoaded()) {
                    palette = Palette.from(LWQApplication.getWallpaperController().getBackgroundImage()).generate();
                    final Looper mainLooper = Looper.getMainLooper();
                    new Handler(mainLooper).post(new Runnable() {
                        @Override
                        public void run() {
                            draw(getSurfaceHolder());
                        }
                    });
                } else {
                    LWQApplication.getWallpaperController().retrieveActiveWallpaper(this);
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(LWQWallpaperEngine.class.getSimpleName(), errorMessage);
            }
        };

        @Override
        public Bundle onCommand(String action, int x, int y, int z, Bundle extras, boolean resultRequested) {
            Log.v(getClass().getSimpleName(), null, new Throwable());
            return super.onCommand(action, x, y, z, extras, resultRequested);
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            setOffsetNotificationsEnabled(true);
            if (BuildConfig.DEBUG) {
                gestureDetectorCompat = new GestureDetectorCompat(LWQWallpaperService.this, this);
                gestureDetectorCompat.setOnDoubleTapListener(this);
            }
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
            Log.v(getClass().getSimpleName(), null, new Throwable());
        }

        @Override
        public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {
            super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep, xPixelOffset, yPixelOffset);
            Log.v(getClass().getSimpleName(), null, new Throwable());
            boolean redraw = this.yPixelOffset != yPixelOffset;
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
            gestureDetectorCompat.onTouchEvent(event);
            Log.v(getClass().getSimpleName(), null, new Throwable());
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            Log.v(getClass().getSimpleName(), "Visible: " + visible, new Throwable());
        }

        void draw(SurfaceHolder holder) {
            final LWQWallpaperController wallpaperController =
                    LWQApplication.getWallpaperController();
            if (!wallpaperController.activeWallpaperLoaded()) {
                if (wallpaperController.activeWallpaperExists()) {
                    wallpaperController.retrieveActiveWallpaper(callback);
                } else {
                    // TODO ultimately does not belong here
                    wallpaperController.generateNewWallpaper(callback);
                }
            }

            final Canvas canvas = holder.lockCanvas();
            canvas.drawColor(getResources().getColor(android.R.color.white));
            canvas.save();

            // Get screen width/height
            final Rect surfaceFrame = getSurfaceHolder().getSurfaceFrame();
            WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            final Display defaultDisplay = windowManager.getDefaultDisplay();
            final Point size = new Point();
            defaultDisplay.getSize(size);
            final int screenWidth = size.x;
            final int screenHeight = holder.getSurfaceFrame().height();

            final int horizontalPadding = (int) (screenWidth * .07);
            final int verticalPadding = (int) (screenHeight * .07);

            final Bitmap backgroundImage = wallpaperController.getBackgroundImage();
            if (backgroundImage != null) {
                Paint bitmapPaint = new Paint();
                bitmapPaint.setAntiAlias(true);
                bitmapPaint.setFilterBitmap(true);
                bitmapPaint.setDither(true);

                float scaleY = (float) surfaceFrame.height() / (float) backgroundImage.getHeight();
                float scaleX = (float) surfaceFrame.width() / (float) backgroundImage.getWidth();
                float finalScale = Math.max(scaleX, scaleY);

                Matrix scaleMatrix = new Matrix();
                scaleMatrix.postScale(finalScale, finalScale);
                canvas.drawBitmap(backgroundImage, scaleMatrix, bitmapPaint);
            } else {
                // TODO default background image
            }

            final Rect drawingArea = new Rect(horizontalPadding, verticalPadding,
                    screenWidth - horizontalPadding, screenHeight - verticalPadding);

            int googleBarOffset = 0;
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

            /*
            canvas.clipRect(drawingArea, Region.Op.REPLACE);
            canvas.drawColor(getResources().getColor(android.R.color.darker_gray));
            */

            int quoteColor = getResources().getColor(android.R.color.black);
            int quoteStrokeColor = quoteColor;
            int authorColor = quoteColor;
            if (palette != null) {
                Palette.Swatch textSwatch = palette.getDarkVibrantSwatch();
                if (textSwatch == null) {
                    textSwatch = palette.getDarkMutedSwatch();
                }
                if (textSwatch != null) {
                    quoteColor = textSwatch.getBodyTextColor();
                    authorColor = textSwatch.getTitleTextColor();
                }

                Palette.Swatch strokeSwatch = palette.getLightVibrantSwatch();
                if (strokeSwatch == null) {
                    strokeSwatch = palette.getLightMutedSwatch();
                }
                if (strokeSwatch != null) {
                    quoteStrokeColor = strokeSwatch.getTitleTextColor();
                }

            } else if (backgroundImage != null) {
                Palette.from(backgroundImage).generate(new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(Palette palette) {
                        LWQWallpaperEngine.this.palette = palette;
                        draw(getSurfaceHolder());
                    }
                });
            }

            // Setup Quote text
            TextPaint quoteTextPaint = new TextPaint();
            quoteTextPaint.setTextAlign(Paint.Align.LEFT);
            quoteTextPaint.setColor(quoteColor);
            quoteTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
            quoteTextPaint.setTypeface(Fonts.JOSEFIN_BOLD.load(LWQWallpaperService.this));
            quoteTextPaint.setTextSize(145f);
            quoteTextPaint.setStyle(Paint.Style.FILL);

            // Setup Author Text
            TextPaint authorTextPaint = new TextPaint(quoteTextPaint);
            authorTextPaint.setTextSize(100f);
            authorTextPaint.setTextAlign(Paint.Align.RIGHT);
            authorTextPaint.setFlags(authorTextPaint.getFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
            authorTextPaint.setColor(authorColor);
            authorTextPaint.setTypeface(Fonts.DAWNING_OF_A_NEW_DAY.load(LWQWallpaperService.this));
            String author = getString(R.string.unknown);
            if (wallpaperController.getAuthor() != null && !wallpaperController.getAuthor().isEmpty()) {
                author = wallpaperController.getAuthor();
            }
            String quote = getString(R.string.unknown);
            if (wallpaperController.getQuote() != null && !wallpaperController.getQuote().isEmpty()) {
                quote = wallpaperController.getQuote();
            }
            StaticLayout quoteLayout = new StaticLayout(quote.toUpperCase(), quoteTextPaint,
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
            strokeText(quoteLayout, quoteStrokeColor, 2f, canvas);

            canvas.translate(drawingArea.width(), quoteLayout.getHeight());
            authorLayout.draw(canvas);
            strokeText(authorLayout, quoteStrokeColor, 1f, canvas);

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

        void strokeText(StaticLayout staticLayout, int color, float width, Canvas canvas) {
            TextPaint strokePaint = new TextPaint(staticLayout.getPaint());
            strokePaint.setColor(color);
            strokePaint.setStyle(Paint.Style.STROKE);
            strokePaint.setStrokeWidth(width);
            strokePaint.setStrokeJoin(Paint.Join.MITER);
            strokePaint.setStrokeCap(Paint.Cap.BUTT);
            StaticLayout strokeLayout = new StaticLayout(staticLayout.getText(), strokePaint,
                    staticLayout.getWidth(), staticLayout.getAlignment(), staticLayout.getSpacingMultiplier(),
                    staticLayout.getSpacingAdd(), true);
            strokeLayout.draw(canvas);
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
            LWQApplication.getWallpaperController().generateNewWallpaper(callback);
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
