package com.stanleyidesis.livewallpaperquotes.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;

import com.stanleyidesis.livewallpaperquotes.LWQApplication;
import com.stanleyidesis.livewallpaperquotes.LWQPreferences;
import com.stanleyidesis.livewallpaperquotes.R;
import com.stanleyidesis.livewallpaperquotes.api.controller.LWQAlarmController;
import com.stanleyidesis.livewallpaperquotes.api.db.Category;
import com.stanleyidesis.livewallpaperquotes.api.event.PreferenceUpdateEvent;
import com.stanleyidesis.livewallpaperquotes.ui.UIUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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
        ADJUSTABLE_SETTINGS,
        AUTOPILOT_SETTINGS;
    }

    SettingsState currentState;
    boolean controlsVisible = false;

    // ProgressBar
    ProgressBar progressBar;
    // Autopilot Views
    View autopilotSettingsContainer;
    // Adjustable Views
    View adjustableSettingsContainer;
    // Wallpaper Actions
    View wallpaperActionsContainer;
    View shareButton;
    View saveButton;
    View adjustButton;
    View skipButton;
    View settingsButton;

    Timer revealControlsTimer;
    TimerTask revealControlsTimerTask = new TimerTask() {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    animateControls(false);
                    animateSilkScreen(SilkScreenState.DEFAULT, containerForState(currentState));
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.fade_in, 0);
        setContentView(R.layout.activity_lwq_settings);
        currentState = SettingsState.NONE;

        // Setup Autopilot stuff
        setupAutopilotSettings();
        // Setup Adjustable stuff
        setupAdjustableSettings();
        // Setup Wallpaper actions
        setupWallpaperActions();
        // Setup progress bar
        setupProgressBar();
        // Setup touch to dismiss
        setupTouchToDismiss();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        switchToSilkScreen(SilkScreenState.REVEAL, null);
        revealControlsTimer = new Timer();
        revealControlsTimer.schedule(revealControlsTimerTask, DateUtils.SECOND_IN_MILLIS * 3);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    void setupAutopilotSettings() {
        autopilotSettingsContainer = findViewById(R.id.group_lwq_settings_autopilot);
        autopilotSettingsContainer.setAlpha(0f);

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
                R.layout.spinner_item,
                backgroundCategories);
        imageCategoryAdapter.setDropDownViewResource(R.layout.spinner_drop_down_item);
        Spinner imageCategorySpinner = (Spinner) autopilotSettingsContainer.findViewById(R.id.spinner_lwq_autopilot_settings_image_category);
        imageCategorySpinner.setAdapter(imageCategoryAdapter);
        imageCategorySpinner.setSelection(currentSelection);
        imageCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
                LWQPreferences.setImageCategoryPreference(LWQApplication.getWallpaperController().getBackgroundCategories().get(index));
                // TODO toast or the slidy thingy from the bottom that says LWQ will apply settings to your next wallpaper
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        final String [] refreshPreferenceOptions = getResources().getStringArray(R.array.refresh_preference_options);
        ArrayAdapter<String> refreshOptionsAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_item,
                refreshPreferenceOptions);
        refreshOptionsAdapter.setDropDownViewResource(R.layout.spinner_drop_down_item);
        Spinner refreshSpinner = (Spinner) autopilotSettingsContainer.findViewById(R.id.spinner_lwq_autopilot_settings_interval);
        refreshSpinner.setAdapter(refreshOptionsAdapter);
        refreshSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
                final int[] refreshValues = getResources().getIntArray(R.array.refresh_preference_values);
                LWQPreferences.setRefreshPreference(refreshValues[index]);
                LWQAlarmController.resetAlarm();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
        updateRefreshSpinner();

        final List<Category> categories = Category.listAll(Category.class);
        Collections.sort(categories, new Comparator<Category>() {
            @Override
            public int compare(Category categoryA, Category categoryB) {
                return categoryA.name.compareTo(categoryB.name);
            }
        });
        final String quoteCategoryPreference = LWQPreferences.getQuoteCategoryPreference();
        int selectedQuoteCategory = 0;
        final String [] quoteCategoryNames = new String[categories.size()];
        for (int i = 0; i < categories.size(); i++) {
            quoteCategoryNames[i] = categories.get(i).name;
            if (quoteCategoryNames[i].equalsIgnoreCase(quoteCategoryPreference)) {
                selectedQuoteCategory = i;
            }
        }

        ArrayAdapter<String> quoteCategoriesAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_item,
                quoteCategoryNames);
        quoteCategoriesAdapter.setDropDownViewResource(R.layout.spinner_drop_down_item);
        Spinner quoteCategorySpinner = (Spinner) autopilotSettingsContainer.findViewById(R.id.spinner_lwq_autopilot_settings_quote_category);
        quoteCategorySpinner.setAdapter(quoteCategoriesAdapter);
        quoteCategorySpinner.setSelection(selectedQuoteCategory);
        quoteCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
                LWQPreferences.setQuoteCategoryPreference(quoteCategoryNames[index]);
                // TODO toast or the slidy thingy from the bottom that says LWQ will apply settings to your next wallpaper
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        UIUtils.setViewAndChildrenEnabled(autopilotSettingsContainer, false);
    }

    void updateRefreshSpinner() {
        Spinner refreshSpinner = (Spinner) autopilotSettingsContainer.findViewById(R.id.spinner_lwq_autopilot_settings_interval);
        final AdapterView.OnItemSelectedListener onItemSelectedListener = refreshSpinner.getOnItemSelectedListener();
        refreshSpinner.setOnItemSelectedListener(null);
        final long refreshPreference = LWQPreferences.getRefreshPreference();
        final int[] refreshValues = getResources().getIntArray(R.array.refresh_preference_values);
        for (int i = 0; i < refreshValues.length; i++) {
            if (refreshPreference == refreshValues[i]) {
                refreshSpinner.setSelection(i);
                break;
            }
        }
        refreshSpinner.setOnItemSelectedListener(onItemSelectedListener);
    }

    void setupAdjustableSettings() {
        adjustableSettingsContainer = findViewById(R.id.group_lwq_settings_adjustable);
        adjustableSettingsContainer.setAlpha(0f);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            adjustableSettingsContainer.findViewById(R.id.rl_lwq_adjustable_settings_blur).setVisibility(View.GONE);
        } else {
            SeekBar blurBar = (SeekBar) adjustableSettingsContainer.findViewById(R.id.sb_lwq_adjustable_settings_blur);
            blurBar.setProgress(LWQPreferences.getBlurPreference());
            blurBar.setOnSeekBarChangeListener(this);
        }

        SeekBar dimBar = (SeekBar) adjustableSettingsContainer.findViewById(R.id.sb_lwq_adjustable_settings_dim);
        dimBar.setProgress(LWQPreferences.getDimPreference());
        dimBar.setOnSeekBarChangeListener(this);
        UIUtils.setViewAndChildrenEnabled(adjustableSettingsContainer, false);
    }

    void setupWallpaperActions() {
        wallpaperActionsContainer = findViewById(R.id.group_lwq_settings_wallpaper_actions);
        shareButton = wallpaperActionsContainer.findViewById(R.id.btn_wallpaper_actions_share);
        saveButton = wallpaperActionsContainer.findViewById(R.id.btn_wallpaper_actions_save);
        adjustButton = wallpaperActionsContainer.findViewById(R.id.btn_wallpaper_actions_adjust);
        skipButton = wallpaperActionsContainer.findViewById(R.id.btn_wallpaper_actions_skip);
        settingsButton = wallpaperActionsContainer.findViewById(R.id.btn_wallpaper_actions_settings);
        View [] buttons = new View[] {shareButton, saveButton, adjustButton, skipButton, settingsButton};
        for (View button : buttons) {
            button.setAlpha(0f);
            button.setTranslationY(button.getHeight() * 2f);
        }
        adjustButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adjustButton.setSelected(currentState != SettingsState.ADJUSTABLE_SETTINGS);
                if (adjustButton.isSelected()) {
                    settingsButton.setSelected(false);
                    animateToState(SettingsState.ADJUSTABLE_SETTINGS);
                } else {
                    animateToState(SettingsState.NONE);
                }
            }
        });
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settingsButton.setSelected(currentState != SettingsState.AUTOPILOT_SETTINGS);
                if (settingsButton.isSelected()) {
                    adjustButton.setSelected(false);
                    animateToState(SettingsState.AUTOPILOT_SETTINGS);
                } else {
                    animateToState(SettingsState.NONE);
                }
            }
        });
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendBroadcast(new Intent(getString(R.string.action_share)));
            }
        });
    }

    void setupProgressBar() {
        progressBar = (ProgressBar) findViewById(R.id.pb_lwq_settings);
        progressBar.setAlpha(0f);
    }

    void setupTouchToDismiss() {
        findViewById(android.R.id.content).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (revealControlsTimerTask.scheduledExecutionTime() > 0) {
                    revealControlsTimerTask.cancel();
                }
                animateSilkScreen(silkScreenState.flip(), null);
                animateToState(SettingsState.NONE);
                animateControls(controlsVisible);
                settingsButton.setSelected(false);
                adjustButton.setSelected(false);
            }
        });
    }

    void animateControls(boolean dismiss) {
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(generateAnimator(shareButton, dismiss, 0),
                generateAnimator(saveButton, dismiss, 15),
                generateAnimator(adjustButton, dismiss, 30),
                generateAnimator(skipButton, dismiss, 45),
                generateAnimator(settingsButton, dismiss, 60));
        animatorSet.start();
        controlsVisible = !dismiss;
    }

    AnimatorSet generateAnimator(View target, boolean dismiss, long startDelay) {
        float [] fadeIn = new float[] {0f, 1f};
        float [] fadeOut = new float[] {1f, 0f};
        final ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(target, "alpha", dismiss ? fadeOut : fadeIn);
        alphaAnimator.setDuration(240);
        alphaAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        final ObjectAnimator translationAnimator = ObjectAnimator.ofFloat(target, "translationY", dismiss ? (target.getHeight() * 2f) : 0f);
        translationAnimator.setDuration(240);
        translationAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setStartDelay(startDelay);
        animatorSet.playTogether(alphaAnimator, translationAnimator);
        return animatorSet;
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
            case AUTOPILOT_SETTINGS:
                return autopilotSettingsContainer;
            case ADJUSTABLE_SETTINGS:
                return adjustableSettingsContainer;
            default:
                return null;
        }
    }

    @Override
    public void onEvent(PreferenceUpdateEvent preferenceUpdateEvent) {
        super.onEvent(preferenceUpdateEvent);
        if (preferenceUpdateEvent.getPreferenceKeyId() == R.string.preference_key_refresh) {
            updateRefreshSpinner();
        }
    }

    // SeekBar Listener

    @Override
    public void onProgressChanged(SeekBar seekBar, int value, boolean b) {
        if (seekBar.getId() == R.id.sb_lwq_adjustable_settings_blur) {
            LWQPreferences.setBlurPreference(value);
        } else {
            LWQPreferences.setDimPreference(value);
        }
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