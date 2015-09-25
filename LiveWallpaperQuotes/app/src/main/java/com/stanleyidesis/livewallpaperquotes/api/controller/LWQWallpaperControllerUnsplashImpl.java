package com.stanleyidesis.livewallpaperquotes.api.controller;

import android.graphics.Bitmap;
import android.util.Log;

import com.stanleyidesis.livewallpaperquotes.LWQApplication;
import com.stanleyidesis.livewallpaperquotes.LWQPreferences;
import com.stanleyidesis.livewallpaperquotes.api.Callback;
import com.stanleyidesis.livewallpaperquotes.api.db.BackgroundImage;
import com.stanleyidesis.livewallpaperquotes.api.db.Category;
import com.stanleyidesis.livewallpaperquotes.api.db.Quote;
import com.stanleyidesis.livewallpaperquotes.api.db.Wallpaper;
import com.stanleyidesis.livewallpaperquotes.api.event.NewWallpaperEvent;
import com.stanleyidesis.livewallpaperquotes.api.network.UnsplashManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.greenrobot.event.EventBus;

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
 * LWQWallpaperControllerUnsplashImpl.java
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
public class LWQWallpaperControllerUnsplashImpl implements LWQWallpaperController {

    enum RetrievalState {
        NONE,
        NEW_WALLPAPER;
    }

    UnsplashManager unsplashManager;
    Wallpaper activeWallpaper;
    Bitmap activeBackgroundImage;
    RetrievalState retrievalState;

    public LWQWallpaperControllerUnsplashImpl() {
        unsplashManager = new UnsplashManager();
        retrievalState = RetrievalState.NONE;
    }

    @Override
    public String getQuote() {
        if (activeWallpaper != null) {
            return activeWallpaper.quote.text;
        }
        return "";
    }

    @Override
    public String getAuthor() {
        if (activeWallpaper != null) {
            return activeWallpaper.quote.author.name;
        }
        return "";
    }

    @Override
    public Bitmap getBackgroundImage() {
        return activeBackgroundImage;
    }

    @Override
    public boolean activeWallpaperLoaded() {
        return activeWallpaper != null && activeBackgroundImage != null;
    }

    @Override
    public synchronized void generateNewWallpaper() {
        if (retrievalState == RetrievalState.NEW_WALLPAPER) {
            return;
        }
        retrievalState = RetrievalState.NEW_WALLPAPER;
        Category fromCategory;
        if (LWQPreferences.isAutoPilot()) {
            final String quoteCategoryPreference = LWQPreferences.getQuoteCategoryPreference();
            if (quoteCategoryPreference == null) {
                fromCategory = Category.random();
            } else {
                fromCategory = Category.findWithName(quoteCategoryPreference);
            }
        } else {
            // TODO
            fromCategory = Category.random();
        }
        LWQApplication.getQuoteController().fetchUnusedQuote(fromCategory, new Callback<Quote>() {

            void finishUp(Quote newQuote, BackgroundImage newBackgroundImage) {
                if (activeWallpaper != null) {
                    activeWallpaper.active = false;
                    activeWallpaper.save();
                }
                discardActiveWallpaper();
                activeWallpaper = new Wallpaper(newQuote, newBackgroundImage, true, System.currentTimeMillis());
                activeWallpaper.save();
                newBackgroundImage.used = true;
                newBackgroundImage.save();
                retrievalState = RetrievalState.NONE;
                notifyListeners(false);
                retrieveActiveWallpaper();
            }

            @Override
            public void onSuccess(final Quote quote) {
                final UnsplashManager.UnsplashCategory unsplashCategory =
                        UnsplashManager.UnsplashCategory.fromName(LWQPreferences.getImageCategoryPreference());
                final BackgroundImage backgroundImage = BackgroundImage.unusedFromCategory(unsplashCategory.sqlName());
                if (backgroundImage != null) {
                    finishUp(quote, backgroundImage);
                    return;
                }
                new UnsplashRetryableRequest(unsplashCategory, 3, 1, new Callback<List<BackgroundImage>>() {
                    @Override
                    public void onSuccess(List<BackgroundImage> backgroundImages) {
                        if (backgroundImages.size() > 0) {
                            finishUp(quote, backgroundImages.get(0));
                        }
                        // Failed to find an unused image :(
                        Log.w(getClass().getSimpleName(), "Failed to find an unused image, going with a random one");
                        finishUp(quote, BackgroundImage.randomFromSource(BackgroundImage.Source.UNSPLASH));
                    }

                    @Override
                    public void onError(String errorMessage, Throwable throwable) {
                        retrievalState = RetrievalState.NONE;
                        notifyListeners(errorMessage, throwable);
                    }
                }).start();
            }

            @Override
            public void onError(String errorMessage, Throwable throwable) {
                retrievalState = RetrievalState.NONE;
                notifyListeners(errorMessage, throwable);
            }
        });
    }

