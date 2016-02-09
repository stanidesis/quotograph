package com.stanleyidesis.quotograph.ui.activity;

import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.stanleyidesis.quotograph.LWQApplication;
import com.stanleyidesis.quotograph.LWQPreferences;
import com.stanleyidesis.quotograph.R;
import com.stanleyidesis.quotograph.api.LWQFirstLaunchTask;
import com.stanleyidesis.quotograph.api.event.FirstLaunchTaskEvent;
import com.stanleyidesis.quotograph.api.event.NetworkConnectivityEvent;
import com.stanleyidesis.quotograph.api.service.LWQWallpaperService;
import com.stanleyidesis.quotograph.ui.UIUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

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
    @Bind(R.id.iv_lwq_activate_tutorial_image_1)
    ImageView tutorialWallpaperOne;
    @Bind(R.id.iv_lwq_activate_tutorial_image_2)
    ImageView tutorialWallpaperTwo;
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

    int wallpaperTop = -1;
    boolean firstLaunchTaskCompleted = false;
    LWQFirstLaunchTask firstLaunchTask;

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
            if (!LWQApplication.getWallpaperController().activeWallpaperLoaded()) {
                LWQApplication.getWallpaperController().retrieveActiveWallpaper();
            }
        } else {
            if (!LWQApplication.getNetworkConnectionListener().getCurrentConnectionType().isConnected()) {
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
        LWQApplication.setComponentsEnabled(LWQApplication.isWallpaperActivated());
        if (requiresActivation() && firstLaunchTaskCompleted && RUN_ONCE) {
            RUN_ONCE = false;
            activate();
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

        if (position == 0) {
            tutorialWallpaperTwo.setAlpha(positionOffset);
        } else if (position == 1) {
            if (wallpaperTop == -1) {
                wallpaperTop = tutorialWallpaperOne.getTop();
            }
            tutorialWallpaperOne.setTop(wallpaperTop + (int) (tutorialWallpaperOne.getMeasuredHeight() * positionOffset));
            tutorialWallpaperTwo.setTop(wallpaperTop + (int) (tutorialWallpaperOne.getMeasuredHeight() * positionOffset));
            tutorialWallpaperOne.setAlpha(1f - positionOffset);
            tutorialWallpaperTwo.setAlpha(1f - positionOffset);

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
        setIndicator(position);
        // Active View
        if (position == 4) {
            activePageFiveView.setEnabled(firstLaunchTaskCompleted);
        } else {
            activateButton.setEnabled(false);
            activateButton.setVisibility(View.GONE);
            progressBar.setEnabled(false);
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {}

    // OnClick
    @OnClick(R.id.button_lwq_activate)
    void activate() {
        LWQApplication.setComponentsEnabled(true);
        try {
            startActivity(new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
                    .putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                            new ComponentName(LWQActivateActivity.this, LWQWallpaperService.class))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } catch (ActivityNotFoundException e) {
            try {
                startActivity(new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            } catch (ActivityNotFoundException e2) {
                Toast.makeText(LWQActivateActivity.this, R.string.error_wallpaper_chooser,
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    // Events

    public void onEvent(final FirstLaunchTaskEvent firstLaunchTaskEvent) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (firstLaunchTaskEvent.didFail()) {
                    Toast.makeText(LWQActivateActivity.this, R.string.first_launch_task_failed, Toast.LENGTH_LONG).show();
                    return;
                }
                firstLaunchTaskCompleted = true;
                activePageFiveView = activateButton;
                if (viewPager.getCurrentItem() == Pages.values().length - 1) {
                    activateButton.setVisibility(View.VISIBLE);
                    activateButton.animate().alpha(1f).setDuration(150).setInterpolator(new AccelerateDecelerateInterpolator()).start();
                    progressBar.animate().alpha(0f).setDuration(100).setInterpolator(new AccelerateDecelerateInterpolator()).start();
                }
            }
        });
    }

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
        if (LWQApplication.isWallpaperActivated()) {
            startActivity(new Intent(this, LWQSettingsActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
            return false;
        }
        return true;
    }

    void setIndicator(int index){
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
