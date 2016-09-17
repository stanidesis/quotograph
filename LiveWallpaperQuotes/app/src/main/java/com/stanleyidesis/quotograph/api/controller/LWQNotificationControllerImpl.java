package com.stanleyidesis.quotograph.api.controller;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;

import com.stanleyidesis.quotograph.AnalyticsUtils;
import com.stanleyidesis.quotograph.LWQApplication;
import com.stanleyidesis.quotograph.R;
import com.stanleyidesis.quotograph.api.event.ImageSaveEvent;
import com.stanleyidesis.quotograph.api.event.WallpaperEvent;
import com.stanleyidesis.quotograph.api.misc.UserSurveyController;
import com.stanleyidesis.quotograph.api.receiver.LWQReceiver;
import com.stanleyidesis.quotograph.ui.UIUtils;
import com.stanleyidesis.quotograph.ui.activity.LWQActivateActivity;
import com.stanleyidesis.quotograph.ui.activity.LWQSaveWallpaperActivity;

import java.io.File;

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
 * LWQNotificationControllerImpl.java
 * @author Stanley Idesis
 *
 * From Quotograph
 * https://github.com/stanidesis/quotograph
 *
 * Please report any issues
 * https://github.com/stanidesis/quotograph/issues
 *
 * Date: 09/19/2015
 */
public class LWQNotificationControllerImpl implements LWQNotificationController {

    static final int NOTIF_ID_PRIMARY = 1;
    static final int NOTIF_ID_GEN_FAILURE = 2;
    static final int NOTIF_ID_SAVE_FAILURE = 3;
    static final int NOTIF_ID_SURVEY = 4;

    boolean newWallpaperIncoming = false;
    int uniqueRequestCode = 0;

    public LWQNotificationControllerImpl() {
        EventBus.getDefault().register(this);
    }

    @Override
    public void postNewWallpaperNotification() {
        dismissWallpaperGenerationFailureNotification();
        final LWQWallpaperController wallpaperController = LWQApplication.getWallpaperController();
        // Compress background to reasonable Square size
        final Bitmap backgroundImage = wallpaperController.getBackgroundImage();
        if (backgroundImage == null) {
            return;
        }
        final Bitmap notificationBitmap = chopToCenterSquare(backgroundImage);

        Context context = LWQApplication.get();

        // Establish basic options
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
        notificationBuilder.setAutoCancel(true);
        notificationBuilder.setCategory(Notification.CATEGORY_SERVICE);
        notificationBuilder.setColor(LWQApplication.get().getResources().getColor(R.color.palette_A100));
        notificationBuilder.setContentInfo(wallpaperController.getAuthor());
        notificationBuilder.setContentTitle(context.getString(R.string.new_quotograph));
        notificationBuilder.setContentText(String.format("\"%s\"", wallpaperController.getQuote()));
        notificationBuilder.setLights(LWQApplication.get().getResources().getColor(R.color.palette_A100), 500, 500);
        notificationBuilder.setLargeIcon(notificationBitmap);
        notificationBuilder.setOngoing(false);
        notificationBuilder.setShowWhen(false);
        notificationBuilder.setSmallIcon(R.mipmap.ic_stat);
        notificationBuilder.setTicker(String.format("New quote from %s", wallpaperController.getAuthor()));
        notificationBuilder.setWhen(System.currentTimeMillis());

        // Create BigTextStyle
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.bigText(wallpaperController.getQuote());
        bigTextStyle.setBigContentTitle(wallpaperController.getAuthor());
        bigTextStyle.setSummaryText(context.getString(R.string.new_quotograph));
        notificationBuilder.setStyle(bigTextStyle);

        // Set Content Intent
        Intent mainIntent = new Intent(context, LWQActivateActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        notificationBuilder.setContentIntent(PendingIntent.getActivity(context, uniqueRequestCode++, mainIntent, 0));

        // Add Share Action
        Intent shareIntent = new Intent(context, LWQReceiver.class);
        shareIntent.setAction(context.getString(R.string.action_share));
        shareIntent.setData(Uri.parse(AnalyticsUtils.URI_SHARE_SOURCE_NOTIFICATION));
        final PendingIntent shareBroadcast = PendingIntent.getBroadcast(context, uniqueRequestCode++, shareIntent, 0);
        final NotificationCompat.Action shareAction = new NotificationCompat.Action.Builder(R.mipmap.ic_share_white,
                context.getString(R.string.share), shareBroadcast).build();
        notificationBuilder.addAction(shareAction);

        // Add save to disk
        Intent saveToDiskIntent = new Intent(context, LWQSaveWallpaperActivity.class);
        saveToDiskIntent.setData(Uri.parse(AnalyticsUtils.URI_SAVE_SOURCE_NOTIFICATION));
        final PendingIntent saveToDiskActivity = PendingIntent.getActivity(context, uniqueRequestCode++, saveToDiskIntent, 0);
        final NotificationCompat.Action saveToDiskAction = new NotificationCompat.Action.Builder(R.mipmap.ic_save_white,
                context.getString(R.string.save_to_disk), saveToDiskActivity).build();
        notificationBuilder.addAction(saveToDiskAction);

        // Add Skip Action
        Intent skipIntent = new Intent(context, LWQReceiver.class);
        skipIntent.setAction(context.getString(R.string.action_change_wallpaper));
        // Track where the skip originated
        skipIntent.setData(Uri.parse(AnalyticsUtils.URI_CHANGE_SOURCE_NOTIFICATION));
        final PendingIntent skipBroadcast =
                PendingIntent.getBroadcast(context, uniqueRequestCode++, skipIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        final NotificationCompat.Action skipAction = new NotificationCompat.Action.Builder(R.mipmap.ic_skip_next_white,
                context.getString(R.string.skip), skipBroadcast).build();
        notificationBuilder.addAction(skipAction);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIF_ID_PRIMARY, notificationBuilder.build());

        notificationBitmap.recycle();
    }

    @Override
    public void dismissNewWallpaperNotification() {
        NotificationManager notificationManager = (NotificationManager) LWQApplication.get().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIF_ID_PRIMARY);
    }

