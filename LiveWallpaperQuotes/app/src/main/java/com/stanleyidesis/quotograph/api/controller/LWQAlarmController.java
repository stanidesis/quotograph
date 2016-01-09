package com.stanleyidesis.quotograph.api.controller;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.stanleyidesis.quotograph.BuildConfig;
import com.stanleyidesis.quotograph.LWQApplication;
import com.stanleyidesis.quotograph.LWQPreferences;
import com.stanleyidesis.quotograph.R;
import com.stanleyidesis.quotograph.api.receiver.LWQReceiver;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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
 * LWQAlarmManager.java
 * @author Stanley Idesis
 *
 * From Live-Wallpaper-Quotes
 * https://github.com/stanidesis/live-wallpaper-quotes
 *
 * Please report any issues
 * https://github.com/stanidesis/live-wallpaper-quotes/issues
 *
 * Date: 08/14/2015
 */
public class LWQAlarmController {
    public static void setTestRepeatingAlarm() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        AlarmManager alarmManager = (AlarmManager) LWQApplication.get()
                .getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(), 1000 * 15, getPendingIntent());
    }

    public static void setRepeatingAlarm() {
        final long refreshPreference = LWQPreferences.getRefreshPreference();
        final int[] refreshOptions = LWQApplication.get().getResources().getIntArray(R.array.refresh_preference_values);
        if (refreshPreference == refreshOptions[0]) {
            // Disabled
            return;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        if (refreshPreference == refreshOptions[1] || refreshPreference == refreshOptions[2]) {
            // Every 30 Minutes or Hour
            calendar.add(Calendar.HOUR, 1);
        } else if (refreshPreference == refreshOptions[3]) {
            // Every Six Hours
            calendar.add(Calendar.HOUR, 6);
        } else if (refreshPreference == refreshOptions[4]) {
            // Every 12 Hours
            calendar.add(Calendar.HOUR, 12);
        } else if (refreshPreference == refreshOptions[5]) {
            // Every Day
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
        } else if (refreshPreference == refreshOptions[6]) {
            // Every Week
            while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
        }
        AlarmManager alarmManager = (AlarmManager) LWQApplication.get()
                .getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(), refreshPreference, getPendingIntent());
        if (BuildConfig.DEBUG && false) {
            final Date date = new Date();
            date.setTime(calendar.getTimeInMillis());
            final String format = SimpleDateFormat.getDateTimeInstance().format(date);
            Toast.makeText(LWQApplication.get(), "Triggering alarm at " + format, Toast.LENGTH_LONG).show();
        }
    }

    public static void cancelRepeatingAlarm() {
        AlarmManager alarmManager = (AlarmManager) LWQApplication.get()
                .getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(getPendingIntent());
    }

    public static void resetAlarm() {
        cancelRepeatingAlarm();
        setRepeatingAlarm();
    }

    static PendingIntent getPendingIntent() {
        Intent newWallpaperIntent = new Intent(LWQApplication.get(), LWQReceiver.class);
        newWallpaperIntent.setAction(LWQApplication.get().getString(R.string.action_change_wallpaper));
        return PendingIntent.getBroadcast(LWQApplication.get(), 0, newWallpaperIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
