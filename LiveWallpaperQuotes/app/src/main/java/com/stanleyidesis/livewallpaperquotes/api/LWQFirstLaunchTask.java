package com.stanleyidesis.livewallpaperquotes.api;

import android.os.AsyncTask;

import com.orm.query.Select;
import com.stanleyidesis.livewallpaperquotes.LWQApplication;
import com.stanleyidesis.livewallpaperquotes.LWQPreferences;
import com.stanleyidesis.livewallpaperquotes.api.db.Category;
import com.stanleyidesis.livewallpaperquotes.api.db.Quote;
import com.stanleyidesis.livewallpaperquotes.api.event.FirstLaunchTaskEvent;
import com.stanleyidesis.livewallpaperquotes.api.event.WallpaperEvent;
import com.stanleyidesis.livewallpaperquotes.api.network.UnsplashManager;

import java.util.List;

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
 * LWQFirstLaunchTask.java
 * @author Stanley Idesis
 *
 * From Live-Wallpaper-Quotes
 * https://github.com/stanidesis/live-wallpaper-quotes
 *
 * Please report any issues
 * https://github.com/stanidesis/live-wallpaper-quotes/issues
 *
 * Date: 08/15/2015
 */
public class LWQFirstLaunchTask extends AsyncTask<Void, String, Void> {

    Callback<List<Category>> fetchCategoriesCallback = new Callback<List<Category>>() {
        @Override
        public void onSuccess(List<Category> categories) {
            publishProgress(categories.size() + " categories fetched");
            if (categories.isEmpty()) {
                categories = Select.from(Category.class).list();
                if (categories.isEmpty()) {
                    publishProgress("Failed to find any categories");
                    return;
                }
            }
            // TODO not first?
            Category initialCategory = categories.get(0);
            for (Category category : categories) {
                if ("inspirational".equalsIgnoreCase(category.name)) {
                    initialCategory = category;
                    break;
                }
            }
            LWQPreferences.setQuoteCategoryPreference(initialCategory.name);
            LWQApplication.getQuoteController().fetchQuotes(initialCategory, fetchQuotesCallback);
        }

        @Override
        public void onError(String errorMessage, Throwable throwable) {
            EventBus.getDefault().post(FirstLaunchTaskEvent.failed(errorMessage, throwable));
        }
    };

    Callback<List<Quote>> fetchQuotesCallback = new Callback<List<Quote>>() {
        @Override
        public void onSuccess(List<Quote> quotes) {
            publishProgress(quotes.size() + " quotes fetched");
            LWQPreferences.setImageCategoryPreference(UnsplashManager.UnsplashCategory.NATURE.sqlName());
            LWQApplication.getWallpaperController().generateNewWallpaper();
        }

        @Override
        public void onError(String errorMessage, Throwable throwable) {
            EventBus.getDefault().post(FirstLaunchTaskEvent.failed(errorMessage, throwable));
        }
    };

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        EventBus.getDefault().register(this);
    }

    @Override
    protected Void doInBackground(Void... params) {
        if (!LWQPreferences.isFirstLaunch()) {
            return null;
        } else if (!LWQApplication.getWallpaperController().activeWallpaperExists()) {
            // Go through everything again
            LWQApplication.getQuoteController().fetchCategories(fetchCategoriesCallback);
        } else {
            LWQApplication.getWallpaperController().retrieveActiveWallpaper();
        }
        return null;
    }

    @Override
    protected void finalize() throws Throwable {
        EventBus.getDefault().unregister(this);
        super.finalize();
    }

    public void onEvent(WallpaperEvent wallpaperEvent) {
        if (wallpaperEvent.didFail()) {
            EventBus.getDefault().post(FirstLaunchTaskEvent.failed(
                    wallpaperEvent.getErrorMessage(),
                    wallpaperEvent.getThrowable()));
        } else if (wallpaperEvent.getStatus() == WallpaperEvent.Status.RETRIEVED_WALLPAPER) {
            LWQPreferences.setFirstLaunch(false);
            EventBus.getDefault().post(FirstLaunchTaskEvent.success());
        }
    }
}
