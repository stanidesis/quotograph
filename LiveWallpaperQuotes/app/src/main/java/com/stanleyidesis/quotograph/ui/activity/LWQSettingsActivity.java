package com.stanleyidesis.quotograph.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.percent.PercentFrameLayout;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.orm.SugarRecord;
import com.orm.query.Select;
import com.orm.util.NamingHelper;
import com.stanleyidesis.quotograph.AdMobUtils;
import com.stanleyidesis.quotograph.AnalyticsUtils;
import com.stanleyidesis.quotograph.BuildConfig;
import com.stanleyidesis.quotograph.IabConst;
import com.stanleyidesis.quotograph.LWQApplication;
import com.stanleyidesis.quotograph.LWQPreferences;
import com.stanleyidesis.quotograph.R;
import com.stanleyidesis.quotograph.api.Callback;
import com.stanleyidesis.quotograph.api.LWQError;
import com.stanleyidesis.quotograph.api.controller.LWQAlarmController;
import com.stanleyidesis.quotograph.api.controller.LWQLoggerHelper;
import com.stanleyidesis.quotograph.api.controller.LWQQuoteControllerHelper;
import com.stanleyidesis.quotograph.api.controller.LWQWallpaperController;
import com.stanleyidesis.quotograph.api.controller.LWQWallpaperControllerHelper;
import com.stanleyidesis.quotograph.api.db.Author;
import com.stanleyidesis.quotograph.api.db.Category;
import com.stanleyidesis.quotograph.api.db.Playlist;
import com.stanleyidesis.quotograph.api.db.PlaylistAuthor;
import com.stanleyidesis.quotograph.api.db.PlaylistCategory;
import com.stanleyidesis.quotograph.api.db.PlaylistQuote;
import com.stanleyidesis.quotograph.api.db.Quote;
import com.stanleyidesis.quotograph.api.event.IabPurchaseEvent;
import com.stanleyidesis.quotograph.api.event.PreferenceUpdateEvent;
import com.stanleyidesis.quotograph.api.event.WallpaperEvent;
import com.stanleyidesis.quotograph.api.misc.UserSurveyController;
import com.stanleyidesis.quotograph.ui.UIUtils;
import com.stanleyidesis.quotograph.ui.activity.modules.LWQChooseImageSourceModule;
import com.stanleyidesis.quotograph.ui.activity.modules.WhatsNewDialog;
import com.stanleyidesis.quotograph.ui.adapter.FontMultiselectAdapter;
import com.stanleyidesis.quotograph.ui.adapter.PlaylistAdapter;
import com.stanleyidesis.quotograph.ui.adapter.SearchResultsAdapter;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import it.sephiroth.android.library.tooltip.Tooltip;

import static butterknife.ButterKnife.findById;

/**
 * Copyright (c) 2016 Stanley Idesis
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * <p>
 * LWQSettingsActivity.java
 *
 * @author Stanley Idesis
 *         <p>
 *         From Quotograph
 *         https://github.com/stanidesis/quotograph
 *         <p>
 *         Please report any issues
 *         https://github.com/stanidesis/quotograph/issues
 *         <p>
 *         Date: 07/11/2015
 */
