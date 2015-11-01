package com.stanleyidesis.livewallpaperquotes.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.orm.StringUtil;
import com.orm.SugarRecord;
import com.orm.query.Select;
import com.stanleyidesis.livewallpaperquotes.LWQApplication;
import com.stanleyidesis.livewallpaperquotes.LWQPreferences;
import com.stanleyidesis.livewallpaperquotes.R;
import com.stanleyidesis.livewallpaperquotes.api.controller.LWQAlarmController;
import com.stanleyidesis.livewallpaperquotes.api.db.Author;
import com.stanleyidesis.livewallpaperquotes.api.db.Playlist;
import com.stanleyidesis.livewallpaperquotes.api.db.PlaylistQuote;
import com.stanleyidesis.livewallpaperquotes.api.db.Quote;
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

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

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
public class LWQSettingsActivity extends LWQWallpaperActivity implements ActivityStateFlags, SeekBar.OnSeekBarChangeListener, PlaylistAdapter.Delegate {

    static class ActivityState {

        State state = null;
        int controlFlags = FLAG_NO_CHANGE;
        int FABFlags = FLAG_NO_CHANGE;
        int FABActionFlags = FLAG_NO_CHANGE;
        int progressBarFlags = FLAG_NO_CHANGE;
        int contentFlags = FLAG_NO_CHANGE;
        int addEditQuoteFlags = FLAG_NO_CHANGE;

        boolean controlFlagSet(int compareWith) {
            return (controlFlags & compareWith) > 0;
        }

        boolean FABFlagSet(int compareWith) {
            return (FABFlags & compareWith) > 0;
        }


        boolean FABActionFlagSet(int compareWith) {
            return (FABActionFlags & compareWith) > 0;
        }

        boolean progressBarFlagsSet(int compareWith) {
            return (progressBarFlags & compareWith) > 0;
        }

        boolean addEditQuoteFlagsSet(int compareWith) {
            return (addEditQuoteFlags & compareWith) > 0;
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

        Builder setSilkScreenState(State state) {
            activityState.state = state;
            return this;
        }

        Builder setControlFlags(int flags) {
            activityState.controlFlags = flags;
            return this;
        }

        Builder setFABFlags(int flags) {
            activityState.FABFlags = flags;
            return this;
        }

        Builder setFABActionFlags(int flags) {
            activityState.FABActionFlags = flags;
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

        Builder setAddEditQuoteFlags(int flags) {
            activityState.addEditQuoteFlags = flags;
            return this;
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
            final State newState = nextActivityState.state;
            if (LWQSettingsActivity.this.state != newState && newState != null) {
                longestAnimation = 300l; // TODO HAX
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        animateState(newState);
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
                // Hide/reveal
                final boolean dismiss = nextActivityState.controlFlagSet(FLAG_HIDE);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        animateWallpaperActions(dismiss);
                    }
                });
                wallpaperActionsContainer.setTag(R.id.view_tag_flags, nextActivityState.controlFlags);
            }

