package com.stanleyidesis.livewallpaperquotes.api;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.stanleyidesis.livewallpaperquotes.LWQApplication;
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
    Bitmap defaultBackgroundImage;
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
        return LWQApplication.get().getString(R.string.default_quote);
    }

    @Override
    public String getAuthor() {
        if (activeWallpaper != null) {
            return activeWallpaper.quote.author.name;
        }
        return LWQApplication.get().getString(R.string.default_author);
    }

    @Override
    public Bitmap getBackgroundImage() {
        if (activeBackgroundImage == null) {
            if (defaultBackgroundImage == null) {
                defaultBackgroundImage = BitmapFactory.decodeResource(LWQApplication.get().getResources(),
                        R.drawable.default_background);
            }
            return defaultBackgroundImage;
        }
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

        Category fromCategory;
        if (activeWallpaper != null) {
            fromCategory = activeWallpaper.quote.category;
        } else {
            fromCategory = Category.random();
        }
        LWQApplication.getQuoteController().fetchUnusedQuote(fromCategory, new Callback<Quote>() {
            @Override
            public void onSuccess(final Quote quote) {
                unsplashManager.getPhotoURLs(1, UnsplashManager.UnsplashCategory.NATURE, new Callback<List<UnsplashManager.LWQUnsplashImage>>() {
                    @Override
                    public void onSuccess(List<UnsplashManager.LWQUnsplashImage> lwqUnsplashImages) {
                        BackgroundImage newBackgroundImage = null;
                        for (UnsplashManager.LWQUnsplashImage unsplashImage : lwqUnsplashImages) {
                            newBackgroundImage = BackgroundImage.findImage(unsplashImage.url);
                            if (newBackgroundImage == null) {
                                newBackgroundImage = new BackgroundImage(unsplashImage.url, BackgroundImage.Source.UNSPLASH);
                                newBackgroundImage.save();
                                break;
                            } else {
                                newBackgroundImage = null;
                            }
                        }
                        if (newBackgroundImage == null) {
                            newBackgroundImage = BackgroundImage.random();
                        }
                        if (activeWallpaper != null) {
                            activeWallpaper.active = false;
                            activeWallpaper.save();
                        }
                        discardActiveWallpaper();
                        activeWallpaper = new Wallpaper(quote, newBackgroundImage, true, System.currentTimeMillis());
                        activeWallpaper.save();
                        retrievalState = null;
                        notifyAndClearListeners(RetrievalState.NEW_WALLPAPER, false);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        retrievalState = null;
                        notifyAndClearListeners(RetrievalState.NEW_WALLPAPER, errorMessage);
                    }
                });
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
                    public void onError(String errorMessage) {

                    }
                });
            } else {
                retrievalState = null;
                notifyAndClearListeners(RetrievalState.ACTIVE_WALLPAPER, "No active wallpaper set");
            }
            return;
        } else if (activeWallpaperLoaded()) {
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
        if (defaultBackgroundImage != null) {
            defaultBackgroundImage.recycle();
            defaultBackgroundImage = null;
        }
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
}
