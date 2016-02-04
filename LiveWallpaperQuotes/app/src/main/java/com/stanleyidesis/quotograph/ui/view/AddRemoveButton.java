package com.stanleyidesis.quotograph.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;

import com.stanleyidesis.quotograph.R;

import butterknife.Bind;
import butterknife.ButterKnife;

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
 * AddRemoveButton.java
 * @author Stanley Idesis
 *
 * From Quotograph
 * https://github.com/stanidesis/quotograph
 *
 * Please report any issues
 * https://github.com/stanidesis/quotograph/issues
 *
 * Date: 11/06/2015
 */
public class AddRemoveButton extends FrameLayout {

    @Bind(R.id.iv_add_remove_button_base)
    View base;
    @Bind(R.id.iv_add_remove_button_rotate)
    View rotate;

    boolean stateRemove = true;

    public AddRemoveButton(Context context) {
        super(context);
        setup();
    }

    public AddRemoveButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public AddRemoveButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup();
    }

    void setup() {
        inflate(getContext(), R.layout.add_remove_button, this);
        setClipToPadding(false);
        ButterKnife.bind(this, this);
    }

    public void setMode(final boolean remove) {
        post(new Runnable() {
            @Override
            public void run() {
                if ((stateRemove && remove) || (!stateRemove && !remove)) {
                    return;
                }
                final ViewPropertyAnimator animate = rotate.animate();
                animate.rotation(remove ? 0f : 90f)
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .setDuration(150)
                        .start();
                stateRemove = remove;
            }
        });
    }

}
