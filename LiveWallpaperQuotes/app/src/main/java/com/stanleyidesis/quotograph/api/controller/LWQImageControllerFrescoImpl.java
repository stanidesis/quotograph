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

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by stanleyidesis on 7/28/15.
 */
public class LWQImageControllerFrescoImpl implements LWQImageController {

    Executor scheduledExecutor;
    Set<CloseableReference<CloseableImage>> localCache;

    public LWQImageControllerFrescoImpl() {
        scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        localCache = new HashSet<>();
    }

    @Override
    public synchronized boolean isCached(String uri) {
        return Fresco.getImagePipeline().isInBitmapMemoryCache(Uri.parse(uri));
    }

    @Override
    public synchronized void retrieveBitmap(String uri, Callback<Bitmap> callback) {
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
            Fresco.getImagePipeline().evictFromCache(Uri.parse(uri));
        }
    }

    @Override
    public void clearCache() {
        for (CloseableReference<CloseableImage> image : localCache){
            image.close();
        }
        localCache.clear();
        Fresco.getImagePipeline().clearCaches();
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
            localCache.add(imageReference);
            if (imageReference != null) {
                callback.onSuccess(((CloseableBitmap)imageReference.get()).getUnderlyingBitmap());
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
