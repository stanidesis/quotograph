package com.stanleyidesis.livewallpaperquotes.api;

import android.graphics.Bitmap;

/**
 * Created by stanleyidesis on 8/1/15.
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
     * Generates a new wallpaper, stores it as active and notifies the caller when loaded
     * @param callback
     */
    void generateNewWallpaper(Callback<Boolean> callback);

    /**
     * This method should return false if LWQ is not the active wallpaper or this is a fresh install.
     *
     * @return true if the application has an active wallpaper (Quote + Background image)
     */
    boolean activeWallpaperExists();

    /**
     * If no active wallpaper exists, the implementation will generate one
     * @param callback
     */
    void retrieveActiveWallpaper(Callback<Boolean> callback);

    /**
     * This method should cleans up any bitmaps and references which the active wallpaper required.
     */
    void discardActiveWallpaper();
}
