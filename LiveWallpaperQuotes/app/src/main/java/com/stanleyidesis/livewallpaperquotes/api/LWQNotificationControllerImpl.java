package com.stanleyidesis.livewallpaperquotes.api;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.support.v4.app.NotificationCompat;
import android.view.WindowManager;

import com.stanleyidesis.livewallpaperquotes.LWQApplication;
import com.stanleyidesis.livewallpaperquotes.R;
import com.stanleyidesis.livewallpaperquotes.ui.activity.LWQActivateActivity;

/**
 * Created by stanleyidesis on 9/19/15.
 *//**
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
 * LWQNotificationControllerImpl.java
 * @author Stanley Idesis
 *
 * From Live-Wallpaper-Quotes
 * https://github.com/stanidesis/live-wallpaper-quotes
 *
 * Please report any issues
 * https://github.com/stanidesis/live-wallpaper-quotes/issues
 *
 * Date: 09/19/2015
 */
public class LWQNotificationControllerImpl implements LWQNotificationController {

    public LWQNotificationControllerImpl() {

    }

    @Override
    public void postNewWallpaperNotification() {
        final LWQWallpaperController wallpaperController = LWQApplication.getWallpaperController();
        // Compress background to reasonable Square size
        final Bitmap backgroundImage = wallpaperController.getBackgroundImage();
        if (backgroundImage == null) {
            return;
        }
        final Point size = new Point();
        ((WindowManager) LWQApplication.get().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getSize(size);
        int screenWidth = size.x;
        int maxBitmapDim = (int) (screenWidth * .3f);
        // Calculate center
        int topLeftX = (backgroundImage.getWidth() / 2) - (maxBitmapDim / 2);
        int topLeftY = (backgroundImage.getHeight() / 2) - (maxBitmapDim / 2);
        final Bitmap notificationBitmap = Bitmap.createBitmap(backgroundImage, topLeftX, topLeftY, maxBitmapDim, maxBitmapDim);

        // Establish basic options
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(LWQApplication.get());
        notificationBuilder.setAutoCancel(false);
        notificationBuilder.setCategory(Notification.CATEGORY_SERVICE);
        notificationBuilder.setContentInfo(wallpaperController.getAuthor());
        notificationBuilder.setContentTitle(LWQApplication.get().getString(R.string.new_quotograph));
        notificationBuilder.setContentText(String.format("\"%s\"", wallpaperController.getQuote()));
        notificationBuilder.setLargeIcon(notificationBitmap);
        notificationBuilder.setOngoing(false);
        notificationBuilder.setShowWhen(false);
        notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
        notificationBuilder.setTicker(String.format("New quote from %s", wallpaperController.getAuthor()));
        notificationBuilder.setWhen(System.currentTimeMillis());

        // Create BigTextStyle
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.bigText(wallpaperController.getQuote());
        bigTextStyle.setBigContentTitle(wallpaperController.getAuthor());
        bigTextStyle.setSummaryText(LWQApplication.get().getString(R.string.new_quotograph));
        notificationBuilder.setStyle(bigTextStyle);

        // Set Content Intent
        Intent mainIntent = new Intent(LWQApplication.get(), LWQActivateActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        notificationBuilder.setContentIntent(PendingIntent.getActivity(LWQApplication.get(), 0, mainIntent, 0));

        NotificationManager notificationManager = (NotificationManager) LWQApplication.get().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notificationBuilder.build());
    }
}
