package com.stanleyidesis.livewallpaperquotes.api.drawing;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;

import com.stanleyidesis.livewallpaperquotes.ui.UIUtils;

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
 * LWQSurfaceHolderDrawScript.java
 * @author Stanley Idesis
 *
 * From Live-Wallpaper-Quotes
 * https://github.com/stanidesis/live-wallpaper-quotes
 *
 * Please report any issues
 * https://github.com/stanidesis/live-wallpaper-quotes/issues
 *
 * Date: 09/20/2015
 */
public class LWQBitmapDrawScript extends LWQDrawScript {

    Bitmap bitmap;
    Canvas canvas;

    public LWQBitmapDrawScript() {
        final Point realScreenSize = UIUtils.getRealScreenSize();
        bitmap = Bitmap.createBitmap(realScreenSize.x, realScreenSize.y, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void finish() {
        bitmap.recycle();
        bitmap = null;
        canvas = null;
    }

    @Override
    protected Canvas reserveCanvas() {
        return canvas;
    }

    @Override
    protected void releaseCanvas(Canvas canvas) {
        this.canvas = canvas;
    }

    @Override
    protected Rect surfaceRect() {
        return new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
    }
}
