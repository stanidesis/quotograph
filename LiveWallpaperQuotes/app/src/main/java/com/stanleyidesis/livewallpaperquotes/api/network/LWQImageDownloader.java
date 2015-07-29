package com.stanleyidesis.livewallpaperquotes.api.network;

import android.net.Uri;
import android.util.Log;

import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by stanleyidesis on 7/28/15.
 */
public class LWQImageDownloader {
    public static LWQImageDownloader getInstance() {
        if (sImageDownloader == null) {
            sImageDownloader = new LWQImageDownloader();
        }
        return sImageDownloader;
    }

    public void fetchFeaturedImage(final LWQUnsplashManager.Category category, final int offset, final Callback<CloseableReference<CloseableImage>> callback) {
        submit(new Runnable() {
            @Override
            public void run() {
                Set<LWQUnsplashManager.Category> categorySet = new HashSet<>();
                categorySet.add(category);
                LWQUnsplashManager.getInstance().getPhotoURLs(1, categorySet, true, new Callback<List<String>>() {
                    @Override
                    public void onSuccess(List<String> photoURLs) {
                        String featuredPhotoURL = photoURLs.get(0);
                        if (photoURLs.size() > offset) {
                            featuredPhotoURL = photoURLs.get(offset);
                        }
                        Uri photoUri = Uri.parse(featuredPhotoURL);
                        ImageRequest request = ImageRequestBuilder
                                .newBuilderWithSource(photoUri)
                                .setLowestPermittedRequestLevel(ImageRequest.RequestLevel.FULL_FETCH)
                                .setProgressiveRenderingEnabled(false)
                                .build();
                        final DataSource<CloseableReference<CloseableImage>> closeableReferenceDataSource =
                                Fresco.getImagePipeline().fetchDecodedImage(request, scheduledExecutorService);
                        closeableReferenceDataSource.subscribe(new LWQDataSubscriber(callback), scheduledExecutorService);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        callback.onError(errorMessage);
                    }
                });
            }
        });
    }

    public void fetchImageAtURL(String url, Callback<CloseableReference<CloseableImage>> callback) {

    }

    private static LWQImageDownloader sImageDownloader;

    private ScheduledExecutorService scheduledExecutorService;

    private LWQImageDownloader() {
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    }

    private void submit(final Runnable runnable) {
        scheduledExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } catch (Exception e) {
                    Log.e(getClass().getSimpleName(), "Error in executor service task", e);
                }
            }
        });
    }

    class LWQDataSubscriber extends BaseDataSubscriber<CloseableReference<CloseableImage>> {

        Callback<CloseableReference<CloseableImage>> callback;

        LWQDataSubscriber(Callback<CloseableReference<CloseableImage>> callback) {
            this.callback = callback;
        }

        @Override
        protected void onNewResultImpl(DataSource<CloseableReference<CloseableImage>> dataSource) {
            if (!dataSource.isFinished()) {
                Log.v(getClass().getSimpleName(), "Not yet finished - this is just another progressive scan.");
                return;
            }

            CloseableReference<CloseableImage> imageReference = dataSource.getResult();
            if (imageReference != null) {
                try {
                    callback.onSuccess(imageReference);
                } finally {
                    CloseableReference.closeSafely(imageReference);
                }
            } else {
                callback.onError("No image, yet?");
            }
        }

        @Override
        protected void onFailureImpl(DataSource<CloseableReference<CloseableImage>> dataSource) {
            final Throwable failureCause = dataSource.getFailureCause();
            failureCause.printStackTrace();
            callback.onError(failureCause.getMessage());
        }
    }
}
