package com.stanleyidesis.quotograph.api.drawing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.renderscript.Script;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.v7.graphics.Palette;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.widget.Toast;

import com.stanleyidesis.quotograph.BuildConfig;
import com.stanleyidesis.quotograph.LWQApplication;
import com.stanleyidesis.quotograph.LWQPreferences;
import com.stanleyidesis.quotograph.R;
import com.stanleyidesis.quotograph.api.Callback;
import com.stanleyidesis.quotograph.api.controller.LWQWallpaperController;
import com.stanleyidesis.quotograph.api.controller.LWQWallpaperControllerHelper;
import com.stanleyidesis.quotograph.ui.UIUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.R.attr.radius;
import static android.renderscript.RenderScript.releaseAllContexts;

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
 * LWQDrawScript.java
 * @author Stanley Idesis
 *
 * From Quotograph
 * https://github.com/stanidesis/quotograph
 *
 * Please report any issues
 * https://github.com/stanidesis/quotograph/issues
 *
 * Date: 08/22/2015
 */
public abstract class LWQDrawScript {

    static ExecutorService executorService;
    static Map<Integer, Palette> paletteCache;
    static final int TEXT_ALPHA = 0xE5FFFFFF;
    static final int STROKE_ALPHA = 0xF2FFFFFF;
    static int swatchIndex;
//    static RenderScript renderScript;

    static {
        executorService = Executors.newSingleThreadScheduledExecutor();
        paletteCache = new HashMap<>();
    }

    Palette palette;
    int drawRequests = 0;

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

    public void requestDraw(final Callback<Boolean> callback) {
        increaseDrawRequests();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    if (getDrawRequests() == 0) {
                        return;
                    }
                    resetDrawRequests();
                    if (!LWQWallpaperControllerHelper.get().activeWallpaperLoaded()) {
                        LWQWallpaperControllerHelper.get().retrieveActiveWallpaper();
                        return;
                    }
                    draw();
                    if (callback != null) {
                        callback.onSuccess(true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void requestDraw() {
        requestDraw(null);
    }

    void draw() {
        final LWQWallpaperController wallpaperController =
                LWQWallpaperControllerHelper.get();
        final Bitmap backgroundImage = wallpaperController.getBackgroundImage();
        if (backgroundImage == null) {
            return;
        }
        // Get screen size
        final Point realScreenSize = UIUtils.getRealScreenSize();
        final int screenWidth = realScreenSize.x;
        final int screenHeight = realScreenSize.y;
        final Rect surfaceFrame = surfaceRect();

        palette = paletteCache.get(backgroundImage.hashCode());
        if (palette == null) {
            LWQDrawScript.paletteCache.clear();
            palette = Palette.from(backgroundImage).generate();
            LWQDrawScript.paletteCache.put(backgroundImage.hashCode(), palette);
            LWQDrawScript.swatchIndex = 0;
        }

        final Canvas canvas = reserveCanvas();
        canvas.save();
        try {
            final int blurPreference = LWQPreferences.getBlurPreference();
            final int dimPreference = LWQPreferences.getDimPreference();
            // Blur or choose the raw background image
            Bitmap toDraw = blurPreference > 0.5f ? generateBitmap(blurPreference, backgroundImage) : backgroundImage;
            drawBitmap(canvas, screenWidth, surfaceFrame, toDraw);
            drawDimmer(canvas, dimPreference);
            drawText(canvas, screenWidth, screenHeight);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            canvas.restore();
            releaseCanvas(canvas);
        }

    }

    /**
     * Draws the quote and author text
     *
     * @param canvas
     * @param screenWidth
     * @param screenHeight
     * @return the rect surrounding both the quote and author
     */
    Rect drawText(Canvas canvas, int screenWidth, int screenHeight) {
        Context context = LWQApplication.get();
        final LWQWallpaperController wallpaperController = LWQWallpaperControllerHelper.get();

        final Palette.Swatch swatch = getSwatch();
        int quoteColor = swatch.getRgb();
        int authorColor = swatch.getRgb();
        int quoteStrokeColor = swatch.getTitleTextColor();

        // Load Typeface
        Typeface quoteTypeFace = wallpaperController.getTypeface();

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
        authorTextPaint.setTypeface(quoteTypeFace);
        String author = context.getString(R.string.unknown);
        if (wallpaperController.getAuthor() != null && !wallpaperController.getAuthor().isEmpty()) {
            author = wallpaperController.getAuthor();
        }
        String quote = context.getString(R.string.unknown);
        if (wallpaperController.getQuote() != null && !wallpaperController.getQuote().isEmpty()) {
            quote = wallpaperController.getQuote();
        }

        // Find the longest word
        String[] words = quote.split(" ");
        Arrays.sort(words, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return rhs.length() - lhs.length();
            }
        });
        final int horizontalPadding = (int) (screenWidth * .07);
        final int verticalPadding = (int) (screenHeight * .2);
        final Rect drawingArea = new Rect(horizontalPadding, verticalPadding,
                screenWidth - horizontalPadding, screenHeight - verticalPadding);

        StaticLayout quoteLayout = new StaticLayout(quote.toUpperCase(), quoteTextPaint,
                drawingArea.width(), Layout.Alignment.ALIGN_NORMAL, 1, 0, true);
        StaticLayout authorLayout = new StaticLayout(author, authorTextPaint,
                drawingArea.width(), Layout.Alignment.ALIGN_NORMAL, 1, 0, true);

        // Correct the quote and author height, if necessary
        quoteLayout = correctFontSize(quoteLayout, drawingArea.height() - authorLayout.getHeight(), words[0]);
        authorLayout = correctFontSize(authorLayout, Integer.MAX_VALUE, author);
        if (authorLayout.getPaint().getTextSize() > quoteLayout.getPaint().getTextSize()) {
            TextPaint paint = authorLayout.getPaint();
            paint.setTextSize(quoteLayout.getPaint().getTextSize());
            authorLayout = new StaticLayout(authorLayout.getText(), paint,
                    authorLayout.getWidth(), authorLayout.getAlignment(), authorLayout.getSpacingMultiplier(),
                    authorLayout.getSpacingAdd(), true);
        }

        // Draw the quote centered vertically
        int centerQuoteOffset = (int)(.5 * (drawingArea.height() - quoteLayout.getHeight()));
        canvas.translate(drawingArea.left, drawingArea.top + centerQuoteOffset);
        quoteLayout.draw(canvas);
        strokeText(quoteLayout, quoteStrokeColor & STROKE_ALPHA, 3f, canvas);

        canvas.translate(drawingArea.width(), quoteLayout.getHeight() + (authorLayout.getHeight() / 4));
        authorLayout.draw(canvas);
        strokeText(authorLayout, quoteStrokeColor & STROKE_ALPHA, 3f, canvas);

        // Re-set the x/y translation
        canvas.setMatrix(null);

        // Crop the rect to quote and author
        drawingArea.top += centerQuoteOffset;
        drawingArea.bottom = drawingArea.top + quoteLayout.getHeight()
                + (int) (1.25 * authorLayout.getHeight());
        return drawingArea;
    }

    void drawDimmer(Canvas canvas, int dimPreference) {
        if (dimPreference > 0) {
            int alpha = (int) Math.floor(255f * (dimPreference / 100f));
            canvas.drawColor(Color.argb(alpha, 0, 0, 0));
        }
    }

    Bitmap generateBitmap(int blurRadius, Bitmap backgroundImage) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            return blurBitmap(backgroundImage, blurRadius);
        }
        return backgroundImage;
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

