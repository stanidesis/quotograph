package com.stanleyidesis.quotograph;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.analytics.ecommerce.Product;
import com.google.android.gms.analytics.ecommerce.ProductAction;
import com.stanleyidesis.quotograph.billing.util.IabResult;
import com.stanleyidesis.quotograph.billing.util.Purchase;
import com.stanleyidesis.quotograph.billing.util.SkuDetails;

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
 * AnalyticsUtils.java
 * @author Stanley Idesis
 *
 * From Quotograph
 * https://github.com/stanidesis/quotograph
 *
 * Please report any issues
 * https://github.com/stanidesis/quotograph/issues
 *
 * Date: 05/01/2015
 */
public class AnalyticsUtils {

    private static void run(Runnable runnable) {
        new Thread(runnable).start();
    }

    public static Product generateProduct(IabConst.Product iabProduct) {
        SkuDetails productDetails = LWQApplication.getProductDetails(iabProduct);
        Product product = new Product()
                .setId(productDetails.getSku())
                .setName(productDetails.getTitle())
                .setCategory(productDetails.getType())
                .setPrice(productDetails.getPriceAmountMicros() / 100)
                .setPosition(iabProduct.ordinal());
        return product;
    }

    public static void trackAttemptedAccess(final IabConst.Product iabProduct, final String screenName) {
        run(new Runnable() {
            @Override
            public void run() {
                Product product = generateProduct(iabProduct);
                HitBuilders.ScreenViewBuilder builder = new HitBuilders.ScreenViewBuilder()
                        .addImpression(product, "Attempted Access")
                        .addProduct(product);
                Tracker defaultTracker = LWQApplication.get().getDefaultTracker();
                defaultTracker.set("&cu",
                        LWQApplication.getProductDetails(iabProduct).getPriceCurrencyCode());
                defaultTracker.setScreenName(screenName);
                defaultTracker.send(builder.build());
            }
        });
    }

    public static void trackTappedProduct(final IabConst.Product iabProduct) {
        run(new Runnable() {
            @Override
            public void run() {
                Product product = generateProduct(iabProduct);
                ProductAction productAction = new ProductAction(ProductAction.ACTION_DETAIL)
                        .setCheckoutStep(1)
                        .setProductActionList("Quotograph Store");
                HitBuilders.ScreenViewBuilder builder = new HitBuilders.ScreenViewBuilder()
                        .addProduct(product)
                        .setProductAction(productAction);
                Tracker defaultTracker = LWQApplication.get().getDefaultTracker();
                defaultTracker.set("&cu",
                        LWQApplication.getProductDetails(iabProduct).getPriceCurrencyCode());
                defaultTracker.setScreenName("quotograph_store");
                defaultTracker.send(builder.build());
            }
        });
    }

    public static void trackFailedPurchase(final IabConst.Product iabProduct) {
        run(new Runnable() {
            @Override
            public void run() {
                Product product = generateProduct(iabProduct);
                SkuDetails skuDetails = LWQApplication.getProductDetails(iabProduct);
                ProductAction productAction =
                        new ProductAction(ProductAction.ACTION_CHECKOUT)
                                .setCheckoutStep(2)
                                .setTransactionAffiliation("Google Play Store")
                                .setTransactionRevenue(skuDetails.getPriceAmountMicros() / 100)
                                .setCheckoutOptions("checkout failed");
                HitBuilders.ScreenViewBuilder builder = new HitBuilders.ScreenViewBuilder()
                        .addProduct(product)
                        .setProductAction(productAction);
                Tracker defaultTracker = LWQApplication.get().getDefaultTracker();
                defaultTracker.set("&cu", skuDetails.getPriceCurrencyCode());
                defaultTracker.setScreenName("transaction");
                defaultTracker.send(builder.build());
            }
        });
    }

    public static void trackProductPurchased(final IabResult result, final Purchase info) {
        run(new Runnable() {
            @Override
            public void run() {
                IabConst.Product purchased = null;
                for (IabConst.Product product : IabConst.Product.values()) {
                    if (product.sku.equalsIgnoreCase(info.getSku())) {
                        purchased = product;
                        break;
                    }
                }
                SkuDetails skuDetails = LWQApplication.getProductDetails(purchased);
                Product product = generateProduct(purchased);
                ProductAction productAction =
                        new ProductAction(ProductAction.ACTION_CHECKOUT)
                                .setCheckoutStep(2)
                                .setTransactionAffiliation("Google Play Store")
                                .setTransactionRevenue(skuDetails.getPriceAmountMicros() / 100)
                                .setTransactionId(info.getOrderId());
                HitBuilders.ScreenViewBuilder builder = new HitBuilders.ScreenViewBuilder()
                        .addProduct(product)
                        .setProductAction(productAction);
                Tracker defaultTracker = LWQApplication.get().getDefaultTracker();
                defaultTracker.set("&cu", skuDetails.getPriceCurrencyCode());
                defaultTracker.setScreenName("transaction");
                defaultTracker.send(builder.build());
            }
        });
    }
}
