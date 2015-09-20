package com.stanleyidesis.livewallpaperquotes.api;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.widget.Toast;

import com.stanleyidesis.livewallpaperquotes.BuildConfig;
import com.stanleyidesis.livewallpaperquotes.LWQApplication;
import com.stanleyidesis.livewallpaperquotes.LWQPreferences;
import com.stanleyidesis.livewallpaperquotes.R;
import com.stanleyidesis.livewallpaperquotes.api.receiver.LWQReceiver;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by stanleyidesis on 8/14/15.
 */
public class LWQAlarmManager {
    public static void setTestRepeatingAlarm() {
        enableAlarmReceiver(true);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        AlarmManager alarmManager = (AlarmManager) LWQApplication.get()
                .getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(), 1000 * 15, getPendingIntent());
    }

    public static void setRepeatingAlarm() {
        enableAlarmReceiver(true);
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
        enableAlarmReceiver(false);
        AlarmManager alarmManager = (AlarmManager) LWQApplication.get()
                .getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(getPendingIntent());
    }

    public static void resetAlarm() {
        cancelRepeatingAlarm();
        setRepeatingAlarm();
    }

    static void enableAlarmReceiver(boolean enabled) {
        ComponentName receiver = new ComponentName(LWQApplication.get(), LWQReceiver.class);
        PackageManager pm = LWQApplication.get().getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    static PendingIntent getPendingIntent() {
        Intent newWallpaperIntent = new Intent(LWQApplication.get(), LWQReceiver.class);
        newWallpaperIntent.setAction(LWQApplication.get().getString(R.string.action_change_wallpaper));
        return PendingIntent.getBroadcast(LWQApplication.get(), 0, newWallpaperIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
