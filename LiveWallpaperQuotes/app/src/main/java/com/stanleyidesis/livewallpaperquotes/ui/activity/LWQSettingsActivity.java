package com.stanleyidesis.livewallpaperquotes.ui.activity;

import android.os.Bundle;
import android.view.View;

import com.stanleyidesis.livewallpaperquotes.LWQPreferences;
import com.stanleyidesis.livewallpaperquotes.R;
import com.stanleyidesis.livewallpaperquotes.ui.fragment.LWQSettingsFragment;

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
 * LWQSettingsActivity.java
 * @author Stanley Idesis
 *
 * From Live-Wallpaper-Quotes
 * https://github.com/stanidesis/live-wallpaper-quotes
 *
 * Please report any issues
 * https://github.com/stanidesis/live-wallpaper-quotes/issues
 *
 * Date: 07/11/2015
 */
public class LWQSettingsActivity extends LWQWallpaperActivity implements LWQSettingsFragment.Delegate {

    View modeButtonContainer;
    View modeAutopilotButton;
    View modeCustomButton;
    View.OnClickListener modeOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            modeAutopilotButton.setSelected(view == modeAutopilotButton);
            modeCustomButton.setSelected(!modeAutopilotButton.isSelected());
            int newMode = view == modeAutopilotButton ?
                    getResources().getInteger(R.integer.preference_mode_autopilot) :
                    getResources().getInteger(R.integer.preference_mode_custom);
            LWQPreferences.setMode(newMode);
        }
    };
    View.OnClickListener modeContinueClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.fade_in, 0);
        setContentView(R.layout.activity_lwq_settings);

        // Setup Mode Selection

        modeButtonContainer = findViewById(R.id.group_lwq_settings_mode_select);
        modeButtonContainer.findViewById(R.id.button_lwq_settings_mode_continue).setOnClickListener(modeContinueClickListener);
        modeAutopilotButton = modeButtonContainer.findViewById(R.id.button_lwq_settings_autopilot);
        modeCustomButton = modeButtonContainer.findViewById(R.id.button_lwq_settings_custom);
        modeAutopilotButton.setSelected(LWQPreferences.isAutoPilot());
        modeCustomButton.setSelected(!modeAutopilotButton.isSelected());
        modeAutopilotButton.setOnClickListener(modeOnClickListener);
        modeCustomButton.setOnClickListener(modeOnClickListener);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        switchToSilkScreen(SilkScreenState.DEFAULT);
    }

    // LWQSettingsFragmentDelegate

    @Override
    public void revealDemo(boolean reveal) {
        animateSilkScreen(reveal ? SilkScreenState.REVEAL : SilkScreenState.DEFAULT);
    }

}