package com.stanleyidesis.livewallpaperquotes.ui.activity;

import android.app.Activity;
import android.util.Log;

import com.stanleyidesis.livewallpaperquotes.api.db.Quote;
import com.stanleyidesis.livewallpaperquotes.api.network.Callback;
import com.stanleyidesis.livewallpaperquotes.api.network.UnsplashManager;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by stanleyidesis on 7/11/15.
 */
public class LWQSettingsActivity extends Activity {

    @Override
    protected void onResume() {
        super.onResume();
        // Test, works
        final Iterator<Quote> allQuotes = Quote.findAll(Quote.class);
        while (allQuotes.hasNext()) {
            final Quote next = allQuotes.next();
            Log.v(getClass().getSimpleName(), "Found quote: \"" + next.text + "\" by " + next.author.name);
        }
        Set<UnsplashManager.Category> categorySet = new HashSet<>();
        categorySet.add(UnsplashManager.Category.NATURE);
        UnsplashManager.getInstance(this).getPhotoURLs(1, categorySet, true, new Callback<List<String>>() {
            @Override
            public void onSuccess(List<String> strings) {
                for (String url : strings) {
                    Log.v(LWQSettingsActivity.this.getClass().getSimpleName(), "URL: " + url);
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.v(LWQSettingsActivity.this.getClass().getSimpleName(), "Error: " + errorMessage);
            }
        });
    }
}