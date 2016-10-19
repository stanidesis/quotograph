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
import android.renderscript.ScriptIntrinsicBlur;
import android.support.v7.graphics.Palette;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.stanleyidesis.quotograph.BuildConfig;
import com.stanleyidesis.quotograph.LWQApplication;
import com.stanleyidesis.quotograph.LWQPreferences;
import com.stanleyidesis.quotograph.R;
import com.stanleyidesis.quotograph.RemoteConfigConst;
import com.stanleyidesis.quotograph.api.Callback;
import com.stanleyidesis.quotograph.api.controller.LWQWallpaperController;
import com.stanleyidesis.quotograph.api.controller.LWQWallpaperControllerHelper;
import com.stanleyidesis.quotograph.ui.UIUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    static SparseArray<Palette> paletteCache;
    static final int TEXT_ALPHA = 0xE5FFFFFF;
    static final int STROKE_ALPHA = 0xF2FFFFFF;
    static int swatchIndex;
    static RenderScript renderScript;

    static {
        executorService = Executors.newSingleThreadScheduledExecutor();
        paletteCache = new SparseArray<>();
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

    private void draw() {
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
            Bitmap toDraw = blurPreference > 0.5f ? generateBlurredBitmap(blurPreference, backgroundImage) : backgroundImage;
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
    private Rect drawText(Canvas canvas, int screenWidth, int screenHeight) {
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
        String author = "";
        if (wallpaperController.getAuthor() != null && !wallpaperController.getAuthor().isEmpty()) {
            author = wallpaperController.getAuthor();
        }
        String quote = "";
        if (wallpaperController.getQuote() != null && !wallpaperController.getQuote().isEmpty()) {
            quote = wallpaperController.getQuote();
        }

        // Find the ending index of each word
        // We use this data to determine whether a line-break occurs in our StaticLayout
        List<Integer> endIndeces = new ArrayList<>();
        String[] words = quote.split("\\s+");
        if (words.length > 0) {
            int runningIndex = 0;
            for (int i = 0; i < words.length; i++) {
                int nextEndCap = quote.indexOf(words[i], runningIndex) + words[i].length() - 1;
                runningIndex = nextEndCap;
                endIndeces.add(nextEndCap);
            }
        } else {
            endIndeces.add(quote.length() - 1);
        }

        final int horizontalPadding = (int) (screenWidth * .07);
        final int verticalPadding = (int) (screenHeight * .2);
        final Rect drawingArea = new Rect(horizontalPadding, verticalPadding,
                screenWidth - horizontalPadding, screenHeight - verticalPadding);

        StaticLayout quoteLayout = new StaticLayout(quote.toUpperCase(), quoteTextPaint,
                drawingArea.width(), Layout.Alignment.ALIGN_NORMAL, 1, 0, true);
        StaticLayout authorLayout = new StaticLayout(author, authorTextPaint,
                drawingArea.width(), Layout.Alignment.ALIGN_NORMAL, 1, 0, true);

        // Correct the quote and author height, if necessary
        quoteLayout = correctFontSize(quoteLayout, drawingArea.height() - authorLayout.getHeight(), endIndeces);
        authorLayout = correctFontSize(authorLayout, Integer.MAX_VALUE, null);
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

    private void drawDimmer(Canvas canvas, int dimPreference) {
        if (dimPreference > 0) {
            int alpha = (int) Math.floor(255f * (dimPreference / 100f));
            canvas.drawColor(Color.argb(alpha, 0, 0, 0));
        }
    }

    private void drawBitmap(Canvas canvas, int screenWidth, Rect surfaceFrame, Bitmap bitmapToDraw) {
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

    private StaticLayout correctFontSize(StaticLayout staticLayout, int maxHeight, List<Integer> endOfWordIndices) {
        final TextPaint textPaint = staticLayout.getPaint();
        boolean heightFixed = false;
        while (!heightFixed) {
            textPaint.setTextSize(textPaint.getTextSize() * .99f);
            staticLayout = new StaticLayout(staticLayout.getText(), textPaint,
                    staticLayout.getWidth(), staticLayout.getAlignment(), staticLayout.getSpacingMultiplier(),
                    staticLayout.getSpacingAdd(), true);

            // Check end of word indices for cut-off
            // This essentially acts as a 'break on white space' function
            boolean noCutOffs = true;
            if (endOfWordIndices != null) {
                for (int i = 0; i < staticLayout.getLineCount() && noCutOffs; i++) {
                    // -1 because it goes one past the last visible character
                    int lineVisibleEnd = staticLayout.getLineVisibleEnd(i) - 1;
                    // Check if the end to each line is the end of a word or quote
                    noCutOffs = noCutOffs && endOfWordIndices.contains(lineVisibleEnd);
                }
            }
            heightFixed = staticLayout.getHeight() <= maxHeight && noCutOffs;
        }
        return staticLayout;
    }

    private void strokeText(StaticLayout staticLayout, int color, float width, Canvas canvas) {
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

    /**
     * This approach uses StackBlurring by Mario Klingemann as long as the device is running
     * an old OS or the Firebase config says so.
     *
     * Leaving the Firebase config in there in case a flurry of OOM errors results.
     *
     * @param blurRadius
     * @param backgroundImage
     * @return
     */
    private Bitmap generateBlurredBitmap(int blurRadius, Bitmap backgroundImage) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN
                || FirebaseRemoteConfig.getInstance().getBoolean(RemoteConfigConst.STACK_BLURRING)) {
            return stackBlur(backgroundImage, 1.0f, blurRadius);
        } else {
            return blurBitmap(backgroundImage, blurRadius);
        }
    }

    private Bitmap blurBitmap(Bitmap original, float radius) {
        Allocation overlayAllocation = null;
        Allocation outAllocation = null;
        ScriptIntrinsicBlur blur = null;
        Bitmap result = null;
        try {
            if (renderScript == null) {
                renderScript = RenderScript.create(LWQApplication.get());
            }
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

    /**
     * Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>
     */
    public Bitmap stackBlur(Bitmap sentBitmap, float scale, int radius) {

        int width = Math.round(sentBitmap.getWidth() * scale);
        int height = Math.round(sentBitmap.getHeight() * scale);
        sentBitmap = Bitmap.createScaledBitmap(sentBitmap, width, height, false);

        Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

        if (radius < 1) {
            return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = ( 0xff000000 & pix[yi] ) | ( dv[rsum] << 16 ) | ( dv[gsum] << 8 ) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.setPixels(pix, 0, w, 0, 0, w, h);

        return (bitmap);
    }

    private Palette.Swatch getSwatch() {
        return palette.getSwatches().get(swatchIndex);
    }

    private synchronized void increaseDrawRequests() {
        drawRequests++;
    }

    private synchronized void resetDrawRequests() {
        drawRequests = 0;
    }

    private synchronized int getDrawRequests() {
        return drawRequests;
    }
}
