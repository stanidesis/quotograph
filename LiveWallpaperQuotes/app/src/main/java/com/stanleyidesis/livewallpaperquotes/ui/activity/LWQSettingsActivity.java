package com.stanleyidesis.livewallpaperquotes.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.percent.PercentRelativeLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewAnimationUtils;
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
import com.stanleyidesis.livewallpaperquotes.api.event.ImageSaveEvent;
import com.stanleyidesis.livewallpaperquotes.api.event.PreferenceUpdateEvent;
import com.stanleyidesis.livewallpaperquotes.api.event.WallpaperEvent;
import com.stanleyidesis.livewallpaperquotes.ui.UIUtils;
import com.stanleyidesis.livewallpaperquotes.ui.adapter.PlaylistAdapter;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

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
public class LWQSettingsActivity extends LWQWallpaperActivity implements StateFlags, SeekBar.OnSeekBarChangeListener {

    static class ActivityState {

        LWQWallpaperActivity.SilkScreenState silkScreenState = null;
        int controlFlags = FLAG_NO_CHANGE;
        int settingsFlags = FLAG_NO_CHANGE;
        int playlistFlags = FLAG_NO_CHANGE;
        int FABFlags = FLAG_NO_CHANGE;
        int actionShareFlags = FLAG_NO_CHANGE;
        int actionSaveFlags = FLAG_NO_CHANGE;
        int actionPlaylistFlags = FLAG_NO_CHANGE;
        int actionSkipFlags = FLAG_NO_CHANGE;
        int actionSettingsFlags = FLAG_NO_CHANGE;
        int progressBarFlags = FLAG_NO_CHANGE;
        int contentFlags = FLAG_NO_CHANGE;

        boolean controlFlagSet(int compareWith) {
            return (controlFlags & compareWith) > 0;
        }

        boolean playlistFlagSet(int compareWith) {
            return (playlistFlags & compareWith) > 0;
        }

        boolean FABFlagSet(int compareWith) {
            return (FABFlags & compareWith) > 0;
        }

        boolean settingsFlagSet(int compareWith) {
            return (settingsFlags & compareWith) > 0;
        }

        boolean progressBarFlagsSet(int compareWith) {
            return (progressBarFlags & compareWith) > 0;
        }

    }

    static class Builder {

        ActivityState activityState;

        ActivityState build() {
            return activityState;
        }

        private Builder() {
            activityState = new ActivityState();
        }

        Builder setSilkScreenState(LWQWallpaperActivity.SilkScreenState silkScreenState) {
            activityState.silkScreenState = silkScreenState;
            return this;
        }

        Builder setControlFlags(int flags) {
            activityState.controlFlags = flags;
            return this;
        }

        Builder setPlaylistFlags(int flags) {
            activityState.playlistFlags = flags;
            return this;
        }

        Builder setFABFlags(int flags) {
            activityState.FABFlags = flags;
            return this;
        }

        Builder setSettingsFlags(int flags) {
            activityState.settingsFlags = flags;
            return this;
        }

        Builder setActionShareFlags(int flags) {
            activityState.actionShareFlags = flags;
            return this;
        }

        Builder setActionSaveFlags(int flags) {
            activityState.actionSaveFlags = flags;
            return this;
        }

        Builder setActionPlaylistFlags(int flags) {
            activityState.actionPlaylistFlags = flags;
            return this;
        }

        Builder setActionSkipFlags(int flags) {
            activityState.actionSkipFlags = flags;
            return this;
        }

        Builder setActionSettingsFlags(int flags) {
            activityState.actionSettingsFlags = flags;
            return this;
        }

        Builder setProgressBarFlags(int flags) {
            activityState.progressBarFlags = flags;
            return this;
        }

        Builder setContentFlags(int flags) {
            activityState.contentFlags = flags;
            return this;
        }

