package com.stanleyidesis.quotograph.ui.activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.stanleyidesis.quotograph.LWQApplication;
import com.stanleyidesis.quotograph.R;
import com.stanleyidesis.quotograph.api.event.ImageSaveEvent;
import com.stanleyidesis.quotograph.api.event.WallpaperEvent;
import com.stanleyidesis.quotograph.ui.UIUtils;

import butterknife.Bind;
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
 * LWQWallpaperActivity.java
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
public abstract class LWQWallpaperActivity extends AppCompatActivity implements ActivityStateFlags {

    static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 0xABCDEF;

    enum BackgroundWallpaperState {
        OBSCURED(.7f),
        REVEALED(0f),
        HIDDEN(1f);

        float screenAlpha;

        BackgroundWallpaperState(float screenAlpha) {
            this.screenAlpha = screenAlpha;
        }
    }

    @Bind(R.id.view_screen_lwq_wallpaper) View silkScreen;
    // Wallpaper Actions
    @Bind(R.id.group_lwq_settings_wallpaper_actions) View wallpaperActionsContainer;
    @Bind(R.id.btn_wallpaper_actions_share) View shareButton;
    @Bind(R.id.btn_wallpaper_actions_save) View saveButton;
    @Bind(R.id.btn_wallpaper_actions_skip) View skipButton;

    BackgroundWallpaperState backgroundWallpaperState;
    boolean wallpaperActionsVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UIUtils.setupFullscreenIfPossible(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        ButterKnife.bind(this);
        setupWallpaperActionContainer();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != REQUEST_CODE_WRITE_EXTERNAL_STORAGE) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            saveWallpaper();
        } else {
            Toast.makeText(this, R.string.write_external_permission_denied_toast, Toast.LENGTH_LONG).show();
        }
    }

    // Event Handling

    public void onEvent(WallpaperEvent wallpaperEvent) {
        if (wallpaperEvent.didFail() || wallpaperEvent.getStatus() == WallpaperEvent.Status.RETRIEVED_WALLPAPER) {
            endActionAnimation(skipButton);
            return;
        }
    }

    public void onEvent(ImageSaveEvent imageSaveEvent) {
        endActionAnimation(saveButton);
    }

    // Setup

    void setupWallpaperActionContainer() {
        wallpaperActionsContainer.setTag(R.id.view_tag_flags, FLAG_HIDE | FLAG_DISABLE);
        wallpaperActionsContainer.setEnabled(false);
        final RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) wallpaperActionsContainer.getLayoutParams();
        layoutParams.bottomMargin = (int) (UIUtils.getNavBarHeight(this) * 1.5);
        wallpaperActionsContainer.setLayoutParams(layoutParams);

        View [] buttons = new View[] {shareButton, saveButton, skipButton};
        for (View button : buttons) {
            button.setAlpha(0f);
            button.setTag(R.id.view_tag_flags, FLAG_ENABLE);
        }
    }

    void switchToState(BackgroundWallpaperState backgroundWallpaperState) {
        silkScreen.setAlpha(backgroundWallpaperState.screenAlpha);
        this.backgroundWallpaperState = backgroundWallpaperState;
    }

    long animateState(BackgroundWallpaperState backgroundWallpaperState) {
        silkScreen.animate().alpha(backgroundWallpaperState.screenAlpha).setDuration(300l)
                .setInterpolator(new AccelerateDecelerateInterpolator()).start();
        this.backgroundWallpaperState = backgroundWallpaperState;
        return 300l;
    }

    void animateWallpaperActions(final boolean dismiss) {
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(generateAnimator(shareButton, dismiss, 0),
                generateAnimator(saveButton, dismiss, 20),
                generateAnimator(skipButton, dismiss, 35));
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                wallpaperActionsVisible = !dismiss;
            }
        });
        animatorSet.start();
    }

    Animator generateAnimator(View target, boolean dismiss, long startDelay) {
        float [] fadeIn = new float[] {0f, 1f};
        float [] fadeOut = new float[] {1f, 0f};
        final ObjectAnimator propAnimator = ObjectAnimator.ofPropertyValuesHolder(target,
                PropertyValuesHolder.ofFloat(View.ALPHA, dismiss ? fadeOut : fadeIn),
                PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, dismiss ? (target.getHeight() * 2f) : 0f));
        propAnimator.setStartDelay(startDelay);
        propAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        propAnimator.setDuration(240);
        return propAnimator;
    }

    void animateAction(View button) {
        final Animator animator = AnimatorInflater.loadAnimator(this, R.animator.progress_rotation);
        animator.setTarget(button);
        button.setTag(R.id.view_tag_animator, animator);
        animator.start();
    }

    void endActionAnimation(final View button) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (button.getTag(R.id.view_tag_animator) != null) {
                    ((Animator)button.getTag(R.id.view_tag_animator)).end();
                    button.setTag(R.id.view_tag_animator, null);
                    skipButton.setEnabled(true);
                    saveButton.setEnabled(true);
                }
            }
        });
    }

    void saveWallpaper() {
        sendBroadcast(new Intent(getString(R.string.action_save)));
        saveButton.setEnabled(false);
        skipButton.setEnabled(false);
        animateAction(saveButton);
    }

    // Click Handling
    @OnClick(R.id.btn_wallpaper_actions_share) void shareWallpaperClick() {
        sendBroadcast(new Intent(getString(R.string.action_share)));
    }

    @OnClick(R.id.btn_wallpaper_actions_save) void saveWallpaperClick() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            saveWallpaper();
            return;
        }
        // Begin permissions flow
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            saveWallpaper();
            return;
        }
        requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
    }

    @OnClick(R.id.btn_wallpaper_actions_skip) void skipWallpaperClick() {
        LWQApplication.getWallpaperController().generateNewWallpaper();
        saveButton.setEnabled(false);
        skipButton.setEnabled(false);
        animateAction(skipButton);
    }
}
