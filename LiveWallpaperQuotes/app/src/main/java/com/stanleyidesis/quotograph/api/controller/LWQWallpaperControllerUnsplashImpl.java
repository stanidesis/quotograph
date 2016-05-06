package com.stanleyidesis.quotograph.api.controller;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;

import com.afollestad.materialdialogs.util.TypefaceHelper;
import com.orm.SugarRecord;
import com.stanleyidesis.quotograph.LWQApplication;
import com.stanleyidesis.quotograph.LWQPreferences;
import com.stanleyidesis.quotograph.R;
import com.stanleyidesis.quotograph.api.BaseCallback;
import com.stanleyidesis.quotograph.api.Callback;
import com.stanleyidesis.quotograph.api.LWQError;
import com.stanleyidesis.quotograph.api.db.Playlist;
import com.stanleyidesis.quotograph.api.db.PlaylistAuthor;
import com.stanleyidesis.quotograph.api.db.PlaylistCategory;
import com.stanleyidesis.quotograph.api.db.PlaylistQuote;
import com.stanleyidesis.quotograph.api.db.Quote;
import com.stanleyidesis.quotograph.api.db.UnsplashCategory;
import com.stanleyidesis.quotograph.api.db.UnsplashPhoto;
import com.stanleyidesis.quotograph.api.db.UserAlbum;
import com.stanleyidesis.quotograph.api.db.UserPhoto;
import com.stanleyidesis.quotograph.api.db.Wallpaper;
import com.stanleyidesis.quotograph.api.event.WallpaperEvent;
import com.stanleyidesis.quotograph.api.network.UnsplashManager;
import com.stanleyidesis.quotograph.ui.Fonts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.greenrobot.event.EventBus;

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
 * LWQWallpaperControllerUnsplashImpl.java
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
public class LWQWallpaperControllerUnsplashImpl implements LWQWallpaperController {

    static final int MAX_RETRIES = 5;

    UnsplashManager unsplashManager;
    Wallpaper activeWallpaper;
    Bitmap activeBackgroundImage;
    RetrievalState retrievalState;

