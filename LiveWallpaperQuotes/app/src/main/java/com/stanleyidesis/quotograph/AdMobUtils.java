package com.stanleyidesis.quotograph;

import android.content.Context;

import com.google.android.gms.ads.AdRequest;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

public final class AdMobUtils {


    public static boolean adsEnabled() {
        if (BuildConfig.DEBUG) {
            return BuildConfig.TEST_ADS;
        }
        // Check if we've disabled ads remotely or if the user purchased 'remove_ads'
        return FirebaseRemoteConfig.getInstance().getBoolean(RemoteConfigConst.ADMOB)
                && !LWQApplication.ownsProduct(IabConst.Product.REMOVE_ADS);
    }

    public static AdRequest buildRequest(Context context) {
        return new AdRequest.Builder()
                .addTestDevice(context.getString(R.string.admob_nexus_5x_device_id))
                .addTestDevice(context.getString(R.string.admob_moto_g_device_id))
                .addKeyword("personalization")
                .addKeyword("personalize")
                .addKeyword("android")
                .addKeyword("google android")
                .addKeyword("android application")
                .addKeyword("android app")
                .addKeyword("quotes")
                .addKeyword("famous quotes")
                .addKeyword("inspiration")
                .addKeyword("inspirational")
                .addKeyword("motivation")
                .addKeyword("motivational")
                .build();
    }
}
