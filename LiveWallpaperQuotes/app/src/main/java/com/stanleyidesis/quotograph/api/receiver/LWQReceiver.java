package com.stanleyidesis.quotograph.api.receiver;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.stanleyidesis.quotograph.LWQApplication;
import com.stanleyidesis.quotograph.R;
import com.stanleyidesis.quotograph.api.controller.LWQAlarmController;
import com.stanleyidesis.quotograph.api.controller.LWQWallpaperController;
import com.stanleyidesis.quotograph.api.misc.UserSurveyController;
import com.stanleyidesis.quotograph.api.service.LWQUpdateService;
import com.stanleyidesis.quotograph.api.task.LWQSaveWallpaperImageTask;

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
 * LWQReceiver.java
 * @author Stanley Idesis
 *
 * From Quotograph
 * https://github.com/stanidesis/quotograph
 *
 * Please report any issues
 * https://github.com/stanidesis/quotograph/issues
 *
 * Date: 08/14/2015
 */
public class LWQReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            // Set the alarm
            LWQAlarmController.resetAlarm();
        } else if (context.getString(R.string.action_change_wallpaper).equals(action)) {
            // Change the wallpaper
            Intent updateService = new Intent(context, LWQUpdateService.class);
            startWakefulService(context, updateService);
            LWQApplication.getNotificationController().dismissNewWallpaperNotification();
            LWQApplication.getNotificationController().dismissWallpaperGenerationFailureNotification();
        } else if (context.getString(R.string.action_share).equals(action)) {
            final LWQWallpaperController wallpaperController = LWQApplication.getWallpaperController();
            final String shareText = String.format("\"%s\" - %s", wallpaperController.getQuote(), wallpaperController.getAuthor());
            final String shareTitle = String.format("Quote by %s", wallpaperController.getAuthor());
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, shareTitle);
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareText);
            final Intent chooser = Intent.createChooser(sharingIntent, context.getResources().getString(R.string.share_using));
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(chooser);
        } else if (context.getString(R.string.action_save).equals(action)) {
            new LWQSaveWallpaperImageTask().execute();
        } else if (context.getString(R.string.action_survey_response).equals(action)) {
            LWQApplication.getNotificationController().dismissSurveyNotification();
            int surveyResponse = Integer.parseInt(intent.getDataString());
            if (surveyResponse < UserSurveyController.RESPONSE_NEVER
                    || surveyResponse > UserSurveyController.RESPONSE_OKAY) {
                return;
            }
            UserSurveyController.handleResponse(surveyResponse);
        }
    }
}
