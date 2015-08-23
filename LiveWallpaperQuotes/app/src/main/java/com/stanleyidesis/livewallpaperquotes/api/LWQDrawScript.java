package com.stanleyidesis.livewallpaperquotes.api;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.v7.graphics.Palette;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.WindowManager;
import android.widget.Toast;

import com.stanleyidesis.livewallpaperquotes.BuildConfig;
import com.stanleyidesis.livewallpaperquotes.LWQApplication;
import com.stanleyidesis.livewallpaperquotes.LWQPreferences;
import com.stanleyidesis.livewallpaperquotes.R;
import com.stanleyidesis.livewallpaperquotes.ui.Fonts;

import java.util.List;

/**
 * Created by stanleyidesis on 8/22/15.
 */
public class LWQDrawScript {
    Palette palette;
    Palette.PaletteAsyncListener paletteAsyncListener;
    SurfaceHolder surfaceHolder;


    private static final int TEXT_ALPHA = 0xE5FFFFFF;
    private static final int STROKE_ALPHA = 0xF2FFFFFF;

    private Typeface quoteTypeFace;
    private Typeface authorTypeFace;
    private int swatchIndex;

    public LWQDrawScript(Palette.PaletteAsyncListener paletteAsyncListener, SurfaceHolder surfaceHolder) {
        this.paletteAsyncListener = paletteAsyncListener;
        this.surfaceHolder = surfaceHolder;
        quoteTypeFace = Fonts.JOSEFIN_BOLD.load(LWQApplication.get());
        authorTypeFace = quoteTypeFace;
    }

    public void setSurfaceHolder(SurfaceHolder surfaceHolder) {
        this.surfaceHolder = surfaceHolder;
    }

    public void setPaletteAsyncListener(Palette.PaletteAsyncListener paletteAsyncListener) {
        this.paletteAsyncListener = paletteAsyncListener;
    }

