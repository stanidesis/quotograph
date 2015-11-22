package com.stanleyidesis.livewallpaperquotes.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;

import com.stanleyidesis.livewallpaperquotes.LWQApplication;
import com.stanleyidesis.livewallpaperquotes.R;
import com.stanleyidesis.livewallpaperquotes.api.Callback;
import com.stanleyidesis.livewallpaperquotes.api.drawing.LWQSurfaceHolderDrawScript;
import com.stanleyidesis.livewallpaperquotes.api.event.ImageSaveEvent;
import com.stanleyidesis.livewallpaperquotes.api.event.PreferenceUpdateEvent;
import com.stanleyidesis.livewallpaperquotes.api.event.WallpaperEvent;
import com.stanleyidesis.livewallpaperquotes.ui.UIUtils;

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
public abstract class LWQWallpaperActivity extends AppCompatActivity implements
        SurfaceHolder.Callback,
        ActivityStateFlags {

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
    @Bind(R.id.surface_lwq_wallpaper) SurfaceView surfaceView;
    // Wallpaper Actions
    @Bind(R.id.group_lwq_settings_wallpaper_actions) View wallpaperActionsContainer;
    @Bind(R.id.btn_wallpaper_actions_share) View shareButton;
    @Bind(R.id.btn_wallpaper_actions_save) View saveButton;
    @Bind(R.id.btn_wallpaper_actions_skip) View skipButton;

    LWQSurfaceHolderDrawScript drawScript;
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
        surfaceView.getHolder().addCallback(this);
        setupWallpaperActions();
        animateState(BackgroundWallpaperState.HIDDEN);
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
    public void surfaceCreated(SurfaceHolder holder) {
        if (!LWQApplication.getWallpaperController().activeWallpaperLoaded()) {
            LWQApplication.getWallpaperController().retrieveActiveWallpaper();
        } else {
            draw();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (!LWQApplication.getWallpaperController().activeWallpaperLoaded()) {
            return;
        }
        draw();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {}

    // Event Handling

    public void onEvent(WallpaperEvent wallpaperEvent) {
        if (wallpaperEvent.didFail()) {
            endActionAnimation(skipButton);
            return;
        }
        if (wallpaperEvent.getStatus() == WallpaperEvent.Status.RETRIEVED_WALLPAPER) {
            endActionAnimation(skipButton);
            draw();
        }
    }

    public void onEvent(PreferenceUpdateEvent preferenceUpdateEvent) {
        if (preferenceUpdateEvent.getPreferenceKeyId() == R.string.preference_key_blur ||
                preferenceUpdateEvent.getPreferenceKeyId() == R.string.preference_key_dim) {
            draw();
        }
    }

    public void onEvent(ImageSaveEvent imageSaveEvent) {
        endActionAnimation(saveButton);
    }

    // Setup

    void setupWallpaperActions() {
        wallpaperActionsContainer.setTag(R.id.view_tag_flags, FLAG_HIDE | FLAG_DISABLE);
        wallpaperActionsContainer.setEnabled(false);
        final RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) wallpaperActionsContainer.getLayoutParams();
        layoutParams.bottomMargin = (int) (UIUtils.getNavBarHeight(this) * 1.5);
        wallpaperActionsContainer.setLayoutParams(layoutParams);

        View [] buttons = new View[] {shareButton, saveButton, skipButton};
        for (View button : buttons) {
            button.setAlpha(0f);
            button.setTranslationY(button.getHeight() * 2f);
            button.setTag(R.id.view_tag_flags, FLAG_ENABLE);
        };
    }

    void draw() {
        if (drawScript == null) {
            drawScript = new LWQSurfaceHolderDrawScript(surfaceView.getHolder());
        } else {
            drawScript.setSurfaceHolder(surfaceView.getHolder());
        }
        drawScript.requestDraw(new Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                didFinishDrawing();
            }

            @Override
            public void onError(String errorMessage, Throwable throwable) {}
        });
    }

    void didFinishDrawing() {
        // Nothing for now
    }

    void switchToState(BackgroundWallpaperState backgroundWallpaperState) {
        silkScreen.setAlpha(backgroundWallpaperState.screenAlpha);
        this.backgroundWallpaperState = backgroundWallpaperState;
    }

    long animateState(BackgroundWallpaperState backgroundWallpaperState) {
        ObjectAnimator silkScreenAnimator = ObjectAnimator.ofFloat(silkScreen, "alpha", silkScreen.getAlpha(), backgroundWallpaperState.screenAlpha);
        silkScreenAnimator.setDuration(300);
        silkScreenAnimator.setInterpolator(new LinearInterpolator());
        silkScreenAnimator.start();
        this.backgroundWallpaperState = backgroundWallpaperState;
        return 300;
    }

    void animateWallpaperActions(final boolean dismiss) {
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(generateAnimator(shareButton, dismiss, 0),
                generateAnimator(saveButton, dismiss, 15),
                generateAnimator(skipButton, dismiss, 30));
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                wallpaperActionsVisible = !dismiss;
            }
        });
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

    // Click Handling
    @OnClick(R.id.btn_wallpaper_actions_share) void shareWallpaper() {
        sendBroadcast(new Intent(getString(R.string.action_share)));
    }

    @OnClick(R.id.btn_wallpaper_actions_save) void saveWallpaperToDisk() {
        sendBroadcast(new Intent(getString(R.string.action_save)));
        saveButton.setEnabled(false);
        skipButton.setEnabled(false);
        animateAction(saveButton);
    }

    @OnClick(R.id.btn_wallpaper_actions_skip) void skipWallpaper() {
        LWQApplication.getWallpaperController().generateNewWallpaper();
        saveButton.setEnabled(false);
        skipButton.setEnabled(false);
        animateAction(skipButton);
    }
}