    @Override
    public void postWallpaperGenerationFailureNotification() {
        // Two actions: Network Settings & Try Again
        LWQApplication context = LWQApplication.get();
        // Establish basic options
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
        notificationBuilder.setAutoCancel(true);
        notificationBuilder.setCategory(Notification.CATEGORY_ERROR);
        notificationBuilder.setColor(LWQApplication.get().getResources().getColor(R.color.palette_A100));
        notificationBuilder.setContentInfo(context.getString(R.string.app_name));
        notificationBuilder.setContentTitle(context.getString(R.string.notification_generation_failure_title));
        notificationBuilder.setContentText(context.getString(R.string.notification_generation_failure_content));
        notificationBuilder.setLights(LWQApplication.get().getResources().getColor(R.color.palette_A100), 500, 500);
        notificationBuilder.setTicker(context.getString(R.string.notification_generation_failure_ticker));
        notificationBuilder.setOngoing(false);
        notificationBuilder.setShowWhen(true);
        notificationBuilder.setLocalOnly(true);
        notificationBuilder.setPriority(NotificationCompat.PRIORITY_MIN);
        notificationBuilder.setSmallIcon(android.R.drawable.stat_notify_error);
        notificationBuilder.setWhen(System.currentTimeMillis());

        // Create BigTextStyle
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(context.getString(R.string.notification_generation_failure_title));
        bigTextStyle.bigText(context.getString(R.string.notification_generation_failure_content));
        notificationBuilder.setStyle(bigTextStyle);

        // Add Settings Action
        Intent settingsIntent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
        final PendingIntent settingsBroadcast = PendingIntent.getActivity(context, uniqueRequestCode++, settingsIntent, 0);
        final NotificationCompat.Action settingsAction = new NotificationCompat.Action.Builder(R.mipmap.ic_settings_white,
                context.getString(R.string.notification_generation_failure_action_settings), settingsBroadcast).build();
        notificationBuilder.addAction(settingsAction);

        // Add Skip Action
        Intent skipIntent = new Intent(context.getString(R.string.action_change_wallpaper));
        skipIntent.setData(Uri.parse(AnalyticsUtils.URI_CHANGE_SOURCE_NOTIFICATION));
        final PendingIntent skipBroadcast = PendingIntent.getBroadcast(context, uniqueRequestCode++, skipIntent, 0);
        final NotificationCompat.Action skipAction = new NotificationCompat.Action.Builder(R.mipmap.ic_refresh_white_36dp,
                context.getString(R.string.notification_generation_failure_action_try_again), skipBroadcast).build();
        notificationBuilder.addAction(skipAction);

        // Set Skip as main action
        notificationBuilder.setContentIntent(skipBroadcast);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIF_ID_GEN_FAILURE, notificationBuilder.build());
    }

    @Override
    public void dismissWallpaperGenerationFailureNotification() {
        NotificationManager notificationManager = (NotificationManager) LWQApplication.get().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIF_ID_GEN_FAILURE);
    }

    @Override
    public void postSavedWallpaperReadyNotification(Uri filePath, Uri imageUri) {
        final Bitmap savedImage = BitmapFactory.decodeFile(filePath.getPath());
        final Bitmap notificationBitmap = chopToCenterSquare(savedImage);
        savedImage.recycle();

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(LWQApplication.get());
        notificationBuilder.setAutoCancel(true);
        notificationBuilder.setCategory(Notification.CATEGORY_SERVICE);
        notificationBuilder.setContentTitle(LWQApplication.get().getString(R.string.notification_title_save_image));
        notificationBuilder.setContentText(LWQApplication.get().getString(R.string.notification_content_save_image));
        notificationBuilder.setLights(LWQApplication.get().getResources().getColor(R.color.palette_A100), 500, 500);
        notificationBuilder.setLargeIcon(notificationBitmap);
        notificationBuilder.setOngoing(false);
        notificationBuilder.setShowWhen(false);
        notificationBuilder.setSmallIcon(R.mipmap.ic_stat);
        notificationBuilder.setTicker(LWQApplication.get().getString(R.string.notification_title_save_image));
        notificationBuilder.setWhen(System.currentTimeMillis());
        notificationBuilder.setColor(LWQApplication.get().getResources().getColor(R.color.palette_A100));

        NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle();
        bigPictureStyle.bigPicture(notificationBitmap);
        bigPictureStyle.bigLargeIcon(notificationBitmap);

        notificationBuilder.setStyle(bigPictureStyle);

        // Set content Intent
        final Intent viewIntent = new Intent(Intent.ACTION_VIEW);
        viewIntent.setDataAndType(imageUri, "image/*");
        final Intent viewChooser = Intent.createChooser(viewIntent, LWQApplication.get().getString(R.string.view_using));
        final PendingIntent viewActivity = PendingIntent.getActivity(LWQApplication.get(),
                uniqueRequestCode++, viewChooser, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setContentIntent(viewActivity);

        // Add Share Action
        final Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(filePath.getPath())));
        final Intent shareChooser = Intent.createChooser(shareIntent, LWQApplication.get().getString(R.string.share_using));
        final PendingIntent shareActivity = PendingIntent.getActivity(LWQApplication.get(),
                uniqueRequestCode++, shareChooser, PendingIntent.FLAG_UPDATE_CURRENT);
        final NotificationCompat.Action shareAction = new NotificationCompat.Action.Builder(R.mipmap.ic_share_white,
                LWQApplication.get().getString(R.string.share), shareActivity).build();
        notificationBuilder.addAction(shareAction);

        NotificationManager notificationManager = (NotificationManager) LWQApplication.get().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(savedImage.hashCode(), notificationBuilder.build());
        notificationBitmap.recycle();
    }

    @Override
    public void postWallpaperSaveFailureNotification() {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(LWQApplication.get());
        notificationBuilder.setAutoCancel(true);
        notificationBuilder.setCategory(Notification.CATEGORY_ERROR);
        notificationBuilder.setColor(LWQApplication.get().getResources().getColor(R.color.palette_A100));
        notificationBuilder.setContentTitle(LWQApplication.get().getString(R.string.notification_title_save_failed));
        notificationBuilder.setContentText(LWQApplication.get().getString(R.string.notification_content_save_failed));
        notificationBuilder.setLights(LWQApplication.get().getResources().getColor(R.color.palette_A100), 500, 500);
        notificationBuilder.setOngoing(false);
        notificationBuilder.setShowWhen(false);
        notificationBuilder.setSmallIcon(R.mipmap.ic_stat);
        notificationBuilder.setTicker(String.format(LWQApplication.get().getString(R.string.notification_title_save_failed)));
        notificationBuilder.setWhen(System.currentTimeMillis());

        Intent saveToDiskIntent = new Intent(LWQApplication.get(), LWQSaveWallpaperActivity.class);
        saveToDiskIntent.setData(Uri.parse(AnalyticsUtils.URI_SAVE_SOURCE_NOTIFICATION));
        final PendingIntent saveToDiskActivity = PendingIntent.getActivity(LWQApplication.get(), uniqueRequestCode++, saveToDiskIntent, 0);
        notificationBuilder.setContentIntent(saveToDiskActivity);

        NotificationManager notificationManager = (NotificationManager) LWQApplication.get().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIF_ID_SAVE_FAILURE, notificationBuilder.build());
    }

    @Override
    public void dismissSurveyNotification() {
        NotificationManager notificationManager = (NotificationManager) LWQApplication.get().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIF_ID_SURVEY);
    }

    @Override
    public void postSurveyNotification() {
        LWQApplication lwqApplication = LWQApplication.get();

        // Prepare PIs
        final String responseAction = lwqApplication.getString(R.string.action_survey_response);
        Intent neverIntent = new Intent(responseAction);
        neverIntent.setClass(lwqApplication, LWQReceiver.class); // I HATE YOU, PENDING INTENT
        neverIntent.setData(Uri.parse(String.valueOf(UserSurveyController.RESPONSE_NEVER)));
        Intent laterIntent = new Intent(responseAction);
        laterIntent.setData(Uri.parse(String.valueOf(UserSurveyController.RESPONSE_LATER)));
        laterIntent.setClass(lwqApplication, LWQReceiver.class);
        Intent okayIntent = new Intent(responseAction);
        okayIntent.setData(Uri.parse(String.valueOf(UserSurveyController.RESPONSE_OKAY)));
        okayIntent.setClass(lwqApplication, LWQReceiver.class);
        PendingIntent neverPI = PendingIntent.getBroadcast(lwqApplication,
                uniqueRequestCode++, neverIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent laterPI = PendingIntent.getBroadcast(lwqApplication,
                uniqueRequestCode++, laterIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent okayPI = PendingIntent.getBroadcast(lwqApplication,
                uniqueRequestCode++, okayIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(lwqApplication);
        notificationBuilder
                .setAutoCancel(true)
                .addAction(R.drawable.md_transparent, lwqApplication.getString(R.string.survey_never), neverPI)
                .addAction(R.drawable.md_transparent, lwqApplication.getString(R.string.survey_later), laterPI)
                .addAction(R.drawable.md_transparent, lwqApplication.getString(R.string.survey_okay), okayPI)
                .setCategory(Notification.CATEGORY_PROMO)
                .setColor(lwqApplication.getResources().getColor(R.color.palette_A100))
                .setContentIntent(okayPI)
                .setContentText(lwqApplication.getString(R.string.survey_cta))
                .setContentTitle(lwqApplication.getString(R.string.survey_title))
                .setDeleteIntent(laterPI)
                .setLights(lwqApplication.getResources().getColor(R.color.palette_A100), 500, 500)
                .setOngoing(false)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setShowWhen(false)
                .setSmallIcon(R.mipmap.ic_stat)
                .setTicker(lwqApplication.getString(R.string.survey_title))
                .setWhen(System.currentTimeMillis());

        NotificationManager notificationManager = (NotificationManager)
                LWQApplication.get().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIF_ID_SURVEY, notificationBuilder.build());
    }

    @Override
    protected void finalize() throws Throwable {
        EventBus.getDefault().unregister(this);
        super.finalize();
    }

    public void onEvent(WallpaperEvent wallpaperEvent) {
        if (wallpaperEvent.getStatus() == WallpaperEvent.Status.GENERATED_NEW_WALLPAPER) {
            newWallpaperIncoming = !wallpaperEvent.didFail();
        } else if (wallpaperEvent.getStatus() == WallpaperEvent.Status.RETRIEVED_WALLPAPER) {
            if (newWallpaperIncoming && LWQApplication.isWallpaperActivated()) {
                postNewWallpaperNotification();
                newWallpaperIncoming = false;
            }
        } else if (wallpaperEvent.getStatus() == WallpaperEvent.Status.GENERATING_NEW_WALLPAPER
                && wallpaperEvent.didFail()) {
            postWallpaperGenerationFailureNotification();
        }
    }

    public void onEvent(ImageSaveEvent imageSaveEvent) {
        if (imageSaveEvent.didFail()) {
            postWallpaperSaveFailureNotification();
        } else {
            postSavedWallpaperReadyNotification(imageSaveEvent.getFileUri(), imageSaveEvent.getContentUri());
        }
    }

    private Bitmap chopToCenterSquare(Bitmap original) {
        int squareDim = Math.min(original.getWidth(), original.getHeight());
        int squareYCoord = (int) (original.getHeight() / 2.0) - (int) (squareDim / 2.0);
        int squareXCoord = (int) (original.getWidth() / 2.0) - (int) (squareDim / 2.0);

        final Point size = UIUtils.getRealScreenSize();
        int screenWidth = Math.min(size.x, size.y);
        int maxBitmapDim = (int) (screenWidth * .3f);
        // Calculate center
        float scale = ((float) maxBitmapDim) / ((float) squareDim);
        Matrix matrix = new Matrix();
        matrix.setScale(scale, scale);
        return Bitmap.createBitmap(original, squareXCoord, squareYCoord,
                squareDim, squareDim, matrix, true);
    }
}
