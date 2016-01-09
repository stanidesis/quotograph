package com.stanleyidesis.quotograph.ui;

import android.content.Context;
import android.graphics.Typeface;

/**
 * Created by stanleyidesis on 7/19/15.
 */
public enum Fonts {
    ADAM_CG_PRO("fonts/ADAMCGPRO.otf"),
    BERLIN("fonts/Berlin.ttf"),
    BERLIN_BOLD("fonts/Berlin Bold.ttf"),
    BERLIN_X_BOLD("fonts/Berlin X-Bold.ttf"),
    DAWNING_OF_A_NEW_DAY("fonts/DawningofaNewDay.ttf"),
    JOSEFIN_BOLD("fonts/JosefinSans-Bold.ttf"),
    JOSEFIN_LIGHT("fonts/JosefinSans-Light.ttf"),
    JOSEFIN_REGULAR("fonts/JosefinSans-Regular.ttf"),
    JOSEFIN_SEMIBOLD("fonts/JosefinSans-SemiBold.ttf"),
    JOSEFIN_THIN("fonts/JosefinSans-Thin.ttf"),
    LONDON("fonts/London.ttf"),
    LONDON_FILL("fonts/LondonFill.ttf"),
    RIOT_SQUAD_NF("fonts/RiotSquadNF.otf"),
    SERIFICO("fonts/Serifiqo.otf");

    String filePath;

    Fonts(String filePath) {
        this.filePath = filePath;
    }

    public Typeface load(Context context) {
        return Typeface.createFromAsset(context.getAssets(), this.filePath);
    }
}
