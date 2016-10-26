package com.stanleyidesis.quotograph;

import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

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

    // Tutorial
    public static final String SCREEN_TUTORIAL_1 = "tutorial_page_1";
    public static final String SCREEN_TUTORIAL_2 = "tutorial_page_2";
    public static final String SCREEN_TUTORIAL_3 = "tutorial_page_3";
    public static final String SCREEN_TUTORIAL_4 = "tutorial_page_4";
    public static final String SCREEN_TUTORIAL_5 = "tutorial_page_5";
    public static final String [] SCREEN_TUTORIALS = {
            SCREEN_TUTORIAL_1,
            SCREEN_TUTORIAL_2,
            SCREEN_TUTORIAL_3,
            SCREEN_TUTORIAL_4,
            SCREEN_TUTORIAL_5
    };
    // Main Views
    public static final String SCREEN_WALLPAPER_PREVIEW = "wallpaper_preview";
    public static final String SCREEN_PLAYLIST = "playlist";
    public static final String SCREEN_SETTINGS = "settings";
    // Dialogs
    public static final String SCREEN_FONTS = "fonts";
    public static final String SCREEN_IMAGES = "images";
    public static final String SCREEN_WHATS_NEW = "whats_new";
    // Add View
    public static final String SCREEN_ADD = "add";
    public static final String SCREEN_ADD_QUOTE = "add_qoute";
    public static final String SCREEN_ADD_SEARCH = "add_search";
    // Surveys
    public static final String SCREEN_SURVEY_POPUP = "survey_popup";
    public static final String SCREEN_SURVEY_DIALOG = "survey_dialog";
    public static final String SCREEN_SURVEY_NOTIF = "survey_notif";
    public static final String SCREEN_SURVEY_PLAYLIST = "survey_playlist";
    // Misc
    public static final String SCREEN_SAVE_QUOTOGRAPH = "save_quotograph";
    public static final String SCREEN_CUSTOM_PHOTOS = "add_custom_photos";

    // Categories
    public static final String CATEGORY_ADS = "ads";
    public static final String CATEGORY_TOOLTIPS = "tooltips";
    public static final String CATEGORY_TEXT_INPUT = "text_input";
    public static final String CATEGORY_FTUE_TASK = "ftue_task";
    public static final String CATEGORY_QUOTE = "quote";
    public static final String CATEGORY_WALLPAPER = "wallpaper";
    public static final String CATEGORY_PLAYLIST = "playlist";
    public static final String CATEGORY_FONTS = "fonts";
    public static final String CATEGORY_SURVEY_NOTIF = "survey_notification";
    public static final String CATEGORY_SURVEY_PLAYLIST = "survey_playlist";
    public static final String CATEGORY_SURVEY_POPUP = "survey_popup";
    public static final String CATEGORY_SURVEY_DIALOG = "survey_dialog";
    public static final String CATEGORY_SURVEY_NONE = "survey_none";
    public static final String CATEGORY_SEARCH = "search";
    // Actions
    public static final String ACTION_TAP = "tapped";
    public static final String ACTION_CLICKTHROUGH = "clicked_through";
    public static final String ACTION_RESPONSE_NEVER = "response_never";
    public static final String ACTION_RESPONSE_LATER = "response_later";
    public static final String ACTION_RESPONSE_OKAY = "response_okay";
    public static final String ACTION_COMPLETED = "completed";
    public static final String ACTION_FAILED = "failed";
    public static final String ACTION_CUT = "cut";
    public static final String ACTION_COPY = "copy";
    public static final String ACTION_PASTE = "paste";
    public static final String ACTION_CREATED = "created";
    public static final String ACTION_ADDED = "added";
    public static final String ACTION_SKIPPED = "skipped";
    public static final String ACTION_SAVED = "saved";
    public static final String ACTION_SHARED = "shared";
    public static final String ACTION_EDITED = "edited";
    public static final String ACTION_REMOVED = "removed";
    public static final String ACTION_STARTED = "started";
    public static final String ACTION_MANUALLY_GEN = "manually_generated";
    public static final String ACTION_AUTOMATICALLY_GEN = "automatically_generated";
    public static final String ACTION_DOUBLE_TAP = "double_tap_to_launch";
    public static final String ACTION_SEARCH = "performed_search";

    // Labels?
    public static final String LABEL_PLAYLIST = "playlist";
    public static final String LABEL_ALARM = "alarm";
    public static final String LABEL_IN_APP = "in_app";
    public static final String LABEL_FROM_NOTIF = "from_notif";
    public static final String LABEL_REMOVE_ADS_BANNER = "remove_ads_banner";
    public static final String LABEL_SKIP_TUTORIAL = "skip_tutorial";

    public static final String URI_CHANGE_SOURCE_ALARM = "alarm";
    public static final String URI_CHANGE_SOURCE_NOTIFICATION = "notif";
    public static final String URI_SAVE_SOURCE_NOTIFICATION = "save_from_notif";
    public static final String URI_SHARE_SOURCE_NOTIFICATION = "share_from_notif";


    public static void trackTutorial(boolean started) {
        FirebaseAnalytics.getInstance(LWQApplication.get())
                .logEvent(started ?  FirebaseAnalytics.Event.TUTORIAL_BEGIN :
                        FirebaseAnalytics.Event.TUTORIAL_COMPLETE, null);
    }

    public static void trackSearch(String query) {
        FirebaseAnalytics.getInstance(LWQApplication.get())
                .logEvent(FirebaseAnalytics.Event.SEARCH,
                        buildBundle(FirebaseAnalytics.Param.SEARCH_TERM, query));
        trackEvent(CATEGORY_SEARCH, ACTION_SEARCH, query);
    }

    public static void trackShare(String category, String label) {
        Bundle bundle = AnalyticsUtils.buildBundle(
                FirebaseAnalytics.Param.CONTENT_TYPE, category);
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, label);
        FirebaseAnalytics.getInstance(LWQApplication.get())
                .logEvent(FirebaseAnalytics.Event.SHARE, bundle);
        // Double log it for GA
        trackEvent(category, ACTION_SHARED, label);
    }

    public static void trackScreenView(String screenName) {
        FirebaseAnalytics.getInstance(LWQApplication.get())
                .logEvent(FirebaseAnalytics.Event.VIEW_ITEM,
                        buildBundle(FirebaseAnalytics.Param.ITEM_NAME, screenName));
    }

    public static void trackEvent(String category, String action) {
        trackEvent(category, action, null, null);
    }
    public static void trackEvent(String category, String action, String label) {
        trackEvent(category, action, label, null);
    }

    public static void trackEvent(String category, String action, Integer value) {
        trackEvent(category, action, null, value);
    }

    public static void trackEvent(String category, String action, String label, Integer value) {
        Bundle bundle = buildBundle(FirebaseAnalytics.Param.CONTENT_TYPE, category);
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, action);
        if (label != null) {
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, label);
        }
        if (value != null) {
            bundle.putInt(FirebaseAnalytics.Param.VALUE, value);
        }
        FirebaseAnalytics.getInstance(LWQApplication.get())
                .logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    public static Bundle buildBundle(String key, String value) {
        Bundle bundle = new Bundle();
        bundle.putString(key, value);
        return bundle;
    }

}
