package com.stanleyidesis.quotograph.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import org.greenrobot.eventbus.Subscribe;
import com.stanleyidesis.quotograph.AnalyticsUtils;
import com.stanleyidesis.quotograph.R;
import com.stanleyidesis.quotograph.api.event.ImageSaveEvent;
import com.stanleyidesis.quotograph.ui.UIUtils;

import org.greenrobot.eventbus.EventBus;

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
 * LWQSaveWallpaperActivity.java
 * @author Stanley Idesis
 *
 * From Quotograph
 * https://github.com/stanidesis/quotograph
 *
 * Please report any issues
 * https://github.com/stanidesis/quotograph/issues
 *
 */
public class LWQSaveWallpaperActivity extends AppCompatActivity {

    public static final int RESULT_CODE_SUCCESS = 0;
    public static final int RESULT_CODE_FAILURE = 1;
    static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 0xABCDEF;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UIUtils.setupFullscreenIfPossible(this);
        // Log the view
        AnalyticsUtils.trackScreenView(AnalyticsUtils.SCREEN_SAVE_QUOTOGRAPH);
        // Log where the save came from
        boolean savedFromNotif =
                AnalyticsUtils.URI_SAVE_SOURCE_NOTIFICATION
                        .equalsIgnoreCase(getIntent().getDataString());
        AnalyticsUtils.trackEvent(AnalyticsUtils.CATEGORY_WALLPAPER,
                AnalyticsUtils.ACTION_SAVED,
                savedFromNotif ? AnalyticsUtils.LABEL_FROM_NOTIF :
                        AnalyticsUtils.LABEL_IN_APP
        );
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        saveWallpaper();
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
            startSaveProcess();
        } else {
            Toast.makeText(this, R.string.write_external_permission_denied_toast, Toast.LENGTH_LONG).show();
            setResult(RESULT_CODE_FAILURE);
            finish();
            overridePendingTransition(0, android.R.anim.fade_out);
        }
    }

    @Subscribe
    public void onEvent(ImageSaveEvent imageSaveEvent) {
        if (imageSaveEvent.didFail()) {
            setResult(RESULT_CODE_FAILURE);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(LWQSaveWallpaperActivity.this,
                            R.string.save_failed, Toast.LENGTH_LONG).show();
                }
            });
        } else {
            setResult(RESULT_CODE_SUCCESS);
        }
        finish();
        overridePendingTransition(0, android.R.anim.fade_out);
    }


    void saveWallpaper() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            startSaveProcess();
            return;
        }
        // Begin permissions flow
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            startSaveProcess();
            return;
        }
        requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
    }

    void startSaveProcess() {
        sendBroadcast(new Intent(getString(R.string.action_save)));
    }
}
