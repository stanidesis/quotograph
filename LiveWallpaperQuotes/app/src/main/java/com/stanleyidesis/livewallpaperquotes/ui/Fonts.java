package com.stanleyidesis.livewallpaperquotes.ui;

import android.content.Context;
import android.graphics.Typeface;

/**
 * Created by stanleyidesis on 7/19/15.
 */
public enum Fonts {
    DAWNING_OF_A_NEW_DAY("fonts/DawningofaNewDay.ttf"),
    SERIFICO("fonts/Serifiqo.otf");

    String filePath;

    Fonts(String filePath) {
        this.filePath = filePath;
    }

    public Typeface load(Context context) {
        return Typeface.createFromAsset(context.getAssets(), this.filePath);
    }
}
