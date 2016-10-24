package com.stanleyidesis.quotograph.ui.adapter;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.percent.PercentRelativeLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.stanleyidesis.quotograph.AdMobUtils;
import com.stanleyidesis.quotograph.AnalyticsUtils;
import com.stanleyidesis.quotograph.R;

import butterknife.Bind;
import butterknife.ButterKnife;

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
 * AdViewHolder.java
 * @author Stanley Idesis
 *
 * From Quotograph
 * https://github.com/stanidesis/quotograph
 *
 * Please report any issues
 * https://github.com/stanidesis/quotograph/issues
 *
 * Date: 10/22/2016
 */

class AdViewHolder extends RecyclerView.ViewHolder {

    @Bind(R.id.prl_ad_container)
    PercentRelativeLayout nativeAdHolder;
    private NativeExpressAdView nativeExpressAdView;

    private int finalHeight;
    private boolean runOnce = true;
    private String configColorKey;
    private int adMobIdResource;
    private int maxAttempts = 10;

    AdViewHolder(Context context, String configColorKey, int adMobIdResource) {
        super(LayoutInflater.from(context).inflate(R.layout.ad_item, null));
        ButterKnife.bind(this, itemView);
        this.configColorKey = configColorKey;
        this.adMobIdResource = adMobIdResource;
        nativeExpressAdView = new NativeExpressAdView(itemView.getContext());
        AdListener adListener = new AdListener() {
            @Override
            public void onAdLoaded() {
                if (AdViewHolder.this.nativeAdHolder.getLayoutParams().height > 0) {
                    return;
                }
                itemView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                itemView.requestLayout();
                ValueAnimator valueAnimator = ValueAnimator.ofInt(0, finalHeight).setDuration(200);
                valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        AdViewHolder.this.nativeAdHolder.getLayoutParams().height = (int) valueAnimator.getAnimatedValue();
                        AdViewHolder.this.nativeAdHolder.requestLayout();
                    }
                });
                valueAnimator.start();
            }

            @Override
            public void onAdOpened() {
            }

            @Override
            public void onAdLeftApplication() {
                AnalyticsUtils.trackEvent(
                        AnalyticsUtils.CATEGORY_ADS,
                        AnalyticsUtils.ACTION_CLICKTHROUGH,
                        nativeExpressAdView.getAdUnitId());
            }

            @Override
            public void onAdFailedToLoad(int i) {
                if (--maxAttempts <= 0) {
                    return;
                }
                requestAd();
            }

            @Override
            public void onAdClosed() {
            }
        };
        nativeExpressAdView.setAdListener(adListener);
        nativeAdHolder.addView(nativeExpressAdView, PercentRelativeLayout.LayoutParams.WRAP_CONTENT,
                PercentRelativeLayout.LayoutParams.WRAP_CONTENT);
        adSetup();
    }

    private void adSetup() {
        nativeAdHolder.post(new Runnable() {
            @Override
            public void run() {
                int maxHeightPixels = nativeAdHolder.getResources().getDimensionPixelSize(R.dimen.admob_ad_height);
                // Set the ad target size
                int maxWidthInt = nativeAdHolder.getWidth();
                maxWidthInt = (int) (maxWidthInt * 1f / itemView.getResources().getDisplayMetrics().density);
                int maxHeightInt = (int) (maxHeightPixels * 1f / itemView.getResources().getDisplayMetrics().density);
                nativeExpressAdView.setAdSize(new AdSize(maxWidthInt, maxHeightInt));
                nativeExpressAdView.setAdUnitId(itemView.getContext().getString(adMobIdResource));

                // Setup animation
                finalHeight = maxHeightPixels;
            }
        });
    }

    private void requestAd() {
        // Start the request
        nativeExpressAdView.loadAd(AdMobUtils.buildRequest(itemView.getContext()));
    }

    public void update() {
        itemView.setBackgroundColor(
                Integer.parseInt(
                        FirebaseRemoteConfig.getInstance()
                                .getString(configColorKey), 16)
        );
        if (runOnce) {
            nativeAdHolder.post(new Runnable() {
                @Override
                public void run() {
                    requestAd();
                }
            });
            runOnce = false;
        }
    }
}
