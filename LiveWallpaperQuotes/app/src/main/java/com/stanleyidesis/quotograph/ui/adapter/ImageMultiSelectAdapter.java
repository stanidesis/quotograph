package com.stanleyidesis.quotograph.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.orm.SugarRecord;
import com.orm.query.Condition;
import com.orm.query.Select;
import com.orm.util.NamingHelper;
import com.stanleyidesis.quotograph.R;
import com.stanleyidesis.quotograph.api.db.UnsplashCategory;
import com.stanleyidesis.quotograph.api.db.UserAlbum;
import com.stanleyidesis.quotograph.api.db.UserPhoto;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

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
 * ImageMultiSelectAdapter.java
 * @author Stanley Idesis
 *
 * From Quotograph
 * https://github.com/stanidesis/quotograph
 *
 * Please report any issues
 * https://github.com/stanidesis/quotograph/issues
 *
 * Date: 04/23/2016
 */
public class ImageMultiSelectAdapter extends RecyclerView.Adapter<ImageMultiSelectAdapter.ImageMultiSelectViewHolder> {

    List<SugarRecord> imageSources;

    public ImageMultiSelectAdapter() {
        imageSources = new ArrayList<>();
        imageSources.addAll(
                Select.from(UnsplashCategory.class)
                        .orderBy(NamingHelper.toSQLNameDefault("title"))
                        .list()
        );
        imageSources.addAll(
                Select.from(UserAlbum.class)
                .orderBy("id")
                .list()
        );
    }

    @Override
    public ImageMultiSelectViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.image_source_item, parent, false);
        return new ImageMultiSelectViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(ImageMultiSelectViewHolder holder, int position) {
        SugarRecord sugarRecord = imageSources.get(position);
        if (sugarRecord instanceof UnsplashCategory) {
            holder.bindTo((UnsplashCategory) imageSources.get(position));
        } else {
            holder.bindTo((UserAlbum) imageSources.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return imageSources.size();
    }

    /**
     * Add an album to the list
     * @param newAlbum
     */
    public void addUserAlbum(UserAlbum newAlbum) {
        imageSources.add(newAlbum);
        notifyItemInserted(imageSources.indexOf(newAlbum));
    }

    class ImageMultiSelectViewHolder
            extends RecyclerView.ViewHolder implements TextView.OnEditorActionListener {

        @Bind(R.id.iv_image_source)
        ImageView imageSource;
        @Bind(R.id.check_use_image_source)
        CheckBox useImageSource;
        @Bind(R.id.et_image_source)
        EditText imageSourceName;
        @Bind(R.id.v_remove_image_source)
        View removeSource;

        SugarRecord boundTo;

        public ImageMultiSelectViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            imageSourceName.setOnEditorActionListener(this);
        }

        void bindTo(UnsplashCategory unsplashCategory) {
            boundTo = unsplashCategory;
            useImageSource.setChecked(unsplashCategory.active);
            imageSourceName.setText(unsplashCategory.title);
            imageSourceName.setFocusable(false);
            imageSourceName.setFocusableInTouchMode(false);
            removeSource.setVisibility(View.GONE);
            switch (unsplashCategory.unsplashId) {
                // Buildings
                case 2:
                    imageSource.setImageResource(R.mipmap.img_building);
                    break;
                // Food & Drink
                case 3:
                    imageSource.setImageResource(R.mipmap.img_food_and_drink);
                    break;
                // Nature
                case 4:
                    imageSource.setImageResource(R.mipmap.img_nature);
                    break;
                // People
                case 6:
                    imageSource.setImageResource(R.mipmap.img_people);
                    break;
                // Technology
                case 7:
                    imageSource.setImageResource(R.mipmap.img_technology);
                    break;
                // Objects
                case 8:
                    imageSource.setImageResource(R.mipmap.img_object);
            }
        }

        void bindTo(UserAlbum userAlbum) {
            boundTo = userAlbum;
            imageSourceName.setText(userAlbum.name);
            imageSourceName.setFocusable(true);
            imageSourceName.setFocusableInTouchMode(true);
            removeSource.setVisibility(View.VISIBLE);
            useImageSource.setChecked(userAlbum.active);
            imageSource.setImageBitmap(
                    ImageLoader.getInstance().loadImageSync(
                            Select.from(UserPhoto.class)
                                    .where (
                                        Condition.prop(
                                                NamingHelper.toSQLNameDefault("album")
                                        ).eq(userAlbum)
                                    ).first().uri)
                    );
        }

        @OnCheckedChanged(R.id.check_use_image_source)
        public void onCheckedChange(CheckBox check, boolean newValue) {
            if (boundTo instanceof UnsplashCategory) {
                ((UnsplashCategory) boundTo).active = newValue;
                boundTo.save();
            } else if (boundTo instanceof UserAlbum) {
                ((UserAlbum) boundTo).active = newValue;
                boundTo.save();
            }
        }

        @OnClick(R.id.btn_image_source)
        public void onClick() {
            useImageSource.performClick();
        }

        @OnClick(R.id.v_remove_image_source)
        public void removeImageSource() {
            if (!(boundTo instanceof UserAlbum)) {
                // Error!
                return;
            }
            int index = imageSources.indexOf(boundTo);
            imageSources.remove(index);
            boundTo.delete();
            notifyItemRemoved(index);
        }

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (!(boundTo instanceof UserAlbum)) {
                // Uh oh...
                return false;
            }
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                UserAlbum album = (UserAlbum) boundTo;
                album.name = v.getText().toString();
                album.save();
            }
            return true;
        }
    }
}
