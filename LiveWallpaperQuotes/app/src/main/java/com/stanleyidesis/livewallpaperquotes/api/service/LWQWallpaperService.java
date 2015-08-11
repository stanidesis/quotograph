package com.stanleyidesis.livewallpaperquotes.api.service;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
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
import android.widget.Toast;

import com.stanleyidesis.livewallpaperquotes.BuildConfig;
import com.stanleyidesis.livewallpaperquotes.LWQApplication;
import com.stanleyidesis.livewallpaperquotes.R;
import com.stanleyidesis.livewallpaperquotes.api.Callback;
import com.stanleyidesis.livewallpaperquotes.api.LWQWallpaperController;
import com.stanleyidesis.livewallpaperquotes.ui.Fonts;

import java.util.List;

/**
 * Created by stanleyidesis on 7/11/15.
 */
public class LWQWallpaperService extends WallpaperService {

    public class LWQWallpaperEngine extends Engine implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

        private static final int TEXT_ALPHA = 0xE5FFFFFF;
        private static final int STROKE_ALPHA = 0xF2FFFFFF;

        private Typeface quoteTypeFace;
        private Typeface authorTypeFace;

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
        private int swatchIndex;
        private Callback<Boolean> callback = new Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean loaded) {
                if (loaded || LWQApplication.getWallpaperController().activeWallpaperLoaded()) {
                    palette = Palette.from(LWQApplication.getWallpaperController().getBackgroundImage()).generate();
                    swatchIndex = -1;
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
            quoteTypeFace = Fonts.JOSEFIN_BOLD.load(LWQWallpaperService.this);
            authorTypeFace = quoteTypeFace;
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

        void draw(SurfaceHolder holder) {
            final LWQWallpaperController wallpaperController =
                    LWQApplication.getWallpaperController();
            if (!wallpaperController.activeWallpaperLoaded()) {
                wallpaperController.retrieveActiveWallpaper(callback);
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
            final int currentAPIVersion = android.os.Build.VERSION.SDK_INT;

            final Bitmap backgroundImage = wallpaperController.getBackgroundImage();
            if (backgroundImage != null) {
                Bitmap drawnBitmap = backgroundImage;
                float blurRadius = PreferenceManager.getDefaultSharedPreferences(LWQWallpaperService.this).getFloat(getString(R.string.preference_key_blur), 2f);
                boolean recycleBitmap = false;
                if (currentAPIVersion >= Build.VERSION_CODES.JELLY_BEAN_MR1 && blurRadius > 0f) {
                    recycleBitmap = true;
                    drawnBitmap = blurBitmap(backgroundImage, 5);
                }

                Paint bitmapPaint = new Paint();
                bitmapPaint.setAntiAlias(true);
                bitmapPaint.setFilterBitmap(true);
                bitmapPaint.setDither(true);

                float scaleY = (float) surfaceFrame.height() / (float) backgroundImage.getHeight();
                float scaleX = (float) surfaceFrame.width() / (float) backgroundImage.getWidth();
                float finalScale = Math.max(scaleX, scaleY);

                Matrix scaleMatrix = new Matrix();
                scaleMatrix.postScale(finalScale, finalScale);

                // Adjust center
                final int bitmapFinalWidth = (int)((float) drawnBitmap.getWidth() * finalScale);
                if (bitmapFinalWidth > screenWidth) {
                    final float dx = -0.5f * (bitmapFinalWidth - screenWidth);
                    canvas.translate(dx, 0);
                    canvas.drawBitmap(drawnBitmap, scaleMatrix, bitmapPaint);
                    canvas.translate(-dx, 0);
                } else {
                    canvas.drawBitmap(drawnBitmap, scaleMatrix, bitmapPaint);
                }
                if (recycleBitmap) {
                    drawnBitmap.recycle();
                }
            }

            final Rect drawingArea = new Rect(horizontalPadding, verticalPadding,
                    screenWidth - horizontalPadding, screenHeight - verticalPadding);

            int googleBarOffset = 0;
            // Google Now Search Offset
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
            int quoteStrokeColor = getResources().getColor(android.R.color.holo_blue_light);
            int authorColor = quoteColor;
            if (palette != null) {
                final Palette.Swatch swatch = getSwatch();
                quoteColor = swatch.getRgb();
                authorColor = swatch.getRgb();
                quoteStrokeColor = swatch.getTitleTextColor();
            } else if (backgroundImage != null) {
                Palette.from(backgroundImage).generate(new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(Palette palette) {
                        LWQWallpaperEngine.this.palette = palette;
                        LWQWallpaperEngine.this.swatchIndex = -1;
                        draw(getSurfaceHolder());
                    }
                });
            }

            // Setup Quote text
            TextPaint quoteTextPaint = new TextPaint();
            quoteTextPaint.setTextAlign(Paint.Align.LEFT);
            quoteTextPaint.setColor(quoteColor & TEXT_ALPHA);
            quoteTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
            quoteTextPaint.setTypeface(quoteTypeFace);
            quoteTextPaint.setTextSize(145f);
            quoteTextPaint.setStyle(Paint.Style.FILL);

            // Setup Author Text
            TextPaint authorTextPaint = new TextPaint(quoteTextPaint);
            authorTextPaint.setTextSize(100f);
            authorTextPaint.setTextAlign(Paint.Align.RIGHT);
            authorTextPaint.setColor(authorColor & TEXT_ALPHA);
            authorTextPaint.setTypeface(authorTypeFace);
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
            strokeText(quoteLayout, quoteStrokeColor & STROKE_ALPHA, 3f, canvas);

            canvas.translate(drawingArea.width(), quoteLayout.getHeight());
            authorLayout.draw(canvas);
            strokeText(authorLayout, quoteStrokeColor & STROKE_ALPHA, 3f, canvas);

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

        Palette.Swatch getSwatch() {
            swatchIndex++;
            final List<Palette.Swatch> swatches = palette.getSwatches();
            if (swatchIndex >= swatches.size()) {
                swatchIndex = 0;
            }
            final Palette.Swatch chosenSwatch = swatches.get(swatchIndex);
            if (!BuildConfig.DEBUG) {
                return chosenSwatch;
            }
            if (chosenSwatch == palette.getMutedSwatch()) {
                Toast.makeText(LWQWallpaperService.this, "Muted Swatch", Toast.LENGTH_LONG).show();
            } else if (chosenSwatch == palette.getVibrantSwatch()) {
                Toast.makeText(LWQWallpaperService.this, "Vibrant Swatch", Toast.LENGTH_LONG).show();
            } else if (chosenSwatch == palette.getDarkMutedSwatch()) {
                Toast.makeText(LWQWallpaperService.this, "Dark Muted Swatch", Toast.LENGTH_LONG).show();
            } else if (chosenSwatch == palette.getDarkVibrantSwatch()) {
                Toast.makeText(LWQWallpaperService.this, "Dark Vibrant Swatch", Toast.LENGTH_LONG).show();
            } else if (chosenSwatch == palette.getLightMutedSwatch()) {
                Toast.makeText(LWQWallpaperService.this, "Light Muted Swatch", Toast.LENGTH_LONG).show();
            } else if (chosenSwatch == palette.getLightVibrantSwatch()) {
                Toast.makeText(LWQWallpaperService.this, "Light Vibrant Swatch", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(LWQWallpaperService.this, "Unknown Swatch", Toast.LENGTH_LONG).show();
            }
            return chosenSwatch;
        }

        Bitmap blurBitmap(Bitmap original, float radius) {
            Bitmap overlay = Bitmap.createBitmap(original.getWidth(), original.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas overlayCanvas = new Canvas(overlay);
            overlayCanvas.drawBitmap(original, 0, 0, null);
            RenderScript renderScript = RenderScript.create(LWQWallpaperService.this);
            Allocation overlayAllocation = Allocation.createFromBitmap(renderScript, overlay);
            ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(renderScript, overlayAllocation.getElement());
            blur.setInput(overlayAllocation);
            blur.setRadius(radius);
            blur.forEach(overlayAllocation);
            overlayAllocation.copyTo(overlay);
            return overlay;
        }

        /*
         * Gesture detection
         */

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
//            draw(getSurfaceHolder());
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
