package com.stanleyidesis.quotograph.api.task;

import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

import com.stanleyidesis.quotograph.LWQApplication;
import com.stanleyidesis.quotograph.R;
import com.stanleyidesis.quotograph.api.BaseCallback;
import com.stanleyidesis.quotograph.api.LWQError;
import com.stanleyidesis.quotograph.api.drawing.LWQBitmapDrawScript;
import com.stanleyidesis.quotograph.api.event.ImageSaveEvent;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

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
 * LWQSaveWallpaperImageTask.java
 * @author Stanley Idesis
 *
 * From Quotograph
 * https://github.com/stanidesis/quotograph
 *
 * Please report any issues
 * https://github.com/stanidesis/quotograph/issues
 *
 * Date: 09/25/2015
 */
public class LWQSaveWallpaperImageTask extends AsyncTask<Void, Void, Boolean> {

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Toast.makeText(LWQApplication.get(), R.string.saving_quotograph, Toast.LENGTH_LONG).show();
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        if (!isExternalStorageWritable()) {
            EventBus.getDefault().post(ImageSaveEvent.failure(LWQError.create("External Storage not writable")));
            return false;
        }
        final File photosDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (photosDirectory == null) {
            EventBus.getDefault().post(ImageSaveEvent.failure(LWQError.create("Photos directory does not exist")));
            return false;
        }
        if (!photosDirectory.exists()) {
            if (!photosDirectory.mkdir()) {
                EventBus.getDefault().post(ImageSaveEvent.failure(LWQError.create("Failed to make a Photos directory")));
            }
        }
        boolean succeeded = true;
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
        final File wallpaperFile = new File(photosDirectory.getPath() + File.separator
                + timeStamp + ".png");
        final LWQBitmapDrawScript drawScript = new LWQBitmapDrawScript();
        drawScript.requestDraw(new BaseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                final Bitmap bitmapToSave = drawScript.getBitmap();
                try {
                    if (!wallpaperFile.createNewFile()) {
                        EventBus.getDefault().post(ImageSaveEvent.failure(LWQError.create("Failed to create new file")));
                        drawScript.finish();
                    }
                    FileOutputStream fos = new FileOutputStream(wallpaperFile);
                    bitmapToSave.compress(Bitmap.CompressFormat.PNG, 0, fos);
                    fos.close();
                    MediaScannerConnection.scanFile(LWQApplication.get(),
                            new String[]{wallpaperFile.toString()}, null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                public void onScanCompleted(String path, Uri uri) {
                                    EventBus.getDefault().post(ImageSaveEvent.success(Uri.parse(path), uri));
                                }
                            });
                } catch (Exception e) {
                    EventBus.getDefault().post(ImageSaveEvent.failure(LWQError.create(e)));
                } finally {
                    drawScript.finish();
                }
            }
        });
        return succeeded;
    }
}
