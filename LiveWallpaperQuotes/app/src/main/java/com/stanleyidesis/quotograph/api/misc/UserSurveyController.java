package com.stanleyidesis.quotograph.api.misc;

import android.content.Intent;
import android.net.Uri;

import com.stanleyidesis.quotograph.LWQApplication;
import com.stanleyidesis.quotograph.LWQPreferences;
import com.stanleyidesis.quotograph.RemoteConfigConst;

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

    public static int SURVEY_VARIANT_NONE = -1;
    public static int SURVEY_VARIANT_NOTIFICATION = 0;
    public static int SURVEY_VARIANT_DIALOG = 1;
    public static int SURVEY_VARIANT_POPUP = 2;
    public static int SURVEY_VARIANT_PLAYLIST = 3;

    public static int RESPONSE_NEVER = 0;
    public static int RESPONSE_LATER = 1;
    public static int RESPONSE_OKAY = 2;

    public static boolean shouldShowSurvey() {
        long surveyLastShownOn = LWQPreferences.getSurveyLastShownOn();
        int surveyResponse = LWQPreferences.getSurveyResponse();
        if (getVariant() == SURVEY_VARIANT_NONE) {
            return false;
        }
        if (surveyResponse == RESPONSE_NEVER
                || surveyResponse == RESPONSE_OKAY) {
            // They've replied affirmatively or negatively,
            // so don't show the survey again
            return false;
        }
        if (surveyLastShownOn == -1) {
            // This is the first time we're checking, so set to system time and
            // don't show it yet, wait one cycle
            LWQPreferences.setSurveyLastShownOn(System.currentTimeMillis());
            return false;
        }
        // Check whether enough time has passed since the last
        // time we showed the survey
        long timeSinceLastSurvey = System.currentTimeMillis() - surveyLastShownOn;
        boolean showSurvey = timeSinceLastSurvey >
                LWQApplication.getRemoteConfig()
                        .getLong(RemoteConfigConst.SURVEY_INTERVAL_IN_MILLIS);
        if (showSurvey) {
            LWQPreferences.setSurveyLastShownOn(System.currentTimeMillis());
        }
        return showSurvey;
    }

    public static long getVariant() {
        return LWQApplication.getRemoteConfig()
                .getLong(RemoteConfigConst.SURVEY_EXPERIMENT);
    }

    /**
     * Handle the response types. They are:
     * RESPONSE_NEVER, RESPONSE_LATER, and
     * RESPONSE_OKAY.
     *
     * @param which
     */
    public static void handleResponse(int which) {
        // TODO analytics, record response
        LWQPreferences.setSurveyResponse(which);
        if (which == RESPONSE_NEVER || which == RESPONSE_LATER) {
            return;
        }
        Intent takeSurvey = new Intent(Intent.ACTION_VIEW);
        takeSurvey.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        takeSurvey.setData(
                Uri.parse(
                        LWQApplication.getRemoteConfig()
                                .getString(RemoteConfigConst.SURVEY_URL)));
        LWQApplication.get().startActivity(takeSurvey);
    }
}
