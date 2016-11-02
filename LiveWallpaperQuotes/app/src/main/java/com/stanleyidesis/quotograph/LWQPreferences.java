package com.stanleyidesis.quotograph;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;

import com.stanleyidesis.quotograph.api.controller.LWQLoggerHelper;
import com.stanleyidesis.quotograph.api.event.PreferenceUpdateEvent;
import com.stanleyidesis.quotograph.ui.Fonts;

import org.greenrobot.eventbus.EventBus;

import java.util.HashSet;
import java.util.Set;

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
 * LWQQuoteControllerBrainyQuoteImpl.java
 * @author Stanley Idesis
 *
 * From Quotograph
 * https://github.com/stanidesis/quotograph
 *
 * Please report any issues
 * https://github.com/stanidesis/quotograph/issues
 *
 * Date: 08/14/2015
 */
public class LWQPreferences {
    static LWQPreferences sInstance;
    static SharedPreferences sharedPreferences;

    static {
        sInstance = new LWQPreferences();
    }

    private LWQPreferences() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(LWQApplication.get());
    }

    public static int getBlurPreference() {
        return sharedPreferences.getInt(LWQApplication.get().getString(R.string.preference_key_blur),
                LWQApplication.get().getResources().getInteger(R.integer.preference_default_blur));
    }

    public static void setBlurPreference(int blurPreference) {
        LWQLoggerHelper.get().logBlurLevel(blurPreference);
        sharedPreferences.edit().putInt(LWQApplication.get().getString(R.string.preference_key_blur), blurPreference).apply();
        EventBus.getDefault().post(PreferenceUpdateEvent.preferenceUpdated(R.string.preference_key_blur, blurPreference));
    }

    public static int getDimPreference() {
        return sharedPreferences.getInt(LWQApplication.get().getString(R.string.preference_key_dim),
                LWQApplication.get().getResources().getInteger(R.integer.preference_default_dim));
    }

    public static void setDimPreference(int dimPreference) {
        LWQLoggerHelper.get().logDimLevel(dimPreference);
        sharedPreferences.edit().putInt(LWQApplication.get().getString(R.string.preference_key_dim), dimPreference).apply();
        EventBus.getDefault().post(PreferenceUpdateEvent.preferenceUpdated(R.string.preference_key_dim, dimPreference));
    }

    public static boolean isFirstLaunch() {
        return sharedPreferences.getBoolean(LWQApplication.get().getString(R.string.preference_key_first_launch), true);
    }

    public static void setFirstLaunch(boolean firstLaunch) {
        sharedPreferences.edit().putBoolean(LWQApplication.get().getString(R.string.preference_key_first_launch), firstLaunch).apply();
    }

    public static long getRefreshPreference() {
        return sharedPreferences.getLong(LWQApplication.get().getString(R.string.preference_key_refresh), DateUtils.DAY_IN_MILLIS);
    }

    public static void setRefreshPreference(long refresh) {
        sharedPreferences.edit().putLong(LWQApplication.get().getString(R.string.preference_key_refresh), refresh).apply();
        EventBus.getDefault().post(PreferenceUpdateEvent.preferenceUpdated(R.string.preference_key_refresh, refresh));
    }

    public static boolean isDoubleTapEnabled() {
        return sharedPreferences.getBoolean(LWQApplication.get().getString(R.string.preference_key_double_tap_to_launch), true);
    }

    public static void setDoubleTapEnabled(boolean enabled) {
        sharedPreferences.edit().putBoolean(LWQApplication.get().getString(R.string.preference_key_double_tap_to_launch), enabled).apply();
    }

    public static void setFontSet(Set<String> fontIds) {
        sharedPreferences.edit().putStringSet(LWQApplication.get().getString(R.string.preference_key_fonts), fontIds).apply();
    }

    public static Set<String> getFontSet() {
        Set<String> defaultSet = new HashSet<>();
        defaultSet.add(String.valueOf(Fonts.JOSEFIN_BOLD.getId()));
        Set<String> stringSet = sharedPreferences.getStringSet(LWQApplication.get().getString(R.string.preference_key_fonts), defaultSet);
        Set<String> mutableSet = new HashSet<>();
        mutableSet.addAll(stringSet);
        return mutableSet;
    }

    public static void setWatermarkEnabled(boolean enabled) {
        sharedPreferences.edit().putBoolean(LWQApplication.get().getString(R.string.preference_key_watermark), enabled).apply();
    }

    public static boolean isWatermarkEnabled() {
        return sharedPreferences.getBoolean(LWQApplication.get().getString(R.string.preference_key_watermark), true);
    }

    public static void setViewedTutorial(boolean finishedTutorial) {
        sharedPreferences.edit().putBoolean(LWQApplication.get().getString(R.string.preference_key_tutorial), finishedTutorial).apply();
    }

    public static boolean viewedTutorial() {
        return sharedPreferences.getBoolean(LWQApplication.get().getString(R.string.preference_key_tutorial), false);
    }

    public static void setViewedTutorialDialog(boolean viewedTutorialDialog) {
        sharedPreferences.edit().putBoolean(LWQApplication.get().getString(R.string.preference_key_tutorial_dialog), viewedTutorialDialog).apply();
    }

    public static boolean viewedTutorialDialog() {
        return sharedPreferences.getBoolean(LWQApplication.get().getString(R.string.preference_key_tutorial_dialog), false);
    }

    public static int getLatestVersionCode() {
        return sharedPreferences.getInt(LWQApplication.get().getString(R.string.preference_key_whats_new_dialog),
                LWQApplication.getVersionCode());
    }

    public static void setLatestVersionCode() {
        sharedPreferences.edit().putInt(
                LWQApplication.get().getString(R.string.preference_key_whats_new_dialog),
                LWQApplication.getVersionCode()).apply();
    }

    public static long getSurveyLastShownOn() {
        return sharedPreferences.getLong(
                LWQApplication.get().getString(R.string.preference_key_survey_last_shown), -1);
    }

    public static void setSurveyLastShownOn(long when) {
        sharedPreferences.edit().putLong(
                LWQApplication.get().getString(R.string.preference_key_survey_last_shown),
                when
        ).apply();
    }

    public static int getSurveyResponse() {
        return sharedPreferences.getInt(
                LWQApplication.get().getString(R.string.preference_key_survey_response), -1);
    }

    public static void setSurveyResponse(int which) {
        sharedPreferences.edit().putInt(
                LWQApplication.get().getString(R.string.preference_key_survey_response),
                which
        ).apply();
    }

    public static boolean didSkipWallpaper() {
        return sharedPreferences.getBoolean(
                LWQApplication.get().getString(R.string.preference_key_skipped_wallpaper),
                false
        );
    }

    public static void setWallpaperSkipped(boolean skipped) {
        sharedPreferences.edit().putBoolean(
                LWQApplication.get().getString(R.string.preference_key_skipped_wallpaper),
                skipped
        ).apply();
    }

    // WARNING: Dumping all preferences
    public static void clearPreferences() {
        sharedPreferences.edit().clear().apply();
    }

    // WARNING: Dumping survey preferences
    public static void clearSurveyPreferences() {
        sharedPreferences.edit()
                .remove(LWQApplication.get().getString(R.string.preference_key_survey_last_shown))
                .remove(LWQApplication.get().getString(R.string.preference_key_survey_response)).apply();
    }
}
