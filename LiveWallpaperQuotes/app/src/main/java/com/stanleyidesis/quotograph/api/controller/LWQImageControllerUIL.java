package com.stanleyidesis.quotograph.api.controller;

import android.graphics.Bitmap;
import android.view.View;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.stanleyidesis.quotograph.LWQApplication;
import com.stanleyidesis.quotograph.api.Callback;
import com.stanleyidesis.quotograph.api.LWQError;

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
 * LWQImageControllerUIL.java
 * @author Stanley Idesis
 *
 * From Quotograph
 * https://github.com/stanidesis/quotograph
 *
 * Please report any issues
 * https://github.com/stanidesis/quotograph/issues
 *
 * Date: 02/12/2016
 */
public class LWQImageControllerUIL implements LWQImageController {

    public LWQImageControllerUIL() {
        DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(LWQApplication.get())
                .threadPriority(Thread.MAX_PRIORITY - 1)
                .defaultDisplayImageOptions(displayImageOptions)
                .build();
        ImageLoader.getInstance().init(config);
    }

    @Override
    public boolean isCached(String uri) {
        return ImageLoader.getInstance().getMemoryCache().keys().contains(uri);
    }

    @Override
    public void retrieveBitmap(String uri, final Callback<Bitmap> callback) {
        ImageLoader.getInstance().loadImage(uri, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {}

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                callback.onError(LWQError.create(failReason.getType().name(), failReason.getCause()));
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                callback.onSuccess(loadedImage);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                callback.onError(LWQError.create("Loading Image Cancelled"));
            }
        });
    }

    @Override
    public Bitmap retrieveBitmapSync(String uri) {
        return ImageLoader.getInstance().loadImageSync(uri);
    }

    @Override
    public void clearBitmap(String uri) {
        ImageLoader.getInstance().getMemoryCache().remove(uri);
        ImageLoader.getInstance().getDiskCache().remove(uri);
    }

    @Override
    public void clearCache() {
        ImageLoader.getInstance().clearDiskCache();
        ImageLoader.getInstance().clearMemoryCache();
    }
}