    Callback<List<Quote>> generateNewWallpaperCallback = new Callback<List<Quote>>() {

        void finishUp(List<Quote> newQuotes, Object photoRecord) {
            // Get the font set first
            Set<String> fontPreferenceSet = LWQPreferences.getFontSet();
            // Discard active wallpaper
            if (activeWallpaper != null) {
                activeWallpaper.active = false;
                activeWallpaper.save();
                // Remove the previously-used font if the set has more than one font in it
                // This prevents reusing the same font twice
                if (fontPreferenceSet.size() > 1) {
                    fontPreferenceSet.remove(String.valueOf(activeWallpaper.typefaceId));
                }
                discardActiveWallpaper();
            }

            // Get a new quote
            Quote newQuote = newQuotes.get(new Random().nextInt(newQuotes.size()));

            // Choose new font, if possible
            int newFontId;
            String[] fontPreferenceArray = new String[fontPreferenceSet.size()];
            fontPreferenceSet.toArray(fontPreferenceArray);
            if (fontPreferenceArray.length == 1) {
                newFontId = Integer.parseInt(fontPreferenceArray[0]);
            } else {
                newFontId = Integer.parseInt
                        (fontPreferenceArray[(new Random()).nextInt(fontPreferenceArray.length)]);
            }
            activeWallpaper = new Wallpaper(newQuote, true, System.currentTimeMillis(),
                    photoRecord instanceof UnsplashPhoto ? Wallpaper.IMAGE_SOURCE_UNSPLASH : Wallpaper.IMAGE_SOURCE_USER,
                    ((SugarRecord) photoRecord).getId(), newFontId);
            activeWallpaper.save();
            LWQApplication.getLogger().logWallpaperCount(activeWallpaper.getId());
            newQuote.used = true;
            newQuote.save();
            setRetrievalState(RetrievalState.NONE);
            notifyWallpaper(WallpaperEvent.Status.GENERATED_NEW_WALLPAPER);
            retrieveActiveWallpaper();
        }

        @Override
        public void onSuccess(final List<Quote> newQuotes) {
            if (newQuotes.isEmpty()) {
                onError(LWQError.create(LWQApplication.get().getString(R.string.failed_to_generate_wallpaper)));
                return;
            }
            List<UnsplashCategory> activeCategories = UnsplashCategory.active();
            List<UserAlbum> activeAlbums = UserAlbum.active();
            boolean useAlbums = activeAlbums.size() > 0
                    && LWQApplication.ownsImageAccess();
            if (activeCategories.size() > 0
                    && useAlbums) {
                // Randomness will determine whether we still use an album
                useAlbums = new Random().nextBoolean();
            }
            if (useAlbums) {
                // Get a random album
                UserAlbum albumToTry = activeAlbums.get(
                        new Random().nextInt(activeAlbums.size()));
                // Find all photos in the album
                List<UserPhoto> userPhotos = UserPhoto.photosFromAlbum(albumToTry);
                // Remove the active currentPhoto, if possible
                UserPhoto currentPhoto = activeWallpaper.recoverUserPhoto();
                if (currentPhoto != null) {
                    userPhotos.remove(currentPhoto);
                }
                // Choose a random photo from the album
                UserPhoto photoToUse = userPhotos.get(
                        new Random().nextInt(userPhotos.size()));
                if (photoToUse == null
                        && activeCategories.size() == 0
                        && currentPhoto != null) {
                    // The user wants to use this and ONLY this photo
                    photoToUse = currentPhoto;
                }
                if (photoToUse != null) {
                    finishUp(newQuotes, photoToUse);
                    return;
                }
            }
            // Fallback to categories
            UnsplashCategory categoryToUse = UnsplashCategory.random();
            if (activeCategories.size() > 0) {
                categoryToUse = activeCategories.get(
                        new Random().nextInt(
                                activeCategories.size()));
            }
            new UnsplashRetryableRequest(categoryToUse, MAX_RETRIES, new BaseCallback<UnsplashPhoto>() {
                @Override
                public void onSuccess(UnsplashPhoto unsplashPhoto) {
                    finishUp(newQuotes, unsplashPhoto == null ? UnsplashPhoto.random() : unsplashPhoto);
                }
            }).start();
        }

        @Override
        public void onError(LWQError error) {
            setRetrievalState(RetrievalState.NONE);
            notifyWallpaper(WallpaperEvent.Status.GENERATING_NEW_WALLPAPER, error);
        }
    };

