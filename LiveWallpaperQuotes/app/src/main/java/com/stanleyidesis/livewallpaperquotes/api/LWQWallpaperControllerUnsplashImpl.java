package com.stanleyidesis.livewallpaperquotes.api;

import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;

import com.stanleyidesis.livewallpaperquotes.LWQApplication;
import com.stanleyidesis.livewallpaperquotes.LWQPreferences;
import com.stanleyidesis.livewallpaperquotes.R;
import com.stanleyidesis.livewallpaperquotes.api.db.BackgroundImage;
import com.stanleyidesis.livewallpaperquotes.api.db.Category;
import com.stanleyidesis.livewallpaperquotes.api.db.Quote;
import com.stanleyidesis.livewallpaperquotes.api.db.Wallpaper;
import com.stanleyidesis.livewallpaperquotes.api.network.UnsplashManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
        ACTIVE_WALLPAPER,
        NEW_WALLPAPER;
    }

    UnsplashManager unsplashManager;
    Wallpaper activeWallpaper;
    Bitmap activeBackgroundImage;
    RetrievalState retrievalState;
    Map<RetrievalState, List<Callback<Boolean>>> listeners;

    public LWQWallpaperControllerUnsplashImpl() {
        unsplashManager = new UnsplashManager();
        listeners = new HashMap<>();
        listeners.put(RetrievalState.ACTIVE_WALLPAPER, new ArrayList<Callback<Boolean>>());
        listeners.put(RetrievalState.NEW_WALLPAPER, new ArrayList<Callback<Boolean>>());
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
    public synchronized void generateNewWallpaper(final Callback<Boolean> callback) {
        if (retrievalState == RetrievalState.NEW_WALLPAPER) {
            listeners.get(RetrievalState.NEW_WALLPAPER).remove(callback);
            listeners.get(RetrievalState.NEW_WALLPAPER).add(callback);
            return;
        } else if (retrievalState == RetrievalState.ACTIVE_WALLPAPER) {
            final List<Callback<Boolean>> activeWallpaperCallbacks = listeners.get(RetrievalState.ACTIVE_WALLPAPER);
            listeners.get(RetrievalState.NEW_WALLPAPER).addAll(activeWallpaperCallbacks);
            activeWallpaperCallbacks.clear();
        }
        retrievalState = RetrievalState.NEW_WALLPAPER;
        listeners.get(RetrievalState.NEW_WALLPAPER).add(callback);

        Category fromCategory = Category.random();
        if (LWQPreferences.isAutoPilot()) {
            final String quoteCategoryPreference = LWQPreferences.getQuoteCategoryPreference();
            if (quoteCategoryPreference == null) {
                fromCategory = Category.random();
            } else {
                fromCategory = Category.findWithName(quoteCategoryPreference);
            }
        } else {
            // TODO
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
                retrievalState = null;
                notifyAndClearListeners(RetrievalState.NEW_WALLPAPER, false);
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
                    public void onError(String errorMessage) {
                        retrievalState = null;
                        notifyAndClearListeners(RetrievalState.NEW_WALLPAPER, errorMessage);
                    }
                }).start();
            }

            @Override
            public void onError(String errorMessage) {
                retrievalState = null;
                notifyAndClearListeners(RetrievalState.NEW_WALLPAPER, errorMessage);
            }
        });
    }

    @Override
    public boolean activeWallpaperExists() {
        return activeWallpaper != null || Wallpaper.active() != null;
    }

    @Override
    public synchronized void retrieveActiveWallpaper(final Callback<Boolean> callback, final boolean generateIfNecessary) {
        if (retrievalState == RetrievalState.ACTIVE_WALLPAPER) {
            if (!listeners.get(RetrievalState.ACTIVE_WALLPAPER).contains(callback)) {
                listeners.get(RetrievalState.ACTIVE_WALLPAPER).add(callback);
            }
            return;
        } else if (retrievalState == RetrievalState.NEW_WALLPAPER) {
            if (!listeners.get(RetrievalState.NEW_WALLPAPER).contains(callback)) {
                listeners.get(RetrievalState.NEW_WALLPAPER).add(callback);
            }
            return;
        } else {
            retrievalState = RetrievalState.ACTIVE_WALLPAPER;
            listeners.get(RetrievalState.ACTIVE_WALLPAPER).add(callback);
        }
        if (!activeWallpaperExists()) {
            if (generateIfNecessary) {
                generateNewWallpaper(new Callback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {
                        retrieveActiveWallpaper(callback, false);
                    }

                    @Override
                    public void onError(String errorMessage) {}
                });
            } else {
                retrievalState = null;
                notifyAndClearListeners(RetrievalState.ACTIVE_WALLPAPER, "No active wallpaper set");
            }
            return;
        }
        if (activeWallpaperLoaded()) {
            retrievalState = null;
            notifyAndClearListeners(RetrievalState.ACTIVE_WALLPAPER, true);
            return;
        }
        if (activeWallpaper == null) {
            activeWallpaper = Wallpaper.active();
        }
        LWQApplication.getImageController().retrieveBitmap(getFullUri(), new Callback<Bitmap>() {
            @Override
            public void onSuccess(Bitmap bitmap) {
                activeBackgroundImage = bitmap;
                retrievalState = null;
                notifyAndClearListeners(RetrievalState.ACTIVE_WALLPAPER, bitmap != null);
            }

            @Override
            public void onError(String errorMessage) {
                retrievalState = null;
                notifyAndClearListeners(RetrievalState.ACTIVE_WALLPAPER, errorMessage);
            }
        });
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

    void notifyAndClearListeners(RetrievalState state, boolean loaded) {
        if (state == RetrievalState.ACTIVE_WALLPAPER) {
            LWQApplication.get().sendBroadcast(new Intent(LWQApplication.get().getString(R.string.broadcast_new_wallpaper_available)));
        }
        final List<Callback<Boolean>> callbacks = listeners.get(state);
        for (Callback<Boolean> callback : callbacks) {
            callback.onSuccess(loaded);
        }
        callbacks.clear();
    }

    void notifyAndClearListeners(RetrievalState state, String errorMessage) {
        final List<Callback<Boolean>> callbacks = listeners.get(state);
        for (Callback<Boolean> callback : callbacks) {
            callback.onError(errorMessage);
        }
        callbacks.clear();
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
                    public void onError(String errorMessage) {
                        callback.onError(errorMessage);
                        attempt();
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
