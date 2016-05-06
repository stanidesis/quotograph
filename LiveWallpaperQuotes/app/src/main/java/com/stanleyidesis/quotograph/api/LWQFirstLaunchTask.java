package com.stanleyidesis.quotograph.api;

import android.os.AsyncTask;

import com.orm.SugarRecord;
import com.orm.query.Select;
import com.stanleyidesis.quotograph.LWQApplication;
import com.stanleyidesis.quotograph.LWQPreferences;
import com.stanleyidesis.quotograph.R;
import com.stanleyidesis.quotograph.api.db.Category;
import com.stanleyidesis.quotograph.api.db.Playlist;
import com.stanleyidesis.quotograph.api.db.PlaylistCategory;
import com.stanleyidesis.quotograph.api.db.UnsplashCategory;
import com.stanleyidesis.quotograph.api.event.FirstLaunchTaskEvent;
import com.stanleyidesis.quotograph.api.event.FirstLaunchTaskUpdate;
import com.stanleyidesis.quotograph.api.event.WallpaperEvent;

import java.util.List;

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
 * LWQFirstLaunchTask.java
 * @author Stanley Idesis
 *
 * From Quotograph
 * https://github.com/stanidesis/quotograph
 *
 * Please report any issues
 * https://github.com/stanidesis/quotograph/issues
 *
 * Date: 08/15/2015
 */
public class LWQFirstLaunchTask extends AsyncTask<Void, String, Void> {

    Callback<List<String>> fetchImageCategoriesCallback = new Callback<List<String>>() {
        @Override
        public void onSuccess(List<String> strings) {
            // Only one category selected by default
            UnsplashCategory category = SugarRecord.listAll(UnsplashCategory.class).get(0);
            category.active = true;
            category.save();
            LWQApplication.getQuoteController().fetchCategories(fetchQuoteCategoriesCallback);
        }

        @Override
        public void onError(LWQError error) {
            EventBus.getDefault().post(FirstLaunchTaskEvent.failed(error));
        }
    };

    Callback<List<Category>> fetchQuoteCategoriesCallback = new Callback<List<Category>>() {
        @Override
        public void onSuccess(List<Category> categories) {
            if (categories.isEmpty()) {
                categories = Select.from(Category.class).list();
                if (categories.isEmpty()) {
                    return;
                }
            }

            Playlist defaultPlaylist = new Playlist(LWQApplication.get().getString(R.string.app_name), true);
            defaultPlaylist.save();

            Category initialCategory = categories.get(0);
            for (Category category : categories) {
                if ("inspirational".equalsIgnoreCase(category.name)) {
                    initialCategory = category;
                    break;
                }
            }
            new PlaylistCategory(defaultPlaylist, initialCategory).save();

            publishProgress("Making your first Quotograph…");
            LWQApplication.getWallpaperController().generateNewWallpaper();
        }

        @Override
        public void onError(LWQError error) {
            EventBus.getDefault().post(FirstLaunchTaskEvent.failed(error));
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
            publishProgress("Fetching quote categories…");
            // Go through everything again
            LWQApplication.getWallpaperController().fetchBackgroundCategories(fetchImageCategoriesCallback);
        } else {
            publishProgress("Retrieving artwork…");
            LWQApplication.getWallpaperController().retrieveActiveWallpaper();
        }
        return null;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        EventBus.getDefault().post(FirstLaunchTaskEvent.failed(LWQError.create("Cancelled by user")));
    }

    @Override
    protected void finalize() throws Throwable {
        EventBus.getDefault().unregister(this);
        super.finalize();
    }

    void publishProgress(int stringId) {
        publishProgress(LWQApplication.get().getString(stringId));
    }

    void publishProgress(String progress) {
        EventBus.getDefault().post(FirstLaunchTaskUpdate.newUpdate(progress));
    }

    public void onEvent(WallpaperEvent wallpaperEvent) {
        if (wallpaperEvent.didFail()) {
            EventBus.getDefault().post(FirstLaunchTaskEvent.failed(wallpaperEvent.getError()));
        } else if (wallpaperEvent.getStatus() == WallpaperEvent.Status.RETRIEVED_WALLPAPER) {
            publishProgress("Setup complete!");
            LWQPreferences.setFirstLaunch(false);
            LWQApplication.setComponentsEnabled(true);
            EventBus.getDefault().post(FirstLaunchTaskEvent.success());
        }
    }
}