    /**
     * For now, disabled. Scrapping Issue #148
     * @param canvas
     * @param screenWidth
     * @param screenHeight
     * @param quoteAndAuthorRect
     */
    void drawWatermark(Canvas canvas, int screenWidth, int screenHeight, Rect quoteAndAuthorRect) {
        if (!LWQPreferences.isWatermarkEnabled()) {
            return;
        }
        Bitmap icon = BitmapFactory.decodeResource(LWQApplication.get().getResources(),
                R.mipmap.ic_launcher);
        // Let's make the icon no more than 10% of the screen size at most
        float maxPixelHeight = ((float) Math.max(screenWidth, screenHeight)) * .1f;
        float scale = maxPixelHeight / (float) icon.getHeight();
        Matrix scaleMatrix = new Matrix();
        scaleMatrix.postScale(scale, scale);
        Paint iconPaint = new Paint();
        iconPaint.setAntiAlias(true);
        iconPaint.setFilterBitmap(true);
        iconPaint.setDither(true);
        iconPaint.setAlpha(100);
        canvas.translate(quoteAndAuthorRect.left, screenHeight - maxPixelHeight);
        canvas.drawBitmap(icon, scaleMatrix, iconPaint);
    }

    StaticLayout correctFontSize(StaticLayout staticLayout, int maxHeight, String longestWord) {
        final TextPaint textPaint = staticLayout.getPaint();
        while (staticLayout.getHeight() > maxHeight
                || staticLayout.getWidth() < StaticLayout.getDesiredWidth(longestWord, textPaint)) {
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
        RenderScript renderScript = null;
        Allocation overlayAllocation = null;
        Allocation outAllocation = null;
        ScriptIntrinsicBlur blur = null;
        Bitmap result = null;
        try {
            renderScript = RenderScript.create(LWQApplication.get());
            overlayAllocation = Allocation.createFromBitmap(renderScript, original);
            outAllocation = Allocation.createTyped(renderScript, overlayAllocation.getType());
            result = Bitmap.createBitmap(original.getWidth(), original.getHeight(), original.getConfig());
            blur = ScriptIntrinsicBlur.create(renderScript, overlayAllocation.getElement());
            blur.setInput(overlayAllocation);
            blur.setRadius(radius);
            blur.forEach(outAllocation);
            outAllocation.copyTo(result);
        } finally {
            if (renderScript != null) {
                renderScript.destroy();
            }
            if (overlayAllocation != null) {
                overlayAllocation.destroy();
            }
            if (outAllocation != null) {
                outAllocation.destroy();
            }
            if (blur != null) {
                blur.destroy();
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                RenderScript.releaseAllContexts();
            }
        }
        return result;
    }

    Palette.Swatch getSwatch() {
        return palette.getSwatches().get(swatchIndex);
    }

    synchronized void increaseDrawRequests() {
        drawRequests++;
    }

    synchronized void resetDrawRequests() {
        drawRequests = 0;
    }

    synchronized int getDrawRequests() {
        return drawRequests;
    }
}
