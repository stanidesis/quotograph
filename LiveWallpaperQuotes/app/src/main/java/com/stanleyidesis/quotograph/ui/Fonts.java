package com.stanleyidesis.quotograph.ui;

import android.content.Context;
import android.graphics.Typeface;

import com.stanleyidesis.quotograph.R;

/**
 * Never change the ID, keep that 1:1
 * Created by stanleyidesis on 7/19/15.
 */
public enum Fonts {

    // DEFAULTS @ 0
    SYSTEM(0, R.string.font_system, null),
    AMATIC_SC_BOLD(1, R.string.font_amatic_sc, "AmaticSC-Bold.ttf"),
    BLACK_OPS_ONE_REGULAR(2, R.string.font_black_ops_one, "BlackOpsOne-Regular.ttf"),
    CRAFTY_GIRLS(3, R.string.font_crafty_girls, "CraftyGirls.ttf"),
    FINGER_PAINT_REGULAR(4, R.string.font_finger_paint, "FingerPaint-Regular.ttf"),
    INDIE_FLOWER(5, R.string.font_indie_flower, "IndieFlower.ttf"),
    JOSEFIN_BOLD(6, R.string.font_josefin_sans_bold, "JosefinSans-Bold.ttf"),
    PACIFICO(7, R.string.font_pacifico, "Pacifico.ttf"),
    PRESS_START_2P(8, R.string.font_press_start_2p, "PressStart2P-Regular.ttf"),
    SPECIAL_ELITE(9, R.string.font_special_elite, "SpecialElite.ttf"),
    UNICA_ONE(10, R.string.font_unica_one, "UnicaOne-Regular.ttf");

    int id;
    int prettyName;
    String fileName;

    Fonts(int id, int prettyName, String filePath) {
        this.id = id;
        this.prettyName = prettyName;
        this.fileName = filePath;
    }

    public Typeface load(Context context) {
        return Typeface.createFromAsset(context.getAssets(), "fonts/" + getFileName());
    }

    public String getPrettyName(Context context) {
        return context.getString(this.prettyName);
    }

    public int getId() {
        return this.id;
    }

    public String getFileName() {
        return this.fileName;
    }

    public static Fonts findById(int id) {
        for (Fonts font : Fonts.values()) {
            if (font.id == id) {
                return font;
            }
        }
        // Return default if font not found
        return Fonts.SYSTEM;
    }
}
