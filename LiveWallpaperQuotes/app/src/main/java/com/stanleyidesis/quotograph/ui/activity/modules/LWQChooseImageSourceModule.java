package com.stanleyidesis.quotograph.ui.activity.modules;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.stanleyidesis.quotograph.R;
import com.stanleyidesis.quotograph.api.db.UserAlbum;
import com.stanleyidesis.quotograph.api.db.UserPhoto;
import com.stanleyidesis.quotograph.ui.UIUtils;
import com.stanleyidesis.quotograph.ui.adapter.ImageMultiSelectAdapter;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
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
 * LWQChooseImageSourceModule.java
 * @author Stanley Idesis
 *
 * From Quotograph
 * https://github.com/stanidesis/quotograph
 *
 * Please report any issues
 * https://github.com/stanidesis/quotograph/issues
 *
 * Date: 04/17/2016
 */
public class LWQChooseImageSourceModule implements Module {

    public interface Delegate {
        void addPhotoAlbum(LWQChooseImageSourceModule module);
    }

    Delegate delegate;

    View root;
    @Bind(R.id.recycler_image_sources)
    RecyclerView recyclerView;

    @Override
    public void initialize(Context context, View root) {
        this.root = root;
        root.setVisibility(View.GONE);
        ButterKnife.bind(this, root);
        setEnabled(false);
        if (context instanceof Delegate) delegate = (Delegate) context;
    }

    @Override
    public void changeVisibility(View anchor, final boolean visible) {
        if (visible && recyclerView.getAdapter() == null) {
            // load now
            recyclerView.setHasFixedSize(true);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            // TODO do not hardcode the grid size on tablets/wider devices
            recyclerView.setLayoutManager(new GridLayoutManager(root.getContext(), 2));
            recyclerView.setAdapter(new ImageMultiSelectAdapter());
        }
        final Animator backgroundAnimator;

        if (Build.VERSION.SDK_INT >= 21) {
            final Point realScreenSize = UIUtils.getRealScreenSize();
            int radius = Math.max(realScreenSize.x, realScreenSize.y);

            Rect anchorRect = new Rect();
            if (anchor != null) {
                anchor.getGlobalVisibleRect(anchorRect);
            } else {
                int boxBoundary = radius / 10;
                anchorRect = new Rect(realScreenSize.x / 2 - boxBoundary,
                        realScreenSize.y / 2 - boxBoundary,
                        realScreenSize.x / 2 + boxBoundary,
                        realScreenSize.y / 2 + boxBoundary);
            }
            backgroundAnimator = ViewAnimationUtils.createCircularReveal(root,
                    anchorRect.centerX(),
                    anchorRect.centerY(),
                    visible ? 0 : radius,
                    visible ? radius : 0);
        } else {
            backgroundAnimator = ObjectAnimator.ofFloat(root, "alpha", visible ? 0f : 1f,
                    visible ? 1f : 0f);
        }
        backgroundAnimator.setDuration(300).setInterpolator(new AccelerateDecelerateInterpolator());
        backgroundAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (visible) {
                    root.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setEnabled(visible);
                if (!visible) {
                    root.setVisibility(View.GONE);
                }
            }
        });
        backgroundAnimator.start();
    }

    @Override
    public void setEnabled(boolean enabled) {
        UIUtils.setViewAndChildrenEnabled(root, enabled);
    }

    @OnClick(R.id.fab_image_source_add)
    public void onFabAdd() {
        if (delegate != null) {
            delegate.addPhotoAlbum(this);
        }
    }

    public void onImagesRecovered(List<String> imageURIs) {
        UserAlbum newAlbum = new UserAlbum("TOUCH TO NAME", true);
        newAlbum.save();
        for (String imageUri : imageURIs) {
            new UserPhoto(imageUri, newAlbum).save();
        }
        ((ImageMultiSelectAdapter) recyclerView.getAdapter()).addUserAlbum(newAlbum);
    }
}
