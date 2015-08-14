package com.stanleyidesis.livewallpaperquotes;

import android.content.res.TypedArray;
import android.util.Log;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.orm.SugarApp;
import com.orm.query.Condition;
import com.orm.query.Select;
import com.stanleyidesis.livewallpaperquotes.api.Callback;
import com.stanleyidesis.livewallpaperquotes.api.LWQImageController;
import com.stanleyidesis.livewallpaperquotes.api.LWQImageControllerFrescoImpl;
import com.stanleyidesis.livewallpaperquotes.api.LWQWallpaperController;
import com.stanleyidesis.livewallpaperquotes.api.LWQWallpaperControllerUnsplashImpl;
import com.stanleyidesis.livewallpaperquotes.api.db.Author;
import com.stanleyidesis.livewallpaperquotes.api.db.Category;
import com.stanleyidesis.livewallpaperquotes.api.db.Quote;

/**
 * Created by stanleyidesis on 7/11/15.
 */
public class LWQApplication extends SugarApp {

    static LWQApplication sApplication;

    LWQWallpaperController wallpaperController;
    LWQImageController imageController;

    @Override
    public void onCreate() {
        super.onCreate();
        Fresco.initialize(this);

        sApplication = this;
        wallpaperController = new LWQWallpaperControllerUnsplashImpl();
        imageController = new LWQImageControllerFrescoImpl();

        // Pre-populate database
        if (LWQPreferences.isFirstLaunch()) {
            final String[] defaultCategoryArray = getResources().getStringArray(R.array.default_category_titles);
            final TypedArray defaultCategoryQuoteMap = getResources().obtainTypedArray(R.array.default_category_quote_map);
            final TypedArray defaultCategoryAuthorMap = getResources().obtainTypedArray(R.array.default_category_author_map);
            for (int i = 0; i < defaultCategoryArray.length; i++) {
                Category defaultCategory = new Category(defaultCategoryArray[i]);
                defaultCategory.save();
                final int quoteArrayResourceId = defaultCategoryQuoteMap.getResourceId(i, -1);
                final int authorArrayResourceId = defaultCategoryAuthorMap.getResourceId(i, -1);
                if (quoteArrayResourceId == -1 || authorArrayResourceId == -1) {
                    // Uh-oh…
                    Log.e(getClass().getSimpleName(), "Missing quote / author array", new Throwable());
                    continue;
                }
                final String[] defaultCategoryQuotes = getResources().getStringArray(quoteArrayResourceId);
                final String[] defaultCategoryAuthors = getResources().getStringArray(authorArrayResourceId);

                // Populate DB with default quotes and authors (repeat authors accounted for)
                for (int j = 0; j < defaultCategoryQuotes.length; j++) {
                    Author defaultAuthor = Select.from(Author.class).where(Condition.prop("name").eq(defaultCategoryAuthors[j])).first();
                    if (defaultAuthor == null) {
                        defaultAuthor = new Author(defaultCategoryAuthors[j]);
                        defaultAuthor.save();
                    }
                    new Quote(defaultCategoryQuotes[j], defaultAuthor, defaultCategory).save();
                }
            }
            defaultCategoryQuoteMap.recycle();
            defaultCategoryAuthorMap.recycle();
            LWQPreferences.setFirstLaunch(false);
            wallpaperController.generateNewWallpaper(new Callback<Boolean>() {
                @Override
                public void onSuccess(Boolean aBoolean) {
                    if (!wallpaperController.activeWallpaperLoaded()) {
                        wallpaperController.retrieveActiveWallpaper(this);
                    }
                }

                @Override
                public void onError(String errorMessage) {}
            });
        }
    }

    public static LWQApplication get() {
        return sApplication;
    }

    public static LWQWallpaperController getWallpaperController() {
        return sApplication.wallpaperController;
    }

    public static LWQImageController getImageController() {
        return sApplication.imageController;
    }
}
