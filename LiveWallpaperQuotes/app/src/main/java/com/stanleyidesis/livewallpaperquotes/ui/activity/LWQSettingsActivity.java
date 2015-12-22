package com.stanleyidesis.livewallpaperquotes.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.orm.StringUtil;
import com.orm.SugarRecord;
import com.orm.query.Select;
import com.stanleyidesis.livewallpaperquotes.LWQApplication;
import com.stanleyidesis.livewallpaperquotes.LWQPreferences;
import com.stanleyidesis.livewallpaperquotes.R;
import com.stanleyidesis.livewallpaperquotes.api.Callback;
import com.stanleyidesis.livewallpaperquotes.api.controller.LWQAlarmController;
import com.stanleyidesis.livewallpaperquotes.api.db.Author;
import com.stanleyidesis.livewallpaperquotes.api.db.Category;
import com.stanleyidesis.livewallpaperquotes.api.db.Playlist;
import com.stanleyidesis.livewallpaperquotes.api.db.PlaylistAuthor;
import com.stanleyidesis.livewallpaperquotes.api.db.PlaylistCategory;
import com.stanleyidesis.livewallpaperquotes.api.db.PlaylistQuote;
import com.stanleyidesis.livewallpaperquotes.api.db.Quote;
import com.stanleyidesis.livewallpaperquotes.api.db.UnsplashCategory;
import com.stanleyidesis.livewallpaperquotes.api.event.ImageSaveEvent;
import com.stanleyidesis.livewallpaperquotes.api.event.PreferenceUpdateEvent;
import com.stanleyidesis.livewallpaperquotes.api.event.WallpaperEvent;
import com.stanleyidesis.livewallpaperquotes.ui.UIUtils;
import com.stanleyidesis.livewallpaperquotes.ui.adapter.PlaylistAdapter;
import com.stanleyidesis.livewallpaperquotes.ui.adapter.SearchResultsAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
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
public class LWQSettingsActivity extends LWQWallpaperActivity implements ActivityStateFlags,
        SeekBar.OnSeekBarChangeListener,
        PlaylistAdapter.Delegate,
        SearchResultsAdapter.Delegate {

    static class ActivityState {
        Map<Integer, Integer> newStateValues = new HashMap<>();
        BackgroundWallpaperState backgroundWallpaperState = null;
        boolean wallpaperControlsVisible = false;
    }

    static class Builder {

        ActivityState activityState;

        ActivityState build() {
            return activityState;
        }

        private Builder() {
            activityState = new ActivityState();
        }

        Builder setViewState(int resId, int stateFlags) {
            activityState.newStateValues.put(resId, stateFlags);
            return this;
        }

        Builder setWallpaperState(BackgroundWallpaperState backgroundWallpaperState) {
            activityState.backgroundWallpaperState = backgroundWallpaperState;
            return this;
        }

        Builder setWallpaperControlsVisible(boolean visible) {
            activityState.wallpaperControlsVisible = visible;
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

            // Wallpaper State
            final BackgroundWallpaperState newBackgroundWallpaperState = nextActivityState.backgroundWallpaperState;
            if (LWQSettingsActivity.this.backgroundWallpaperState != newBackgroundWallpaperState && newBackgroundWallpaperState != null) {
                longestAnimation = 300l; // TODO HAX
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        animateState(newBackgroundWallpaperState);
                    }
                });
            }

            // Wallpaper Controls
            if (LWQSettingsActivity.this.wallpaperActionsVisible != nextActivityState.wallpaperControlsVisible) {
                final boolean dismiss = !nextActivityState.wallpaperControlsVisible;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        animateWallpaperActions(dismiss);
                    }
                });
            }

            // The MAIN loop…
            for (int resourceId : nextActivityState.newStateValues.keySet()) {
                final View view = ButterKnife.findById(LWQSettingsActivity.this, resourceId);
                if (view == null) {
                    continue;
                }
                int currentFlags = (int) view.getTag(R.id.view_tag_flags);
                int newFlags = nextActivityState.newStateValues.get(resourceId);
                if (currentFlags == newFlags) {
                    continue;
                }
                // Enable/disable
                int enableDisableFlags = newFlags & (FLAG_ENABLE | FLAG_DISABLE);
                if ((currentFlags & enableDisableFlags) == 0 && enableDisableFlags > 0) {
                    final boolean enable = (newFlags & FLAG_ENABLE) > 0;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            UIUtils.setViewAndChildrenEnabled(view, enable);
                        }
                    });
                    currentFlags &= ~(FLAG_ENABLE | FLAG_DISABLE);
                    currentFlags |= enableDisableFlags;
                }

                // Reveal/Hide
                int revealHideFlags = newFlags & (FLAG_REVEAL | FLAG_HIDE);
                if ((currentFlags & revealHideFlags) == 0 && revealHideFlags > 0) {
                    final boolean dismiss = (newFlags & FLAG_HIDE) > 0;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((Runnable) view.getTag(dismiss
                                    ? R.id.view_tag_animator_hide
                                    : R.id.view_tag_animator_reveal)).run();
                        }
                    });
                    currentFlags &= ~(FLAG_REVEAL | FLAG_HIDE);
                    currentFlags |= revealHideFlags;
                }
                // Rotate/No Rotate
                int rotateNoRotateFlags = newFlags & (FLAG_ROTATE | FLAG_NO_ROTATE);
                if ((currentFlags & rotateNoRotateFlags) == 0 && rotateNoRotateFlags > 0) {
                    final boolean rotate = (newFlags & FLAG_ROTATE) > 0;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                        ((Runnable) view.getTag(rotate ?
                                R.id.view_tag_animator_rotate
                                : R.id.view_tag_animator_unrotate)).run();
                        }
                    });
                    currentFlags &= ~(FLAG_ROTATE | FLAG_NO_ROTATE);
                    currentFlags |= rotateNoRotateFlags;
                }
                view.setTag(R.id.view_tag_flags, currentFlags);
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
            .setWallpaperState(BackgroundWallpaperState.HIDDEN)
            .setWallpaperControlsVisible(false)
            .setViewState(R.id.group_lwq_settings_content, FLAG_HIDE | FLAG_DISABLE)
            .setViewState(R.id.fab_lwq_reveal, FLAG_HIDE | FLAG_DISABLE | FLAG_NO_ROTATE)
            .setViewState(R.id.group_lwq_settings_fabs, FLAG_HIDE)
            .setViewState(R.id.pb_lwq_settings, FLAG_HIDE)
            .build();

    ActivityState revealContentState = new Builder()
            .setWallpaperState(BackgroundWallpaperState.OBSCURED)
            .setWallpaperControlsVisible(false)
            .setViewState(R.id.group_lwq_settings_content, FLAG_REVEAL | FLAG_ENABLE)
            .setViewState(R.id.fab_lwq_reveal, FLAG_NO_ROTATE)
            .setViewState(R.id.group_lwq_settings_fabs, FLAG_HIDE)
            .setViewState(R.id.group_lwq_fab_screen_add_edit_quote, FLAG_HIDE | FLAG_DISABLE)
            .setViewState(R.id.group_lwq_fab_screen_search, FLAG_HIDE | FLAG_DISABLE)
            .setViewState(R.id.pb_lwq_settings, FLAG_HIDE)
            .build();

    ActivityState revealWallpaperState = new Builder()
            .setWallpaperState(BackgroundWallpaperState.REVEALED)
            .setWallpaperControlsVisible(true)
            .setViewState(R.id.group_lwq_settings_content, FLAG_HIDE | FLAG_DISABLE)
            .build();

    ActivityState revealWallpaperEditModeState = new Builder()
            .setWallpaperState(BackgroundWallpaperState.REVEALED)
            .setViewState(R.id.group_lwq_settings_content, FLAG_HIDE)
            .build();

    ActivityState revealFABActionsState = new Builder()
            .setViewState(R.id.group_lwq_settings_fabs, FLAG_REVEAL | FLAG_ENABLE)
            .setViewState(R.id.fab_lwq_reveal, FLAG_ROTATE)
            .setViewState(R.id.group_lwq_fab_screen_add_edit_quote, FLAG_HIDE | FLAG_DISABLE)
            .setViewState(R.id.group_lwq_fab_screen_search, FLAG_HIDE | FLAG_DISABLE)
            .build();

    ActivityState revealAddEditQuoteState = new Builder()
            .setViewState(R.id.group_lwq_settings_fabs, FLAG_REVEAL | FLAG_ENABLE)
            .setViewState(R.id.fab_lwq_reveal, FLAG_ROTATE)
            .setViewState(R.id.group_lwq_fab_screen_add_edit_quote, FLAG_REVEAL | FLAG_ENABLE)
            .setViewState(R.id.group_lwq_fab_screen_search, FLAG_HIDE | FLAG_DISABLE)
            .build();

    ActivityState revealSearchState = new Builder()
            .setViewState(R.id.group_lwq_settings_fabs, FLAG_REVEAL | FLAG_ENABLE)
            .setViewState(R.id.fab_lwq_reveal, FLAG_ROTATE | FLAG_ENABLE)
            .setViewState(R.id.group_lwq_fab_screen_add_edit_quote, FLAG_HIDE | FLAG_DISABLE)
            .setViewState(R.id.group_lwq_fab_screen_search, FLAG_REVEAL | FLAG_ENABLE)
            .setViewState(R.id.pb_lwq_settings, FLAG_HIDE)
            .build();

    ActivityState revealSearchInProgress = new Builder()
            .setViewState(R.id.group_lwq_settings_fabs, FLAG_REVEAL | FLAG_DISABLE)
            .setViewState(R.id.fab_lwq_reveal, FLAG_REVEAL | FLAG_DISABLE)
            .setViewState(R.id.group_lwq_fab_screen_add_edit_quote, FLAG_HIDE | FLAG_DISABLE)
            .setViewState(R.id.group_lwq_fab_screen_search, FLAG_REVEAL | FLAG_DISABLE)
            .setViewState(R.id.pb_lwq_settings, FLAG_REVEAL)
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
            changeState(revealContentState);
            revealControlsTimer = null;
            revealControlsTimerTask = null;
        }
    };

    // Content
    @Bind(R.id.group_lwq_settings_content)
    View content;

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

    // ProgressBar
    @Bind(R.id.pb_lwq_settings)
    ProgressBar progressBar;

    // FAB
    @Bind(R.id.group_lwq_settings_fabs)
    View fabContainer;
    @Bind(R.id.fab_lwq_reveal)
    View fabAdd;
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

    // Search
    @Bind(R.id.group_lwq_fab_screen_search)
    View searchContainer;
    @Bind(R.id.actv_fab_screen_search)
    AppCompatAutoCompleteTextView editableQuery;
    SearchResultsAdapter searchResultsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.fade_in, 0);
        setContentView(R.layout.activity_lwq_settings);
        ButterKnife.bind(this);
        // Setup content
        setupContent();
        // Setup ViewPager and Controls
        setupViewPagerAndTabs();
        // Setup FAB
        setupFABs();
        // Setup Add/Edit
        setupAddEditQuote();
        // Setup search
        setupSearch();
        // Setup playlist
        setupPlaylist();
        // Setup settings
        setupSettings();
        // Setup Wallpaper actions
        setupWallpaperActions();
        // Setup progress bar
        setupProgressBar();
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
        if (activityState == revealWallpaperState || activityState == revealFABActionsState) {
            changeState(revealContentState);
        } else if (activityState == revealSearchState || activityState == revealAddEditQuoteState) {
            changeState(revealFABActionsState);
        } else {
            super.onBackPressed();
        }
    }

    // Setup

    void setupContent() {
        content.setVisibility(View.GONE);
        content.setAlpha(0f);
        content.setTag(R.id.view_tag_flags, FLAG_HIDE | FLAG_DISABLE);
        UIUtils.setViewAndChildrenEnabled(content, false);
        content.setTag(R.id.view_tag_animator_hide, new Runnable() {
            @Override
            public void run() {
                animateContent(true);
            }
        });
        content.setTag(R.id.view_tag_animator_reveal, new Runnable() {
            @Override
            public void run() {
                animateContent(false);
            }
        });
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
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                animateFAB(fabAdd, position != 0).start();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.palette_500));
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setIcon(R.drawable.selectable_playlist_button);
        tabLayout.getTabAt(1).setIcon(R.drawable.selectable_settings_button);
    }

    void setupFABs() {
        fabContainer.setTag(R.id.view_tag_flags, FLAG_HIDE | FLAG_ENABLE);
        fabContainer.setTag(R.id.view_tag_animator_reveal, new Runnable() {
            @Override
            public void run() {
                animateFABActions(false).start();
            }
        });
        fabContainer.setTag(R.id.view_tag_animator_hide, new Runnable() {
            @Override
            public void run() {
                animateFABActions(true).start();
            }
        });

        View fabWrapper = ButterKnife.findById(this, R.id.fl_lwq_fab_preview);
        PercentRelativeLayout.LayoutParams layoutParams = (PercentRelativeLayout.LayoutParams) fabWrapper.getLayoutParams();
        layoutParams.bottomMargin = (int) (UIUtils.getNavBarHeight(this) * 1.5);
        fabWrapper.setLayoutParams(layoutParams);

        fabAdd.setAlpha(0f);
        fabAdd.setVisibility(View.GONE);
        fabAdd.setTag(R.id.view_tag_flags, FLAG_HIDE | FLAG_DISABLE | FLAG_NO_ROTATE);
        fabAdd.setTag(R.id.view_tag_animator_reveal, new Runnable() {
            @Override
            public void run() {
                animateFAB(fabAdd, false).start();
            }
        });
        fabAdd.setTag(R.id.view_tag_animator_hide, new Runnable() {
            @Override
            public void run() {
                animateFAB(fabAdd, true).start();
            }
        });
        fabAdd.setTag(R.id.view_tag_animator_rotate, new Runnable() {
            @Override
            public void run() {
                animateFABRotation(true).start();
            }
        });
        fabAdd.setTag(R.id.view_tag_animator_unrotate, new Runnable() {
            @Override
            public void run() {
                animateFABRotation(false).start();
            }
        });

        fabBackground.setVisibility(View.GONE);

        fabCreate.setAlpha(0f);
        fabCreate.setVisibility(View.GONE);

        fabSearch.setAlpha(0f);
        fabSearch.setVisibility(View.GONE);

        animateFAB(fabAdd, false).start();
    }

    void setupAddEditQuote() {
        addEditQuote.setAlpha(0f);
        addEditQuote.setVisibility(View.GONE);
        addEditQuote.setTag(R.id.view_tag_flags, FLAG_HIDE | FLAG_DISABLE);
        addEditQuote.setTag(R.id.view_tag_animator_hide, new Runnable() {
            @Override
            public void run() {
                animateContainer(addEditQuote, true);
            }
        });
        addEditQuote.setTag(R.id.view_tag_animator_reveal, new Runnable() {
            @Override
            public void run() {
                animateContainer(addEditQuote, false);
            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                Activity context = LWQSettingsActivity.this;
                final List<Author> list = Select.from(Author.class).orderBy(StringUtil.toSQLName("name")).list();
                String [] allAuthors = new String[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    allAuthors[i] = list.get(i).name;
                }
                final ArrayAdapter<String> authorAdapter = new ArrayAdapter<>(context,
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

    void setupSearch() {
        searchContainer.setAlpha(0f);
        searchContainer.setVisibility(View.GONE);
        searchContainer.setTag(R.id.view_tag_flags, FLAG_HIDE | FLAG_DISABLE);
        searchContainer.setTag(R.id.view_tag_animator_hide, new Runnable() {
            @Override
            public void run() {
                animateContainer(searchContainer, true);
            }
        });
        searchContainer.setTag(R.id.view_tag_animator_reveal, new Runnable() {
            @Override
            public void run() {
                animateContainer(searchContainer, false);
            }
        });
        editableQuery.setSupportBackgroundTintList(getResources().getColorStateList(R.color.text_field_state_list));
        editableQuery.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_NULL) {
                    performSearch();
                }
                return false;
            }
        });

        // RecyclerView
        searchResultsAdapter = new SearchResultsAdapter();
        searchResultsAdapter.setDelegate(this);
        final RecyclerView recyclerView = ButterKnife.findById(this, R.id.recycler_fab_screen_search_results);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(searchResultsAdapter);

        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<Category> categories = Category.listAll(Category.class);
                final List<Author> authors = Author.listAll(Author.class);
                String[] allHints = new String[categories.size() + authors.size()];
                for (int i = 0; i < categories.size(); i++) {
                    allHints[i] = categories.get(i).name;
                }
                for (int i = 0; i < authors.size(); i++) {
                    allHints[categories.size() + i] = authors.get(i).name;
                }
                final ArrayAdapter<String> searchAdapter = new ArrayAdapter<>(LWQSettingsActivity.this,
                        R.layout.support_simple_spinner_dropdown_item, allHints);
                LWQSettingsActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        editableQuery.setAdapter(searchAdapter);
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
        UnsplashCategory unsplashCategory = UnsplashCategory.find(LWQPreferences.getImageCategoryPreference());
        int currentSelection = 0;
        if (unsplashCategory != null) {
            for (String category : backgroundCategories) {
                if (category.equalsIgnoreCase(unsplashCategory.title)) {
                    currentSelection = backgroundCategories.indexOf(category);
                    break;
                }
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
                UnsplashCategory unsplashCategory = UnsplashCategory.find((String) adapterView.getAdapter().getItem(index));
                LWQPreferences.setImageCategoryPreference(unsplashCategory.unsplashId);
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

        AppCompatCheckBox doubleTapCheckbox = ButterKnife.findById(this, R.id.check_lwq_settings_double_tap);
        doubleTapCheckbox.setChecked(LWQPreferences.isDoubleTapEnabled());
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
        progressBar.setTag(R.id.view_tag_animator_hide, new Runnable() {
            @Override
            public void run() {
                animateProgressBar(false);
            }
        });
        progressBar.setTag(R.id.view_tag_animator_reveal, new Runnable() {
            @Override
            public void run() {
                animateProgressBar(true);
            }
        });
    }

    // Click Handling

    @OnClick(R.id.btn_fab_screen_search) void performSearch() {
        dismissKeyboard(editableQuery);
        changeState(revealSearchInProgress);
        final int itemCount = searchResultsAdapter.getItemCount();
        searchResultsAdapter.setSearchResults(new ArrayList<Object>());
        searchResultsAdapter.notifyItemRangeRemoved(0, itemCount);
        LWQApplication.getQuoteController().fetchQuotes(editableQuery.getText().toString().trim(), new Callback<List<Object>>() {
            @Override
            public void onSuccess(final List<Object> objects) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        changeState(revealSearchState);
                        searchResultsAdapter.setSearchResults(objects);
                        searchResultsAdapter.notifyItemRangeInserted(0, searchResultsAdapter.getItemCount());
                    }
                });
            }

            @Override
            public void onError(String errorMessage, Throwable throwable) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LWQSettingsActivity.this, "Search pooped out :(, please try again later.", Toast.LENGTH_LONG).show();
                        changeState(revealSearchState);
                    }
                });
            }
        });
    }

    @OnClick(R.id.view_screen_lwq_wallpaper) void revealContent() {
        if (activityState == revealWallpaperState) {
            return;
        }
        changeState(revealContentState);
    }

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
        // TODO check permission
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
            changeState(revealContentState);
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
        changeState(revealContentState);
        UIUtils.dismissKeyboard(this);
    }

    @OnClick(R.id.btn_fab_screen_cancel) void dismissAddEditQuote() {
        editingQuote = null;
        editingQuotePosition = -1;
        changeState(revealFABActionsState);
        UIUtils.dismissKeyboard(this);
    }

    @OnClick(R.id.fab_lwq_reveal) void toggleAddScreen() {
        if (activityState == revealFABActionsState
                || activityState == revealAddEditQuoteState
                || activityState == revealSearchState) {
            dismissKeyboard(editableAuthor);
            dismissKeyboard(editableQuote);
            dismissKeyboard(editableQuery);
            changeState(revealContentState);
        } else {
            changeState(revealFABActionsState);
        }
    }

    @OnClick(R.id.fab_lwq_search) void revealSearch() {
        if (activityState == revealSearchState) {
            changeState(revealFABActionsState);
            dismissKeyboard(editableQuery);
        } else {
            editableQuery.setText("");
            editableQuery.requestFocusFromTouch();
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            changeState(revealSearchState);
        }
    }

    @OnClick(R.id.fab_lwq_preview) void toggleWallpaper() {
        changeState(revealWallpaperState);
    }

    @OnCheckedChanged(R.id.check_lwq_settings_double_tap)
    void onDoubleTapCheckChange(boolean checked) {
        LWQPreferences.setDoubleTapEnabled(checked);
    }

    @Override
    void saveWallpaperToDisk() {
        super.saveWallpaperToDisk();
        animateProgressBar(true);
    }

    @Override
    void skipWallpaper() {
        super.skipWallpaper();
        animateProgressBar(true);
    }

    // Animation

    void animateProgressBar(final boolean reveal) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.animate()
                        .alpha(reveal ? 1f : 0f)
                        .setDuration(150)
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                progressBar.setTag(R.id.view_tag_flags, reveal ? FLAG_REVEAL : FLAG_HIDE);
                            }
                        })
                        .start();
            }
        });
    }

    void animateContent(final boolean dismiss) {
        content.clearAnimation();
        content.setVisibility(View.VISIBLE);
        content.animate().alpha(dismiss ? 0f : 1f).setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (dismiss) {
                            content.setVisibility(View.GONE);
                        }
                    }
                }).start();
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
                    container.setVisibility(View.GONE);
                }
            }
        });
        animator.start();
    }

    ViewPropertyAnimator animateFABRotation(final boolean rotate) {
        final ViewPropertyAnimator animate = fabAdd.animate();
        animate.rotation(rotate ? 45f : 0f)
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
            fabAdd.getGlobalVisibleRect(fabRect);
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

    void dismissKeyboard(View tokenOwner) {
        // Dismiss the keyboard
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(tokenOwner.getWindowToken(), 0);
    }

    @Override
    void didFinishDrawing() {
        if (activityState == initialState) {
            changeState(revealWallpaperState);
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
        if (wallpaperEvent.didFail()) {
            animateProgressBar(false);
        } else if (wallpaperEvent.getStatus() != WallpaperEvent.Status.RETRIEVED_WALLPAPER) {
            changeState(initialState);
            animateProgressBar(true);
        } else {
            animateProgressBar(false);
        }
    }

    @Override
    public void onEvent(ImageSaveEvent imageSaveEvent) {
        super.onEvent(imageSaveEvent);
        animateProgressBar(false);
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
        changeState(revealContentState);
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

    @Override
    public void onMakeQuotograph(PlaylistAdapter adapter, int position) {
        final Object item = adapter.getItem(position);
        if (item instanceof PlaylistCategory) {
            LWQApplication.getWallpaperController().generateNewWallpaper((PlaylistCategory) item);
        } else if (item instanceof PlaylistAuthor) {
            LWQApplication.getWallpaperController().generateNewWallpaper((PlaylistAuthor) item);
        } else if (item instanceof PlaylistQuote) {
            LWQApplication.getWallpaperController().generateNewWallpaper((PlaylistQuote) item);
        }
    }

    // Search Delegate

    @Override
    public void onRemove(SearchResultsAdapter adapter, Object playlistItem) {
        if (playlistItem instanceof PlaylistCategory) {
            ((PlaylistCategory) playlistItem).delete();
        } else if (playlistItem instanceof PlaylistAuthor) {
            ((PlaylistAuthor) playlistItem).delete();
        } else if (playlistItem instanceof PlaylistQuote) {
            ((PlaylistQuote) playlistItem).delete();
        }
        playlistAdapter.removeItem(playlistItem);
    }

    @Override
    public Object onAdd(SearchResultsAdapter adapter, Object model) {
        SugarRecord playlistItem = null;
        if (model instanceof Category) {
            playlistItem = new PlaylistCategory(Playlist.active(), (Category) model);
        } else if (model instanceof Author) {
            playlistItem = new PlaylistAuthor(Playlist.active(), (Author) model);
        } else if (model instanceof Quote) {
            playlistItem = new PlaylistQuote(Playlist.active(), (Quote) model);
        }
        if (playlistItem != null) {
            playlistItem.save();
            playlistAdapter.insertItem(playlistItem);
        }
        return playlistItem;
    }

}