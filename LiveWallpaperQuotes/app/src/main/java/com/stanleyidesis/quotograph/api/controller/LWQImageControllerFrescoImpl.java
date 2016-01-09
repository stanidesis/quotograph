package com.stanleyidesis.quotograph.api.controller;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.image.CloseableBitmap;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.stanleyidesis.quotograph.api.Callback;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by stanleyidesis on 7/28/15.
 */
public class LWQImageControllerFrescoImpl implements LWQImageController {

    Map<String, CloseableReference<CloseableImage>> imageCache;
    Executor scheduledExecutor;

    public LWQImageControllerFrescoImpl() {
        imageCache = new HashMap<>();
        scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public synchronized boolean isCached(String uri) {
        return imageCache.containsKey(uri);
    }

    @Override
    public synchronized void retrieveBitmap(String uri, Callback<Bitmap> callback) {
        if (isCached(uri)) {
            final CloseableReference<CloseableImage> closeableImageCloseableReference = imageCache.get(uri);
            callback.onSuccess(((CloseableBitmap)closeableImageCloseableReference.get()).getUnderlyingBitmap());
            return;
        }
        Uri photoUri = Uri.parse(uri);
        ImageRequest request = ImageRequestBuilder
                .newBuilderWithSource(photoUri)
                .setLowestPermittedRequestLevel(ImageRequest.RequestLevel.FULL_FETCH)
                .setProgressiveRenderingEnabled(false)
                .build();
        final DataSource<CloseableReference<CloseableImage>> closeableReferenceDataSource =
                Fresco.getImagePipeline().fetchDecodedImage(request, scheduledExecutor);
        closeableReferenceDataSource.subscribe(new LWQDataSubscriber(uri, callback), scheduledExecutor);
    }

    @Override
    public synchronized void clearBitmap(String uri) {
        if (isCached(uri)) {
            final CloseableReference<CloseableImage> removedImage = imageCache.remove(uri);
            Uri fullUri = Uri.parse(uri);
            Fresco.getImagePipeline().evictFromMemoryCache(fullUri);
            CloseableReference.closeSafely(removedImage);
        }
    }

    Map<String, CloseableReference<CloseableImage>> getCache() {
        return imageCache;
    }

    class LWQDataSubscriber extends BaseDataSubscriber<CloseableReference<CloseableImage>> {

        String uri;
        Callback<Bitmap> callback;

        LWQDataSubscriber(String uri, Callback<Bitmap> callback) {
            this.uri = uri;
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
                    callback.onSuccess(((CloseableBitmap)imageReference.get()).getUnderlyingBitmap());
                } finally {
                    clearBitmap(uri);
                    getCache().put(uri, imageReference);
                }
            } else {
                callback.onError("Failed to recover the image", null);
            }
        }

        @Override
        protected void onFailureImpl(DataSource<CloseableReference<CloseableImage>> dataSource) {
            final Throwable failureCause = dataSource.getFailureCause();
            if (failureCause != null) {
                failureCause.printStackTrace();
                callback.onError(failureCause.getMessage(), failureCause);
            } else {
                callback.onError("Unknown", null);
            }
        }
    }
}
