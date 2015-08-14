package com.stanleyidesis.livewallpaperquotes.api;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.stanleyidesis.livewallpaperquotes.LWQApplication;
import com.stanleyidesis.livewallpaperquotes.R;
import com.stanleyidesis.livewallpaperquotes.api.receiver.LWQReceiver;

import java.util.Calendar;

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
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        AlarmManager alarmManager = (AlarmManager) LWQApplication.get()
                .getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, getPendingIntent());
    }

    public static void cancelRepeatingAlarm() {
        enableAlarmReceiver(false);
        AlarmManager alarmManager = (AlarmManager) LWQApplication.get()
                .getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(getPendingIntent());
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
