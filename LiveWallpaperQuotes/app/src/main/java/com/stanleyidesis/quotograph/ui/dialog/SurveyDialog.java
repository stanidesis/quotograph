package com.stanleyidesis.quotograph.ui.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.annotation.NonNull;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.stanleyidesis.quotograph.R;
import com.stanleyidesis.quotograph.api.misc.UserSurveyController;

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
 * SurveyDialog.java
 * @author Stanley Idesis
 *
 * From Quotograph
 * https://github.com/stanidesis/quotograph
 *
 * Please report any issues
 * https://github.com/stanidesis/quotograph/issues
 *
 * Date: 09/12/2016
 */
public class SurveyDialog {
    public static void showDialog(Activity activity) {
        MaterialDialog.SingleButtonCallback buttonCallback = new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                // Which comes in reverse, 0 = okay, 1 = later, and 2 = never
                // This quick negation and abs value flips it right-side round
                UserSurveyController.handleResponse(Math.abs(which.ordinal() - 2));
            }
        };
        new MaterialDialog.Builder(activity)
                .title(R.string.survey_title)
                .content(R.string.survey_cta)
                .autoDismiss(true)
                .negativeText(R.string.survey_never)
                .neutralText(R.string.survey_later)
                .positiveText(R.string.survey_okay)
                .onAny(buttonCallback)
                .canceledOnTouchOutside(false)
                .cancelable(false)
                .cancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        // If they somehow manage to cancel it...
                        UserSurveyController.handleResponse(UserSurveyController.RESPONSE_LATER);
                    }
                })
                .show();
    }
}
