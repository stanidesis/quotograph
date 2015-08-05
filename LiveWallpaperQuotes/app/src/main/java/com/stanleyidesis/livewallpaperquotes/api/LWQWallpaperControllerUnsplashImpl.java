package com.stanleyidesis.livewallpaperquotes.api;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.stanleyidesis.livewallpaperquotes.LWQApplication;
import com.stanleyidesis.livewallpaperquotes.R;
import com.stanleyidesis.livewallpaperquotes.api.db.BackgroundImage;
import com.stanleyidesis.livewallpaperquotes.api.db.Quote;
import com.stanleyidesis.livewallpaperquotes.api.db.Wallpaper;
import com.stanleyidesis.livewallpaperquotes.api.network.LWQUnsplashManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Created by stanleyidesis on 8/1/15.
 */
public class LWQWallpaperControllerUnsplashImpl implements LWQWallpaperController {

    enum RetrievalState {
        ACTIVE_WALLPAPER,
        NEW_WALLPAPER;
    }

    LWQUnsplashManager unsplashManager;
    Wallpaper activeWallpaper;
    Bitmap activeBackgroundImage;
    Bitmap defaultBackgroundImage;
    RetrievalState retrievalState;
    Map<RetrievalState, List<Callback<Boolean>>> listeners;

    public LWQWallpaperControllerUnsplashImpl() {
        unsplashManager = new LWQUnsplashManager();
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
    public synchronized void generateNewWallpaper(Callback<Boolean> callback) {
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

        // TODO query BrainyQuote
        final Quote newQuote = Quote.random();
        Set<LWQUnsplashManager.Category> categories = new HashSet<>();
        categories.add(LWQUnsplashManager.Category.NATURE);
        unsplashManager.getPhotoURLs(1, categories, new Random().nextBoolean(), new Callback<List<LWQUnsplashManager.LWQUnsplashImage>>() {
            @Override
            public void onSuccess(List<LWQUnsplashManager.LWQUnsplashImage> lwqUnsplashImages) {
                BackgroundImage newBackgroundImage = null;
                for (LWQUnsplashManager.LWQUnsplashImage unsplashImage : lwqUnsplashImages) {
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
                activeWallpaper = new Wallpaper(newQuote, newBackgroundImage, true, System.currentTimeMillis());
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
    public boolean activeWallpaperExists() {
        return activeWallpaper != null || Wallpaper.active() != null;
    }

    @Override
    public synchronized void retrieveActiveWallpaper(final Callback<Boolean> callback) {
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
            retrievalState = null;
            notifyAndClearListeners(RetrievalState.ACTIVE_WALLPAPER, "No active wallpaper set");
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
        if (activeWallpaperLoaded()) {
            if (defaultBackgroundImage != null) {
                defaultBackgroundImage.recycle();
                defaultBackgroundImage = null;
            }
            LWQApplication.getImageController().clearBitmap(getFullUri());
            activeBackgroundImage = null;
            activeWallpaper = null;
        }
    }

    String getFullUri() {
        if (activeWallpaper.backgroundImage.source == BackgroundImage.Source.UNSPLASH) {
            return LWQUnsplashManager.appendJPGFormat(activeWallpaper.backgroundImage.uri);
        }
        return activeWallpaper.backgroundImage.uri;
    }

    void notifyAndClearListeners(RetrievalState state, boolean loaded) {
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
