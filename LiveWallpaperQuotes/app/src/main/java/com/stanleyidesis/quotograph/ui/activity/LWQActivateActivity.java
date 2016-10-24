package com.stanleyidesis.quotograph.ui.activity;

import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.stanleyidesis.quotograph.AnalyticsUtils;
import com.stanleyidesis.quotograph.LWQApplication;
import com.stanleyidesis.quotograph.LWQPreferences;
import com.stanleyidesis.quotograph.R;
import com.stanleyidesis.quotograph.api.controller.LWQWallpaperControllerHelper;
import com.stanleyidesis.quotograph.api.event.FirstLaunchTaskEvent;
import com.stanleyidesis.quotograph.api.event.FirstLaunchTaskUpdate;
import com.stanleyidesis.quotograph.api.event.NetworkConnectivityEvent;
import com.stanleyidesis.quotograph.api.network.NetworkConnectionListener;
import com.stanleyidesis.quotograph.api.service.LWQWallpaperService;
import com.stanleyidesis.quotograph.api.task.LWQFirstLaunchTask;
import com.stanleyidesis.quotograph.ui.UIUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Copyright (c) 2016 Stanley Idesis
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
 * LWQActivateActivity.java
 * @author Stanley Idesis
 *
 * From Quotograph
 * https://github.com/stanidesis/quotograph
 *
 * Please report any issues
 * https://github.com/stanidesis/quotograph/issues
 *
 * Date: 09/06/2015
 */
public class LWQActivateActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {

    // First run check
    static boolean RUN_ONCE = true;

    private enum Pages {
        WELCOME,
        SET_AS_WALLPAPER,
        CHOOSE_SOURCES,
        SETTINGS,
        LOADING;
    }

    @BindColor(R.color.palette_200)
    int colorOne;
    @BindColor(R.color.palette_300)
    int colorTwo;
    @BindColor(R.color.palette_400)
    int colorThree;
    @BindColor(R.color.palette_500)
    int colorFour;
    @BindColor(R.color.palette_600)
    int colorFive;
    List<Integer> colorList = new ArrayList<>();

    @Bind(R.id.viewpager_lwq_activate)
    ViewPager viewPager;
    @Bind(R.id.ll_lwq_activate_indicators)
    LinearLayout indicators;
    @Bind(R.id.pb_lwq_activate)
    View progressBar;
    @Bind(R.id.button_lwq_activate)
    View activateButton;
    View activePageFiveView;
    @Bind({R.id.tv_tut_category, R.id.tv_tut_author, R.id.tv_tut_own_quote})
    List<TextView> sourceBubbles;
    @Bind({R.id.lwq_activate_tut_0,R.id.lwq_activate_tut_1,
            R.id.lwq_activate_tut_2,R.id.lwq_activate_tut_3,R.id.lwq_activate_tut_4,})
    List<View> viewPages;
    @Bind(R.id.rl_tut_clock)
    View clock;
    @Bind(R.id.v_tut_hour_hand)
    View hourHand;
    @Bind(R.id.v_tut_minute_hand)
    View minuteHand;
    boolean pivotsCalculated;

    Snackbar activeSnackbar;
    FirstLaunchTaskUpdate latestFirstLaunchTaskUpdate;
    boolean firstLaunchTaskCompleted = false;
    LWQFirstLaunchTask firstLaunchTask;
    boolean trackInitialPageView = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UIUtils.setupFullscreenIfPossible(this);
        setContentView(R.layout.activity_lwq_activate);
        ButterKnife.bind(this);

        colorList.add(colorOne);
        colorList.add(colorTwo);
        colorList.add(colorThree);
        colorList.add(colorFour);
        colorList.add(colorFive);

