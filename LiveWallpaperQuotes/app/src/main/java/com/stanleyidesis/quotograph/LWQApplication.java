package com.stanleyidesis.quotograph;

import android.app.Activity;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.StrictMode;
import android.support.annotation.NonNull;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.orm.SugarApp;
import com.stanleyidesis.quotograph.api.LWQError;
import com.stanleyidesis.quotograph.api.controller.LWQImageControllerHelper;
import com.stanleyidesis.quotograph.api.controller.LWQLoggerHelper;
import com.stanleyidesis.quotograph.api.event.IabPurchaseEvent;
import com.stanleyidesis.quotograph.api.receiver.LWQReceiver;
import com.stanleyidesis.quotograph.api.service.LWQWallpaperService;
import com.stanleyidesis.quotograph.billing.util.IabBroadcastReceiver;
import com.stanleyidesis.quotograph.billing.util.IabHelper;
import com.stanleyidesis.quotograph.billing.util.IabLic;
import com.stanleyidesis.quotograph.billing.util.IabResult;
import com.stanleyidesis.quotograph.billing.util.Inventory;
import com.stanleyidesis.quotograph.billing.util.Purchase;
import com.stanleyidesis.quotograph.billing.util.SkuDetails;
import com.stanleyidesis.quotograph.ui.activity.LWQSettingsActivity;
import com.stanleyidesis.quotograph.ui.adapter.ImageMultiSelectAdapter;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.fabric.sdk.android.Fabric;
import io.fabric.sdk.android.services.concurrency.AsyncTask;

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
 * LWQApplication.java
 * @author Stanley Idesis
 *
 * From Quotograph
 * https://github.com/stanidesis/quotograph
 *
 * Please report any issues
 * https://github.com/stanidesis/quotograph/issues
 *
 * Date: 07/11/2015
 */
