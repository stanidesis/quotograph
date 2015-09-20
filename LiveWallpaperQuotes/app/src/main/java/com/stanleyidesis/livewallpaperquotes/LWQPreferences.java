package com.stanleyidesis.livewallpaperquotes;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;

import com.stanleyidesis.livewallpaperquotes.api.event.PreferenceUpdateEvent;

import de.greenrobot.event.EventBus;

/**
 * Copyright (c) 2015 Stanley Idesis
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
 * From Live-Wallpaper-Quotes
 * https://github.com/stanidesis/live-wallpaper-quotes
 *
 * Please report any issues
 * https://github.com/stanidesis/live-wallpaper-quotes/issues
 *
 * Date: 08/14/2015
 */
public class LWQPreferences {
    static LWQPreferences sInstance;
    static SharedPreferences sharedPreferences;

    static {
        sInstance = new LWQPreferences();
    }

    LWQPreferences() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(LWQApplication.get());
    }

    public static int getBlurPreference() {
        return sharedPreferences.getInt(LWQApplication.get().getString(R.string.preference_key_blur), 0);
    }

    public static void setBlurPreference(int blurPreference) {
        sharedPreferences.edit().putInt(LWQApplication.get().getString(R.string.preference_key_blur), blurPreference).apply();
        EventBus.getDefault().post(PreferenceUpdateEvent.preferenceUpdated(R.string.preference_key_blur, blurPreference));
    }

    public static int getDimPreference() {
        return sharedPreferences.getInt(LWQApplication.get().getString(R.string.preference_key_dim), 0);
    }

    public static void setDimPreference(int dimPreference) {
        sharedPreferences.edit().putInt(LWQApplication.get().getString(R.string.preference_key_dim), dimPreference).apply();
        EventBus.getDefault().post(PreferenceUpdateEvent.preferenceUpdated(R.string.preference_key_dim, dimPreference));
    }

    public static boolean isFirstLaunch() {
        return sharedPreferences.getBoolean(LWQApplication.get().getString(R.string.preference_key_first_launch), true);
    }

    public static void setFirstLaunch(boolean firstLaunch) {
        sharedPreferences.edit().putBoolean(LWQApplication.get().getString(R.string.preference_key_first_launch), firstLaunch).apply();
    }

    public static String getImageCategoryPreference() {
        return sharedPreferences.getString(LWQApplication.get().getString(R.string.preference_key_image_category), null);
    }

    public static void setImageCategoryPreference(String imageCategory) {
        sharedPreferences.edit().putString(LWQApplication.get().getString(R.string.preference_key_image_category), imageCategory).apply();
        EventBus.getDefault().post(PreferenceUpdateEvent.preferenceUpdated(R.string.preference_key_image_category, imageCategory));
    }

    public static String getQuoteCategoryPreference() {
        return sharedPreferences.getString(LWQApplication.get().getString(R.string.preference_key_quote_category), null);
    }

    public static void setQuoteCategoryPreference(String quoteCategory) {
        sharedPreferences.edit().putString(LWQApplication.get().getString(R.string.preference_key_quote_category), quoteCategory).apply();
        EventBus.getDefault().post(PreferenceUpdateEvent.preferenceUpdated(R.string.preference_key_quote_category, quoteCategory));
    }

    public static long getRefreshPreference() {
        return sharedPreferences.getLong(LWQApplication.get().getString(R.string.preference_key_refresh), DateUtils.DAY_IN_MILLIS);
    }

    public static void setRefreshPreference(long refresh) {
        sharedPreferences.edit().putLong(LWQApplication.get().getString(R.string.preference_key_refresh), refresh).apply();
        EventBus.getDefault().post(PreferenceUpdateEvent.preferenceUpdated(R.string.preference_key_refresh, refresh));
    }

    public static boolean isAutoPilot() {
        final Resources resources = LWQApplication.get().getResources();
        final int autopilotMode = resources.getInteger(R.integer.preference_mode_autopilot);
        final int mode = sharedPreferences.getInt(resources.getString(R.string.preference_key_mode), autopilotMode);
        return mode == autopilotMode;
    }

    public static void setMode(int mode) {
        sharedPreferences.edit().putInt(LWQApplication.get().getString(R.string.preference_key_mode), mode).apply();
        EventBus.getDefault().post(PreferenceUpdateEvent.preferenceUpdated(R.string.preference_key_mode, mode));
    }

}