public class LWQSettingsActivity extends LWQWallpaperActivity implements ActivityStateFlags,
        SeekBar.OnSeekBarChangeListener,
        PlaylistAdapter.Delegate,
        SearchResultsAdapter.Delegate,
        MaterialDialog.ListCallback,
        DialogInterface.OnCancelListener,
        LWQChooseImageSourceModule.Delegate,
        Tooltip.Callback,
        UserSurveyController.Delegate {

    static class ActivityState {
        int page = -1;
        Map<Integer, Integer> newStateValues = new HashMap<>();
        BackgroundWallpaperState backgroundWallpaperState = null;
    }

    static class Builder {

        ActivityState activityState;

        ActivityState build() {
            return activityState;
        }

        private Builder() {
            activityState = new ActivityState();
        }

        private Builder(ActivityState copyState) {
            activityState = new ActivityState();
            activityState.page = copyState.page;
            activityState.newStateValues.putAll(copyState.newStateValues);
            activityState.backgroundWallpaperState = copyState.backgroundWallpaperState;
        }

        Builder setViewPagerPage(int page) {
            activityState.page = page;
            return this;
        }

        Builder setViewState(int resId, int stateFlags) {
            activityState.newStateValues.put(resId, stateFlags);
            return this;
        }

        Builder setWallpaperState(BackgroundWallpaperState backgroundWallpaperState) {
            activityState.backgroundWallpaperState = backgroundWallpaperState;
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

            long longestAnimation = 0;

            // ViewPager Page Set
            if (nextActivityState.page > -1) {
                final int newPage = nextActivityState.page;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        viewPager.setCurrentItem(newPage, true);
                    }
                });
            }

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

            // The MAIN loop…
            for (int resourceId : nextActivityState.newStateValues.keySet()) {
                final View view = findById(LWQSettingsActivity.this, resourceId);
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

    ActivityState stateInitial = new Builder()
            .setWallpaperState(BackgroundWallpaperState.HIDDEN)
            .setViewPagerPage(0)
            .setViewState(R.id.fab_lwq_plus, FLAG_HIDE | FLAG_DISABLE | FLAG_NO_ROTATE)
            .setViewState(R.id.group_lwq_settings_content, FLAG_HIDE)
            .setViewState(R.id.group_lwq_settings_fab_screen, FLAG_HIDE)
            .setViewState(R.id.pb_lwq_settings, FLAG_HIDE)
            .setViewState(R.id.btn_wallpaper_actions_skip, FLAG_DISABLE)
            .setViewState(R.id.btn_wallpaper_actions_save, FLAG_DISABLE)
            .setViewState(R.id.btn_wallpaper_actions_share, FLAG_DISABLE)
            .setViewState(R.id.group_lwq_settings_choose_image_source, FLAG_HIDE | FLAG_DISABLE)
            .build();

    ActivityState stateSkipWallpaper = new Builder()
            .setWallpaperState(BackgroundWallpaperState.HIDDEN)
            .setViewState(R.id.pb_lwq_settings, FLAG_REVEAL)
            .setViewState(R.id.btn_wallpaper_actions_skip, FLAG_ROTATE | FLAG_DISABLE)
            .setViewState(R.id.btn_wallpaper_actions_save, FLAG_DISABLE)
            .build();

    ActivityState stateSaveWallpaper = new Builder(stateSkipWallpaper)
            .setWallpaperState(BackgroundWallpaperState.REVEALED)
            .setViewState(R.id.btn_wallpaper_actions_skip, FLAG_DISABLE)
            .setViewState(R.id.btn_wallpaper_actions_save, FLAG_ROTATE | FLAG_DISABLE)
            .build();

    ActivityState stateSaveSkipCompleted = new Builder()
            .setWallpaperState(BackgroundWallpaperState.REVEALED)
            .setViewState(R.id.btn_wallpaper_actions_skip, FLAG_NO_ROTATE | FLAG_ENABLE)
            .setViewState(R.id.btn_wallpaper_actions_save, FLAG_NO_ROTATE | FLAG_ENABLE)
            .setViewState(R.id.pb_lwq_settings, FLAG_HIDE)
            .build();

    ActivityState stateSaveSkipCompletedObscured = new Builder(stateSaveSkipCompleted)
            .setWallpaperState(BackgroundWallpaperState.OBSCURED)
            .build();

    ActivityState statePlaylist = new Builder()
            .setViewPagerPage(1)
            .setWallpaperState(BackgroundWallpaperState.OBSCURED)
            .setViewState(R.id.group_lwq_settings_content, FLAG_REVEAL)
            .setViewState(R.id.fab_lwq_plus, FLAG_NO_ROTATE)
            .setViewState(R.id.group_lwq_settings_fab_screen, FLAG_HIDE)
            .setViewState(R.id.group_lwq_fab_screen_add_edit_quote, FLAG_HIDE | FLAG_DISABLE)
            .setViewState(R.id.group_lwq_fab_screen_search, FLAG_HIDE | FLAG_DISABLE)
            .setViewState(R.id.group_lwq_settings_choose_image_source, FLAG_HIDE | FLAG_DISABLE)
            .setViewState(R.id.pb_lwq_settings, FLAG_HIDE)
            .build();

    ActivityState stateSettings = new Builder(statePlaylist)
            .setViewPagerPage(2)
            .build();

    ActivityState stateWallpaperEdit = new Builder()
            .setWallpaperState(BackgroundWallpaperState.REVEALED)
            .setViewState(R.id.group_lwq_settings_content, FLAG_HIDE)
            .build();

    ActivityState stateWallpaper = new Builder(stateWallpaperEdit)
            .setViewPagerPage(0)
            .setViewState(R.id.btn_wallpaper_actions_skip, FLAG_ENABLE | FLAG_NO_ROTATE)
            .setViewState(R.id.btn_wallpaper_actions_save, FLAG_ENABLE | FLAG_NO_ROTATE)
            .setViewState(R.id.btn_wallpaper_actions_share, FLAG_ENABLE)
            .setViewState(R.id.pb_lwq_settings, FLAG_HIDE)
            .build();

    ActivityState stateAddReveal = new Builder()
            .setViewState(R.id.fab_lwq_plus, FLAG_ROTATE | FLAG_ENABLE)
            .setViewState(R.id.group_lwq_settings_fab_screen, FLAG_REVEAL | FLAG_ENABLE)
            .setViewState(R.id.group_lwq_fab_screen_add_edit_quote, FLAG_HIDE | FLAG_DISABLE)
            .setViewState(R.id.group_lwq_fab_screen_search, FLAG_HIDE | FLAG_DISABLE)
            .setViewState(R.id.group_lwq_settings_choose_image_source, FLAG_HIDE | FLAG_DISABLE)
            .build();

    ActivityState stateAddEditQuote = new Builder(stateAddReveal)
            .setViewState(R.id.group_lwq_fab_screen_add_edit_quote, FLAG_REVEAL | FLAG_ENABLE)
            .build();

    ActivityState stateSearch = new Builder(stateAddReveal)
            .setViewState(R.id.group_lwq_fab_screen_search, FLAG_REVEAL | FLAG_ENABLE)
            .setViewState(R.id.group_lwq_settings_fab_screen, FLAG_REVEAL | FLAG_ENABLE)
            .setViewState(R.id.pb_lwq_settings, FLAG_HIDE)
            .build();

    ActivityState stateSearchInProgress = new Builder(stateAddReveal)
            .setViewState(R.id.fab_lwq_plus, FLAG_ROTATE | FLAG_DISABLE)
            .setViewState(R.id.group_lwq_settings_fab_screen, FLAG_REVEAL | FLAG_DISABLE)
            .setViewState(R.id.group_lwq_fab_screen_search, FLAG_REVEAL | FLAG_DISABLE)
            .setViewState(R.id.pb_lwq_settings, FLAG_REVEAL)
            .build();

    ActivityState stateChooseImageSources = new Builder(stateSettings)
            .setViewState(R.id.group_lwq_settings_choose_image_source, FLAG_REVEAL | FLAG_ENABLE)
            .build();

    private static final int REQUEST_CODE_SAVE = 0;
    private static final int REQUEST_CODE_ALBUM = 1;

    // Current ActivityState
    ActivityState activityState = null;

    // State Queue
    BlockingDeque<ActivityState> stateBlockingDeque = new LinkedBlockingDeque<>();

    // Executes state changes
    ExecutorService changeStateExecutorService = Executors.newSingleThreadScheduledExecutor();

    // PlaylistAdapter
    PlaylistAdapter playlistAdapter;

    // Editing this Quote
    Quote editingQuote;
    int editingQuotePosition;

    // Seekbar Status
    boolean isModifyingSeekSetting;

    // Should we show tooltips?
    boolean showTutorialTips = true;
    Set<TutorialTooltips> visibleTips;

    // Ads
    InterstitialAd interstitialAd;
    boolean interstitialVisible;

    // Content
    @Bind(R.id.group_lwq_settings_content)
    View content;

    // Layout Controls
    @Bind(R.id.tab_layout_lwq_settings)
    TabLayout tabLayout;
    @Bind(R.id.viewpager_lwq_settings)
    ViewPager viewPager;

    // Preview Container and Wallpaper Actions
    @Bind(R.id.group_lwq_settings_wallpaper_preview_wrapper)
    View wallpaperPreviewWrapper;
    @Bind(R.id.btn_wallpaper_actions_share)
    View shareButton;
    @Bind(R.id.btn_wallpaper_actions_save)
    View saveButton;
    @Bind(R.id.btn_wallpaper_actions_skip)
    View skipButton;

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

    // Choose Images
    @Bind(R.id.group_lwq_settings_choose_image_source)
    View chooseImagesContainer;
    // This is a slight departure from the way this class currently handles UI elements.
    // This module encapsulates some functionality, thereby hiding it from this class.
    LWQChooseImageSourceModule chooseImageSourceModule;

    // Choose Fonts
    MaterialDialog chooseFontsDialog;

    // FAB
    @Bind(R.id.group_lwq_settings_fab_screen)
    View fabContainer;
    @Bind(R.id.fab_lwq_plus)
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
        setContentView(R.layout.activity_lwq_settings);
        ButterKnife.bind(this);

        content.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                content.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                View[] buttons = new View[]{shareButton, saveButton, skipButton};
                for (View button : buttons) {
                    button.setAlpha(0f);
                    button.setTranslationY(button.getHeight() * 2);
                    button.requestLayout();
                }
                animateWallpaperActions(false);
            }
        });

        showTutorialTips = !LWQPreferences.viewedTutorial();
        if (showTutorialTips) {
            visibleTips = new HashSet<>();
        }

        // Fetch remote config
        LWQApplication.fetchRemoteConfig();

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
        // Setup image source chooser
        setupChooseImageSources();

        // Track initial Screen View
        AnalyticsUtils.trackScreenView(
                AnalyticsUtils.SCREEN_WALLPAPER_PREVIEW);

        // Ads
        setupInterstitialAd();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (LWQWallpaperControllerHelper.get().activeWallpaperLoaded()) {
            changeState(stateWallpaper);
        } else {
            changeState(stateInitial);
        }
        if (WhatsNewDialog.shouldShowDialog()) {
            showWhatsNewDialog();
        }
    }

    @SuppressWarnings("WrongConstant")
    @SuppressLint("NewApi")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_SAVE) {
            changeState(stateSaveSkipCompleted);
            return;
        } else if (requestCode == IabConst.PURCHASE_REQUEST_CODE) {
            LWQApplication.getIabHelper().handleActivityResult(requestCode, resultCode, data);
        } else if (requestCode == REQUEST_CODE_ALBUM) {
            if (resultCode != RESULT_OK) {
                return;
            }
            List<String> resultUris = new ArrayList<>();
//            final int takeFlags = data.getFlags()
//                    & Intent.FLAG_GRANT_READ_URI_PERMISSION;
            if (data.getClipData() != null) {
                ClipData clipData = data.getClipData();
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    Uri imageUri = clipData.getItemAt(i).getUri();
//                    getContentResolver().takePersistableUriPermission(imageUri, takeFlags);
                    resultUris.add(imageUri.toString());
                }
            } else if (data.getData() != null) {
                Uri imageUri = data.getData();
//                getContentResolver().takePersistableUriPermission(imageUri, takeFlags);
                resultUris.add(imageUri.toString());
            }
            chooseImageSourceModule.onImagesRecovered(resultUris);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (LWQWallpaperControllerHelper.get().getRetrievalState()
                != LWQWallpaperController.RetrievalState.NONE) {
            changeState(stateSkipWallpaper);
        } else if (activityState == stateSearchInProgress) {
            changeState(stateSearch);
            AnalyticsUtils.trackScreenView(AnalyticsUtils.SCREEN_ADD_SEARCH);
        }
        // Show survey if applicable
        UserSurveyController.showSurvey(this);
    }

    @Override
    public void onBackPressed() {
        if (activityState == stateAddReveal) {
            changeState(statePlaylist);
        } else if (activityState == stateSearch || activityState == stateAddEditQuote) {
            changeState(stateAddReveal);
            AnalyticsUtils.trackScreenView(AnalyticsUtils.SCREEN_ADD);
        } else if (activityState == stateChooseImageSources) {
            changeState(stateSettings);
        } else {
            if (activityState == stateSkipWallpaper || activityState == stateSaveWallpaper) {
                changeState(stateSaveSkipCompleted);
            }
            super.onBackPressed();
        }
    }

    // Setup

    private void setupInterstitialAd() {
        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getString(R.string.admob_prime_interstitial));
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
            }
        });
        requestNewInterstitial();
    }

    private void requestNewInterstitial() {
        if (BuildConfig.DEBUG && !BuildConfig.TEST_ADS) {
            return;
        }
        // Ew, Admob, ew...
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                interstitialAd.loadAd(AdMobUtils.buildRequest(LWQSettingsActivity.this));
            }
        }, 1);
    }

    void setupContent() {
        content.setAlpha(0f);
        content.setTag(R.id.view_tag_flags, FLAG_HIDE);
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
        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(new PagerAdapter() {

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                switch (position) {
                    case 0:
                        return wallpaperPreviewWrapper;
                    case 1:
                        return playlistWrapper;
                    default:
                        return settingsWrapper;
                }
            }

            @Override
            public int getCount() {
                return 3;
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
                switch (position) {
                    case 0:
                        if (activityState != stateSkipWallpaper) {
                            setBackgroundAlpha(BackgroundWallpaperState.OBSCURED.screenAlpha * positionOffset);
                        }
                        // Fab Add
                        fabAdd.setAlpha(positionOffset);
                        fabAdd.setScaleX(positionOffset);
                        fabAdd.setScaleY(positionOffset);
                        // Content
                        content.setAlpha(positionOffset);
                        // At .3, share is gone, at .6, save is gone, and at 1, skip is gone
                        shareButton.setAlpha(1f - (Math.min(positionOffset, 1 / 3f) / (1 / 3f)));
                        shareButton.setTranslationY((1f - shareButton.getAlpha()) * shareButton.getHeight() * 2);
                        saveButton.setAlpha(1f - (Math.min(positionOffset, 2 / 3f) / (2 / 3f)));
                        saveButton.setTranslationY((1f - saveButton.getAlpha()) * saveButton.getHeight() * 2);
                        skipButton.setAlpha(1f - positionOffset);
                        skipButton.setTranslationY((1f - skipButton.getAlpha()) * skipButton.getHeight() * 2);
                        break;
                    case 1:
                        fabAdd.setAlpha(1f - positionOffset);
                        fabAdd.setScaleX(1f - positionOffset);
                        fabAdd.setScaleY(1f - positionOffset);
                }
            }

            @Override
            public void onPageSelected(int position) {
                // Content
                content.setAlpha(position == 0 ? 0f : 1f);

                // Add Fab
                fabAdd.setScaleX(position == 1 ? 1f : 0f);
                fabAdd.setScaleY(position == 1 ? 1f : 0f);
                fabAdd.setAlpha(position == 1 ? 1f : 0f);
                fabAdd.setEnabled(position == 1);


                // Tooltips
                if (position == 1) {
                    // Log screen view
                    AnalyticsUtils.trackScreenView(AnalyticsUtils.SCREEN_PLAYLIST);
                    showTutorialTip(TutorialTooltips.ADD);
                } else if (position == 2) {
                    // Log screen view
                    AnalyticsUtils.trackScreenView(AnalyticsUtils.SCREEN_SETTINGS);
                    // Delay it just a little because the view messes up otherwise
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            showTutorialTip(TutorialTooltips.SETTING);
                        }
                    }, 200);
                } else {
                    // Log screen view
                    AnalyticsUtils.trackScreenView(AnalyticsUtils.SCREEN_WALLPAPER_PREVIEW);
                }

                // Action Buttons
                View[] buttons = new View[]{shareButton, saveButton, skipButton};
                for (View button : buttons) {
                    button.setTranslationY(position == 0 ? 0f : button.getHeight() * 2f);
                    button.setAlpha(position == 0 ? 1f : 0f);
                    button.setEnabled(position == 0);
                }

                if (activityState == stateSkipWallpaper) {
                    return;
                }
                // Background
                setBackgroundAlpha(position == 0 ?
                        BackgroundWallpaperState.REVEALED.screenAlpha :
                        BackgroundWallpaperState.OBSCURED.screenAlpha
                );
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.palette_500));
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setIcon(R.drawable.selectable_preview_button);
        tabLayout.getTabAt(0).setContentDescription("Preview Quotograph");
        tabLayout.getTabAt(1).setIcon(R.drawable.selectable_playlist_button);
        tabLayout.getTabAt(1).setContentDescription("Playlist");
        tabLayout.getTabAt(2).setIcon(R.drawable.selectable_settings_button);
        tabLayout.getTabAt(2).setContentDescription("Settings");
    }

    void setupWallpaperActions() {
        int navBarHeight = UIUtils.getNavBarHeight(this);
        if (navBarHeight > 0) {
            View wallpaperActionsContainer = findById(this, R.id.group_lwq_settings_wallpaper_actions);
            float navBarHeightPercentage = (float) navBarHeight / (float) UIUtils.getRealScreenSize().y;
            final PercentFrameLayout.LayoutParams layoutParams =
                    (PercentFrameLayout.LayoutParams) wallpaperActionsContainer.getLayoutParams();
            layoutParams.getPercentLayoutInfo().bottomMarginPercent += navBarHeightPercentage;
            wallpaperActionsContainer.requestLayout();
        }
        shareButton.setTag(R.id.view_tag_flags, FLAG_ENABLE | FLAG_REVEAL);
        skipButton.setTag(R.id.view_tag_flags, FLAG_ENABLE | FLAG_REVEAL | FLAG_NO_ROTATE);
        skipButton.setTag(R.id.view_tag_animator_rotate, new Runnable() {
            @Override
            public void run() {
                animateAction(skipButton);
            }
        });
        skipButton.setTag(R.id.view_tag_animator_unrotate, new Runnable() {
            @Override
            public void run() {
                endActionAnimation(skipButton);
            }
        });
        saveButton.setTag(R.id.view_tag_flags, FLAG_ENABLE | FLAG_REVEAL | FLAG_NO_ROTATE);
        saveButton.setTag(R.id.view_tag_animator_rotate, new Runnable() {
            @Override
            public void run() {
                animateAction(saveButton);
            }
        });
        saveButton.setTag(R.id.view_tag_animator_unrotate, new Runnable() {
            @Override
            public void run() {
                endActionAnimation(saveButton);
            }
        });
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

        int navBarHeight = UIUtils.getNavBarHeight(this);
        if (navBarHeight > 0) {
            float navBarHeightPercentage = (float) navBarHeight / (float) UIUtils.getRealScreenSize().y;
            View fabAddWrapper = findById(this, R.id.fl_lwq_fab_reveal);
            PercentRelativeLayout.LayoutParams layoutParams =
                    (PercentRelativeLayout.LayoutParams) fabAddWrapper.getLayoutParams();
            layoutParams.getPercentLayoutInfo().bottomMarginPercent += navBarHeightPercentage;
            fabAddWrapper.requestLayout();
        }

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
                final List<Author> list = Select.from(Author.class).orderBy(NamingHelper.toSQLNameDefault("name")).list();
                String[] allAuthors = new String[list.size()];
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
        final RecyclerView recyclerView = findById(this, R.id.recycler_fab_screen_search_results);
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
        RecyclerView recyclerView = findById(this, R.id.recycler_playlist);
        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(playlistAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    void setupSettings() {
        final String[] refreshPreferenceOptions = getResources().getStringArray(R.array.refresh_preference_options);
        ArrayAdapter<String> refreshOptionsAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_item,
                refreshPreferenceOptions);
        refreshOptionsAdapter.setDropDownViewResource(R.layout.spinner_drop_down_item);
        Spinner refreshSpinner = findById(settingsContainer, R.id.spinner_lwq_settings_refresh);
        refreshSpinner.setAdapter(refreshOptionsAdapter);
        refreshSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
                // Log refresh rate choice
                LWQLoggerHelper.get().logRefreshRate((String) adapterView.getAdapter().getItem(index));
                final int[] refreshValues = getResources().getIntArray(R.array.refresh_preference_values);
                LWQPreferences.setRefreshPreference(refreshValues[index]);
                LWQAlarmController.resetAlarm();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        updateRefreshSpinner();

        // Blur
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            findById(settingsContainer, R.id.sb_lwq_settings_blur).setVisibility(View.GONE);
        } else {
            SeekBar blurBar = findById(settingsContainer, R.id.sb_lwq_settings_blur);
            blurBar.setProgress(LWQPreferences.getBlurPreference());
            blurBar.setOnSeekBarChangeListener(this);
        }

        // Dim
        SeekBar dimBar = findById(settingsContainer, R.id.sb_lwq_settings_dim);
        dimBar.setProgress(LWQPreferences.getDimPreference());
        dimBar.setOnSeekBarChangeListener(this);

        AppCompatCheckBox doubleTapCheckbox = findById(this, R.id.check_lwq_settings_double_tap);
        doubleTapCheckbox.setChecked(LWQPreferences.isDoubleTapEnabled());

        // Fonts
        findById(settingsContainer, R.id.btn_lwq_settings_fonts).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseFontsDialog = new MaterialDialog.Builder(LWQSettingsActivity.this)
                        .title("Choose Fonts")
                        .adapter(new FontMultiselectAdapter(LWQSettingsActivity.this),
                                LWQSettingsActivity.this)
                        .alwaysCallMultiChoiceCallback()
                        .autoDismiss(false)
                        .canceledOnTouchOutside(true)
                        .cancelListener(LWQSettingsActivity.this)
                        .build();
                chooseFontsDialog.show();
                // Log view
                AnalyticsUtils.trackScreenView(AnalyticsUtils.SCREEN_FONTS);
            }
        });

        // Images
        findById(settingsContainer, R.id.btn_lwq_settings_images)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        changeState(stateChooseImageSources);
                        // Log view
                        AnalyticsUtils.trackScreenView(AnalyticsUtils.SCREEN_IMAGES);
                    }
                });
    }

    void updateRefreshSpinner() {
        Spinner refreshSpinner = findById(settingsContainer, R.id.spinner_lwq_settings_refresh);
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
        progressBar.setTag(R.id.view_tag_flags, FLAG_HIDE | FLAG_ENABLE);
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

    void setupChooseImageSources() {
        chooseImageSourceModule = new LWQChooseImageSourceModule();
        chooseImageSourceModule.initialize(this, chooseImagesContainer);
        chooseImagesContainer.setTag(R.id.view_tag_flags, FLAG_HIDE | FLAG_DISABLE);
        chooseImagesContainer.setTag(R.id.view_tag_animator_hide, new Runnable() {
            @Override
            public void run() {
                chooseImageSourceModule.changeVisibility(
                        findById(LWQSettingsActivity.this,
                                R.id.btn_lwq_settings_images),
                        false);
            }
        });
        chooseImagesContainer.setTag(R.id.view_tag_animator_reveal, new Runnable() {
            @Override
            public void run() {
                chooseImageSourceModule.changeVisibility(
                        findById(LWQSettingsActivity.this,
                                R.id.btn_lwq_settings_images),
                        true);
            }
        });
    }

    // Click Handling

    // Hanlde the click for every settings option
    @OnClick({R.id.tv_lwq_settings_blur, R.id.tv_lwq_settings_dim,
            R.id.tv_lwq_settings_double_tap, R.id.tv_lwq_settings_fonts,
            R.id.tv_lwq_settings_images, R.id.tv_lwq_settings_refresh,
            R.id.tv_lwq_settings_whats_new})
    void showSettingsTooltip(View view) {
        switch (view.getId()) {
            case R.id.tv_lwq_settings_blur:
                showToolTip(SettingsTooltips.BLUR, null);
                break;
            case R.id.tv_lwq_settings_dim:
                showToolTip(SettingsTooltips.DIM, null);
                break;
            case R.id.tv_lwq_settings_double_tap:
                showToolTip(SettingsTooltips.DOUBLE_TAP, null);
                break;
            case R.id.tv_lwq_settings_fonts:
                showToolTip(SettingsTooltips.FONTS, null);
                break;
            case R.id.tv_lwq_settings_images:
                showToolTip(SettingsTooltips.IMAGES, null);
                break;
            case R.id.tv_lwq_settings_refresh:
                showToolTip(SettingsTooltips.REFRESH, null);
                break;
            case R.id.tv_lwq_settings_whats_new:
                showToolTip(SettingsTooltips.WHATS_NEW, null);
                break;
        }
    }

    @OnClick(R.id.btn_lwq_settings_whats_new)
    void showWhatsNewDialog() {
        // Log view
        AnalyticsUtils.trackScreenView(AnalyticsUtils.SCREEN_WHATS_NEW);
        // Show dialog
        WhatsNewDialog dialog = new WhatsNewDialog();
        dialog.initialize(this, null);
        dialog.changeVisibility(null, true);
    }

    @OnClick(R.id.btn_fab_screen_search)
    void performSearch() {
        UIUtils.dismissKeyboard(this);
        changeState(stateSearchInProgress);
        final int itemCount = searchResultsAdapter.getItemCount();
        searchResultsAdapter.setSearchResults(new ArrayList<Object>());
        searchResultsAdapter.notifyItemRangeRemoved(0, itemCount);
        String query = editableQuery.getText().toString().trim();
        LWQQuoteControllerHelper.get().fetchQuotes(query, new Callback<List<Object>>() {
            @Override
            public void onSuccess(final List<Object> objects) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        changeState(stateSearch);
                        searchResultsAdapter.setSearchResults(objects);
                        searchResultsAdapter.notifyItemRangeInserted(0, searchResultsAdapter.getItemCount());
                    }
                });
            }

            @Override
            public void onError(LWQError error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LWQSettingsActivity.this, "Search pooped out :(, please try again later.", Toast.LENGTH_LONG).show();
                        changeState(stateSearch);
                    }
                });
            }
        });
        // Log the search
        AnalyticsUtils.trackSearch(query);
    }

    @OnClick(R.id.fab_lwq_create_quote)
    void revealAddEditQuote() {
        if (activityState == stateAddEditQuote) {
            changeState(stateAddReveal);
            // Log the view
            AnalyticsUtils.trackScreenView(AnalyticsUtils.SCREEN_ADD);
        } else {
            editableAuthor.setText("");
            editableQuote.setText("");
            changeState(stateAddEditQuote);
            // Log the view
            AnalyticsUtils.trackScreenView(AnalyticsUtils.SCREEN_ADD_QUOTE);
        }
    }

    @OnClick(R.id.btn_fab_screen_save)
    void saveQuote() {
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
            changeState(statePlaylist);
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
        changeState(statePlaylist);
        UIUtils.dismissKeyboard(this);
        // Log quote
        AnalyticsUtils.trackEvent(
                AnalyticsUtils.CATEGORY_QUOTE,
                AnalyticsUtils.ACTION_CREATED,
                "".equalsIgnoreCase(author.name)
                        ? null : author.name
        );
    }

    @OnClick(R.id.btn_fab_screen_cancel)
    void dismissAddEditQuote() {
        editingQuote = null;
        editingQuotePosition = -1;
        changeState(stateAddReveal);
        UIUtils.dismissKeyboard(this);
        // Log the view
        AnalyticsUtils.trackScreenView(AnalyticsUtils.SCREEN_ADD);
    }

    @OnClick(R.id.fab_lwq_plus)
    void toggleAddScreen() {
        if (activityState == stateAddReveal
                || activityState == stateAddEditQuote
                || activityState == stateSearch) {
            UIUtils.dismissKeyboard(this);
            changeState(statePlaylist);
            // Log the view
            AnalyticsUtils.trackScreenView(AnalyticsUtils.SCREEN_PLAYLIST);
        } else {
            changeState(stateSearch);
            // Log the view
            AnalyticsUtils.trackScreenView(AnalyticsUtils.SCREEN_ADD_SEARCH);
        }
    }

    @OnClick(R.id.fab_lwq_search)
    void revealSearch() {
        if (activityState == stateSearch) {
            changeState(stateAddReveal);
            UIUtils.dismissKeyboard(this);
            // Log the view
            AnalyticsUtils.trackScreenView(AnalyticsUtils.SCREEN_ADD);
        } else {
            editableQuery.clearComposingText();
            editableQuery.clearFocus();
            changeState(stateSearch);
            // Log the view
            AnalyticsUtils.trackScreenView(AnalyticsUtils.SCREEN_ADD_SEARCH);
        }
    }

    @OnClick(R.id.btn_wallpaper_actions_skip)
    void skipWallpaperClick() {
        AnalyticsUtils.trackEvent(AnalyticsUtils.CATEGORY_WALLPAPER,
                AnalyticsUtils.ACTION_SKIPPED,
                AnalyticsUtils.LABEL_IN_APP);
        LWQWallpaperControllerHelper.get().generateNewWallpaper();
        // Show an interstitial, if applicable
        if (interstitialAd.isLoaded()) {
            interstitialAd.show();
        }
    }

    @OnClick(R.id.btn_wallpaper_actions_save)
    void saveWallpaperClick() {
        startActivityForResult(new Intent(this, LWQSaveWallpaperActivity.class), REQUEST_CODE_SAVE);
        changeState(stateSaveWallpaper);
    }

    @OnClick(R.id.btn_wallpaper_actions_share)
    void shareWallpaperClick() {
        sendBroadcast(new Intent(getString(R.string.action_share)));
    }

    @OnCheckedChanged(R.id.check_lwq_settings_double_tap)
    void onDoubleTapCheckChange(boolean checked) {
        LWQPreferences.setDoubleTapEnabled(checked);
    }

    // Animation

    void endActionAnimation(final View button) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (button.getTag(R.id.view_tag_animator) != null) {
                    ((Animator) button.getTag(R.id.view_tag_animator)).end();
                    button.setTag(R.id.view_tag_animator, null);
                }
            }
        });
    }

    void animateWallpaperActions(final boolean dismiss) {
        AnimatorSet animatorSet = new AnimatorSet();
        Animator shareButtonAnimator = generateAnimator(shareButton, dismiss, 0);
        if (!dismiss) {
            // Delay the tooltip a bit to make sure it lines up right
            shareButtonAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            showTutorialTip(TutorialTooltips.SHARE);
                        }
                    }, 500);
                }
            });
        }
        animatorSet.playTogether(shareButtonAnimator,
                generateAnimator(saveButton, dismiss, 20),
                generateAnimator(skipButton, dismiss, 35));
        animatorSet.start();
    }

    Animator generateAnimator(View target, boolean dismiss, long startDelay) {
        float[] fadeIn = new float[]{0f, 1f};
        float[] fadeOut = new float[]{1f, 0f};
        final ObjectAnimator propAnimator = ObjectAnimator.ofPropertyValuesHolder(target,
                PropertyValuesHolder.ofFloat(View.ALPHA, dismiss ? fadeOut : fadeIn),
                PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, dismiss ? (target.getHeight() * 2f) : 0f));
        propAnimator.setStartDelay(startDelay);
        propAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        propAnimator.setDuration(240);
        return propAnimator;
    }

    void animateAction(final View button) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Animator animator = AnimatorInflater.loadAnimator(LWQSettingsActivity.this, R.animator.progress_rotation);
                animator.setTarget(button);
                button.setTag(R.id.view_tag_animator, animator);
                animator.start();
            }
        });
    }

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
                        }).start();
            }
        });
    }

    void animateContent(final boolean dismiss) {
        content.clearAnimation();
        content.animate().alpha(dismiss ? 0f : 1f).setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
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
                    UIUtils.dismissKeyboard(LWQSettingsActivity.this);
                } else {
                    UIUtils.revealKeyboard(container == addEditQuote ? editableQuote : editableQuery);
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
        float[] toFrom = dismiss ? new float[]{1f, .2f} : new float[]{.2f, 1f};
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
        Animator backgroundAnimator;

        if (Build.VERSION.SDK_INT >= 21) {
            Rect fabRect = new Rect();
            fabAdd.getGlobalVisibleRect(fabRect);
            final Point realScreenSize = UIUtils.getRealScreenSize();
            int radius = Math.max(realScreenSize.x, realScreenSize.y);
            backgroundAnimator = ViewAnimationUtils.createCircularReveal(fabBackground,
                    fabRect.centerX(),
                    fabRect.centerY(),
                    dismiss ? radius : 0,
                    dismiss ? 0 : radius);
        } else {
            backgroundAnimator = ObjectAnimator.ofFloat(fabBackground, "alpha", dismiss ? 1f : 0f, dismiss ? 0f : 1f);
        }
        backgroundAnimator.setDuration(300).setInterpolator(new AccelerateDecelerateInterpolator());
        backgroundAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (dismiss) {
                    fabBackground.setVisibility(View.GONE);
                    // Tell them to swipe again for settings!
                    showTutorialTip(TutorialTooltips.SWIPE_AGAIN);
                } else {
                    // Tell them about search!
                    showTutorialTip(TutorialTooltips.SEARCH);
                }
            }
        });
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

    void addRemovePlaylistItem(Object item, boolean add) {
        String analyticsLabel;
        if (item instanceof PlaylistCategory) {
            analyticsLabel = ((PlaylistCategory) item).category.name;
        } else if (item instanceof PlaylistAuthor) {
            analyticsLabel = ((PlaylistAuthor) item).author.name;
        } else if (item instanceof PlaylistQuote) {
            String quote = ((PlaylistQuote) item).quote.text;
            analyticsLabel = quote.substring(0, Math.min(35, quote.length())) + "...";
        } else {
            return;
        }
        if (add) {
            playlistAdapter.insertItem(item);
        } else {
            if (playlistAdapter.getPlaylistItemCount() == 1) {
                Toast.makeText(this, "Your playlist may not be empty", Toast.LENGTH_LONG).show();
                return;
            }
            playlistAdapter.removeItem(item);
            ((SugarRecord) item).delete();
        }
        // Log it
        AnalyticsUtils.trackEvent(AnalyticsUtils.CATEGORY_PLAYLIST,
                add ? AnalyticsUtils.ACTION_ADDED : AnalyticsUtils.ACTION_REMOVED,
                analyticsLabel);
    }

    // Event Handling

    @Subscribe
    public void onEvent(final IabPurchaseEvent purchaseEvent) {
        // TODO remove ads
    }

    @Subscribe
    public void onEvent(PreferenceUpdateEvent preferenceUpdateEvent) {
        if (preferenceUpdateEvent.getPreferenceKeyId() == R.string.preference_key_refresh) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateRefreshSpinner();
                }
            });
        }
    }

    @Subscribe
    public void onEvent(final WallpaperEvent wallpaperEvent) {
        if (wallpaperEvent.didFail()) {
            changeState(viewPager.getCurrentItem() == 0 ?
                    stateSaveSkipCompleted : stateSaveSkipCompletedObscured);
            if (wallpaperEvent.getErrorMessage() == null) {
                return;
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(LWQSettingsActivity.this,
                            wallpaperEvent.getErrorMessage(),
                            Toast.LENGTH_LONG).show();
                }
            });
        } else if (wallpaperEvent.getStatus() == WallpaperEvent.Status.RENDERED_WALLPAPER) {
            if (!isModifyingSeekSetting) {
                changeState(viewPager.getCurrentItem() == 0 ?
                        stateSaveSkipCompleted : stateSaveSkipCompletedObscured);
            } else if (activityState == stateInitial) {
                changeState(stateWallpaper);
            }
        } else {
            changeState(stateSkipWallpaper);
        }
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
        isModifyingSeekSetting = true;
        changeState(stateWallpaperEdit);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        isModifyingSeekSetting = false;
        changeState(stateSettings);
    }

    // PlaylistAdapter.Delegate

    @Override
    public void onPlaylistItemRemove(PlaylistAdapter adapter, int position) {
        addRemovePlaylistItem(adapter.getItem(position), false);
    }

    @Override
    public void onQuoteEdit(PlaylistAdapter adapter, int position) {
        final Object item = adapter.getItem(position);
        if (item instanceof PlaylistQuote) {
            editingQuotePosition = position;
            editingQuote = ((PlaylistQuote) item).quote;
            editableAuthor.setText(editingQuote.author.name);
            editableQuote.setText(editingQuote.text);
            if (activityState != stateAddEditQuote) {
                changeState(stateAddEditQuote);
                AnalyticsUtils.trackScreenView(AnalyticsUtils.SCREEN_ADD_QUOTE);
            }
        }
        // Log the edit
        AnalyticsUtils.trackEvent(AnalyticsUtils.CATEGORY_QUOTE,
                AnalyticsUtils.ACTION_EDITED);
    }

    @Override
    public void onMakeQuotograph(PlaylistAdapter adapter, int position) {
        final Object item = adapter.getItem(position);
        if (item instanceof PlaylistCategory) {
            LWQWallpaperControllerHelper.get().generateNewWallpaper((PlaylistCategory) item);
        } else if (item instanceof PlaylistAuthor) {
            LWQWallpaperControllerHelper.get().generateNewWallpaper((PlaylistAuthor) item);
        } else if (item instanceof PlaylistQuote) {
            LWQWallpaperControllerHelper.get().generateNewWallpaper((PlaylistQuote) item);
        }
        // Track manual creation
        AnalyticsUtils.trackEvent(AnalyticsUtils.CATEGORY_WALLPAPER,
                AnalyticsUtils.ACTION_MANUALLY_GEN,
                AnalyticsUtils.LABEL_PLAYLIST);
        if (interstitialAd.isLoaded()) {
            interstitialAd.show();
        }
    }

    @Override
    public void onRemove(SearchResultsAdapter adapter, Object playlistItem) {
        addRemovePlaylistItem(playlistItem, false);
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
            addRemovePlaylistItem(playlistItem, true);
        }
        return playlistItem;
    }

    // MaterialDialog Delegates

    @Override
    public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
        FontMultiselectAdapter adapter = (FontMultiselectAdapter) dialog.getListView().getAdapter();
        adapter.addOrRemoveFont(which);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        if (!(dialog instanceof MaterialDialog)) {
            return;
        }
        MaterialDialog materialDialog = (MaterialDialog) dialog;
        if (materialDialog.getListView() != null &&
                materialDialog.getListView().getAdapter() instanceof FontMultiselectAdapter) {
            FontMultiselectAdapter adapter = (FontMultiselectAdapter) materialDialog.getListView()
                    .getAdapter();
            adapter.setDefaultsIfNecessary();
        }
        chooseFontsDialog = null;
    }


    // LWQChooseImageSourceModule delegate

    @Override
    public void addPhotoAlbum(LWQChooseImageSourceModule module) {
        Intent documentPicker = new Intent(Intent.ACTION_GET_CONTENT);
        documentPicker.setType("image/*");
        documentPicker.addCategory(Intent.CATEGORY_OPENABLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            documentPicker.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }
        startActivityForResult(Intent.createChooser(documentPicker, "Choose photo(s)"), REQUEST_CODE_ALBUM);
        // Track viewing
        AnalyticsUtils.trackScreenView(
                AnalyticsUtils.SCREEN_CUSTOM_PHOTOS);
    }

    // UserSurveyController Delegate

    @Override
    public void revealSurveyInPlaylist() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                playlistAdapter.setShowSurvey(true);
            }
        });
    }


    // Tooltip Sequence

    private enum SettingsTooltips {

        BLUR(R.string.tt_setting_blur, R.id.sb_lwq_settings_blur),
        DIM(R.string.tt_setting_dim, R.id.sb_lwq_settings_dim),
        FONTS(R.string.tt_setting_fonts, R.id.btn_lwq_settings_fonts),
        IMAGES(R.string.tt_setting_images, R.id.btn_lwq_settings_images),
        REFRESH(R.string.tt_setting_refresh, R.id.spinner_lwq_settings_refresh),
        DOUBLE_TAP(R.string.tt_setting_double_tap, R.id.check_lwq_settings_double_tap),
        WHATS_NEW(R.string.tt_setting_whats_new, R.id.btn_lwq_settings_whats_new);

        int stringId;
        int anchorId;

        SettingsTooltips(int stringId, int anchorId) {
            this.stringId = stringId;
            this.anchorId = anchorId;
        }
    }

    private enum TutorialTooltips {
        SHARE(R.string.tt_share_quotograph,
                R.id.btn_wallpaper_actions_share,
                Tooltip.ClosePolicy.TOUCH_ANYWHERE_NO_CONSUME),
        SAVE(R.string.tt_save_quotograph,
                R.id.btn_wallpaper_actions_save,
                Tooltip.ClosePolicy.TOUCH_ANYWHERE_NO_CONSUME),
        SKIP(R.string.tt_skip_quotograph,
                R.id.btn_wallpaper_actions_skip,
                Tooltip.ClosePolicy.TOUCH_ANYWHERE_NO_CONSUME),
        SWIPE(R.string.tt_swipe_left_wallpaper,
                R.id.viewpager_lwq_settings,
                Tooltip.ClosePolicy.TOUCH_ANYWHERE_NO_CONSUME,
                Tooltip.Gravity.CENTER,
                true),
        ADD(R.string.tt_add_quotes,
                R.id.fab_lwq_plus,
                Tooltip.ClosePolicy.TOUCH_ANYWHERE_NO_CONSUME,
                Tooltip.Gravity.LEFT,
                true),
        SEARCH(R.string.tt_search_for_quotes,
                R.id.actv_fab_screen_search,
                Tooltip.ClosePolicy.TOUCH_ANYWHERE_NO_CONSUME,
                Tooltip.Gravity.LEFT,
                false),
        WRITE(R.string.tt_write_quotes,
                R.id.fab_lwq_create_quote,
                Tooltip.ClosePolicy.TOUCH_ANYWHERE_NO_CONSUME,
                Tooltip.Gravity.LEFT,
                false),
        EXIT(R.string.tt_exit_add,
                R.id.fab_lwq_plus,
                Tooltip.ClosePolicy.TOUCH_ANYWHERE_NO_CONSUME,
                Tooltip.Gravity.LEFT,
                true),
        SWIPE_AGAIN(R.string.tt_swipe_left_playlist,
                R.id.viewpager_lwq_settings,
                Tooltip.ClosePolicy.TOUCH_ANYWHERE_NO_CONSUME,
                Tooltip.Gravity.CENTER,
                true),
        SETTING(R.string.tt_setting_info,
                R.id.tv_lwq_settings_dim,
                Tooltip.ClosePolicy.TOUCH_ANYWHERE_NO_CONSUME,
                Tooltip.Gravity.BOTTOM,
                false);

        int stringId;
        boolean stop;
        int anchorId;
        Tooltip.ClosePolicy closePolicy;
        Tooltip.Gravity gravity;

        TutorialTooltips(int stringId, int anchorId, Tooltip.ClosePolicy closePolicy) {
            this(stringId, anchorId, closePolicy, Tooltip.Gravity.TOP, false);
        }

        TutorialTooltips(int stringId, int anchorId, Tooltip.ClosePolicy closePolicy, Tooltip.Gravity gravity, boolean stop) {
            this.stringId = stringId;
            this.anchorId = anchorId;
            this.closePolicy = closePolicy;
            this.gravity = gravity;
            this.stop = stop;
        }
    }

    void showTutorialTip(TutorialTooltips tooltip) {
        if (!showTutorialTips || visibleTips.contains(tooltip)) {
            return;
        }
        // Assume they've seen the tutorial after we reveal the share tip
        if (tooltip == TutorialTooltips.SHARE) {
            LWQPreferences.setViewedTutorial(true);
            AnalyticsUtils.trackEvent(
                    AnalyticsUtils.CATEGORY_TOOLTIPS,
                    AnalyticsUtils.ACTION_STARTED
            );
        }
        showToolTip(tooltip, this);
    }

    void showToolTip(Object tooltipObj, Tooltip.Callback callback) {
        int id = 0;
        int stringId = 0;
        int anchorId = 0;
        Tooltip.ClosePolicy closePolicy =
                Tooltip.ClosePolicy.TOUCH_ANYWHERE_NO_CONSUME;
        Tooltip.Gravity gravity = Tooltip.Gravity.BOTTOM;
        if (tooltipObj instanceof TutorialTooltips) {
            TutorialTooltips tooltip = (TutorialTooltips) tooltipObj;
            id = tooltip.ordinal();
            stringId = tooltip.stringId;
            anchorId = tooltip.anchorId;
            closePolicy = tooltip.closePolicy;
            gravity = tooltip.gravity;
        } else {
            SettingsTooltips tooltip = (SettingsTooltips) tooltipObj;
            stringId = tooltip.stringId;
            anchorId = tooltip.anchorId;
        }
        Tooltip.Builder builder = new Tooltip.Builder(id)
                .activateDelay(500)
                .closePolicy(closePolicy, 0)
                .fadeDuration(200)
                .fitToScreen(true)
                .floatingAnimation(Tooltip.AnimationBuilder.SLOW)
                .showDelay(500)
                .text(getResources(), stringId)
                .withCallback(callback);
        if (anchorId > 0) {
            builder.anchor(findById(this, anchorId), gravity);
        }
        Tooltip.make(this, builder.build()).show();
    }

    @Override
    public void onTooltipClose(Tooltip.TooltipView tooltipView, boolean b, boolean b1) {
        TutorialTooltips[] allTips = TutorialTooltips.values();
        // Remove from visible
        visibleTips.remove(allTips[tooltipView.getTooltipId()]);

        // Reached the end of the tips?
        if (allTips.length == tooltipView.getTooltipId() + 1) {
            showTutorialTips = false;
            AnalyticsUtils.trackEvent(
                    AnalyticsUtils.CATEGORY_TOOLTIPS,
                    AnalyticsUtils.ACTION_COMPLETED
            );
            return;
        }

        // Should stop?
        if (allTips[tooltipView.getTooltipId()].stop) return;

        // Show the next one!
        showTutorialTip(allTips[tooltipView.getTooltipId() + 1]);
    }

    @Override
    public void onTooltipFailed(Tooltip.TooltipView tooltipView) {
    }

    @Override
    public void onTooltipShown(Tooltip.TooltipView tooltipView) {
        visibleTips.add(TutorialTooltips.values()[tooltipView.getTooltipId()]);
    }

    @Override
    public void onTooltipHidden(Tooltip.TooltipView tooltipView) {
    }
}