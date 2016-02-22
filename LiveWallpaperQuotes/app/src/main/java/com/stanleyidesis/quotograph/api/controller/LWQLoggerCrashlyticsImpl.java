package com.stanleyidesis.quotograph.api.controller;

import com.crashlytics.android.Crashlytics;
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
 * LWQLoggerCrashlyticsImpl.java
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
public class LWQLoggerCrashlyticsImpl implements LWQLogger {

    static final String KEY_WALLPAPER_COUNT = "wallpaper_count";
    static final String KEY_WALLPAPER_RETRIEVAL_STATE = "wallpaper_retrieval_state";
    static final String KEY_WALLPAPER_ACTIVE = "wallpaper_active";

    @Override
    public void logWallpaperCount(long count) {
        Crashlytics.setLong(KEY_WALLPAPER_COUNT, count);
    }

    @Override
    public void logWallpaperRetrievalState(LWQWallpaperController.RetrievalState retrievalState) {
        Crashlytics.setString(KEY_WALLPAPER_RETRIEVAL_STATE, retrievalState.name());
    }

    @Override
    public void logWallpaperActive(boolean active) {
        Crashlytics.setBool(KEY_WALLPAPER_ACTIVE, active);
    }

    @Override
    public void logError(LWQError error) {
        Crashlytics.log(error.getErrorMessage());
        Crashlytics.logException(error.getErrorThrowable());
    }
}
