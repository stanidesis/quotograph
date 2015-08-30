package com.stanleyidesis.livewallpaperquotes.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;

import com.stanleyidesis.livewallpaperquotes.LWQApplication;
import com.stanleyidesis.livewallpaperquotes.LWQPreferences;
import com.stanleyidesis.livewallpaperquotes.R;

import java.util.List;

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
 * LWQSettingsFragment.java
 * @author Stanley Idesis
 *
 * From Live-Wallpaper-Quotes
 * https://github.com/stanidesis/live-wallpaper-quotes
 *
 * Please report any issues
 * https://github.com/stanidesis/live-wallpaper-quotes/issues
 *
 * Date: 08/29/2015
 */
public class LWQSettingsFragment extends Fragment implements SeekBar.OnSeekBarChangeListener, AdapterView.OnItemSelectedListener {

    // SeekBar.OnSeekBarChangeListener

    @Override
    public void onProgressChanged(SeekBar seekBar, int value, boolean b) {
        LWQPreferences.setBlurPreference(value);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        delegate.revealDemo(true);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        delegate.revealDemo(false);
        // TODO refresh wallpaper
    }

    // AdapterView.OnItemSelectedListener

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
        LWQPreferences.setImageCategoryPreference(LWQApplication.getWallpaperController().getBackgroundCategories().get(index));
        // TODO refresh wallpaper
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public interface Delegate {
        void revealDemo(boolean reveal);
    }

    Delegate delegate;

    public LWQSettingsFragment() {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lwq_settings, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        delegate = (Delegate) context;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        SeekBar blurBar = (SeekBar) getView().findViewById(R.id.sb_fragment_lwq_settings_blur);
        blurBar.setOnSeekBarChangeListener(this);
        blurBar.setProgress((int) LWQPreferences.getBlurPreference());

        final List<String> backgroundCategories = LWQApplication.getWallpaperController().getBackgroundCategories();
        final String imageCategoryPreference = LWQPreferences.getImageCategoryPreference();
        int currentSelection = 0;
        for (String category : backgroundCategories) {
            if (category.equalsIgnoreCase(imageCategoryPreference)) {
                currentSelection = backgroundCategories.indexOf(category);
                break;
            }
        }
        ArrayAdapter<String> imageCategoryAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item,
                backgroundCategories);
        imageCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner imageCategorySpinner = (Spinner) getView().
                findViewById(R.id.spinner_fragment_lwq_settings_image_category);
        imageCategorySpinner.setAdapter(imageCategoryAdapter);
        imageCategorySpinner.setSelection(currentSelection);
        imageCategorySpinner.setOnItemSelectedListener(this);
    }
}
