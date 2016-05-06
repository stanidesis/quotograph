package com.stanleyidesis.quotograph.ui.activity.modules;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.stanleyidesis.quotograph.LWQApplication;
import com.stanleyidesis.quotograph.LWQPreferences;
import com.stanleyidesis.quotograph.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

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
 * WhatsNewDialog.java
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
public class WhatsNewDialog implements Module {

    public static boolean shouldShowDialog() {
        return LWQApplication.getVersionCode() > LWQPreferences.getLatestVersionCode();
    }

    Activity activity;
    MaterialDialog dialog;

    @Override
    public void initialize(Context context, View root) {
        this.activity = (Activity) context;
    }

    @Override
    public void changeVisibility(View anchor, boolean visible) {
        if (visible) {
            dialog = new MaterialDialog.Builder(activity)
                    .content(convertToString())
                    .title("What's New!")
                    .positiveText(android.R.string.ok)
                    .build();
            dialog.show();
            LWQPreferences.setLatestVersionCode();
        } else if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
    }

    @Override
    public boolean isVisible() {
        return dialog != null && dialog.isShowing();
    }

    @Override
    public void setEnabled(boolean enabled) {
        // Nothing
    }

    private Spanned convertToString() {
        InputStream inputStream = activity.getResources().openRawResource(R.raw.whats_new);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int bytesRead;
        try {
            bytesRead = inputStream.read();
            while (bytesRead != -1)
            {
                byteArrayOutputStream.write(bytesRead);
                bytesRead = inputStream.read();
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Html.fromHtml(byteArrayOutputStream.toString());
    }
}