        static ActivityState buildInitialState() {
            final Builder builder = new Builder();
            return builder.setSilkScreenState(LWQWallpaperActivity.SilkScreenState.HIDDEN)
                    .setControlFlags(FLAG_HIDE | FLAG_DISABLE)
                    .setPlaylistFlags(FLAG_HIDE | FLAG_DISABLE)
                    .setSettingsFlags(FLAG_HIDE | FLAG_DISABLE)
                    .setFABFlags(FLAG_HIDE | FLAG_DISABLE)
                    .setActionPlaylistFlags(FLAG_UNSELECTED)
                    .setActionSettingsFlags(FLAG_UNSELECTED)
                    .setProgressBarFlags(FLAG_HIDE).build();
        }

        static ActivityState buildRevealControls() {
            final Builder builder = new Builder();
            return builder.setSilkScreenState(LWQWallpaperActivity.SilkScreenState.OBSCURED)
                    .setControlFlags(FLAG_REVEAL | FLAG_ENABLE)
                    .setPlaylistFlags(FLAG_HIDE | FLAG_DISABLE)
                    .setSettingsFlags(FLAG_HIDE | FLAG_DISABLE)
                    .setFABFlags(FLAG_HIDE | FLAG_DISABLE)
                    .build();
        }

        static ActivityState buildRevealWallpaper() {
            final Builder builder = new Builder();
            return builder.setSilkScreenState(LWQWallpaperActivity.SilkScreenState.REVEALED)
                    .setControlFlags(FLAG_HIDE | FLAG_DISABLE)
                    .setPlaylistFlags(FLAG_HIDE | FLAG_DISABLE)
                    .setFABFlags(FLAG_HIDE | FLAG_DISABLE)
                    .setSettingsFlags(FLAG_HIDE | FLAG_DISABLE)
                    .setActionPlaylistFlags(FLAG_UNSELECTED)
                    .setActionSettingsFlags(FLAG_UNSELECTED)
                    .build();
        }

        static ActivityState buildRevealPlaylist() {
            final Builder builder = new Builder();
            return builder.setSilkScreenState(LWQWallpaperActivity.SilkScreenState.OBSCURED)
                    .setPlaylistFlags(FLAG_REVEAL | FLAG_ENABLE)
                    .setFABFlags(FLAG_REVEAL | FLAG_ENABLE)
                    .setSettingsFlags(FLAG_HIDE | FLAG_DISABLE)
                    .setActionPlaylistFlags(FLAG_SELECTED)
                    .setActionSettingsFlags(FLAG_UNSELECTED)
                    .build();
        }

        static ActivityState buildRevealSettings() {
            final Builder builder = new Builder();
            return builder.setSilkScreenState(LWQWallpaperActivity.SilkScreenState.OBSCURED)
                    .setControlFlags(FLAG_REVEAL | FLAG_ENABLE)
                    .setPlaylistFlags(FLAG_HIDE | FLAG_DISABLE)
                    .setSettingsFlags(FLAG_REVEAL | FLAG_ENABLE)
                    .setFABFlags(FLAG_HIDE | FLAG_DISABLE)
                    .setActionPlaylistFlags(FLAG_UNSELECTED)
                    .setActionSettingsFlags(FLAG_SELECTED)
                    .build();
        }

        static ActivityState buildRevealWallpaperEditMode() {
            final Builder builder = new Builder();
            return builder.setSilkScreenState(LWQWallpaperActivity.SilkScreenState.REVEALED)
                    .setControlFlags(FLAG_HIDE | FLAG_DISABLE)
                    .build();
        }

        static ActivityState buildRevealSaveToDisk() {
            final Builder builder = new Builder();
            return builder.setSilkScreenState(LWQWallpaperActivity.SilkScreenState.OBSCURED)
                    .setActionSaveFlags(FLAG_DISABLE | FLAG_ROTATE)
                    .setActionSkipFlags(FLAG_DISABLE)
                    .setProgressBarFlags(FLAG_REVEAL)
                    .build();
        }

