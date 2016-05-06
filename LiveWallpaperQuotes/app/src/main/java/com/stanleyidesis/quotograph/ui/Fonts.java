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
    ABRIL_FATFACE(11, R.string.font_abril_fatface, "AbrilFatface-Regular.ttf"),
    AMATIC_SC_BOLD(1, R.string.font_amatic_sc, "AmaticSC-Bold.ttf"),
    ANTON(12, R.string.font_anton, "Anton.ttf"),
    BLACK_OPS_ONE_REGULAR(2, R.string.font_black_ops_one, "BlackOpsOne-Regular.ttf"),
    CABIN_SKETCH(13, R.string.font_cabin_sketch, "CabinSketch-Bold.ttf"),
    FINGER_PAINT_REGULAR(4, R.string.font_finger_paint, "FingerPaint-Regular.ttf"),
    FREDERICKA_THE_GREAT(14, R.string.font_fredericka_the_great, "FrederickatheGreat-Regular.ttf"),
    JOSEFIN_BOLD(6, R.string.font_josefin_sans_bold, "JosefinSans-Bold.ttf"),
    SPECIAL_ELITE(9, R.string.font_special_elite, "SpecialElite.ttf"),
    SPIRAX(15, R.string.font_spirax, "Spirax-Regular.ttf"),
    UNICA_ONE(10, R.string.font_unica_one, "UnicaOne-Regular.ttf"),
    VT323(16, R.string.font_vt323, "VT323-Regular.ttf");

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