    @Override
    public boolean activeWallpaperExists() {
        return activeWallpaper != null || Wallpaper.active() != null;
    }

    @Override
    public synchronized boolean retrieveActiveWallpaper() {
        if (!activeWallpaperExists()) {
            retrievalState = RetrievalState.NONE;
            notifyListeners("No Wallpaper active", null);
            return false;
        }
        if (activeWallpaperLoaded()) {
            retrievalState = RetrievalState.NONE;
            notifyListeners(true);
            return false;
        }
        if (activeWallpaper == null) {
            activeWallpaper = Wallpaper.active();
        }
        LWQApplication.getImageController().retrieveBitmap(getFullUri(), new Callback<Bitmap>() {
            @Override
            public void onSuccess(Bitmap bitmap) {
                activeBackgroundImage = bitmap;
                retrievalState = RetrievalState.NONE;
                notifyListeners(bitmap != null);
            }

            @Override
            public void onError(String errorMessage, Throwable throwable) {
                retrievalState = RetrievalState.NONE;
                notifyListeners(errorMessage, throwable);
            }
        });
        return true;
    }

    @Override
    public void discardActiveWallpaper() {
        if (activeWallpaperLoaded()) {
            LWQApplication.getImageController().clearBitmap(getFullUri());
            activeBackgroundImage = null;
        }
        activeWallpaper = null;
    }

    @Override
    public List<String> getBackgroundCategories() {
        List<String> categoryList = new ArrayList<>();
        for (UnsplashManager.UnsplashCategory unsplashCategory : UnsplashManager.UnsplashCategory.values()) {
            categoryList.add(unsplashCategory.prettyName);
        }
        Collections.sort(categoryList);
        return categoryList;
    }

    String getFullUri() {
        if (activeWallpaper.backgroundImage.source == BackgroundImage.Source.UNSPLASH) {
            return UnsplashManager.appendJPGFormat(activeWallpaper.backgroundImage.uri);
        }
        return activeWallpaper.backgroundImage.uri;
    }

    void notifyListeners(boolean loaded) {
        EventBus.getDefault().post(NewWallpaperEvent.newWallpaper(loaded));
    }

    void notifyListeners(String errorMessage, Throwable throwable) {
        EventBus.getDefault().post(NewWallpaperEvent.newWallpaperFailed(errorMessage, throwable));
    }

    class UnsplashRetryableRequest {
        private boolean started = false;

        UnsplashManager.UnsplashCategory category;
        int numberOfAttempts;
        int startingPageNumber;
        Callback<List<BackgroundImage>> callback;

        ExecutorService executorService;
        List<BackgroundImage> newBackgroundImages;

        Runnable downloadRunnable = new Runnable() {
            @Override
            public void run() {
                unsplashManager.getPhotoURLs(startingPageNumber, category, new Callback<List<UnsplashManager.LWQUnsplashImage>>() {
                    @Override
                    public void onSuccess(List<UnsplashManager.LWQUnsplashImage> lwqUnsplashImages) {
                        for (UnsplashManager.LWQUnsplashImage unsplashImage : lwqUnsplashImages) {
                            BackgroundImage existingBackgroundImage = BackgroundImage.findImage(unsplashImage.url);
                            if (existingBackgroundImage == null) {
                                final BackgroundImage newBackgroundImage = new BackgroundImage(unsplashImage.url,
                                        BackgroundImage.Source.UNSPLASH, category.sqlName(), false);
                                newBackgroundImage.save();
                                newBackgroundImages.add(newBackgroundImage);
                            }
                        }
                        attempt();
                    }

                    @Override
                    public void onError(String errorMessage, Throwable throwable) {
                        callback.onError(errorMessage, throwable);
                    }
                });
            }
        };

        public UnsplashRetryableRequest(UnsplashManager.UnsplashCategory category,
                                        int numberOfAttempts,
                                        int startingPageNumber,
                                        Callback<List<BackgroundImage>> callback) {
            this.category = category;
            this.numberOfAttempts = numberOfAttempts;
            this.startingPageNumber = startingPageNumber;
            this.callback = callback;
            this.executorService = Executors.newSingleThreadExecutor();
            this.newBackgroundImages = new ArrayList<>();
        }

        public void start() {
            if (started) {
                return;
            }
            started = true;
            attempt();
        }

        private void attempt() {
            if (numberOfAttempts > 0 && newBackgroundImages.size() == 0) {
                startingPageNumber++;
                numberOfAttempts--;
                executorService.submit(downloadRunnable);
            } else {
                callback.onSuccess(newBackgroundImages);
            }
        }
    }
}
