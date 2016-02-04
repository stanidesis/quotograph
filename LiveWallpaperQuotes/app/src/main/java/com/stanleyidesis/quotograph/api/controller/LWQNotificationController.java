package com.stanleyidesis.quotograph.api.controller;

/**
 * Created by stanleyidesis on 9/19/15.
 */

import android.net.Uri;

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
 * LWQNotificationController.java
 * @author Stanley Idesis
 *
 * From Quotograph
 * https://github.com/stanidesis/quotograph
 *
 * Please report any issues
 * https://github.com/stanidesis/quotograph/issues
 *
 * Date: 09/19/2015
 */
public interface LWQNotificationController {
    /**
     * Manually post a new Wallpaper notification if so desired.
     * The controller should also subscribe to the EventBus, posting
     * a new Wallpaper notification when appropriate.
     */
    void postNewWallpaperNotification();

    /**
     * If present, dismiss the current New wallpaper notification
     */
    void dismissNewWallpaperNotification();

    /**
     * Present a notification which shows the saved image. Clicking
     * it should let the user view the picture, it should also feature a
     * share action that presents a chooser.
     *
     * @param filePath
     * @param imageUri
     */
    void postSavedWallpaperReadyNotification(Uri filePath, Uri imageUri);

    /**
     * If saving the wallpaper fails, notify the user that something went wrong.
     */
    void postWallpaperSaveFailureNotification();
}
