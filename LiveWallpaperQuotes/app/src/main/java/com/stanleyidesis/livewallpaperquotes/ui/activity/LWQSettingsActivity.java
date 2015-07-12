package com.stanleyidesis.livewallpaperquotes.ui.activity;

import android.app.Activity;
import android.util.Log;

import com.stanleyidesis.livewallpaperquotes.api.db.Quote;

import java.util.Iterator;

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
    }
}
