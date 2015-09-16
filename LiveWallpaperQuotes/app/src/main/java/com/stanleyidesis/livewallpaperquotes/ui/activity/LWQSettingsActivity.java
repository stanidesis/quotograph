package com.stanleyidesis.livewallpaperquotes.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;

import com.stanleyidesis.livewallpaperquotes.LWQApplication;
import com.stanleyidesis.livewallpaperquotes.LWQPreferences;
import com.stanleyidesis.livewallpaperquotes.R;
import com.stanleyidesis.livewallpaperquotes.api.LWQAlarmManager;
import com.stanleyidesis.livewallpaperquotes.ui.UIUtils;

import java.util.List;

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
public class LWQSettingsActivity extends LWQWallpaperActivity implements SeekBar.OnSeekBarChangeListener {

    enum SettingsState {
        NONE,
        MODE_SELECTION,
        AUTOPILOT_SETTINGS,
        CUSTOM_SETTINGS;
    }

    SettingsState currentState;

    // Mode Selection Views
    View modeSelectionContainer;
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
            if (LWQPreferences.isAutoPilot()) {
                animateToState(SettingsState.AUTOPILOT_SETTINGS);
            } else {
                // TODO
                animateToState(SettingsState.NONE);
            }
        }
    };

    // Autopilot Views
    View autopilotSettingsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.fade_in, 0);
        setContentView(R.layout.activity_lwq_settings);
        currentState = SettingsState.NONE;

        // Setup Mode Selection
        setupModeSelection();
        // Setup Autopilot Stuff
        setupAutopilotSettings();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        switchToSilkScreen(SilkScreenState.DEFAULT, null);
        animateToState(SettingsState.MODE_SELECTION);
    }

    @Override
    public void onBackPressed() {
        if (currentState != SettingsState.MODE_SELECTION) {
            animateToState(SettingsState.MODE_SELECTION);
            return;
        }
        super.onBackPressed();
    }

    void setupModeSelection() {
        modeSelectionContainer = findViewById(R.id.group_lwq_settings_mode_select);
        modeSelectionContainer.setAlpha(0f);
        modeSelectionContainer.findViewById(R.id.button_lwq_settings_mode_continue).setOnClickListener(modeContinueClickListener);
        modeAutopilotButton = modeSelectionContainer.findViewById(R.id.button_lwq_settings_autopilot);
        modeCustomButton = modeSelectionContainer.findViewById(R.id.button_lwq_settings_custom);
        modeAutopilotButton.setSelected(LWQPreferences.isAutoPilot());
        modeCustomButton.setSelected(!modeAutopilotButton.isSelected());
        modeAutopilotButton.setOnClickListener(modeOnClickListener);
        modeCustomButton.setOnClickListener(modeOnClickListener);
        UIUtils.setViewAndChildrenEnabled(modeSelectionContainer, false);
    }

    void setupAutopilotSettings() {
        autopilotSettingsContainer = findViewById(R.id.group_lwq_settings_autopilot);
        autopilotSettingsContainer.setAlpha(0f);
        SeekBar blurBar = (SeekBar) autopilotSettingsContainer.findViewById(R.id.sb_lwq_autopilot_settings_blur);
        blurBar.setOnSeekBarChangeListener(this);
        blurBar.setProgress((int) LWQPreferences.getBlurPreference());

        final List<String> backgroundCategories = LWQApplication.getWallpaperController().getBackgroundCategories();
        final String imageCategoryPreference = LWQPreferences.getImageCategoryPreference();
        int currentSelection = 0;
        for (String category : backgroundCategories) {
            if (category.equalsIgnoreCase(imageCategoryPreference)) {
                currentSelection = backgroundCategories.indexOf(category);
                break;
            }
        }
        ArrayAdapter<String> imageCategoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                backgroundCategories);
        imageCategoryAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        Spinner imageCategorySpinner = (Spinner) autopilotSettingsContainer.findViewById(R.id.spinner_lwq_autopilot_settings_image_category);
        imageCategorySpinner.setAdapter(imageCategoryAdapter);
        imageCategorySpinner.setSelection(currentSelection);
        imageCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
                LWQPreferences.setImageCategoryPreference(LWQApplication.getWallpaperController().getBackgroundCategories().get(index));
                // TODO refresh wallpaper
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        Spinner refreshSpinner = (Spinner) autopilotSettingsContainer.findViewById(R.id.spinner_lwq_autopilot_settings_interval);
        final long refreshPreference = LWQPreferences.getRefreshPreference();
        final int[] refreshValues = getResources().getIntArray(R.array.refresh_preference_values);
        for (int i = 0; i < refreshValues.length; i++) {
            if (refreshPreference == refreshValues[i]) {
                refreshSpinner.setSelection(i);
                break;
            }
        }
        refreshSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
                final int[] refreshValues = getResources().getIntArray(R.array.refresh_preference_values);
                LWQPreferences.setRefreshPreference(refreshValues[index]);
                LWQAlarmManager.resetAlarm();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
        UIUtils.setViewAndChildrenEnabled(autopilotSettingsContainer, false);
    }

    void animateToState(final SettingsState newState) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (currentState == newState) {
                    return;
                }
                final View newContainer = containerForState(newState);
                final View currentContainer = containerForState(currentState);
                if (newContainer != null) {
                    UIUtils.setViewAndChildrenEnabled(newContainer, true);
                    final Animator enterAnimator = AnimatorInflater.loadAnimator(LWQSettingsActivity.this, R.animator.enter_from_right);
                    enterAnimator.setTarget(newContainer);
                    enterAnimator.start();
                }
                if (currentContainer != null) {
                    UIUtils.setViewAndChildrenEnabled(currentContainer, false);
                    final Animator exitAnimator = AnimatorInflater.loadAnimator(LWQSettingsActivity.this, R.animator.exit_to_left);
                    exitAnimator.setTarget(currentContainer);
                    exitAnimator.start();
                }
                currentState = newState;
            }
        });
    }

    View containerForState(SettingsState settingsState) {
        switch (settingsState) {
            case NONE:
                return null;
            case MODE_SELECTION:
                return modeSelectionContainer;
            case AUTOPILOT_SETTINGS:
                return autopilotSettingsContainer;
            default:
                return null;
        }
    }

    // SeekBar Listener

    @Override
    public void onProgressChanged(SeekBar seekBar, int value, boolean b) {
        LWQPreferences.setBlurPreference(value);
        // TODO refresh wallpaper
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        animateSilkScreen(SilkScreenState.REVEAL, containerForState(currentState));
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        animateSilkScreen(SilkScreenState.DEFAULT, containerForState(currentState));
    }

}