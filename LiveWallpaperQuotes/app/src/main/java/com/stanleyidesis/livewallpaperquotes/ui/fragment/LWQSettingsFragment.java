package com.stanleyidesis.livewallpaperquotes.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stanleyidesis.livewallpaperquotes.R;

/**
 * Created by stanleyidesis on 8/29/15.
 */
public class LWQSettingsFragment extends Fragment {

    public LWQSettingsFragment() {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lwq_settings, container, false);
    }
}
