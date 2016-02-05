package com.stanleyidesis.quotograph.api.controller;

import android.graphics.Bitmap;

import com.stanleyidesis.quotograph.api.Callback;

/**
 * Created by stanleyidesis on 8/2/15.
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
