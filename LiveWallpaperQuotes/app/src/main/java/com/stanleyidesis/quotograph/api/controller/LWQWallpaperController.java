package com.stanleyidesis.quotograph.api.controller;

import android.graphics.Bitmap;
import android.graphics.Typeface;

import com.stanleyidesis.quotograph.api.Callback;
import com.stanleyidesis.quotograph.api.db.PlaylistAuthor;
import com.stanleyidesis.quotograph.api.db.PlaylistCategory;
import com.stanleyidesis.quotograph.api.db.PlaylistQuote;

import java.util.List;

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
 * LWQWallpaperController.java
 * @author Stanley Idesis
 *
 * From Quotograph
 * https://github.com/stanidesis/quotograph
 *
 * Please report any issues
 * https://github.com/stanidesis/quotograph/issues
 *
 * Date: 08/01/2015
 */
public interface LWQWallpaperController {

    /**
     * The state of the controller
     */
    enum RetrievalState {
        // Nothing retreived
        NONE,
        // Retrieving the Active Wallpaper
        ACTIVE_WALLPAPER,
        // Retrieving a New Wallpaper
        NEW_WALLPAPER
    }

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
     * @return the Typeface required for the active Quotograph, or @null
     */
    Typeface getTypeface();

    /**
     * @return true if the active wallpaper is accessible now
     */
    boolean activeWallpaperLoaded();

    /**
     * Convenience to create a new wallpaper from quotes in this category
     * @param category The category from which to draw quotes from when generating a new wallpaper
     */
    void generateNewWallpaper(PlaylistCategory category);

    /**
     * Convenience to create a new wallpaper from quotes by an author.
     * @param author The author whose quotes to use when we generate a new wallpaper
     */
    void generateNewWallpaper(PlaylistAuthor author);

    /**
     * Convenience to create a new wallpaper using a specific quote.
     * @param quote The quote to use when generating a new wallpaper
     */
    void generateNewWallpaper(PlaylistQuote quote);

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
     * Fetch background categories, replace existing if necessary.
     *
     * @param callback This callback receives the list returned from {@link #getBackgroundCategories()} once fetched.
     */
    void fetchBackgroundCategories(Callback<List<String>> callback);

    /**
     * @return the available categories from which the user may choose their background, if applicable
     */
    List<String> getBackgroundCategories();

    /**
     * @return the current state of retrieval
     */
    RetrievalState getRetrievalState();
}