        static ActivityState buildSaveToDiskComplete() {
            final Builder builder = new Builder();
            return builder.setSilkScreenState(LWQWallpaperActivity.SilkScreenState.OBSCURED)
                    .setActionSaveFlags(FLAG_ENABLE | FLAG_NO_ROTATE)
                    .setActionSkipFlags(FLAG_ENABLE)
                    .setProgressBarFlags(FLAG_HIDE)
                    .build();
        }

        static ActivityState buildRevealSkip() {
            final Builder builder = new Builder();
            return builder.setSilkScreenState(LWQWallpaperActivity.SilkScreenState.HIDDEN)
                    .setActionSaveFlags(FLAG_DISABLE)
                    .setActionSkipFlags(FLAG_DISABLE | FLAG_ROTATE)
                    .setActionPlaylistFlags(FLAG_DISABLE | FLAG_UNSELECTED)
                    .setActionSettingsFlags(FLAG_DISABLE | FLAG_UNSELECTED)
                    .setActionShareFlags(FLAG_DISABLE)
                    .setContentFlags(FLAG_DISABLE)
                    .setPlaylistFlags(FLAG_HIDE | FLAG_DISABLE)
                    .setSettingsFlags(FLAG_HIDE | FLAG_DISABLE)
                    .setFABFlags(FLAG_HIDE | FLAG_DISABLE)
                    .setProgressBarFlags(FLAG_REVEAL)
                    .build();
        }

        static ActivityState buildRevealSkipCompleted() {
            final Builder builder = new Builder();
            return builder.setSilkScreenState(SilkScreenState.REVEALED)
                    .setActionShareFlags(FLAG_ENABLE)
                    .setActionSaveFlags(FLAG_ENABLE)
                    .setActionPlaylistFlags(FLAG_ENABLE)
                    .setActionSkipFlags(FLAG_ENABLE | FLAG_NO_ROTATE)
                    .setActionSettingsFlags(FLAG_ENABLE)
                    .setContentFlags(FLAG_ENABLE)
                    .setProgressBarFlags(FLAG_HIDE)
                    .build();
        }
    }

    Runnable changeStateRunnable = new Runnable() {
        @Override
        public void run() {
            if (stateBlockingDeque.isEmpty()) {
                return;
            }
            ActivityState nextActivityState = null;
            try {
                nextActivityState = stateBlockingDeque.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (nextActivityState == null || nextActivityState == activityState) {
                return;
            }

            long longestAnimation = 0l;

            // SilkScreenState
            final SilkScreenState newSilkScreenState = nextActivityState.silkScreenState;
            if (LWQSettingsActivity.this.silkScreenState != newSilkScreenState) {
                longestAnimation = 300l; // TODO HAX
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        animateSilkScreen(newSilkScreenState);
                    }
                });
            }

