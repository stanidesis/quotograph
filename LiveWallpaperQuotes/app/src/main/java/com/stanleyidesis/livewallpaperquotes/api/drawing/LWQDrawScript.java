package com.stanleyidesis.livewallpaperquotes.api.drawing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Looper;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.v7.graphics.Palette;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.widget.Toast;

import com.stanleyidesis.livewallpaperquotes.BuildConfig;
import com.stanleyidesis.livewallpaperquotes.LWQApplication;
import com.stanleyidesis.livewallpaperquotes.LWQPreferences;
import com.stanleyidesis.livewallpaperquotes.R;
import com.stanleyidesis.livewallpaperquotes.api.controller.LWQWallpaperController;
import com.stanleyidesis.livewallpaperquotes.ui.Fonts;
import com.stanleyidesis.livewallpaperquotes.ui.UIUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 * LWQDrawScript.java
 * @author Stanley Idesis
 *
 * From Live-Wallpaper-Quotes
 * https://github.com/stanidesis/live-wallpaper-quotes
 *
 * Please report any issues
 * https://github.com/stanidesis/live-wallpaper-quotes/issues
 *
 * Date: 08/22/2015
 */
public abstract class LWQDrawScript {

    static Map<Integer, Palette> paletteCache;
    static final int TEXT_ALPHA = 0xE5FFFFFF;
    static final int STROKE_ALPHA = 0xF2FFFFFF;
    static int swatchIndex;
    static Typeface quoteTypeFace;
    static Typeface authorTypeFace;
    static Bitmap cachedBackground;
    static int cachedBackgroundHashCode;
    static int cachedBlur;

    static {
        quoteTypeFace = Fonts.JOSEFIN_BOLD.load(LWQApplication.get());
        authorTypeFace = quoteTypeFace;
        paletteCache = new HashMap<>();
    }

    Palette palette;

    /**
     * @return a Canvas, ready to draw
     */
    protected abstract Canvas reserveCanvas();

    /**
     * Release the canvas when ready
     * @param canvas
     */
    protected abstract void releaseCanvas(Canvas canvas);

    /**
     * @return a Rect that represents the full Surface size
     */
    protected abstract Rect surfaceRect();

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
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            Log.e(getClass().getSimpleName(), "Executing draw() on UI thread, switch to a background thread.", new Throwable());
            return;
        }
        final LWQWallpaperController wallpaperController =
                LWQApplication.getWallpaperController();
        final Context context = LWQApplication.get();
        final Bitmap backgroundImage = wallpaperController.getBackgroundImage();
        if (backgroundImage == null) {
            return;
        }
        // Get screen width/height
        final Rect surfaceFrame = surfaceRect();
        final Point realScreenSize = UIUtils.getRealScreenSize();
        final int screenWidth = realScreenSize.x;
        final int screenHeight = realScreenSize.y;
        palette = paletteCache.get(backgroundImage.hashCode());
        if (palette == null) {
            LWQDrawScript.paletteCache.clear();
            palette = Palette.from(backgroundImage).generate();
            LWQDrawScript.paletteCache.put(backgroundImage.hashCode(), palette);
            LWQDrawScript.swatchIndex = 0;
        }

        final Canvas canvas = reserveCanvas();
        canvas.save();

        final int blurPreference = LWQPreferences.getBlurPreference();
        // Determine cache validity
        if (cachedBackground == null || cachedBlur != blurPreference ||
                backgroundImage.hashCode() != cachedBackgroundHashCode) {
            if (cachedBackground != null && cachedBackground != backgroundImage) {
                cachedBackground.recycle();
            }
            cachedBackgroundHashCode = backgroundImage.hashCode();
            cachedBackground = generateBitmap(blurPreference, backgroundImage);
            cachedBlur = blurPreference;
        }
        drawBitmap(canvas, screenWidth, surfaceFrame, cachedBackground);
        drawDimmer(canvas, LWQPreferences.getDimPreference());
        drawText(canvas, screenWidth, screenHeight);

        canvas.restore();
        releaseCanvas(canvas);
    }

    private void drawText(Canvas canvas, int screenWidth, int screenHeight) {
        Context context = LWQApplication.get();
        final LWQWallpaperController wallpaperController = LWQApplication.getWallpaperController();

        final Palette.Swatch swatch = getSwatch();
        int quoteColor = swatch.getRgb();
        int authorColor = swatch.getRgb();
        int quoteStrokeColor = swatch.getTitleTextColor();

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

        final int horizontalPadding = (int) (screenWidth * .07);
        final int verticalPadding = (int) (screenHeight * .2);
        final Rect drawingArea = new Rect(horizontalPadding, verticalPadding,
                screenWidth - horizontalPadding, screenHeight - verticalPadding);

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
    }

    void drawDimmer(Canvas canvas, int dimPreference) {
        if (dimPreference > 0) {
            int alpha = (int) Math.floor(255f * (dimPreference / 100f));
            canvas.drawColor(Color.argb(alpha, 0, 0, 0));
        }
    }

    Bitmap generateBitmap(int blurRadius, Bitmap backgroundImage) {
        Bitmap drawnBitmap = backgroundImage;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && blurRadius > 0f) {
            drawnBitmap = blurBitmap(backgroundImage, blurRadius);
        }
        return drawnBitmap;
    }

    void drawBitmap(Canvas canvas, int screenWidth, Rect surfaceFrame, Bitmap bitmapToDraw) {
        Paint bitmapPaint = new Paint();
        bitmapPaint.setAntiAlias(true);
        bitmapPaint.setFilterBitmap(true);
        bitmapPaint.setDither(true);

        float scaleY = (float) surfaceFrame.height() / (float) bitmapToDraw.getHeight();
        float scaleX = (float) surfaceFrame.width() / (float) bitmapToDraw.getWidth();
        float finalScale = Math.max(scaleX, scaleY);

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.postScale(finalScale, finalScale);

        // Adjust center
        final int bitmapFinalWidth = (int)((float) bitmapToDraw.getWidth() * finalScale);
        if (bitmapFinalWidth <= screenWidth) {
            canvas.drawBitmap(bitmapToDraw, scaleMatrix, bitmapPaint);
            return;
        }
        final float dx = -0.5f * (bitmapFinalWidth - screenWidth);
        canvas.translate(dx, 0);
        canvas.drawBitmap(bitmapToDraw, scaleMatrix, bitmapPaint);
        canvas.translate(-dx, 0);
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
        RenderScript renderScript = RenderScript.create(LWQApplication.get());
        Allocation overlayAllocation = Allocation.createFromBitmap(renderScript, original);

        ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(renderScript, overlayAllocation.getElement());
        blur.setRadius(radius);
        blur.setInput(overlayAllocation);

        Bitmap result = Bitmap.createBitmap(original.getWidth(), original.getHeight(), original.getConfig());
        Allocation outAllocation = Allocation.createFromBitmap(renderScript, original);
        blur.forEach(outAllocation);
        outAllocation.copyTo(result);

        overlayAllocation.destroy();
        outAllocation.destroy();
        renderScript.destroy();
        return result;
    }

    Palette.Swatch getSwatch() {
        return palette.getSwatches().get(swatchIndex);
    }
}
