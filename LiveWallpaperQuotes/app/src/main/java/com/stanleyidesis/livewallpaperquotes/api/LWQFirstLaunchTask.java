package com.stanleyidesis.livewallpaperquotes.api;

import android.os.AsyncTask;

import com.orm.query.Select;
import com.stanleyidesis.livewallpaperquotes.LWQApplication;
import com.stanleyidesis.livewallpaperquotes.LWQPreferences;
import com.stanleyidesis.livewallpaperquotes.api.db.Category;
import com.stanleyidesis.livewallpaperquotes.api.db.Quote;

import java.util.List;
import java.util.Random;

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
            // TODO not random?
            final Category randomCategory = categories.get(new Random().nextInt(categories.size()));
            LWQApplication.getQuoteController().fetchQuotes(randomCategory, fetchQuotesCallback);
        }

        @Override
        public void onError(String errorMessage) {
            publishProgress(errorMessage);
        }
    };

    Callback<List<Quote>> fetchQuotesCallback = new Callback<List<Quote>>() {
        @Override
        public void onSuccess(List<Quote> quotes) {
            publishProgress(quotes.size() + " quotes fetched");
            LWQApplication.getWallpaperController().retrieveActiveWallpaper(retrieveWallpaperCallback, true);
        }

        @Override
        public void onError(String errorMessage) {}
    };

    Callback<Boolean> retrieveWallpaperCallback = new Callback<Boolean>() {
        @Override
        public void onSuccess(Boolean aBoolean) {
            publishProgress("New wallpaper retrieved");
            LWQPreferences.setFirstLaunch(false);
        }

        @Override
        public void onError(String errorMessage) {}
    };

    @Override
    protected Void doInBackground(Void... params) {
        if (!LWQPreferences.isFirstLaunch()) {
            return null;
        } else if (!LWQApplication.getWallpaperController().activeWallpaperExists()) {
            // Go through everything again
            LWQApplication.getQuoteController().fetchCategories(fetchCategoriesCallback);
        } else {
            LWQApplication.getWallpaperController().retrieveActiveWallpaper(retrieveWallpaperCallback, false);
        }
        return null;
    }
}
