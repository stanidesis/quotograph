package com.stanleyidesis.quotograph.api.controller;

import com.crashlytics.android.Crashlytics;
import com.stanleyidesis.quotograph.LWQApplication;
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
 * LWQLoggerImpl.java
 * @author Stanley Idesis
 *
 * From Quotograph
 * https://github.com/stanidesis/quotograph
 *
 * Please report any issues
 * https://github.com/stanidesis/quotograph/issues
 *
 * Date: 02/21/2016
 */
public class LWQLoggerImpl implements LWQLogger {

    static final String KEY_WALLPAPER_COUNT = "wallpaper_count";
    static final String KEY_WALLPAPER_RETRIEVAL_STATE = "wallpaper_state";
    static final String KEY_WALLPAPER_ACTIVE = "wallpaper_active";
    static final String KEY_BLUR_LEVEL = "blur_level";
    static final String KEY_DIM_LEVEL = "dim_level";
    static final String KEY_REFRESH_RATE = "refresh_rate";
    static final String KEY_CATEGORY_COUNT = "category_count";
    static final String KEY_AUTHOR_COUNT = "author_count";
    static final String KEY_QUOTE_COUNT = "quote_count";

    @Override
    public void logWallpaperCount(long count) {
        Crashlytics.setLong(KEY_WALLPAPER_COUNT, count);
        LWQApplication.getAnalytics()
                .setUserProperty(KEY_WALLPAPER_COUNT, String.valueOf(count));
    }

    @Override
    public void logWallpaperRetrievalState(LWQWallpaperController.RetrievalState retrievalState) {
        Crashlytics.setString(KEY_WALLPAPER_RETRIEVAL_STATE, retrievalState.name());
        LWQApplication.getAnalytics()
                .setUserProperty(KEY_WALLPAPER_RETRIEVAL_STATE, retrievalState.name());
    }

    @Override
    public void logWallpaperActive(boolean active) {
        Crashlytics.setBool(KEY_WALLPAPER_ACTIVE, active);
        LWQApplication.getAnalytics()
                .setUserProperty(KEY_WALLPAPER_ACTIVE, String.valueOf(active));
    }

    @Override
    public void logBlurLevel(int blur) {
        Crashlytics.setInt(KEY_BLUR_LEVEL, blur);
        LWQApplication.getAnalytics()
                .setUserProperty(KEY_BLUR_LEVEL, String.valueOf(blur));
    }

    @Override
    public void logDimLevel(int dim) {
        Crashlytics.setInt(KEY_DIM_LEVEL, dim);
        LWQApplication.getAnalytics()
                .setUserProperty(KEY_DIM_LEVEL, String.valueOf(dim));
    }

    @Override
    public void logRefreshRate(String refreshRateString) {
        Crashlytics.setString(KEY_REFRESH_RATE, refreshRateString);
        LWQApplication.getAnalytics()
                .setUserProperty(KEY_REFRESH_RATE, refreshRateString);
    }

    @Override
    public void logCategoryCount(int count) {
        Crashlytics.setInt(KEY_CATEGORY_COUNT, count);
        LWQApplication.getAnalytics()
                .setUserProperty(KEY_CATEGORY_COUNT, String.valueOf(count));
    }

    @Override
    public void logAuthorCount(int count) {
        Crashlytics.setInt(KEY_AUTHOR_COUNT, count);
        LWQApplication.getAnalytics()
                .setUserProperty(KEY_AUTHOR_COUNT, String.valueOf(count));
    }

    @Override
    public void logQuoteCount(int count) {
        Crashlytics.setInt(KEY_QUOTE_COUNT, count);
        LWQApplication.getAnalytics()
                .setUserProperty(KEY_QUOTE_COUNT, String.valueOf(count));
    }

    @Override
    public void logError(LWQError error) {
        Crashlytics.log(error.getErrorMessage());
        Crashlytics.logException(error.getErrorThrowable());
    }
}