package com.stanleyidesis.livewallpaperquotes.ui;

import android.view.View;
import android.view.ViewGroup;

/**
 * Created by stanleyidesis on 9/13/15.
 */
public class UIUtils {

    public static void setViewAndChildrenEnabled(View view, boolean enabled) {
        view.setEnabled(enabled);
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                setViewAndChildrenEnabled(child, enabled);
            }
        }
    }

}