            // FAB flags
            int FABFlags = (int) fab.getTag(R.id.view_tag_flags);
            if (nextActivityState.FABFlags != FABFlags && nextActivityState.FABFlags != FLAG_NO_CHANGE) {
                // Enable/disable
                int enableDisableFlags = nextActivityState.FABFlags & (FLAG_ENABLE | FLAG_DISABLE);
                if ((FABFlags & enableDisableFlags) == 0 && enableDisableFlags > 0) {
                    final boolean enable = nextActivityState.FABFlagSet(FLAG_ENABLE);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            fab.setEnabled(enable);
                        }
                    });
                    FABFlags &= ~(FLAG_ENABLE | FLAG_DISABLE);
                    FABFlags |= enableDisableFlags;
                }
                // Reveal/Hide
                int revealHideFlags = nextActivityState.FABFlags & (FLAG_REVEAL | FLAG_HIDE);
                if ((FABFlags & revealHideFlags) == 0 && revealHideFlags > 0) {
                    final boolean dismiss = nextActivityState.FABFlagSet(FLAG_HIDE);
                    longestAnimation = Math.max(longestAnimation, 200l); // TODO HAX
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            animateFAB(fab, dismiss).start();
                        }
                    });
                    FABFlags &= ~(FLAG_REVEAL | FLAG_HIDE);
                    FABFlags |= revealHideFlags;
                }
                // Rotate/No Rotate
                int rotateNoRotateFlags = nextActivityState.FABFlags & (FLAG_ROTATE | FLAG_NO_ROTATE);
                if ((FABFlags & rotateNoRotateFlags) == 0 && rotateNoRotateFlags > 0) {
                    final boolean rotate = nextActivityState.FABFlagSet(FLAG_ROTATE);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            animateFABRotation(rotate).start();
                        }
                    });
                    FABFlags &= ~(FLAG_ROTATE | FLAG_NO_ROTATE);
                    FABFlags |= rotateNoRotateFlags;
                }
                fab.setTag(R.id.view_tag_flags, FABFlags);
            }

            // FAB Action Flags
            int FABActionFlags = (int) fabBackground.getTag(R.id.view_tag_flags);
            if (nextActivityState.FABActionFlags != FABActionFlags && nextActivityState.FABActionFlags != FLAG_NO_CHANGE) {
                // Hide (disable) / Reveal (enable)
                final boolean dismiss = nextActivityState.FABActionFlagSet(FLAG_HIDE);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        animateFABActions(dismiss).start();
                    }
                });
                fabBackground.setTag(R.id.view_tag_flags, nextActivityState.FABActionFlags);
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

            // Add/Edit Quote
            int addEditQuoteFlags = (int) addEditQuote.getTag(R.id.view_tag_flags);
            if (addEditQuoteFlags != nextActivityState.addEditQuoteFlags && nextActivityState.addEditQuoteFlags != FLAG_NO_CHANGE) {
                // Reveal/Hide
                if (nextActivityState.addEditQuoteFlagsSet(FLAG_HIDE) || nextActivityState.addEditQuoteFlagsSet(FLAG_REVEAL)) {
                    final boolean dismiss = nextActivityState.addEditQuoteFlagsSet(FLAG_HIDE);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            animateContainer(addEditQuote, dismiss);
                        }
                    });
                }
                addEditQuote.setTag(R.id.view_tag_flags, nextActivityState.addEditQuoteFlags);
            }

            activityState = nextActivityState;
            try {
                Thread.sleep(longestAnimation);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    ActivityState initialState = new Builder()
            .setSilkScreenState(State.HIDDEN)
            .setFABFlags(FLAG_HIDE | FLAG_DISABLE | FLAG_NO_ROTATE)
            .setFABActionFlags(FLAG_HIDE | FLAG_DISABLE)
            .setProgressBarFlags(FLAG_HIDE)
            .build();

    ActivityState revealControlsState = new Builder()
            .setSilkScreenState(State.REVEALED)
            .setFABFlags(FLAG_HIDE | FLAG_DISABLE | FLAG_NO_ROTATE)
            .setFABActionFlags(FLAG_HIDE | FLAG_DISABLE)
            .build();

    ActivityState revealPlaylistState = new Builder()
            .setSilkScreenState(State.OBSCURED)
            .setFABFlags(FLAG_REVEAL | FLAG_ENABLE | FLAG_NO_ROTATE)
            .setFABActionFlags(FLAG_HIDE | FLAG_DISABLE)
            .setAddEditQuoteFlags(FLAG_HIDE | FLAG_DISABLE)
            .build();

    ActivityState revealSaveToDiskCompletedState = new Builder()
            .setSilkScreenState(State.OBSCURED)
            .setProgressBarFlags(FLAG_HIDE)
            .build();

    ActivityState revealSettingsState = new Builder()
            .setSilkScreenState(State.OBSCURED)
            .setFABFlags(FLAG_HIDE | FLAG_DISABLE | FLAG_NO_ROTATE)
            .build();

    ActivityState revealSkipState = new Builder()
            .setSilkScreenState(State.HIDDEN)
            .setContentFlags(FLAG_DISABLE)
            .setFABFlags(FLAG_HIDE | FLAG_DISABLE | FLAG_NO_ROTATE)
            .setFABActionFlags(FLAG_HIDE | FLAG_DISABLE)
            .setProgressBarFlags(FLAG_REVEAL)
            .build();

    ActivityState revealSkipCompletedState = new Builder()
            .setSilkScreenState(State.REVEALED)
            .setContentFlags(FLAG_ENABLE)
            .setProgressBarFlags(FLAG_HIDE)
            .build();

    ActivityState revealWallpaperState = new Builder()
            .setSilkScreenState(State.REVEALED)
            .setFABFlags(FLAG_HIDE | FLAG_DISABLE | FLAG_NO_ROTATE)
            .setFABActionFlags(FLAG_HIDE | FLAG_DISABLE)
            .build();

    ActivityState revealWallpaperEditModeState = new Builder()
            .setSilkScreenState(State.REVEALED)
            .build();

    ActivityState revealFABActionsState = new Builder()
            .setFABActionFlags(FLAG_REVEAL | FLAG_ENABLE)
            .setFABFlags(FLAG_ROTATE)
            .setAddEditQuoteFlags(FLAG_HIDE | FLAG_DISABLE)
            .build();

    ActivityState revealAddEditQuoteState = new Builder()
            .setFABActionFlags(FLAG_REVEAL | FLAG_ENABLE)
            .setFABFlags(FLAG_ROTATE)
            .setAddEditQuoteFlags(FLAG_REVEAL | FLAG_ENABLE)
            .build();

    // Current ActivityState
    ActivityState activityState = null;

    // State Queue
    BlockingDeque<ActivityState> stateBlockingDeque = new LinkedBlockingDeque<>();

    // Executes state changes
    ExecutorService changeStateExecutorService = Executors.newSingleThreadScheduledExecutor();

    // Timer Task to reveal controls
    Timer revealControlsTimer;
    TimerTask revealControlsTimerTask = new TimerTask() {
        @Override
        public void run() {
            changeState(revealPlaylistState);
            revealControlsTimer = null;
            revealControlsTimerTask = null;
        }
    };

    // PlaylistAdapter
    PlaylistAdapter playlistAdapter;
    // Editing this Quote
    Quote editingQuote;
    int editingQuotePosition;

    // Layout Controls
    @Bind(R.id.tab_layout_lwq_settings)
    TabLayout tabLayout;
    @Bind(R.id.viewpager_lwq_settings)
    ViewPager viewPager;

    // Settings Container
    @Bind(R.id.group_lwq_settings_settings_wrapper)
    View settingsWrapper;
    @Bind(R.id.group_lwq_settings_settings)
    View settingsContainer;

    // Playlist Container
    @Bind(R.id.group_lwq_settings_playlist_wrapper)
    View playlistWrapper;
    @Bind(R.id.group_lwq_settings_playlist)
    View playlistContainer;

    // ProgressBar
    @Bind(R.id.pb_lwq_settings)
    ProgressBar progressBar;

    // FAB
    @Bind(R.id.fab_lwq_reveal)
    View fab;
    @Bind(R.id.view_fab_background)
    View fabBackground;

    // FAB Actions
    @Bind(R.id.fab_lwq_search)
    View fabSearch;
    @Bind(R.id.fab_lwq_create_quote)
    View fabCreate;

    // Add/Edit Quote
    @Bind(R.id.group_lwq_fab_screen_add_edit_quote)
    View addEditQuote;
    @Bind(R.id.et_fab_screen_quote)
    EditText editableQuote;
    @Bind(R.id.actv_fab_screen_author)
    AppCompatAutoCompleteTextView editableAuthor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.fade_in, 0);
        setContentView(R.layout.activity_lwq_settings);
        ButterKnife.bind(this);

        // Setup ViewPager and Controls
        setupViewPagerAndTabs();
        // Setup FAB
        setupFABs();
        // Setup Add/Edit
        setupAddEditQuote();
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

    void setupViewPagerAndTabs() {
        viewPager.setAdapter(new PagerAdapter() {

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                switch (position) {
                    case 0:
                        return playlistWrapper;
                    default:
                        return settingsWrapper;
                }
            }

            @Override
            public int getCount() {
                return 2;
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return "";
            }
        });
        tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.palette_500));
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setIcon(R.drawable.selectable_playlist_button);
        tabLayout.getTabAt(1).setIcon(R.drawable.selectable_settings_button);
    }

    void setupFABs() {
        fab.setAlpha(0f);
        fab.setVisibility(View.GONE);
        fab.setEnabled(false);
        fab.setTag(R.id.view_tag_flags, FLAG_HIDE | FLAG_DISABLE | FLAG_NO_ROTATE);
        View fabContainer = ButterKnife.findById(this, R.id.fl_lwq_fab_reveal);
        PercentRelativeLayout.LayoutParams layoutParams = (PercentRelativeLayout.LayoutParams) fabContainer.getLayoutParams();
        layoutParams.bottomMargin = (int) (UIUtils.getNavBarHeight(this) * 1.5);
        fabContainer.setLayoutParams(layoutParams);

        fabBackground.setVisibility(View.GONE);
        fabBackground.setTag(R.id.view_tag_flags, FLAG_HIDE | FLAG_DISABLE);

        fabCreate.setAlpha(0f);
        fabCreate.setVisibility(View.GONE);

        fabSearch.setAlpha(0f);
        fabSearch.setVisibility(View.GONE);
    }

    void setupAddEditQuote() {
        addEditQuote.setAlpha(0f);
        addEditQuote.setVisibility(View.GONE);
        addEditQuote.setEnabled(false);
        addEditQuote.setTag(R.id.view_tag_flags, FLAG_HIDE | FLAG_DISABLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Activity context = LWQSettingsActivity.this;
                if (context == null) {
                    return;
                }
                final List<Author> list = Select.from(Author.class).orderBy(StringUtil.toSQLName("name")).list();
                String [] allAuthors = new String[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    allAuthors[i] = list.get(i).name;
                }
                final ArrayAdapter<String> authorAdapter = new ArrayAdapter<String>(context,
                        R.layout.support_simple_spinner_dropdown_item, allAuthors);
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        editableAuthor.setAdapter(authorAdapter);
                    }
                });
            }
        }).start();
    }

    void setupPlaylist() {
        playlistAdapter = new PlaylistAdapter(this);
        RecyclerView recyclerView = ButterKnife.findById(this, R.id.recycler_playlist);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(playlistAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    void setupSettings() {

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
        Spinner imageCategorySpinner = ButterKnife.findById(settingsContainer, R.id.spinner_lwq_settings_image_category);
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
        Spinner refreshSpinner = ButterKnife.findById(settingsContainer, R.id.spinner_lwq_settings_interval);
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
            ButterKnife.findById(settingsContainer, R.id.sb_lwq_settings_blur).setVisibility(View.GONE);
        } else {
            SeekBar blurBar = ButterKnife.findById(settingsContainer, R.id.sb_lwq_settings_blur);
            blurBar.setProgress(LWQPreferences.getBlurPreference());
            blurBar.setOnSeekBarChangeListener(this);
        }

        // Dim
        SeekBar dimBar = ButterKnife.findById(settingsContainer, R.id.sb_lwq_settings_dim);
        dimBar.setProgress(LWQPreferences.getDimPreference());
        dimBar.setOnSeekBarChangeListener(this);
    }

    void updateRefreshSpinner() {
        Spinner refreshSpinner = ButterKnife.findById(settingsContainer, R.id.spinner_lwq_settings_interval);
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

    void setupProgressBar() {
        progressBar.setAlpha(0f);
        progressBar.setTag(R.id.view_tag_flags, FLAG_HIDE);
    }

    void setupContent(boolean enable) {
        View content = ButterKnife.findById(this, android.R.id.content);
        content.setTag(R.id.view_tag_flags, enable ? FLAG_ENABLE : FLAG_DISABLE);
        content.setEnabled(enable);
    }

    // Click Handling

    @OnClick(R.id.fab_lwq_create_quote) void revealAddEditQuote() {
        if (activityState == revealAddEditQuoteState) {
            changeState(revealFABActionsState);
        } else {
            editableAuthor.setText("");
            editableQuote.setText("");
            changeState(revealAddEditQuoteState);
        }
    }

    @OnClick(R.id.btn_fab_screen_save) void saveQuote() {
        Author author = Author.findAuthor(editableAuthor.getText().toString().trim());
        if (author == null) {
            // Create a new Author
            author = new Author(editableAuthor.getText().toString().trim(), true);
            author.save();
        }
        if (editingQuote != null) {
            editingQuote.author = author;
            editingQuote.text = editableQuote.getText().toString().trim();
            editingQuote.save();
            playlistAdapter.notifyItemChanged(editingQuotePosition);
            editingQuote = null;
            editingQuotePosition = -1;
            changeState(revealPlaylistState);
            return;
        }
        Quote quote = Quote.find(editableQuote.getText().toString().trim(), author);
        if (quote == null) {
            quote = new Quote(editableQuote.getText().toString().trim(), author, null);
            quote.save();
        }
        PlaylistQuote playlistQuote = new PlaylistQuote(Playlist.active(), quote);
        playlistQuote.save();
        playlistAdapter.insertItem(playlistQuote);
        changeState(revealPlaylistState);
        UIUtils.dismissKeyboard(this);
    }

    @OnClick(R.id.btn_fab_screen_cancel) void dismissAddEditQuote() {
        editingQuote = null;
        editingQuotePosition = -1;
        changeState(revealFABActionsState);
        UIUtils.dismissKeyboard(this);
    }

    @OnClick(R.id.fab_lwq_reveal) void toggleAddScreen() {
        if (activityState == revealFABActionsState || activityState == revealAddEditQuoteState) {
            changeState(revealPlaylistState);
        } else {
            changeState(revealFABActionsState);
        }
    }

    @OnClick(android.R.id.content) void toggleShowWallpaper() {
        if (revealControlsTimer != null) {
            revealControlsTimer.purge();
            revealControlsTimer.cancel();
            revealControlsTimer = null;
            revealControlsTimerTask = null;
        }
        if (activityState == revealWallpaperState) {
            changeState(revealControlsState);
        } else {
            changeState(revealWallpaperState);
        }
    }

    // Animation

    void animateContainer(final View container, final boolean dismiss) {
        container.setVisibility(View.VISIBLE);
        Animator animator = dismiss ? AnimatorInflater.loadAnimator(this, R.animator.exit_to_left) :
                AnimatorInflater.loadAnimator(this, R.animator.enter_from_right);
        animator.setTarget(container);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (dismiss) {
                    container.setVisibility(View.GONE);
                }
            }
        });
        animator.start();
    }

    ViewPropertyAnimator animateFABRotation(final boolean rotate) {
        final ViewPropertyAnimator animate = fab.animate();
        animate.rotationBy(rotate ? 45f : -45f)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(150);
        return animate;
    }

    Animator animateFAB(final View fabToAnimate, final boolean dismiss) {
        fabToAnimate.setVisibility(View.VISIBLE);
        float [] toFrom = dismiss ? new float[] {1f, .2f} : new float[] {.2f, 1f};
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                fabToAnimate.setVisibility(dismiss ? View.GONE : View.VISIBLE);
            }
        });
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(fabToAnimate, "alpha", toFrom);
        alphaAnimator.setDuration(200);
        alphaAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        // Create a scale + fade animation
        ObjectAnimator scaleDown = ObjectAnimator.ofPropertyValuesHolder(fabToAnimate,
                PropertyValuesHolder.ofFloat("scaleX", toFrom),
                PropertyValuesHolder.ofFloat("scaleY", toFrom));
        scaleDown.setDuration(200);
        scaleDown.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.playTogether(scaleDown, alphaAnimator);
        return animatorSet;
    }

    Animator animateFABActions(final boolean dismiss) {
        fabBackground.setVisibility(View.VISIBLE);
        Animator backgroundAnimator = null;

        if (Build.VERSION.SDK_INT >= 21) {
            Rect fabRect = new Rect();
            fab.getGlobalVisibleRect(fabRect);
            final Point realScreenSize = UIUtils.getRealScreenSize();
            int radius = Math.max(realScreenSize.x, realScreenSize.y);
            final Animator circularReveal = ViewAnimationUtils.createCircularReveal(fabBackground,
                    fabRect.centerX(),
                    fabRect.centerY(),
                    dismiss ? radius : 0,
                    dismiss ? 0 : radius);
            circularReveal.setDuration(300);
            circularReveal.setInterpolator(new AccelerateDecelerateInterpolator());
            circularReveal.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (dismiss) {
                        fabBackground.setVisibility(View.GONE);
                    }
                }
            });
            backgroundAnimator = circularReveal;
        } else {
            // TODO?
        }
        final long shortDelay = 50;
        final long longDelay = 100;
        final Animator createAnimator = animateFAB(fabCreate, dismiss);
        final Animator searchAnimator = animateFAB(fabSearch, dismiss);
        createAnimator.setStartDelay(dismiss ? longDelay : shortDelay);
        searchAnimator.setStartDelay(dismiss ? shortDelay : longDelay);
        AnimatorSet allAnimations = new AnimatorSet();
        allAnimations.playTogether(backgroundAnimator, createAnimator, searchAnimator);
        return allAnimations;
    }

    // Misc

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
            revealControlsTimer.schedule(revealControlsTimerTask, DateUtils.SECOND_IN_MILLIS * 2);
        }
    }

    // Event Handling

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

    // PlaylistAdapter.Delegate

    @Override
    public void onPlaylistItemRemove(PlaylistAdapter adapter, int position) {
        if (adapter.getItemCount() == 1) {
            Toast.makeText(this, "Your playlist may not be empty", Toast.LENGTH_LONG).show();
            return;
        }
        final Object item = adapter.getItem(position);
        SugarRecord record = (SugarRecord) item;
        record.delete();
        adapter.removeItem(position);
    }

    @Override
    public void onQuoteEdit(PlaylistAdapter adapter, int position) {
        final Object item = adapter.getItem(position);
        if (item instanceof PlaylistQuote) {
            editingQuotePosition = position;
            editingQuote = ((PlaylistQuote) item).quote;
            editableAuthor.setText(editingQuote.author.name);
            editableQuote.setText(editingQuote.text);
            if (activityState != revealAddEditQuoteState) {
                changeState(revealAddEditQuoteState);
            }
        }
    }

}