            // Content flags
            int contentFlags = (int) findViewById(android.R.id.content).getTag(R.id.view_tag_flags);
            if (nextActivityState.contentFlags != contentFlags && nextActivityState.contentFlags != FLAG_NO_CHANGE) {
                // Enable/disable
                if ((nextActivityState.contentFlags & FLAG_ENABLE) > 0 || (nextActivityState.contentFlags & FLAG_DISABLE) > 0) {
                    final boolean enabled = (nextActivityState.contentFlags & FLAG_ENABLE) > 0;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setupContent(enabled);
                        }
                    });
                }
                findViewById(android.R.id.content).setTag(R.id.view_tag_flags, contentFlags);
            }

            // Control flags
            int controlFlags = (int) wallpaperActionsContainer.getTag(R.id.view_tag_flags);
            if (nextActivityState.controlFlags != controlFlags && nextActivityState.controlFlags != FLAG_NO_CHANGE) {
                // Enable/disable
                if (nextActivityState.controlFlagSet(FLAG_DISABLE) || nextActivityState.controlFlagSet(FLAG_ENABLE)) {
                    final boolean enabled = nextActivityState.controlFlagSet(FLAG_ENABLE);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            wallpaperActionsContainer.setEnabled(enabled);
                        }
                    });
                }
                // Reveal/Hide
                if (nextActivityState.controlFlagSet(FLAG_HIDE) || nextActivityState.controlFlagSet(FLAG_REVEAL)) {
                    longestAnimation = Math.max(longestAnimation, 300l); // TODO HAX
                    final boolean dismiss = nextActivityState.controlFlagSet(FLAG_HIDE);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            animateControls(dismiss);
                        }
                    });
                }
                wallpaperActionsContainer.setTag(R.id.view_tag_flags, nextActivityState.controlFlags);
            }

            // Settings flags
            int settingsFlags = (int) settingsContainer.getTag(R.id.view_tag_flags);
            if (nextActivityState.settingsFlags != settingsFlags && nextActivityState.settingsFlags != FLAG_NO_CHANGE) {
                // Enable/disable
                if (nextActivityState.settingsFlagSet(FLAG_DISABLE) || nextActivityState.settingsFlagSet(FLAG_ENABLE)) {
                    final boolean enable = nextActivityState.settingsFlagSet(FLAG_ENABLE);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            UIUtils.setViewAndChildrenEnabled(settingsContainer, enable);
                        }
                    });
                }
                // Reveal/Hide
                if (nextActivityState.settingsFlagSet(FLAG_HIDE) || nextActivityState.settingsFlagSet(FLAG_REVEAL)) {
                    final boolean dismiss = nextActivityState.settingsFlagSet(FLAG_HIDE);
                    longestAnimation = Math.max(longestAnimation, dismiss ? 300l : 600l); // TODO HAX
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            animateContainer(settingsContainer, dismiss);
                        }
                    });
                }
                settingsContainer.setTag(R.id.view_tag_flags, nextActivityState.settingsFlags);
            }

            // Playlist flags
            int playlistFlags = (int) playlistContainer.getTag(R.id.view_tag_flags);
            if (nextActivityState.playlistFlags != playlistFlags && nextActivityState.playlistFlags != FLAG_NO_CHANGE) {
                // Enable/disable
                if (nextActivityState.playlistFlagSet(FLAG_DISABLE) || nextActivityState.playlistFlagSet(FLAG_ENABLE)) {
                    final boolean enable = nextActivityState.playlistFlagSet(FLAG_ENABLE);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            UIUtils.setViewAndChildrenEnabled(playlistContainer, enable);
                        }
                    });
                }
                // Reveal/Hide
                if (nextActivityState.playlistFlagSet(FLAG_HIDE) || nextActivityState.playlistFlagSet(FLAG_REVEAL)) {
                    final boolean dismiss = nextActivityState.playlistFlagSet(FLAG_HIDE);
                    longestAnimation = Math.max(longestAnimation, dismiss ? 300l : 600l); // TODO HAX
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            animateContainer(playlistContainer, dismiss);
                        }
                    });
                }
                playlistContainer.setTag(R.id.view_tag_flags, nextActivityState.playlistFlags);
            }

            // FAB flags
            int FABFlags = (int) fab.getTag(R.id.view_tag_flags);
            if (nextActivityState.FABFlags != FABFlags && nextActivityState.FABFlags != FLAG_NO_CHANGE) {
                // Enable/disable
                if (nextActivityState.FABFlagSet(FLAG_DISABLE) || nextActivityState.FABFlagSet(FLAG_ENABLE)) {
                    final boolean enable = nextActivityState.FABFlagSet(FLAG_ENABLE);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            UIUtils.setViewAndChildrenEnabled(fab, enable);
                        }
                    });
                }
                // Reveal/Hide
                if (nextActivityState.FABFlagSet(FLAG_HIDE) || nextActivityState.FABFlagSet(FLAG_REVEAL)) {
                    final boolean dismiss = nextActivityState.FABFlagSet(FLAG_HIDE);
                    longestAnimation = Math.max(longestAnimation, 200l); // TODO HAX
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            animatePlaylistFAB(dismiss).start();
                        }
                    });
                }
                fab.setTag(R.id.view_tag_flags, nextActivityState.FABFlags);
            }

            // Actions
            int [] actionButtonFlags = new int [] {nextActivityState.actionShareFlags,
                    nextActivityState.actionSaveFlags, nextActivityState.actionPlaylistFlags,
                    nextActivityState.actionSkipFlags, nextActivityState.actionSettingsFlags};
            View [] actionViews = new View[] {shareButton, saveButton, playlistButton, skipButton, settingsButton};
            for (int i = 0; i < actionButtonFlags.length; i++) {
                final View actionView = actionViews[i];
                int newButtonFlags = actionButtonFlags[i];
                int buttonFlags = (int) actionView.getTag(R.id.view_tag_flags);
                if (buttonFlags == newButtonFlags || newButtonFlags == FLAG_NO_CHANGE) {
                    continue;
                }
                // Enable/disable
                final boolean disable = (newButtonFlags & FLAG_DISABLE) > 0;
                final boolean enable = (newButtonFlags & FLAG_ENABLE) > 0;
                if (disable || enable) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            actionView.setEnabled(enable);
                        }
                    });
                }

                // Selected/Unselected
                final boolean selected = (newButtonFlags & FLAG_SELECTED) > 0;
                final boolean unselected = (newButtonFlags & FLAG_UNSELECTED) > 0;
                if ((selected || unselected) && (actionView.isSelected() != selected)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            actionView.setSelected(selected);
                        }
                    });
                }

                // Rotate/Stop rotate
                boolean rotate = (newButtonFlags & FLAG_ROTATE) > 0;
                boolean stopRotate = (newButtonFlags & FLAG_NO_ROTATE) > 0;
                if (stopRotate) {
                    final Object tag = actionView.getTag(R.id.view_tag_animator);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (tag instanceof Animator) {
                                Animator animator = (Animator) tag;
                                animator.end();
                            }
                        }
                    });
                    actionView.setTag(R.id.view_tag_animator, null);
                } else if (rotate) {
                    final Animator rotationAnimator = AnimatorInflater.loadAnimator(LWQSettingsActivity.this, R.animator.progress_rotation);
                    rotationAnimator.setTarget(actionViews[i]);
                    actionView.setTag(R.id.view_tag_animator, rotationAnimator);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final Object tag = actionView.getTag(R.id.view_tag_animator);
                            if (tag instanceof Animator) {
                                ((Animator) tag).start();
                            }
                        }
                    });
                }
                actionView.setTag(R.id.view_tag_flags, actionButtonFlags[i]);
            }

            // Progress bar
            int progressBarFlags = (int) progressBar.getTag(R.id.view_tag_flags);
            if (nextActivityState.progressBarFlags != progressBarFlags && nextActivityState.progressBarFlags != FLAG_NO_CHANGE) {
                // Reveal/Hide
                if (nextActivityState.progressBarFlagsSet(FLAG_HIDE) || nextActivityState.progressBarFlagsSet(FLAG_REVEAL)) {
                    final boolean dismiss = nextActivityState.progressBarFlagsSet(FLAG_HIDE);
                    longestAnimation = Math.max(longestAnimation, 150l); // TODO HAX
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.animate()
                                    .alpha(dismiss ? 0f : 1f)
                                    .setDuration(150)
                                    .setInterpolator(new AccelerateDecelerateInterpolator())
                                    .start();
                        }
                    });
                }
                progressBar.setTag(R.id.view_tag_flags, nextActivityState.progressBarFlags);
            }
            activityState = nextActivityState;
            try {
                Thread.sleep(longestAnimation);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    ActivityState initialState = Builder.buildInitialState();
    ActivityState revealControlsState = Builder.buildRevealControls();
    ActivityState revealPlaylistState = Builder.buildRevealPlaylist();
    ActivityState revealSaveToDiskState = Builder.buildRevealSaveToDisk();
    ActivityState revealSaveToDiskCompletedState = Builder.buildSaveToDiskComplete();
    ActivityState revealSettingsState = Builder.buildRevealSettings();
    ActivityState revealSkipState = Builder.buildRevealSkip();
    ActivityState revealSkipCompletedState = Builder.buildRevealSkipCompleted();
    ActivityState revealWallpaperState = Builder.buildRevealWallpaper();
    ActivityState revealWallpaperEditModeState  = Builder.buildRevealWallpaperEditMode();

    // Current ActivityState
    ActivityState activityState = null;
    // State Queue
    BlockingDeque<ActivityState> stateBlockingDeque = new LinkedBlockingDeque<>();
    // Executes state changes
    ExecutorService changeStateExecutorService = Executors.newSingleThreadScheduledExecutor();

    Timer revealControlsTimer;
    TimerTask revealControlsTimerTask = new TimerTask() {
        @Override
        public void run() {
            changeState(revealControlsState);
        }
    };

    // ProgressBar
    ProgressBar progressBar;
    // Playlist
    View playlistContainer;
    // FAB
    View fab;
    // Settings
    View settingsContainer;
    // Wallpaper Actions
    View wallpaperActionsContainer;
    View shareButton;
    View saveButton;
    View playlistButton;
    View skipButton;
    View settingsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.fade_in, 0);
        setContentView(R.layout.activity_lwq_settings);

        // Setup FAB
        setupFAB();
        // Setup playlist
        setupPlaylist();
        // Setup settings
        setupSettings();
        // Setup Wallpaper actions
        setupWallpaperActions();
        // Setup progress bar
        setupProgressBar();
        // Setup content
        setupContent(true);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (LWQApplication.getWallpaperController().activeWallpaperLoaded()) {
            changeState(revealWallpaperState);
        } else {
            changeState(initialState);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    void setupFAB() {
        fab = findViewById(R.id.fab_lwq_settings);
        fab.setTranslationY(fab.getHeight() * 2);
        fab.setAlpha(0f);
        fab.setVisibility(View.INVISIBLE);
        fab.setTag(R.id.view_tag_flags, FLAG_HIDE | FLAG_DISABLE);
    }

    void setupPlaylist() {
        playlistContainer = findViewById(R.id.group_lwq_settings_playlist);
        playlistContainer.setAlpha(0f);
        playlistContainer.setVisibility(View.INVISIBLE);
        playlistContainer.setTag(R.id.view_tag_flags, FLAG_HIDE | FLAG_DISABLE);

        RecyclerView recyclerView = (RecyclerView) playlistContainer.findViewById(R.id.recycler_playlist);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new PlaylistAdapter());
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        UIUtils.setViewAndChildrenEnabled(playlistContainer, false);
    }

    void setupSettings() {
        settingsContainer = findViewById(R.id.group_lwq_settings_settings);
        settingsContainer.setAlpha(0f);
        settingsContainer.setVisibility(View.INVISIBLE);
        settingsContainer.setTag(R.id.view_tag_flags, FLAG_HIDE | FLAG_DISABLE);
        UIUtils.setViewAndChildrenEnabled(settingsContainer, false);

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
        Spinner imageCategorySpinner = (Spinner) settingsContainer.findViewById(R.id.spinner_lwq_autopilot_settings_image_category);
        imageCategorySpinner.setAdapter(imageCategoryAdapter);
        imageCategorySpinner.setSelection(currentSelection);
        imageCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
                LWQPreferences.setImageCategoryPreference(LWQApplication.getWallpaperController().getBackgroundCategories().get(index));
                // TODO toast or the slidy thingy from the bottom that says LWQ will apply settings to your next wallpaper
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        final String [] refreshPreferenceOptions = getResources().getStringArray(R.array.refresh_preference_options);
        ArrayAdapter<String> refreshOptionsAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_item,
                refreshPreferenceOptions);
        refreshOptionsAdapter.setDropDownViewResource(R.layout.spinner_drop_down_item);
        Spinner refreshSpinner = (Spinner) settingsContainer.findViewById(R.id.spinner_lwq_autopilot_settings_interval);
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

        // Blur
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            settingsContainer.findViewById(R.id.rl_lwq_settings_blur).setVisibility(View.GONE);
        } else {
            SeekBar blurBar = (SeekBar) settingsContainer.findViewById(R.id.sb_lwq_settings_blur);
            blurBar.setProgress(LWQPreferences.getBlurPreference());
            blurBar.setOnSeekBarChangeListener(this);
        }

        // Dim
        SeekBar dimBar = (SeekBar) settingsContainer.findViewById(R.id.sb_lwq_settings_dim);
        dimBar.setProgress(LWQPreferences.getDimPreference());
        dimBar.setOnSeekBarChangeListener(this);
        UIUtils.setViewAndChildrenEnabled(settingsContainer, false);
    }

    void updateRefreshSpinner() {
        Spinner refreshSpinner = (Spinner) settingsContainer.findViewById(R.id.spinner_lwq_autopilot_settings_interval);
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

    void setupWallpaperActions() {
        wallpaperActionsContainer = findViewById(R.id.group_lwq_settings_wallpaper_actions);
        wallpaperActionsContainer.setTag(R.id.view_tag_flags, FLAG_HIDE | FLAG_DISABLE);
        wallpaperActionsContainer.setEnabled(false);
        final PercentRelativeLayout.LayoutParams layoutParams = (PercentRelativeLayout.LayoutParams) wallpaperActionsContainer.getLayoutParams();
        layoutParams.bottomMargin = (int) (UIUtils.getNavBarHeight(this) * 1.3);
        wallpaperActionsContainer.setLayoutParams(layoutParams);

        shareButton = wallpaperActionsContainer.findViewById(R.id.btn_wallpaper_actions_share);
        saveButton = wallpaperActionsContainer.findViewById(R.id.btn_wallpaper_actions_save);
        playlistButton = wallpaperActionsContainer.findViewById(R.id.btn_wallpaper_actions_adjust);
        skipButton = wallpaperActionsContainer.findViewById(R.id.btn_wallpaper_actions_skip);
        settingsButton = wallpaperActionsContainer.findViewById(R.id.btn_wallpaper_actions_settings);
        View [] buttons = new View[] {shareButton, saveButton, playlistButton, skipButton, settingsButton};
        for (View button : buttons) {
            button.setAlpha(0f);
            button.setTranslationY(button.getHeight() * 2f);
            button.setTag(R.id.view_tag_flags, FLAG_ENABLE);
        }
        playlistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activityState != revealPlaylistState) {
                    view.setSelected(true);
                    changeState(revealPlaylistState);
                } else {
                    view.setSelected(false);
                    changeState(revealControlsState);
                }
            }
        });
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activityState != revealSettingsState) {
                    view.setSelected(true);
                    changeState(revealSettingsState);
                } else {
                    view.setSelected(false);
                    changeState(revealControlsState);
                }
            }
        });
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendBroadcast(new Intent(getString(R.string.action_share)));
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeState(revealSaveToDiskState);
                sendBroadcast(new Intent(getString(R.string.action_save)));
            }
        });
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeState(revealSkipState);
                LWQApplication.getWallpaperController().generateNewWallpaper();
            }
        });
    }

    void setupProgressBar() {
        progressBar = (ProgressBar) findViewById(R.id.pb_lwq_settings);
        progressBar.setAlpha(0f);
        progressBar.setTag(R.id.view_tag_flags, FLAG_HIDE);
    }

    void setupContent(boolean enable) {
        View content = findViewById(android.R.id.content);
        content.setTag(R.id.view_tag_flags, enable ? FLAG_ENABLE : FLAG_DISABLE);
        content.setEnabled(enable);
        content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (revealControlsTimerTask.scheduledExecutionTime() > 0) {
                    revealControlsTimerTask.cancel();
                }
                if (activityState == revealWallpaperState) {
                    changeState(revealControlsState);
                } else {
                    changeState(revealWallpaperState);
                }
            }
        });
    }

    void animateContainer(final View container, final boolean dismiss) {
        container.setVisibility(View.VISIBLE);
        Animator animator = dismiss ? AnimatorInflater.loadAnimator(this, R.animator.exit_to_left) :
                AnimatorInflater.loadAnimator(this, R.animator.enter_from_right);
        animator.setTarget(container);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (dismiss) {
                    container.setVisibility(View.INVISIBLE);
                }
            }
        });
        animator.start();
    }

    void animateControls(boolean dismiss) {
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(generateAnimator(shareButton, dismiss, 0),
                generateAnimator(saveButton, dismiss, 15),
                generateAnimator(playlistButton, dismiss, 30),
                generateAnimator(skipButton, dismiss, 45),
                generateAnimator(settingsButton, dismiss, 60));
        animatorSet.start();
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

    Animator animatePlaylistFAB(final boolean dismiss) {
        fab.setVisibility(View.VISIBLE);
        float [] toFrom = dismiss ? new float[] {1f, .2f} : new float[] {.2f, 1f};
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                fab.setVisibility(dismiss ? View.INVISIBLE : View.VISIBLE);
            }
        });
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(fab, "alpha", toFrom);
        alphaAnimator.setDuration(200);
        alphaAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        if (Build.VERSION.SDK_INT < 21) {
            final int centerXY = fab.getMeasuredHeight();
            final int maxRadius = fab.getWidth() / 2;
            final Animator circularReveal = ViewAnimationUtils.createCircularReveal(fab, centerXY, centerXY,
                    dismiss ? maxRadius : 0,
                    dismiss ? 0 : maxRadius);
            circularReveal.setInterpolator(new AccelerateDecelerateInterpolator());
            circularReveal.setDuration(200);
            circularReveal.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (dismiss) {
                        fab.setVisibility(View.INVISIBLE);
                    }
                }
            });
            animatorSet.playTogether(circularReveal, alphaAnimator);
            return animatorSet;
        }

        // Create a scale + fade animation
        ObjectAnimator scaleDown = ObjectAnimator.ofPropertyValuesHolder(fab,
                PropertyValuesHolder.ofFloat("scaleX", toFrom),
                PropertyValuesHolder.ofFloat("scaleY", toFrom));
        scaleDown.setDuration(200);
        scaleDown.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.playTogether(scaleDown, alphaAnimator);
        return animatorSet;
    }

    void changeState(ActivityState activityState) {
        try {
            stateBlockingDeque.put(activityState);
            changeStateExecutorService.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        changeStateRunnable.run();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    void didFinishDrawing() {
        if (activityState == initialState) {
            changeState(revealWallpaperState);
        } else if (activityState == revealSkipState) {
            changeState(revealSkipCompletedState);
        }
        if (revealControlsTimer == null) {
            revealControlsTimer = new Timer();
            revealControlsTimer.schedule(revealControlsTimerTask, DateUtils.SECOND_IN_MILLIS * 3);
        }
    }

    @Override
    public void onEvent(PreferenceUpdateEvent preferenceUpdateEvent) {
        super.onEvent(preferenceUpdateEvent);
        if (preferenceUpdateEvent.getPreferenceKeyId() == R.string.preference_key_refresh) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateRefreshSpinner();
                }
            });
        }
    }

    @Override
    public void onEvent(final WallpaperEvent wallpaperEvent) {
        super.onEvent(wallpaperEvent);
        if (wallpaperEvent.didFail() && activityState == revealSkipState) {
            changeState(revealSkipCompletedState);
        } else if (wallpaperEvent.getStatus() != WallpaperEvent.Status.RETRIEVED_WALLPAPER) {
            changeState(revealSkipState);
        }
    }

    public void onEvent(ImageSaveEvent imageSaveEvent) {
        changeState(revealSaveToDiskCompletedState);
    }

    // SeekBar Listener

    @Override
    public void onProgressChanged(SeekBar seekBar, int value, boolean b) {
        if (seekBar.getId() == R.id.sb_lwq_settings_blur) {
            LWQPreferences.setBlurPreference(value);
        } else {
            LWQPreferences.setDimPreference(value);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        changeState(revealWallpaperEditModeState);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        changeState(revealSettingsState);
    }

}