package com.stanleyidesis.livewallpaperquotes.ui.activity;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.facebook.drawee.view.SimpleDraweeView;
import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.photos.PhotoList;
import com.stanleyidesis.livewallpaperquotes.R;
import com.stanleyidesis.livewallpaperquotes.api.db.Quote;
import com.stanleyidesis.livewallpaperquotes.api.network.Callback;
import com.stanleyidesis.livewallpaperquotes.api.network.FlickrManager;

import java.util.Iterator;

/**
 * Created by stanleyidesis on 7/11/15.
 */
public class LWQSettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lwq_settings);
        FlickrManager.getInstance(this).searchFlickr(new String[]{"landmark", "scenic"}, new Callback<PhotoList>() {
            @Override
            public void onSuccess(final PhotoList returnValue) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < returnValue.size(); i++) {
                            final Photo photo = returnValue.get(i);
                            Log.v(getClass().getSimpleName(), photo.getLarge2048Url());
                        }
                        Uri uri = Uri.parse(returnValue.get(0).getLarge2048Url());
                        SimpleDraweeView draweeView = (SimpleDraweeView) findViewById(R.id.my_image_view);
                        draweeView.setImageURI(uri);
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {

            }
        });
    }

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