package com.stanleyidesis.livewallpaperquotes.api.network;

import android.content.Context;
import android.util.Log;

import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.FlickrException;
import com.googlecode.flickrjandroid.photos.PhotoList;
import com.googlecode.flickrjandroid.photos.PhotosInterface;
import com.googlecode.flickrjandroid.photos.SearchParameters;
import com.stanleyidesis.livewallpaperquotes.R;

import org.json.JSONException;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by stanleyidesis on 7/27/15.
 */
public class FlickrManager {

    private static FlickrManager sFlickrManager;

    public static final FlickrManager getInstance(Context context) {
        if (sFlickrManager == null) {
            sFlickrManager = new FlickrManager(context);
        }
        return sFlickrManager;
    }

    Flickr flickr;
    ScheduledExecutorService executorService;

    FlickrManager(Context context) {
        flickr = new Flickr(context.getString(R.string.flickr_api_key));
        executorService = Executors.newSingleThreadScheduledExecutor();
    }

    void execute(final Runnable runnable) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } catch (Exception e) {
                    Log.e(FlickrManager.this.getClass().getSimpleName(), "Interrupted Execution", e);
                }
            }
        });
    }

    public void searchFlickr(final String[] tags, final Callback<PhotoList> photoListCallback) {
        execute(new Runnable() {
            @Override
            public void run() {
                final SearchParameters searchParameters = new SearchParameters();
                try {
                    searchParameters.setTags(tags);
                    searchParameters.setSort(SearchParameters.INTERESTINGNESS_DESC);
                    searchParameters.setAccuracy(11);
                    searchParameters.setLatitude("37.7833");
                    searchParameters.setLongitude("-122.4167");
                    final PhotosInterface photosInterface = flickr.getPhotosInterface();
                    final PhotoList photos = photosInterface.search(searchParameters, 200, 1);
                    photoListCallback.onSuccess(photos);
                } catch (FlickrException e) {
                    e.printStackTrace();
                    photoListCallback.onError(e.getErrorMessage());
                } catch (JSONException e) {
                    e.printStackTrace();
                    photoListCallback.onError(e.getMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                    photoListCallback.onError(e.getMessage());
                }
            }
        });
    }
}