        setupSourceBubbles();
        setupViewPager();
        setIndicator(0);
        setupClock();
        activePageFiveView = progressBar;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        firstLaunchTaskCompleted = !LWQPreferences.isFirstLaunch();
        activateButton.setEnabled(firstLaunchTaskCompleted);
        activateButton.setVisibility(firstLaunchTaskCompleted ? View.VISIBLE : View.GONE);
        progressBar.setEnabled(!firstLaunchTaskCompleted);
        progressBar.setVisibility(firstLaunchTaskCompleted ? View.GONE : View.VISIBLE);
        activePageFiveView = firstLaunchTaskCompleted ? activateButton : progressBar;
        activePageFiveView.requestLayout();
        if (firstLaunchTaskCompleted) {
            if (!LWQWallpaperControllerHelper.get().activeWallpaperLoaded()) {
                LWQWallpaperControllerHelper.get().retrieveActiveWallpaper();
            }
        } else {
            if (!NetworkConnectionListener.get().getCurrentConnectionType().isConnected()) {
                presentNetworkRequiredDialog();
            } else {
                firstLaunchTask = new LWQFirstLaunchTask();
                firstLaunchTask.execute();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!requiresActivation()) {
            startActivity(new Intent(this, LWQSettingsActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
        } else if (firstLaunchTaskCompleted && RUN_ONCE) {
            RUN_ONCE = false;
            activate();
        } else if (trackInitialPageView) {
            trackInitialPageView = false;
            // Log the beginning of the tutorial
            AnalyticsUtils.trackTutorial(true);
            // Log the first view
            AnalyticsUtils.trackScreenView(AnalyticsUtils.SCREEN_TUTORIAL_1);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    // OnPageChangeListener

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        // Background Color

        int firstColor = colorList.get(position);
        int secondColor = colorList.get((position + 1) % 5);
        int redBlend = Color.red(firstColor) + (int) (positionOffset * (Color.red(secondColor) - Color.red(firstColor)));
        int greenBlend = Color.green(firstColor) + (int) (positionOffset * (Color.green(secondColor) - Color.green(firstColor)));
        int blueBlend = Color.blue(firstColor) + (int) (positionOffset * (Color.blue(secondColor) - Color.blue(firstColor)));
        viewPager.setBackgroundColor(Color.rgb(redBlend, greenBlend, blueBlend));

        // Title Alpha

        final View view = viewPages.get(position);
        ButterKnife.findById(view, R.id.tv_tut_headline).setAlpha(1f - positionOffset);
        ButterKnife.findById(view, R.id.tv_tut_details).setAlpha(1f - positionOffset);
        if (viewPages.size() > position + 1) {
            final View nextView = viewPages.get(position + 1);
            ButterKnife.findById(nextView, R.id.tv_tut_headline).setAlpha(positionOffset);
            ButterKnife.findById(nextView, R.id.tv_tut_details).setAlpha(positionOffset);
        }

        if (position == 1) {
            fadeBubbles(positionOffset, true);
        } else if (position == 2) {
            fadeBubbles(positionOffset, false);
            calculatePivots();
            clock.setAlpha(positionOffset);
            hourHand.setAlpha(positionOffset);
            minuteHand.setAlpha(positionOffset);
            minuteHand.setRotation(360f * positionOffset);
            hourHand.setRotation(360f/5f*positionOffset);
        } else if (position == 3) {
            activePageFiveView.setVisibility(View.VISIBLE);
            activePageFiveView.setAlpha(positionOffset);
            activePageFiveView.setScaleX(positionOffset);
            activePageFiveView.setScaleY(positionOffset);

            clock.setAlpha(1f - positionOffset);
            minuteHand.setRotation(360f * positionOffset);
            hourHand.setRotation(360f/5f + 360f/5*positionOffset);
        }
    }

    @Override
    public void onPageSelected(int position) {
        // Log screen view
        AnalyticsUtils.trackScreenView(AnalyticsUtils.SCREEN_TUTORIALS[position]);
        setIndicator(position);
        if (position == viewPages.size() - 1) {
            activePageFiveView.setEnabled(firstLaunchTaskCompleted);
            if (activeSnackbar != null && !firstLaunchTaskCompleted) {
                activeSnackbar = build(latestFirstLaunchTaskUpdate.getUpdate());
                activeSnackbar.show();
            }
        } else {
            activateButton.setEnabled(false);
            activateButton.setVisibility(View.GONE);
            progressBar.setEnabled(false);
            progressBar.setVisibility(View.GONE);
            if (activeSnackbar != null) {
                activeSnackbar.dismiss();
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {}

    // OnClick
    @OnClick(R.id.button_lwq_activate)
    void activate() {
        try {
            startActivity(new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
                    .putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                            new ComponentName(LWQActivateActivity.this, LWQWallpaperService.class))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            Toast.makeText(LWQActivateActivity.this,
                    getString(R.string.toast_tap_set_wallpaper), Toast.LENGTH_LONG).show();
        } catch (ActivityNotFoundException e) {
            try {
                startActivity(new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                Toast.makeText(LWQActivateActivity.this,
                        getString(R.string.toast_tap_set_wallpaper), Toast.LENGTH_LONG).show();
            } catch (ActivityNotFoundException e2) {
                Toast.makeText(LWQActivateActivity.this, R.string.error_wallpaper_chooser,
                        Toast.LENGTH_LONG).show();
            }
        }
        // Log tutorial as completed
        AnalyticsUtils.trackTutorial(false);
    }

    Snackbar build(String string) {
        Snackbar snackbar = Snackbar.make(viewPager, string, Snackbar.LENGTH_INDEFINITE);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) snackbar.getView().getLayoutParams();
        layoutParams.bottomMargin += UIUtils.getNavBarHeight(LWQActivateActivity.this);
        snackbar.getView().setLayoutParams(layoutParams);
        return snackbar;
    }

    // Events

    @Subscribe
    public void onEvent(final FirstLaunchTaskUpdate firstLaunchTaskUpdate) {
        latestFirstLaunchTaskUpdate = firstLaunchTaskUpdate;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (activeSnackbar != null) {
                    activeSnackbar.dismiss();
                }
                activeSnackbar = build(firstLaunchTaskUpdate.getUpdate());
                if (viewPager.getCurrentItem() == viewPages.size() - 1) {
                    activeSnackbar.show();
                }
            }
        });
    }

    @Subscribe
    public void onEvent(final FirstLaunchTaskEvent firstLaunchTaskEvent) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (firstLaunchTaskEvent.didFail()) {
                    if (activeSnackbar != null) {
                        activeSnackbar.dismiss();
                    }
                    activeSnackbar = build(getString(R.string.first_launch_task_failed));
                    activeSnackbar.setAction("Retry", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            progressBar.animate().alpha(1f).setDuration(150).setInterpolator(new AccelerateDecelerateInterpolator()).start();
                            activeSnackbar.dismiss();
                            firstLaunchTask = new LWQFirstLaunchTask();
                            firstLaunchTask.execute();
                        }
                    });
                    activeSnackbar.show();
                    progressBar.animate().alpha(0f).setDuration(150).setInterpolator(new AccelerateDecelerateInterpolator()).start();
                    return;
                }
                firstLaunchTaskCompleted = true;
                activePageFiveView = activateButton;
                if (viewPager.getCurrentItem() == Pages.values().length - 1) {
                    activateButton.setEnabled(true);
                    activateButton.setVisibility(View.VISIBLE);
                    activateButton.animate().alpha(1f).setDuration(150).setInterpolator(new AccelerateDecelerateInterpolator()).start();
                    progressBar.setEnabled(false);
                    progressBar.animate().alpha(0f).setDuration(100).setInterpolator(new AccelerateDecelerateInterpolator()).start();

                    if (activeSnackbar != null) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (activeSnackbar == null) {
                                    return;
                                }
                                activeSnackbar.dismiss();
                                activeSnackbar = null;
                            }
                        }, 5000);
                    }
                }
            }
        });
    }

    @Subscribe
    public void onEvent(NetworkConnectivityEvent networkConnectivityEvent) {
        if (networkConnectivityEvent.getNewConnectionType().isConnected() && firstLaunchTask == null) {
            firstLaunchTask = new LWQFirstLaunchTask();
            firstLaunchTask.execute();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    UIUtils.presentDialog(LWQActivateActivity.this, R.string.network_connection_established_title,
                            R.string.network_connection_established_message,
                            R.string.network_connection_established_positive, null, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }, null);
                }
            });
        } else if (!networkConnectivityEvent.getNewConnectionType().isConnected()) {
            if (firstLaunchTask != null && !firstLaunchTask.isCancelled()) {
                firstLaunchTask.cancel(true);
                firstLaunchTask = null;
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    presentNetworkRequiredDialog();
                }
            });
        }
    }

    // Setup

    void setupClock() {
        clock.setAlpha(0f);
        hourHand.setAlpha(0f);
        minuteHand.setAlpha(0f);
    }

    void calculatePivots() {
        if (pivotsCalculated) {
            return;
        }
        pivotsCalculated = true;
        hourHand.setPivotY(hourHand.getMeasuredHeight() - (hourHand.getMeasuredWidth() / 2));
        minuteHand.setPivotY(minuteHand.getMeasuredHeight() - (minuteHand.getMeasuredWidth() / 2));
    }

    void setupSourceBubbles() {
        sourceBubbles.get(0).setRotation(-5f);
        sourceBubbles.get(1).setRotation(5f);
        sourceBubbles.get(2).setRotation(-10f);
        for (View bubble : sourceBubbles) {
            bubble.setVisibility(View.GONE);
            bubble.setScaleX(0f);
            bubble.setScaleY(0f);
            bubble.setAlpha(0f);
        }
    }

    void fadeBubbles(float percentage, boolean reveal) {
        List<Float> percentageShifts =new ArrayList<>();
        percentageShifts.add(1/3f);
        percentageShifts.add(2/3f);
        percentageShifts.add(1f);
        for (View bubble : sourceBubbles) {
            bubble.setVisibility(View.VISIBLE);
            float actualPercentage = reveal ? percentage : 1f - percentage;
            float shiftedPosition = actualPercentage / percentageShifts.remove(0);
            if (shiftedPosition > 1f) {
                shiftedPosition = 1f;
            }
            bubble.setAlpha(shiftedPosition);
            bubble.setScaleX(shiftedPosition);
            bubble.setScaleY(shiftedPosition);
        }
    }

    void setupViewPager() {
        viewPager.setAdapter(new TutorialAdapter());
        viewPager.setOffscreenPageLimit(5);
        viewPager.addOnPageChangeListener(this);
        viewPager.setBackgroundColor(colorOne);
    }

    boolean requiresActivation() {
        return !LWQApplication.isWallpaperActivated();
    }

    void setIndicator(int index) {
        if (index < Pages.values().length) {
            for(int i = 0 ; i < Pages.values().length; i++){
                indicators.getChildAt(i).setSelected(i == index);
            }
        }
    }

    // Misc.

    void presentNetworkRequiredDialog() {
        UIUtils.presentDialog(this, R.string.network_connection_required_title,
                R.string.network_connection_required_message,
                android.R.string.ok, R.string.network_connection_required_negative,
                null, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        try {
                            startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                        } catch (Exception e) {}
                    }
                });
    }

    class TutorialAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return viewPages.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            return viewPages.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {}
    }
}
