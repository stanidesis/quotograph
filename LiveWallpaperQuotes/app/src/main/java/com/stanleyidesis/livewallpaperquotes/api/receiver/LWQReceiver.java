package com.stanleyidesis.livewallpaperquotes.api.receiver;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.stanleyidesis.livewallpaperquotes.R;
import com.stanleyidesis.livewallpaperquotes.api.service.LWQUpdateService;

import static com.stanleyidesis.livewallpaperquotes.api.LWQAlarmManager.setRepeatingAlarm;

/**
 * Created by stanleyidesis on 8/14/15.
 */
public class LWQReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // Set the alarm
            setRepeatingAlarm();
        } else if (context.getString(R.string.action_change_wallpaper).equals(intent.getAction())) {
            // Change the wallpaper
            Intent updateService = new Intent(context, LWQUpdateService.class);
            startWakefulService(context, updateService);
        }
    }
}