public class LWQApplication extends SugarApp implements IabHelper.OnIabSetupFinishedListener,
        IabBroadcastReceiver.IabBroadcastListener,
        IabHelper.QueryInventoryFinishedListener,
        IabHelper.OnIabPurchaseFinishedListener {

    /**
     * Performs asynchronous work to prepare any
     * application components that require disk writes
     * and network access during initialization.
     */
    private class LoadApplicationTask extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            // Bug tracking
            CrashlyticsCore core = new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build();
            Fabric.with(LWQApplication.get(), new Crashlytics.Builder().core(core).build());

            // ImageLoader Requires Disk Access to Initialize
            LWQImageControllerHelper.get();

            // Remote Config
            FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                    .setDeveloperModeEnabled(BuildConfig.DEBUG)
                    .build();
            FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
            firebaseRemoteConfig.setConfigSettings(configSettings);
            firebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);

            // Firebase Analytics
            FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(LWQApplication.get());
            firebaseAnalytics.setAnalyticsCollectionEnabled(
                            firebaseRemoteConfig.getBoolean(
                                    RemoteConfigConst.FIREBASE_ANALYTICS)
                                    && !BuildConfig.DEBUG);
            firebaseAnalytics.setMinimumSessionDuration(5000);

            // AdMob
            MobileAds.initialize(getApplicationContext(), getString(R.string.admob_app_id));
            MobileAds.setAppMuted(true);

            // Enable
            setComponentsEnabled(!LWQPreferences.isFirstLaunch());

            // Cache images if FTUETask completed
            if (!LWQPreferences.isFirstLaunch()) {
                cacheRemoteImageAssets();
            }
            return null;
        }
    }

    static LWQApplication sApplication;

    IabHelper iabHelper;
    IabBroadcastReceiver iabBroadcastReceiver;
    Set<IabConst.Product> ownedProducts = new HashSet<>();
    Map<IabConst.Product, SkuDetails> productDetails = new HashMap<>();

    @Override
    public void onCreate() {
        if (!BuildConfig.TEST_ADS) setStrictMode(BuildConfig.DEBUG);

        super.onCreate();
        sApplication = this;

        // Billing
        iabHelper = new IabHelper(this, IabLic.retrieveLic());
        iabHelper.enableDebugLogging(BuildConfig.DEBUG, getString(R.string.app_name) + ".BILLING");
        iabHelper.startSetup(this);

        // Asynch Application Dependencies
        new LoadApplicationTask().execute();
    }

    @Override
    protected void attachBaseContext(Context base) {
//        MultiDex.install(this);
        super.attachBaseContext(base);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if (getIabHelper() != null) {
            getIabHelper().dispose();
        }
    }

    public static LWQApplication get() {
        return sApplication;
    }

    public static IabHelper getIabHelper() {
        return get().iabHelper;
    }

    public static void fetchRemoteConfig() {
        FirebaseRemoteConfig.getInstance().fetch()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            return;
                        }
                        FirebaseRemoteConfig.getInstance().activateFetched();
                    }
                });
    }

    public static boolean ownsProduct(IabConst.Product product) {
        return get().ownedProducts.contains(product);
    }

    public static SkuDetails getProductDetails(IabConst.Product product) {
        return get().productDetails.get(product);
    }

    public static void purchaseProduct(Activity activity, IabConst.Product product) {
        getIabHelper().launchPurchaseFlow(activity, product.sku,
                IabConst.PURCHASE_REQUEST_CODE, get());
    }

    public static void setStrictMode(boolean enabled) {
        if (enabled) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());
        } else {
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.LAX);
            StrictMode.setVmPolicy(StrictMode.VmPolicy.LAX);
        }
    }

    public static boolean isWallpaperActivated() {
        final WallpaperManager wallpaperManager = WallpaperManager.getInstance(sApplication);
        final WallpaperInfo wallpaperInfo = wallpaperManager.getWallpaperInfo();
        boolean active = wallpaperInfo != null && sApplication.getPackageName().equalsIgnoreCase(wallpaperInfo.getPackageName());
        LWQLoggerHelper.get().logWallpaperActive(active);
        return active;
    }

    public static void setComponentsEnabled(boolean enabled) {
        ComponentName receiver = new ComponentName(LWQApplication.get(), LWQReceiver.class);
        ComponentName wallpaperService = new ComponentName(LWQApplication.get(), LWQWallpaperService.class);
        ComponentName settingsActivity = new ComponentName(LWQApplication.get(), LWQSettingsActivity.class);
        PackageManager pm = LWQApplication.get().getPackageManager();
        pm.setComponentEnabledSetting(receiver,
                enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                        : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(wallpaperService,
                enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                        : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(settingsActivity,
                enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                        : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    public static int getVersionCode() {
        try {
            return get().getPackageManager()
                    .getPackageInfo(get().getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Download additional images before the user needs to see them.
     * This is critical when displaying the store and the image selection dialog.
     */
    public static void cacheRemoteImageAssets() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (ImageMultiSelectAdapter.KnownUnsplashCategories category :
                        ImageMultiSelectAdapter.KnownUnsplashCategories.values()) {
                    if (ImageLoader.getInstance().getDiskCache().get(category.imgSource) == null) {
                        ImageLoader.getInstance().loadImage(category.imgSource, null);
                    }
                }
            }
        }).start();
    }

    ////////////////////////////////////////////////////////////
    // IAB Interfaces
    ////////////////////////////////////////////////////////////

    @Override
    public void onIabSetupFinished(IabResult result) {
        // Check for failure
        if (result.isFailure()) {
            LWQError.log(result.getMessage());
            return;
        }
        // If we've detached, return
        if (iabHelper == null) {
            return;
        }
        // Register a receiver
        iabBroadcastReceiver = new IabBroadcastReceiver(this);
        IntentFilter broadcastFilter = new IntentFilter(IabBroadcastReceiver.ACTION);
        registerReceiver(iabBroadcastReceiver, broadcastFilter);
        // Query inventory
        receivedBroadcast();
    }

    @Override
    public void receivedBroadcast() {
        // Something has changed, refresh inventory
        List<String> skus = new ArrayList<>(IabConst.Product.values().length);
        for (IabConst.Product product : IabConst.Product.values()) {
            skus.add(product.sku);
        }
        getIabHelper().queryInventoryAsync(true, skus, this);
    }

    @Override
    public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
        if (result.isFailure()) {
            LWQError.log(result.getMessage());
            return;
        }
        ownedProducts.clear();
        for (IabConst.Product product : IabConst.Product.values()) {
            if (inventory.hasPurchase(product.sku)) {
                ownedProducts.add(product);
            }
            productDetails.put(product, inventory.getSkuDetails(product.sku));
        }
    }

    @Override
    public void onIabPurchaseFinished(IabResult result, Purchase info) {
        if (getIabHelper() == null) {
            return;
        }
        if (result.isFailure()) {
            EventBus.getDefault().post(IabPurchaseEvent.failed(result.getMessage()));
            return;
        }
        String purchasedSku = info.getSku();
        IabConst.Product purchased = null;
        for (IabConst.Product product : IabConst.Product.values()) {
            if (product.sku.equalsIgnoreCase(purchasedSku)) {
                purchased = product;
                ownedProducts.add(product);
            }
        }
        if (purchased == null) {
            // Huh?
            return;
        }
        EventBus.getDefault().post(IabPurchaseEvent.success(purchased));
    }
}
