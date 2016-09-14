package com.stanleyidesis.quotograph.ui.debug;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.stanleyidesis.quotograph.LWQApplication;
import com.stanleyidesis.quotograph.LWQPreferences;
import com.stanleyidesis.quotograph.RemoteConfigConst;
import com.stanleyidesis.quotograph.ui.activity.LWQSurveyActivity;
import com.stanleyidesis.quotograph.ui.dialog.SurveyDialog;

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
 * DebugDialog.java
 * @author Stanley Idesis
 *
 * From Quotograph
 * https://github.com/stanidesis/quotograph
 *
 * Please report any issues
 * https://github.com/stanidesis/quotograph/issues
 *
 * Date: 09/11/2016
 */
public class DebugDialog {
    public static void show(final Context context) {
        CharSequence [] list = new CharSequence[] {
                "Clear all Preferences",
                "Clear Survey Preferences",
                "Subtract Day From 'survey_last_shown'",
                "Show Survey Notification",
                "Show Survey Dialog",
                "Show Survey Popup",
                "Print Survey Data to Logs",
                "Fetch RemoteConfig"
        };
        MaterialDialog.ListCallback listCallback = new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                switch (which) {
                    case 0: // Clear all prefs
                        LWQPreferences.clearPreferences();
                        break;
                    case 1: // Clear survey prefs
                        LWQPreferences.clearSurveyPreferences();
                        break;
                    case 2: // Subtract one day from the last time the survey was shown
                        long current = LWQPreferences.getSurveyLastShownOn();
                        current = current == -1 ? System.currentTimeMillis() : current;
                        LWQPreferences.setSurveyLastShownOn(current - DateUtils.DAY_IN_MILLIS);
                        break;
                    case 3: // Show the survey notification
                        LWQApplication.getNotificationController().postSurveyNotification();
                        break;
                    case 4: // Show the survey dialog
                        SurveyDialog.showDialog((Activity) context);
                        break;
                    case 5: // Show survey popup
                        context.startActivity(new Intent(context, LWQSurveyActivity.class));
                        break;
                    case 6: // Print survey data to logs
                        String tag = "DebugDialog-Survey";
                        Log.v(tag, "Variant: " + LWQApplication.getRemoteConfig().getLong(RemoteConfigConst.SURVEY_EXPERIMENT));
                        Log.v(tag, "Delay: " + LWQApplication.getRemoteConfig().getLong(RemoteConfigConst.SURVEY_DELAY_IN_MILLIS));
                        Log.v(tag, "Interval: " + LWQApplication.getRemoteConfig().getLong(RemoteConfigConst.SURVEY_INTERVAL_IN_MILLIS));
                        Log.v(tag, "Response: " + LWQPreferences.getSurveyResponse());
                        Log.v(tag, "Last Shown On: " + DateUtils.formatDateTime(context, LWQPreferences.getSurveyLastShownOn(),
                                DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_WEEKDAY));
                        break;
                    case 7: // Fetch Remote Config
                        LWQApplication.fetchRemoteConfig();
                }
            }
        };
        MaterialDialog debugDialog =
                new MaterialDialog.Builder(context)
                .title("Debug Dialog")
                .autoDismiss(false)
                .canceledOnTouchOutside(false)
                .items(list)
                .itemsCallback(listCallback)
                .build();
        debugDialog.show();
    }
}
