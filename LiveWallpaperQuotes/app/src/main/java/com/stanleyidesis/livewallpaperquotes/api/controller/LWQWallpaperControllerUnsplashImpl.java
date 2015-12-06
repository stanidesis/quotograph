package com.stanleyidesis.livewallpaperquotes.api.controller;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.orm.SugarRecord;
import com.stanleyidesis.livewallpaperquotes.LWQApplication;
import com.stanleyidesis.livewallpaperquotes.LWQPreferences;
import com.stanleyidesis.livewallpaperquotes.R;
import com.stanleyidesis.livewallpaperquotes.api.BaseCallback;
import com.stanleyidesis.livewallpaperquotes.api.Callback;
import com.stanleyidesis.livewallpaperquotes.api.db.Playlist;
import com.stanleyidesis.livewallpaperquotes.api.db.PlaylistAuthor;
import com.stanleyidesis.livewallpaperquotes.api.db.PlaylistCategory;
import com.stanleyidesis.livewallpaperquotes.api.db.PlaylistQuote;
import com.stanleyidesis.livewallpaperquotes.api.db.Quote;
import com.stanleyidesis.livewallpaperquotes.api.db.UnsplashCategory;
import com.stanleyidesis.livewallpaperquotes.api.db.UnsplashPhoto;
import com.stanleyidesis.livewallpaperquotes.api.db.UserPhoto;
import com.stanleyidesis.livewallpaperquotes.api.db.Wallpaper;
import com.stanleyidesis.livewallpaperquotes.api.event.WallpaperEvent;
import com.stanleyidesis.livewallpaperquotes.api.network.UnsplashManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
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

    static final int MAX_RETRIES = 5;

    enum RetrievalState {
        NONE,
        NEW_WALLPAPER;
    }

    UnsplashManager unsplashManager;
    Wallpaper activeWallpaper;
    Bitmap activeBackgroundImage;
    RetrievalState retrievalState;

    Callback<List<Quote>> generateNewWallpaperCallback = new Callback<List<Quote>>() {

        void finishUp(List<Quote> newQuotes, Object photoRecord) {
            Quote newQuote = newQuotes.get(new Random().nextInt(newQuotes.size()));
            if (activeWallpaper != null) {
                activeWallpaper.active = false;
                activeWallpaper.save();
                discardActiveWallpaper();
            }
            activeWallpaper = new Wallpaper(newQuote, true, System.currentTimeMillis(),
                    photoRecord instanceof UnsplashPhoto ? Wallpaper.IMAGE_SOURCE_UNSPLASH : Wallpaper.IMAGE_SOURCE_USER,
                    ((SugarRecord) photoRecord).getId());
            activeWallpaper.save();
            retrievalState = RetrievalState.NONE;
            notifyWallpaper(WallpaperEvent.Status.GENERATED_NEW_WALLPAPER);
            retrieveActiveWallpaper();
        }

        @Override
        public void onSuccess(final List<Quote> newQuotes) {
            final UnsplashCategory unsplashCategory =
                    UnsplashCategory.find(LWQPreferences.getImageCategoryPreference());
            new UnsplashRetryableRequest(unsplashCategory, MAX_RETRIES, new BaseCallback<UnsplashPhoto>() {
                @Override
                public void onSuccess(UnsplashPhoto unsplashPhoto) {
                    finishUp(newQuotes, unsplashPhoto == null ? UnsplashPhoto.random() : unsplashPhoto);
                }
            }).start();
        }

        @Override
        public void onError(String errorMessage, Throwable throwable) {
            retrievalState = RetrievalState.NONE;
            notifyWallpaper(WallpaperEvent.Status.GENERATING_NEW_WALLPAPER, errorMessage, throwable);
        }
    };

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
    public void generateNewWallpaper(PlaylistCategory category) {
        _generateNewWallpaper(category);
    }

    @Override
    public void generateNewWallpaper(PlaylistAuthor author) {
        _generateNewWallpaper(author);
    }

    @Override
    public void generateNewWallpaper(PlaylistQuote quote) {
        _generateNewWallpaper(quote);
    }

    @Override
    public synchronized void generateNewWallpaper() {
        if (retrievalState == RetrievalState.NEW_WALLPAPER) {
            return;
        }
        notifyWallpaper(WallpaperEvent.Status.GENERATING_NEW_WALLPAPER);
        retrievalState = RetrievalState.NEW_WALLPAPER;
        final Playlist activePlaylist = Playlist.active();
        final List<Object> options = new ArrayList<>();
        options.addAll(activePlaylist.categories());
        options.addAll(activePlaylist.authors());
        options.addAll(activePlaylist.quotes());
        _generateNewWallpaper(options.get(new Random().nextInt(options.size())));
    }

    void _generateNewWallpaper(final Object playlistObject) {
        if (playlistObject instanceof PlaylistCategory) {
            LWQApplication.getQuoteController().fetchUnusedQuotes(((PlaylistCategory) playlistObject).category, new Callback<List<Quote>>() {
                @Override
                public void onSuccess(List<Quote> quotes) {
                    if (quotes.size() > 0) {
                        generateNewWallpaperCallback.onSuccess(quotes);
                    } else {
                        // No new Quotes found
                        generateNewWallpaperCallback.onSuccess(Quote.allFromCategory(((PlaylistCategory) playlistObject).category));
                    }
                }

                @Override
                public void onError(String errorMessage, Throwable throwable) {
                    retrievalState = RetrievalState.NONE;
                    notifyWallpaper(WallpaperEvent.Status.GENERATING_NEW_WALLPAPER, errorMessage, throwable);
                }
            });
        } else if (playlistObject instanceof PlaylistAuthor) {
            LWQApplication.getQuoteController().fetchUnusedQuotesBy(((PlaylistAuthor) playlistObject).author, new Callback<List<Quote>>() {
                @Override
                public void onSuccess(List<Quote> quotes) {
                    if (quotes.size() > 0) {
                        generateNewWallpaperCallback.onSuccess(quotes);
                    } else {
                        // No new Quotes found
                        generateNewWallpaperCallback.onSuccess(Quote.allFromAuthor(((PlaylistAuthor) playlistObject).author));
                    }
                }

                @Override
                public void onError(String errorMessage, Throwable throwable) {
                    retrievalState = RetrievalState.NONE;
                    notifyWallpaper(WallpaperEvent.Status.GENERATING_NEW_WALLPAPER, errorMessage, throwable);
                }
            });
        } else if (playlistObject instanceof PlaylistQuote) {
            generateNewWallpaperCallback.onSuccess(Collections.singletonList(((PlaylistQuote) playlistObject).quote));
        }
    }

    @Override
    public boolean activeWallpaperExists() {
        return activeWallpaper != null || Wallpaper.active() != null;
    }

    @Override
    public synchronized boolean retrieveActiveWallpaper() {
        if (!activeWallpaperExists()) {
            retrievalState = RetrievalState.NONE;
            notifyWallpaper(WallpaperEvent.Status.RETRIEVING_WALLPAPER, "No Wallpaper active", null);
            return false;
        }
        if (activeWallpaperLoaded()) {
            retrievalState = RetrievalState.NONE;
            notifyWallpaper(WallpaperEvent.Status.RETRIEVED_WALLPAPER);
            return false;
        }
        notifyWallpaper(WallpaperEvent.Status.RETRIEVING_WALLPAPER);
        if (activeWallpaper == null) {
            activeWallpaper = Wallpaper.active();
        }
        if (activeWallpaper.imageSource == Wallpaper.IMAGE_SOURCE_UNSPLASH) {
            LWQApplication.getImageController().retrieveBitmap(getFullUri(), new Callback<Bitmap>() {
                @Override
                public void onSuccess(Bitmap bitmap) {
                    activeBackgroundImage = bitmap;
                    retrievalState = RetrievalState.NONE;
                    notifyWallpaper(WallpaperEvent.Status.RETRIEVED_WALLPAPER);
                }

                @Override
                public void onError(String errorMessage, Throwable throwable) {
                    retrievalState = RetrievalState.NONE;
                    notifyWallpaper(WallpaperEvent.Status.RETRIEVING_WALLPAPER, errorMessage, throwable);
                }
            });
        } else if (activeWallpaper.imageSource == Wallpaper.IMAGE_SOURCE_RESOURCE) {
            // TODO use a good background image for the default
            Resources resources = LWQApplication.get().getResources();
            activeBackgroundImage = BitmapFactory.decodeResource(resources, R.drawable.ic_launcher);
            notifyWallpaper(WallpaperEvent.Status.RETRIEVED_WALLPAPER);
        } else if (activeWallpaper.imageSource == Wallpaper.IMAGE_SOURCE_USER) {
            UserPhoto userPhoto = activeWallpaper.recoverUserPhoto();
            activeBackgroundImage = BitmapFactory.decodeFile(userPhoto.uri);
            notifyWallpaper(WallpaperEvent.Status.RETRIEVED_WALLPAPER);
        }
        return true;
    }

    @Override
    public void discardActiveWallpaper() {
        if (activeWallpaperLoaded()) {
            if (activeWallpaper.imageSource == Wallpaper.IMAGE_SOURCE_UNSPLASH) {
                LWQApplication.getImageController().clearBitmap(getFullUri());
            } else {
                activeBackgroundImage.recycle();
            }
        }
        activeBackgroundImage = null;
        activeWallpaper = null;
    }

    @Override
    public void fetchBackgroundCategories(final Callback<List<String>> callback) {
        Executors.newSingleThreadScheduledExecutor().submit(new Runnable() {
            @Override
            public void run() {
                Object resultObject = unsplashManager.fetchAllCategories();
                if (resultObject instanceof String) {
                    callback.onError(String.valueOf(resultObject), null);
                } else {
                    callback.onSuccess(getBackgroundCategories());
                }
            }
        });
    }

    @Override
    public List<String> getBackgroundCategories() {
        List<UnsplashCategory> unsplashCategories = UnsplashCategory.listAll(UnsplashCategory.class);
        List<String> categoryTitles = new ArrayList<>();
        for (UnsplashCategory category : unsplashCategories) {
            categoryTitles.add(category.title);
        }
        Collections.sort(categoryTitles);
        return categoryTitles;
    }

    String getFullUri() {
        if (activeWallpaper.imageSource == Wallpaper.IMAGE_SOURCE_UNSPLASH) {
            return activeWallpaper.recoverUnsplashPhoto().fullURL;
        }
        return activeWallpaper.recoverUserPhoto().uri;
    }

    void notifyWallpaper(WallpaperEvent.Status status) {
        EventBus.getDefault().post(WallpaperEvent.withStatus(status));
    }

    void notifyWallpaper(WallpaperEvent.Status status, String errorMessage, Throwable throwable) {
        EventBus.getDefault().post(WallpaperEvent.failedWithStatus(status, errorMessage, throwable));
    }

    class UnsplashRetryableRequest {
        private boolean started = false;

        UnsplashCategory category;
        Callback<UnsplashPhoto> callback;
        int numberOfAttempts;

        ExecutorService executorService;
        UnsplashPhoto newBackgroundImage;

        Runnable downloadRunnable = new Runnable() {
            @Override
            public void run() {
                final Object result = unsplashManager.fetchRandomPhoto(category, true, null);
                if (result instanceof String) {
                    attempt();
                    return;
                }
                UnsplashPhoto unsplashPhoto = (UnsplashPhoto) result;
                if (!Wallpaper.exists(Wallpaper.IMAGE_SOURCE_UNSPLASH, unsplashPhoto.getId())) {
                    newBackgroundImage = unsplashPhoto;
                }
                attempt();
            }
        };

        public UnsplashRetryableRequest(UnsplashCategory category,
                                        int numberOfAttempts,
                                        Callback<UnsplashPhoto> callback) {
            this.category = category;
            this.numberOfAttempts = numberOfAttempts;
            this.callback = callback;
            this.executorService = Executors.newSingleThreadExecutor();
        }

        public void start() {
            if (started) {
                return;
            }
            started = true;
            attempt();
        }

        private void attempt() {
            if (newBackgroundImage != null) {
                callback.onSuccess(newBackgroundImage);
            } else if (numberOfAttempts > 0) {
                numberOfAttempts--;
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            downloadRunnable.run();
                        } catch (Exception e) {
                            e.printStackTrace();
                            attempt();
                        }
                    }
                });
            } else {
                callback.onSuccess(null);
            }
        }
    }
}
