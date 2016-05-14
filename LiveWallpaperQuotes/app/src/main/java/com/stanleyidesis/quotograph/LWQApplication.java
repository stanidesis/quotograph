package com.stanleyidesis.quotograph;

import android.app.Activity;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.IntentFilter;
import android.content.pm.PackageManager;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.orm.SugarApp;
import com.stanleyidesis.quotograph.api.LWQError;
import com.stanleyidesis.quotograph.api.controller.LWQImageController;
import com.stanleyidesis.quotograph.api.controller.LWQImageControllerUIL;
import com.stanleyidesis.quotograph.api.controller.LWQLogger;
import com.stanleyidesis.quotograph.api.controller.LWQLoggerCrashlyticsImpl;
import com.stanleyidesis.quotograph.api.controller.LWQNotificationController;
import com.stanleyidesis.quotograph.api.controller.LWQNotificationControllerImpl;
import com.stanleyidesis.quotograph.api.controller.LWQQuoteController;
import com.stanleyidesis.quotograph.api.controller.LWQQuoteControllerBrainyQuoteImpl;
import com.stanleyidesis.quotograph.api.controller.LWQWallpaperController;
import com.stanleyidesis.quotograph.api.controller.LWQWallpaperControllerUnsplashImpl;
import com.stanleyidesis.quotograph.api.event.IabPurchaseEvent;
import com.stanleyidesis.quotograph.api.network.NetworkConnectionListener;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.greenrobot.event.EventBus;
import io.fabric.sdk.android.Fabric;

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

    static LWQApplication sApplication;

    LWQWallpaperController wallpaperController;
    LWQImageController imageController;
    LWQQuoteController quoteController;
    LWQNotificationController notificationController;
    LWQLogger logger;
    NetworkConnectionListener networkConnectionListener;
    Tracker tracker;
    IabHelper iabHelper;
    IabBroadcastReceiver iabBroadcastReceiver;
    Set<IabConst.Product> ownedProducts = new HashSet<>();
    Map<IabConst.Product, SkuDetails> productDetails = new HashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;

        // Bug tracking
        CrashlyticsCore core = new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build();
        Fabric.with(this, new Crashlytics.Builder().core(core).build());

        // Analytics
        getDefaultTracker().enableAdvertisingIdCollection(true);
        getDefaultTracker().enableAutoActivityTracking(true);

        // Billing
        iabHelper = new IabHelper(this, IabLic.retrieveLic());
        iabHelper.enableDebugLogging(BuildConfig.DEBUG, getString(R.string.app_name) + ".BILLING");
        iabHelper.startSetup(this);

        // Application controllers
        logger = new LWQLoggerCrashlyticsImpl();
        wallpaperController = new LWQWallpaperControllerUnsplashImpl();
        imageController = new LWQImageControllerUIL();
        quoteController = new LWQQuoteControllerBrainyQuoteImpl();
        notificationController = new LWQNotificationControllerImpl();
        networkConnectionListener = new NetworkConnectionListener(this);

        // Enable
        setComponentsEnabled(!LWQPreferences.isFirstLaunch());

        // Cache images if FTUETask completed
        if (!LWQPreferences.isFirstLaunch()) {
            cacheRemoteImageAssets();
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if (getIabHelper() != null) {
            getIabHelper().dispose();
        }
    }

    synchronized public Tracker getDefaultTracker() {
        if (tracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            tracker = analytics.newTracker(R.xml.global_tracker);
        }
        return tracker;
    }

    public static LWQApplication get() {
        return sApplication;
    }

    public static LWQWallpaperController getWallpaperController() {
        return sApplication.wallpaperController;
    }

    public static LWQImageController getImageController() {
        return sApplication.imageController;
    }

    public static LWQQuoteController getQuoteController() {
        return sApplication.quoteController;
    }

    public static LWQNotificationController getNotificationController() {
        return sApplication.notificationController;
    }

    public static NetworkConnectionListener getNetworkConnectionListener() {
        return sApplication.networkConnectionListener;
    }

    public static LWQLogger getLogger() {
        return sApplication.logger;
    }

    public static IabHelper getIabHelper() {
        return get().iabHelper;
    }

    public static boolean ownsFontAccess() {
        return ownsProduct(IabConst.Product.FONTS)
                || ownsProduct(IabConst.Product.FONTS_IMAGES)
                || ownsProduct(IabConst.Product.QUOTOGRAPH_INSPIRED);
    }

    public static boolean ownsImageAccess() {
        return ownsProduct(IabConst.Product.IMAGES)
                || ownsProduct(IabConst.Product.FONTS_IMAGES)
                || ownsProduct(IabConst.Product.QUOTOGRAPH_INSPIRED);
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

    public static boolean isWallpaperActivated() {
        final WallpaperManager wallpaperManager = WallpaperManager.getInstance(sApplication);
        final WallpaperInfo wallpaperInfo = wallpaperManager.getWallpaperInfo();
        boolean active = wallpaperInfo != null && sApplication.getPackageName().equalsIgnoreCase(wallpaperInfo.getPackageName());
        getLogger().logWallpaperActive(active);
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
                for (IabConst.Product product : IabConst.Product.values()) {
                    if (ImageLoader.getInstance().getDiskCache().get(product.imgSource) == null) {
                        ImageLoader.getInstance().loadImage(product.imgSource, null);
                    }
                }
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
        AnalyticsUtils.trackProductPurchased(result, info);
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
