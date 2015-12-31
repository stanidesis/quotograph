package com.stanleyidesis.livewallpaperquotes;

import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.util.Log;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.orm.SugarApp;
import com.orm.query.Condition;
import com.orm.query.Select;
import com.stanleyidesis.livewallpaperquotes.api.controller.LWQImageController;
import com.stanleyidesis.livewallpaperquotes.api.controller.LWQImageControllerFrescoImpl;
import com.stanleyidesis.livewallpaperquotes.api.controller.LWQNotificationController;
import com.stanleyidesis.livewallpaperquotes.api.controller.LWQNotificationControllerImpl;
import com.stanleyidesis.livewallpaperquotes.api.controller.LWQQuoteController;
import com.stanleyidesis.livewallpaperquotes.api.controller.LWQQuoteControllerBrainyQuoteImpl;
import com.stanleyidesis.livewallpaperquotes.api.controller.LWQWallpaperController;
import com.stanleyidesis.livewallpaperquotes.api.controller.LWQWallpaperControllerUnsplashImpl;
import com.stanleyidesis.livewallpaperquotes.api.db.Author;
import com.stanleyidesis.livewallpaperquotes.api.db.Category;
import com.stanleyidesis.livewallpaperquotes.api.db.Quote;
import com.stanleyidesis.livewallpaperquotes.api.network.NetworkConnectionListener;
import com.stanleyidesis.livewallpaperquotes.api.receiver.LWQReceiver;
import com.stanleyidesis.livewallpaperquotes.api.service.LWQWallpaperService;

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
 * LWQApplication.java
 * @author Stanley Idesis
 *
 * From Live-Wallpaper-Quotes
 * https://github.com/stanidesis/live-wallpaper-quotes
 *
 * Please report any issues
 * https://github.com/stanidesis/live-wallpaper-quotes/issues
 *
 * Date: 07/11/2015
 */
public class LWQApplication extends SugarApp {

    static LWQApplication sApplication;

    LWQWallpaperController wallpaperController;
    LWQImageController imageController;
    LWQQuoteController quoteController;
    LWQNotificationController notificationController;
    NetworkConnectionListener networkConnectionListener;

    @Override
    public void onCreate() {
        super.onCreate();
        Fresco.initialize(this);

        sApplication = this;
        wallpaperController = new LWQWallpaperControllerUnsplashImpl();
        imageController = new LWQImageControllerFrescoImpl();
        quoteController = new LWQQuoteControllerBrainyQuoteImpl();
        notificationController = new LWQNotificationControllerImpl();
        networkConnectionListener = new NetworkConnectionListener(this);

        setComponentsEnabled(isWallpaperActivated());
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

    public static LWQQuoteController getQuoteController() {
        return sApplication.quoteController;
    }

    public static LWQNotificationController getNotificationController() {
        return sApplication.notificationController;
    }

    public static NetworkConnectionListener getNetworkConnectionListener() {
        return sApplication.networkConnectionListener;
    }

    public static boolean isWallpaperActivated() {
        final WallpaperManager wallpaperManager = WallpaperManager.getInstance(sApplication);
        final WallpaperInfo wallpaperInfo = wallpaperManager.getWallpaperInfo();
        return wallpaperInfo != null && sApplication.getPackageName().equalsIgnoreCase(wallpaperInfo.getPackageName());
    }

    public static void setComponentsEnabled(boolean enabled) {
        ComponentName receiver = new ComponentName(LWQApplication.get(), LWQReceiver.class);
        ComponentName wallpaperService = new ComponentName(LWQApplication.get(), LWQWallpaperService.class);
        PackageManager pm = LWQApplication.get().getPackageManager();
        pm.setComponentEnabledSetting(receiver,
                enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                        : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(wallpaperService,
                enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                        : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);

    }

    private void populateDefaults() {
        final String[] defaultCategoryArray = getResources().getStringArray(R.array.default_category_titles);
        final TypedArray defaultCategoryQuoteMap = getResources().obtainTypedArray(R.array.default_category_quote_map);
        final TypedArray defaultCategoryAuthorMap = getResources().obtainTypedArray(R.array.default_category_author_map);
        for (int i = 0; i < defaultCategoryArray.length; i++) {
            Category defaultCategory = new Category(defaultCategoryArray[i], Category.Source.DEFAULT);
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
                    defaultAuthor = new Author(defaultCategoryAuthors[j], false);
                    defaultAuthor.save();
                }
                new Quote(defaultCategoryQuotes[j], defaultAuthor, defaultCategory).save();
            }
        }
        defaultCategoryQuoteMap.recycle();
        defaultCategoryAuthorMap.recycle();
    }
}