    public void changeSwatch() {
        swatchIndex++;
        final List<Palette.Swatch> swatches = palette.getSwatches();
        if (swatchIndex >= swatches.size()) {
            swatchIndex = 0;
        }
        final Palette.Swatch chosenSwatch = swatches.get(swatchIndex);
        if (!BuildConfig.DEBUG) {
            return;
        }
        if (chosenSwatch == palette.getMutedSwatch()) {
            Toast.makeText(LWQApplication.get(), "Muted Swatch", Toast.LENGTH_LONG).show();
        } else if (chosenSwatch == palette.getVibrantSwatch()) {
            Toast.makeText(LWQApplication.get(), "Vibrant Swatch", Toast.LENGTH_LONG).show();
        } else if (chosenSwatch == palette.getDarkMutedSwatch()) {
            Toast.makeText(LWQApplication.get(), "Dark Muted Swatch", Toast.LENGTH_LONG).show();
        } else if (chosenSwatch == palette.getDarkVibrantSwatch()) {
            Toast.makeText(LWQApplication.get(), "Dark Vibrant Swatch", Toast.LENGTH_LONG).show();
        } else if (chosenSwatch == palette.getLightMutedSwatch()) {
            Toast.makeText(LWQApplication.get(), "Light Muted Swatch", Toast.LENGTH_LONG).show();
        } else if (chosenSwatch == palette.getLightVibrantSwatch()) {
            Toast.makeText(LWQApplication.get(), "Light Vibrant Swatch", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(LWQApplication.get(), "Unknown Swatch", Toast.LENGTH_LONG).show();
        }
    }

    public void draw() {
        final LWQWallpaperController wallpaperController =
                LWQApplication.getWallpaperController();
        if (!wallpaperController.activeWallpaperLoaded()) {
            Log.v(getClass().getSimpleName(), "Wallpaper not loaded, cannot draw");
            return;
        }

        Context context = LWQApplication.get();

        final Canvas canvas = surfaceHolder.lockCanvas();
        canvas.drawColor(context.getResources().getColor(android.R.color.white));
        canvas.save();

        // Get screen width/height
        final Rect surfaceFrame = surfaceHolder.getSurfaceFrame();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        final Display defaultDisplay = windowManager.getDefaultDisplay();
        final Point size = new Point();
        defaultDisplay.getSize(size);
        final int screenWidth = size.x;
        final int screenHeight = surfaceHolder.getSurfaceFrame().height();

        final int horizontalPadding = (int) (screenWidth * .07);
        final int verticalPadding = (int) (screenHeight * .2);
        final int currentAPIVersion = android.os.Build.VERSION.SDK_INT;

        final Bitmap backgroundImage = wallpaperController.getBackgroundImage();
        if (backgroundImage != null) {
            Bitmap drawnBitmap = backgroundImage;
            float blurRadius = LWQPreferences.getBlurPreference();
            boolean recycleBitmap = false;
            if (currentAPIVersion >= Build.VERSION_CODES.JELLY_BEAN_MR1 && blurRadius > 0f) {
                recycleBitmap = true;
                drawnBitmap = blurBitmap(backgroundImage, blurRadius);
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

            /*
            canvas.clipRect(drawingArea, Region.Op.REPLACE);
            canvas.drawColor(getResources().getColor(android.R.color.darker_gray));
            */

        int quoteColor = context.getResources().getColor(android.R.color.black);
        int quoteStrokeColor = context.getResources().getColor(android.R.color.holo_blue_light);
        int authorColor = quoteColor;
        if (palette != null) {
            final Palette.Swatch swatch = getSwatch();
            quoteColor = swatch.getRgb();
            authorColor = swatch.getRgb();
            quoteStrokeColor = swatch.getTitleTextColor();
        } else {
            if (backgroundImage != null) {
                Palette.from(backgroundImage).generate(new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(Palette palette) {
                        LWQDrawScript.this.palette = palette;
                        LWQDrawScript.this.swatchIndex = 0;
                        paletteAsyncListener.onGenerated(palette);
                    }
                });
            }
            canvas.restore();
            surfaceHolder.unlockCanvasAndPost(canvas);
            return;
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
        String author = context.getString(R.string.unknown);
        if (wallpaperController.getAuthor() != null && !wallpaperController.getAuthor().isEmpty()) {
            author = wallpaperController.getAuthor();
        }
        String quote = context.getString(R.string.unknown);
        if (wallpaperController.getQuote() != null && !wallpaperController.getQuote().isEmpty()) {
            quote = wallpaperController.getQuote();
        }
        StaticLayout quoteLayout = new StaticLayout(quote.toUpperCase(), quoteTextPaint,
                drawingArea.width(), Layout.Alignment.ALIGN_NORMAL, 1, 0, true);
        StaticLayout authorLayout = new StaticLayout(author, authorTextPaint,
                drawingArea.width(), Layout.Alignment.ALIGN_NORMAL, 1, 0, true);

        // Correct the quote height, if necessary
        quoteLayout = correctFontSize(quoteLayout, drawingArea.height() - authorLayout.getHeight());

        // Draw the quote centered vertically
        int centerQuoteOffset = (int)(.5 * (drawingArea.height() - quoteLayout.getHeight()));
        canvas.translate(drawingArea.left, drawingArea.top + centerQuoteOffset);
        quoteLayout.draw(canvas);
        strokeText(quoteLayout, quoteStrokeColor & STROKE_ALPHA, 3f, canvas);

        canvas.translate(drawingArea.width(), quoteLayout.getHeight());
        authorLayout.draw(canvas);
        strokeText(authorLayout, quoteStrokeColor & STROKE_ALPHA, 3f, canvas);

        canvas.restore();
        surfaceHolder.unlockCanvasAndPost(canvas);
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

    Bitmap blurBitmap(Bitmap original, float radius) {
        Bitmap overlay = Bitmap.createBitmap(original.getWidth(), original.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas overlayCanvas = new Canvas(overlay);
        overlayCanvas.drawBitmap(original, 0, 0, null);
        RenderScript renderScript = RenderScript.create(LWQApplication.get());
        Allocation overlayAllocation = Allocation.createFromBitmap(renderScript, overlay);
        ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(renderScript, overlayAllocation.getElement());
        blur.setInput(overlayAllocation);
        blur.setRadius(radius);
        blur.forEach(overlayAllocation);
        overlayAllocation.copyTo(overlay);
        return overlay;
    }

    Palette.Swatch getSwatch() {
        return palette.getSwatches().get(swatchIndex);
    }
}
