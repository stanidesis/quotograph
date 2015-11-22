package com.stanleyidesis.livewallpaperquotes.ui.activity;

import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.percent.PercentRelativeLayout;
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

import com.stanleyidesis.livewallpaperquotes.LWQApplication;
import com.stanleyidesis.livewallpaperquotes.LWQPreferences;
import com.stanleyidesis.livewallpaperquotes.R;
import com.stanleyidesis.livewallpaperquotes.api.LWQFirstLaunchTask;
import com.stanleyidesis.livewallpaperquotes.api.event.FirstLaunchTaskEvent;
import com.stanleyidesis.livewallpaperquotes.api.service.LWQWallpaperService;
import com.stanleyidesis.livewallpaperquotes.ui.UIUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

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
 * LWQActivateActivity.java
 * @author Stanley Idesis
 *
 * From Live-Wallpaper-Quotes
 * https://github.com/stanidesis/live-wallpaper-quotes
 *
 * Please report any issues
 * https://github.com/stanidesis/live-wallpaper-quotes/issues
 *
 * Date: 09/06/2015
 */
public class LWQActivateActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {

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

    int wallpaperTop = -1;
    boolean firstLaunchTaskCompleted = false;

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
        activePageFiveView = progressBar;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (LWQPreferences.isFirstLaunch()) {
            new LWQFirstLaunchTask().execute();
        } else {
            firstLaunchTaskCompleted = true;
            activateButton.setEnabled(false);
            activateButton.setVisibility(View.GONE);
            progressBar.setEnabled(false);
            progressBar.setVisibility(View.GONE);
            activePageFiveView = activateButton;
            LWQApplication.getWallpaperController().retrieveActiveWallpaper();
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
        } else if (position == 3) {
            activePageFiveView.setVisibility(View.VISIBLE);
            activePageFiveView.setAlpha(positionOffset);
            activePageFiveView.setScaleX(positionOffset);
            activePageFiveView.setScaleY(positionOffset);
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
    public void onPageScrollStateChanged(int state) {

    }

    // OnClick
    @OnClick(R.id.button_lwq_activate)
    void activate() {
        LWQWallpaperService.setServiceEnabled(true);
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
                    Toast.makeText(LWQActivateActivity.this, "Error: " + firstLaunchTaskEvent.getErrorMessage(), Toast.LENGTH_LONG).show();
                    return;
                }
                firstLaunchTaskCompleted = true;
                activePageFiveView = activateButton;
                if (viewPager.getCurrentItem() == Pages.values().length - 1) {
                    activateButton.animate().alpha(1f).setDuration(150).setInterpolator(new AccelerateDecelerateInterpolator()).start();
                    progressBar.animate().alpha(0f).setDuration(100).setInterpolator(new AccelerateDecelerateInterpolator()).start();
                }
            }
        });
    }


    // Setup

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
            float shiftedPosition = percentage / percentageShifts.remove(0);
            if (shiftedPosition > 1f || shiftedPosition < 0f) {
                continue;
            }
            float finalValue = reveal ? shiftedPosition : 1f - shiftedPosition;
            bubble.setAlpha(finalValue);
            bubble.setScaleX(finalValue);
            bubble.setScaleY(finalValue);
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


    // TODO remove?
    void setupIndicators() {
        PercentRelativeLayout.LayoutParams layoutParams = (PercentRelativeLayout.LayoutParams) indicators.getLayoutParams();
        layoutParams.bottomMargin = UIUtils.getNavBarHeight(this);
        indicators.setLayoutParams(layoutParams);
    }

    void setIndicator(int index){
        if (index < Pages.values().length) {
            for(int i = 0 ; i < Pages.values().length; i++){
                indicators.getChildAt(i).setSelected(i == index);
            }
        }
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
