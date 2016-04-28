package com.stanleyidesis.quotograph.api.controller;

import android.graphics.Bitmap;

import com.stanleyidesis.quotograph.api.Callback;

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
 * LWQImageController.java
 * @author Stanley Idesis
 *
 * From Quotograph
 * https://github.com/stanidesis/quotograph
 *
 * Please report any issues
 * https://github.com/stanidesis/quotograph/issues
 *
 * Date: 08/02/2015
 */
public interface LWQImageController {
    /**
     * @param uri
     * @return true if an image of this URI is cached in memory and readily accessible
     */
    boolean isCached(String uri);

    /**
     * Retrieve the bitmap, either locally from cache or from the network.
     *
     * @param uri
     * @param callback
     */
    void retrieveBitmap(String uri, Callback<Bitmap> callback);

    /**
     * A synchronous retrieval of a Uri
     * 
     * @param uri
     * @return
     */
    Bitmap retrieveBitmapSync(String uri);

    /**
     * Clean up memory, recycle, and in general release the bitmap associated with this uri.
     *
     * @param uri
     */
    void clearBitmap(String uri);

    /**
     * Empty the cache completely.
     */
    void clearCache();
}
