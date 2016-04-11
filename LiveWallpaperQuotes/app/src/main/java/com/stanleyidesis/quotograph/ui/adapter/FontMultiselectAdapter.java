package com.stanleyidesis.quotograph.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;

import com.afollestad.materialdialogs.util.TypefaceHelper;
import com.stanleyidesis.quotograph.LWQPreferences;
import com.stanleyidesis.quotograph.R;
import com.stanleyidesis.quotograph.ui.Fonts;

import java.util.Set;

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
 * FontMultiselectAdapter.java
 * @author Stanley Idesis
 *
 * From Quotograph
 * https://github.com/stanidesis/quotograph
 *
 * Please report any issues
 * https://github.com/stanidesis/quotograph/issues
 *
 * Date: 04/03/2016
 */
public class FontMultiselectAdapter extends BaseAdapter {

    Context context;
    Fonts [] allFonts;
    Set<String> selectedFonts;

    public FontMultiselectAdapter(Context context) {
        this.context = context;
        this.allFonts = Fonts.values();
        this.selectedFonts = LWQPreferences.getFontSet();
    }

    @Override
    public int getCount() {
        return allFonts.length;
    }

    @Override
    public Object getItem(int position) {
        return allFonts[position];
    }

    @Override
    public long getItemId(int position) {
        return allFonts[position].getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(this.context).inflate(R.layout.font_option_item, null);
        }

        Fonts font = allFonts[position];

        CheckedTextView checkedTextView = (CheckedTextView) convertView.findViewById(R.id.ctv_font_option_item);
        checkedTextView.setTypeface(TypefaceHelper.get(context, font.getFileName()));
        checkedTextView.setText(font.getPrettyName(context));
        checkedTextView.setChecked(selectedFonts.contains(String.valueOf(font.getId())));
        return convertView;
    }

    /**
     * Add or remove the font at this position from the user's preferences.
     *
     * @param position
     */
    public void addOrRemoveFont(int position) {
        Fonts selectedFont = allFonts[position];
        String fontId = String.valueOf(selectedFont.getId());
        if (selectedFonts.contains(fontId)) {
            selectedFonts.remove(fontId);
        } else {
            selectedFonts.add(fontId);
        }
        LWQPreferences.setFontSet(selectedFonts);
        notifyDataSetChanged();
    }

    /**
     * If the user de-selects all fonts, revert to using the System Font
     */
    public void setDefaultsIfNecessary() {
        if (selectedFonts.isEmpty()) {
            addOrRemoveFont(0);
        }
    }
}