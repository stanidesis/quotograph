package com.stanleyidesis.livewallpaperquotes.api;

import android.graphics.Bitmap;

import java.util.List;

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
 * LWQWallpaperController.java
 * @author Stanley Idesis
 *
 * From Live-Wallpaper-Quotes
 * https://github.com/stanidesis/live-wallpaper-quotes
 *
 * Please report any issues
 * https://github.com/stanidesis/live-wallpaper-quotes/issues
 *
 * Date: 08/01/2015
 */
public interface LWQWallpaperController {
    /**
     * @return the unformatted quote text
     */
    String getQuote();

    /**
     * @return the quote's author
     */
    String getAuthor();

    /**
     * @return the background image Bitmap
     */
    Bitmap getBackgroundImage();

    /**
     * @return true if the active wallpaper is accessible now
     */
    boolean activeWallpaperLoaded();

    /**
     * Generates a new wallpaper, stores it as active and notifies the EventBus when completed
     */
    void generateNewWallpaper();

    /**
     * This method should return false if LWQ is not the active wallpaper or this is a fresh install.
     *
     * @return true if the application has an active wallpaper (Quote + Background image)
     */
    boolean activeWallpaperExists();

    /**
     * Retrieves the active wallpaper into mem cache. Only returns `true` if it begins to load the
     * active wallpaper.
     */
    boolean retrieveActiveWallpaper();

    /**
     * This method should cleans up any bitmaps and references which the active wallpaper required.
     */
    void discardActiveWallpaper();

    /**
     * @return the available categories from which the user may choose their background, if applicable
     */
    List<String> getBackgroundCategories();
}
