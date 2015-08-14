package com.stanleyidesis.livewallpaperquotes;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by stanleyidesis on 8/14/15.
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

    public static float getBlurPreference() {
        return sharedPreferences.getFloat(LWQApplication.get().getString(R.string.preference_key_blur), 0f);
    }

    public static void setBlurPreference(float blurPreference) {
        sharedPreferences.edit().putFloat(LWQApplication.get().getString(R.string.preference_key_blur), blurPreference).apply();
    }

    public static boolean isFirstLaunch() {
        return sharedPreferences.getBoolean(LWQApplication.get().getString(R.string.preference_key_first_launch), true);
    }

    public static void setFirstLaunch(boolean firstLaunch) {
        sharedPreferences.edit().putBoolean(LWQApplication.get().getString(R.string.preference_key_first_launch), firstLaunch).apply();
    }
}
