package com.stanleyidesis.quotograph.ui.activity.modules;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.stanleyidesis.quotograph.AnalyticsUtils;
import com.stanleyidesis.quotograph.LWQApplication;
import com.stanleyidesis.quotograph.R;
import com.stanleyidesis.quotograph.api.event.IabPurchaseEvent;
import com.stanleyidesis.quotograph.IabConst;
import com.stanleyidesis.quotograph.ui.adapter.IapProductAdapter;

import de.greenrobot.event.EventBus;

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
 * LWQStoreDialogModule.java
 * @author Stanley Idesis
 *
 * From Quotograph
 * https://github.com/stanidesis/quotograph
 *
 * Please report any issues
 * https://github.com/stanidesis/quotograph/issues
 *
 * Date: 05/01/2016
 */
public class LWQStoreDialogModule implements Module,
        IapProductAdapter.Delegate,
        DialogInterface.OnDismissListener,
        DialogInterface.OnCancelListener {

    Activity activity;
    MaterialDialog dialog;
    private IabConst.Product attemptedPurchase;

    @Override
    public void initialize(Context context, View root) {
        this.activity = (Activity) context;
        EventBus.getDefault().register(this);
    }

    @Override
    protected void finalize() throws Throwable {
        EventBus.getDefault().unregister(this);
        super.finalize();
    }

    @Override
    public void changeVisibility(View anchor, boolean visible) {
        if (!visible) {
            if (dialog != null) {
                dialog.dismiss();
                dialog = null;
            }
            return;
        }
        dialog = new MaterialDialog.Builder(activity)
                .adapter(new IapProductAdapter(this), null)
                .autoDismiss(true)
                .title(R.string.iap_dialog_title)
                .titleGravity(GravityEnum.CENTER)
                .build();
        dialog.show();
    }

    @Override
    public boolean isVisible() {
        return dialog != null && dialog.isShowing();
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        // TODO analytics?
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        // TODO analytics?
    }

    // Listen for Purchase Events

    public void onEvent(IabPurchaseEvent iabPurchaseEvent) {
        if (iabPurchaseEvent.didFail() || dialog == null
                || !dialog.isShowing()
                || activity == null) {
            if (iabPurchaseEvent.didFail() && attemptedPurchase != null) {
                AnalyticsUtils.trackFailedPurchase(attemptedPurchase);
                attemptedPurchase = null;
            }
            return;
        }
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                IapProductAdapter productAdapter =
                        (IapProductAdapter) dialog.getListView().getAdapter();
                productAdapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
    }

    // Adapter Delegate

    @Override
    public void purchaseProduct(IabConst.Product product) {
        attemptedPurchase = product;
        AnalyticsUtils.trackTappedProduct(product);
        LWQApplication.purchaseProduct(activity, product);
    }
}
