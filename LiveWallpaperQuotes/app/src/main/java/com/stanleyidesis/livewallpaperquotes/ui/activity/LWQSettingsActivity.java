package com.stanleyidesis.livewallpaperquotes.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.stanleyidesis.livewallpaperquotes.LWQApplication;
import com.stanleyidesis.livewallpaperquotes.LWQPreferences;
import com.stanleyidesis.livewallpaperquotes.R;
import com.stanleyidesis.livewallpaperquotes.api.Callback;
import com.stanleyidesis.livewallpaperquotes.api.LWQDrawScript;
import com.stanleyidesis.livewallpaperquotes.ui.fragment.LWQSettingsFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by stanleyidesis on 7/11/15.
 */
public class LWQSettingsActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    ScheduledExecutorService scheduledExecutorService;
    LWQDrawScript drawScript;
    BroadcastReceiver newWallpaperBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (drawScript != null) {
                drawScript.draw();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.fade_in, 0);
        setContentView(R.layout.activity_lwq_settings);
        fullScreenIfPossible();

        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surface_lwq_settings);
        surfaceView.getHolder().addCallback(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_lwq_settings);
        setSupportActionBar(toolbar);

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager_lwq_settings);
        viewPager.setAdapter(new PagerAdapter(getSupportFragmentManager()));

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout_lwq_settings);
        tabLayout.setupWithViewPager(viewPager);

        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(getString(R.string.broadcast_new_wallpaper_available));
        registerReceiver(newWallpaperBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(newWallpaperBroadcastReceiver);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (LWQPreferences.isFirstLaunch()) {
            return;
        }
        LWQApplication.getWallpaperController().retrieveActiveWallpaper(new Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                draw();
            }

            @Override
            public void onError(String errorMessage) {

            }
        }, true);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (LWQPreferences.isFirstLaunch()) {
            return;
        }
        draw();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    void fullScreenIfPossible() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }

    void draw() {
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surface_lwq_settings);
        if (drawScript == null) {
            drawScript = new LWQDrawScript(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(Palette palette) {
                    draw();
                }
            }, surfaceView.getHolder());
        } else {
            drawScript.setSurfaceHolder(surfaceView.getHolder());
        }
        scheduledExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    drawScript.draw();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    class PagerAdapter extends FragmentPagerAdapter {

        List<Fragment> fragmentList;

        public PagerAdapter(FragmentManager fm) {
            super(fm);
            fragmentList = new ArrayList<>();
            fragmentList.add(new LWQSettingsFragment());
            fragmentList.add(new LWQSettingsFragment());
            fragmentList.add(new LWQSettingsFragment());
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "Settings";
        }
    }
}