package com.stanleyidesis.quotograph.api.controller;

import com.stanleyidesis.quotograph.api.LWQError;

/**
 * Created by Stanley Idesis on 10/12/2016.
 */

public class LWQDebugLogger implements LWQLogger {
    @Override
    public void logWallpaperCount(long count) {}

    @Override
    public void logWallpaperRetrievalState(LWQWallpaperController.RetrievalState retrievalState) {}

    @Override
    public void logWallpaperActive(boolean active) {}

    @Override
    public void logBlurLevel(int blur) {}

    @Override
    public void logDimLevel(int dim) {}

    @Override
    public void logRefreshRate(String refreshRateString) {}

    @Override
    public void logCategoryCount(int count) {}

    @Override
    public void logAuthorCount(int count) {}

    @Override
    public void logQuoteCount(int count) {}

    @Override
    public void logError(LWQError error) {}
}
