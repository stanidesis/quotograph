package com.stanleyidesis.quotograph.api.misc;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.stanleyidesis.quotograph.AnalyticsUtils;
import com.stanleyidesis.quotograph.LWQApplication;
import com.stanleyidesis.quotograph.LWQPreferences;
import com.stanleyidesis.quotograph.RemoteConfigConst;
import com.stanleyidesis.quotograph.api.controller.LWQNotificationControllerHelper;
import com.stanleyidesis.quotograph.ui.activity.LWQSurveyActivity;
import com.stanleyidesis.quotograph.ui.dialog.SurveyDialog;

import java.util.Timer;
import java.util.TimerTask;

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
 * UserSurveyController.java
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
public class UserSurveyController {

    public interface Delegate {
        void revealSurveyInPlaylist();
    }

    public static final int SURVEY_VARIANT_NONE = -1;
    public static final int SURVEY_VARIANT_NOTIFICATION = 0;
    public static final int SURVEY_VARIANT_DIALOG = 1;
    public static final int SURVEY_VARIANT_POPUP = 2;
    public static final int SURVEY_VARIANT_PLAYLIST = 3;

    public static int RESPONSE_NEVER = 0;
    public static int RESPONSE_LATER = 1;
    public static int RESPONSE_OKAY = 2;

    private static TimerTask surveyTimerTask;
    private static Timer surveyTimer;

    private static boolean shouldShowSurvey() {
        if (getVariant() == SURVEY_VARIANT_NONE) {
            return false;
        }
        int surveyResponse = LWQPreferences.getSurveyResponse();
        if (surveyResponse == RESPONSE_NEVER
                || surveyResponse == RESPONSE_OKAY) {
            // They've replied affirmatively or negatively,
            // so don't show the survey again
            return false;
        }
        long surveyLastShownOn = LWQPreferences.getSurveyLastShownOn();
        if (surveyLastShownOn == -1L) {
            // This is the first time we're checking, so set to system time and
            // don't show it yet, wait one cycle
            LWQPreferences.setSurveyLastShownOn(System.currentTimeMillis());
            return false;
        }
        // Check whether enough time has passed since the last
        // time we showed the survey
        long timeSinceLastSurvey = System.currentTimeMillis() - surveyLastShownOn;
        return timeSinceLastSurvey >
                FirebaseRemoteConfig.getInstance()
                        .getLong(RemoteConfigConst.SURVEY_INTERVAL_IN_MILLIS);
    }

    public static <T extends Activity & Delegate> void showSurvey(final T activity) {
        if (!shouldShowSurvey() || surveyTimerTask != null) {
            return;
        }
        surveyTimer = new Timer();
        surveyTimerTask = new TimerTask() {
            @Override
            public void run() {
                _showSurvey(activity);
                surveyTimer = null;
                surveyTimerTask = null;
            }
        };
        surveyTimer.schedule(surveyTimerTask, getDelay());
    }

    /**
     * Internal showSurvey that actually does the work.
     *
     * @param activity
     * @param <T>
     */
    static <T extends Activity & Delegate> void _showSurvey(final T activity) {
        // Bail if the user is exiting the Activity or if it's no longer visible
        if (activity.isFinishing()
                || !activity.getWindow().isActive()
                || !activity.getWindow().getDecorView().isShown()) {
            return;
        }
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                switch ((int) getVariant()) {
                    case SURVEY_VARIANT_NOTIFICATION:
                        LWQNotificationControllerHelper.get().postSurveyNotification();
                        AnalyticsUtils.trackScreenView(AnalyticsUtils.SCREEN_SURVEY_NOTIF);
                        break;
                    case SURVEY_VARIANT_DIALOG:
                        SurveyDialog.showDialog(activity);
                        AnalyticsUtils.trackScreenView(AnalyticsUtils.SCREEN_SURVEY_DIALOG);
                        break;
                    case SURVEY_VARIANT_PLAYLIST:
                        AnalyticsUtils.trackScreenView(AnalyticsUtils.SCREEN_SURVEY_PLAYLIST);
                        activity.revealSurveyInPlaylist();
                        break;
                    case SURVEY_VARIANT_POPUP:
                    default:
                        activity.startActivity(new Intent(activity, LWQSurveyActivity.class));
                }
            }
        });
        LWQPreferences.setSurveyLastShownOn(System.currentTimeMillis());
    }

    static long getVariant() {
        return FirebaseRemoteConfig.getInstance()
                .getLong(RemoteConfigConst.SURVEY_EXPERIMENT);
    }

    static String getVariantCategory(long variant) {
        if (0L == variant) {
            return AnalyticsUtils.CATEGORY_SURVEY_NOTIF;
        } else if (1L == variant) {
            return AnalyticsUtils.CATEGORY_SURVEY_DIALOG;
        } else if (2L == variant) {
            return AnalyticsUtils.CATEGORY_SURVEY_PLAYLIST;
        } else if (3L == variant) {
            return AnalyticsUtils.CATEGORY_SURVEY_POPUP;
        } else {
            return AnalyticsUtils.CATEGORY_SURVEY_NONE;
        }
    }

    static long getDelay() {
        return FirebaseRemoteConfig.getInstance()
                .getLong(RemoteConfigConst.SURVEY_DELAY_IN_MILLIS);
    }

    /**
     * Handle the response types. They are:
     * RESPONSE_NEVER, RESPONSE_LATER, and
     * RESPONSE_OKAY.
     *
     * @param which
     */
    public static void handleResponse(int which) {
        LWQPreferences.setSurveyResponse(which);
        AnalyticsUtils.trackEvent(
                getVariantCategory(getVariant()),
                which == RESPONSE_NEVER ? AnalyticsUtils.ACTION_RESPONSE_NEVER :
                        which == RESPONSE_LATER ? AnalyticsUtils.ACTION_RESPONSE_LATER :
                                AnalyticsUtils.ACTION_RESPONSE_OKAY
        );
        if (which == RESPONSE_NEVER || which == RESPONSE_LATER) {
            return;
        }
        Intent takeSurvey = new Intent(Intent.ACTION_VIEW);
        takeSurvey.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        takeSurvey.setData(
                Uri.parse(
                        FirebaseRemoteConfig.getInstance()
                                .getString(RemoteConfigConst.SURVEY_URL)));
        LWQApplication.get().startActivity(takeSurvey);
    }
}
