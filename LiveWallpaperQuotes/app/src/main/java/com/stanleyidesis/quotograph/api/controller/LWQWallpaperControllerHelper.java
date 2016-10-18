package com.stanleyidesis.quotograph.api.controller;

/**
 * Created by Stanley Idesis on 10/12/2016.
 */

public class LWQWallpaperControllerHelper {
    static LWQWallpaperController lwqWallpaperController;

    public static LWQWallpaperController get() {
        if (lwqWallpaperController != null) {
            return lwqWallpaperController;
        }
        lwqWallpaperController = new LWQWallpaperControllerUnsplashImpl();
        return get();
    }
}
