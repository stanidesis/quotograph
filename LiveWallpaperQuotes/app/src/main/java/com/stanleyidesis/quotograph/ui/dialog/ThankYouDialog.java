package com.stanleyidesis.quotograph.ui.dialog;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.stanleyidesis.quotograph.LWQApplication;
import com.stanleyidesis.quotograph.R;
import com.stanleyidesis.quotograph.RemoteConfigConst;
import com.stanleyidesis.quotograph.api.LWQError;
import com.stanleyidesis.quotograph.api.controller.LWQLoggerHelper;
import com.stanleyidesis.quotograph.ui.view.GifView;

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
 * ThankYouDialog.java
 * @author Stanley Idesis
 *
 * From Quotograph
 * https://github.com/stanidesis/quotograph
 *
 * Please report any issues
 * https://github.com/stanidesis/quotograph/issues
 *
 * Date: 10/23/2016
 */
public class ThankYouDialog {
    public static void showDialog(Activity activity) {
        MaterialDialog.SingleButtonCallback buttonCallback = new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                if (which == DialogAction.POSITIVE) {
                    // Send to Play Store? (Depends on build variant, technically)
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.stanleyidesis.quotograph"));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                    LWQApplication.get().startActivity(intent);
                }
            }
        };
        View thankYouContent = LayoutInflater.from(activity).inflate(R.layout.layout_thank_you, null);
        final View progressBar = thankYouContent.findViewById(R.id.pb_thank_you);
        final View frownyFace = thankYouContent.findViewById(R.id.tv_thank_you_frown);
        ((TextView) thankYouContent.findViewById(R.id.tv_thank_you_message))
                .setText(FirebaseRemoteConfig.getInstance().getString(RemoteConfigConst.THANK_YOU_MESSAGE));
        GifView gifView = (GifView) thankYouContent.findViewById(R.id.gview_thank_you);
        gifView.setGifURL(FirebaseRemoteConfig.getInstance().getString(RemoteConfigConst.THANK_YOU_IMAGE),
                new GifView.Callback() {
                    @Override
                    public void onGifLoaded() {
                        progressBar.animate()
                                .alpha(0f)
                                .setDuration(200)
                                .setInterpolator(new AccelerateDecelerateInterpolator())
                                .start();
                    }

                    @Override
                    public void onGifFailure(String message) {
                        progressBar.setVisibility(View.GONE);
                        frownyFace.setVisibility(View.VISIBLE);
                        frownyFace.animate().alpha(1f)
                                .setDuration(200)
                                .setInterpolator(new AccelerateDecelerateInterpolator())
                                .start();
                        // Analytics
                        LWQLoggerHelper.get()
                                .logError(LWQError.create("Gif Download Failure: " + message));
                        Toast.makeText(LWQApplication.get(),
                                "Error Retrieving Puppies! :'(",
                                Toast.LENGTH_LONG).show();
                    }
                });
        new MaterialDialog.Builder(activity)
                .title(R.string.survey_title)
                .customView(thankYouContent, true)
                .autoDismiss(true)
                .negativeText(R.string.thank_you_negative)
                .positiveText(R.string.thank_you_positive)
                .onAny(buttonCallback)
                .canceledOnTouchOutside(false)
                .cancelable(false)
                .show();
    }
}