    public LWQWallpaperControllerUnsplashImpl() {
        unsplashManager = new UnsplashManager();
        setRetrievalState(RetrievalState.NONE);
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
    public Typeface getTypeface() {
        if (activeWallpaper == null) {
            return null;
        }
        Fonts fontById = Fonts.findById((int) activeWallpaper.typefaceId);
        return TypefaceHelper.get(LWQApplication.get(), fontById.getFileName());
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
        final Playlist activePlaylist = Playlist.active();
        final List<Object> options = new ArrayList<>();
        options.addAll(activePlaylist.categories());
        options.addAll(activePlaylist.authors());
        options.addAll(activePlaylist.quotes());
        _generateNewWallpaper(options.get(new Random().nextInt(options.size())));
    }

    void _generateNewWallpaper(final Object playlistObject) {
        if (getRetrievalState() == RetrievalState.NEW_WALLPAPER) {
            return;
        }
        if (!LWQApplication.getNetworkConnectionListener().getCurrentConnectionType().isConnected()) {
            notifyWallpaper(WallpaperEvent.Status.GENERATING_NEW_WALLPAPER, LWQError.create(LWQApplication.get().getString(R.string.network_connection_required_title)));
            return;
        }
        setRetrievalState(RetrievalState.NEW_WALLPAPER);
        notifyWallpaper(WallpaperEvent.Status.GENERATING_NEW_WALLPAPER);

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
                public void onError(LWQError error) {
                    setRetrievalState(RetrievalState.NONE);
                    notifyWallpaper(WallpaperEvent.Status.GENERATING_NEW_WALLPAPER, error);
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
                public void onError(LWQError error) {
                    setRetrievalState(RetrievalState.NONE);
                    notifyWallpaper(WallpaperEvent.Status.GENERATING_NEW_WALLPAPER, error);
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
            notifyWallpaper(WallpaperEvent.Status.RETRIEVING_WALLPAPER, LWQError.create("No active Wallpaper found"));
            return false;
        } else if (activeWallpaperLoaded()) {
            notifyWallpaper(WallpaperEvent.Status.RETRIEVED_WALLPAPER);
            return false;
        } else if (getRetrievalState() == RetrievalState.ACTIVE_WALLPAPER) {
            return false;
        }

        if (activeWallpaper == null) {
            activeWallpaper = Wallpaper.active();
        }

        setRetrievalState(RetrievalState.ACTIVE_WALLPAPER);
        notifyWallpaper(WallpaperEvent.Status.RETRIEVING_WALLPAPER);
        if (activeWallpaper.imageSource == Wallpaper.IMAGE_SOURCE_UNSPLASH) {
            LWQApplication.getImageController().retrieveBitmap(getFullUri(), new Callback<Bitmap>() {
                @Override
                public void onSuccess(Bitmap bitmap) {
                    activeBackgroundImage = bitmap;
                    setRetrievalState(RetrievalState.NONE);
                    notifyWallpaper(WallpaperEvent.Status.RETRIEVED_WALLPAPER);
                }

                @Override
                public void onError(LWQError error) {
                    setRetrievalState(RetrievalState.NONE);
                    notifyWallpaper(WallpaperEvent.Status.RETRIEVING_WALLPAPER, error);
                }
            });
        } else if (activeWallpaper.imageSource == Wallpaper.IMAGE_SOURCE_RESOURCE) {
            // TODO use a good background image for the default
            Resources resources = LWQApplication.get().getResources();
            activeBackgroundImage = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher);
            notifyWallpaper(WallpaperEvent.Status.RETRIEVED_WALLPAPER);
            setRetrievalState(RetrievalState.NONE);
        } else if (activeWallpaper.imageSource == Wallpaper.IMAGE_SOURCE_USER) {
            UserPhoto userPhoto = activeWallpaper.recoverUserPhoto();
            activeBackgroundImage = LWQApplication.getImageController()
                    .retrieveBitmapSync(userPhoto.uri);
            notifyWallpaper(WallpaperEvent.Status.RETRIEVED_WALLPAPER);
            setRetrievalState(RetrievalState.NONE);
        }
        return true;
    }

    @Override
    public void discardActiveWallpaper() {
        discardActiveBitmap();
        activeWallpaper = null;
    }

    @Override
    public void fetchBackgroundCategories(final Callback<List<String>> callback) {
        Executors.newSingleThreadScheduledExecutor().submit(new Runnable() {
            @Override
            public void run() {
                Object resultObject = unsplashManager.fetchAllCategories();
                if (resultObject instanceof LWQError) {
                    callback.onError((LWQError) resultObject);
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

    @Override
    synchronized public RetrievalState getRetrievalState() {
        return retrievalState;
    }

    synchronized void setRetrievalState(RetrievalState retrievalState) {
        this.retrievalState = retrievalState;
        LWQApplication.getLogger().logWallpaperRetrievalState(retrievalState);
    }

    String getFullUri() {
        if (activeWallpaper.imageSource == Wallpaper.IMAGE_SOURCE_UNSPLASH) {
            return activeWallpaper.recoverUnsplashPhoto().fullURL;
        }
        return activeWallpaper.recoverUserPhoto().uri;
    }

    void discardActiveBitmap() {
        if (activeWallpaperLoaded()) {
            if (activeWallpaper.imageSource == Wallpaper.IMAGE_SOURCE_UNSPLASH) {
                LWQApplication.getImageController().clearBitmap(getFullUri());
            }
        }
        activeBackgroundImage = null;
    }

    void notifyWallpaper(WallpaperEvent.Status status) {
        EventBus.getDefault().post(WallpaperEvent.withStatus(status));
    }

    void notifyWallpaper(WallpaperEvent.Status status, LWQError error) {
        EventBus.getDefault().post(WallpaperEvent.failedWithStatus(status, error));
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
                if (result instanceof LWQError) {
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
                            LWQError.create(e);
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
