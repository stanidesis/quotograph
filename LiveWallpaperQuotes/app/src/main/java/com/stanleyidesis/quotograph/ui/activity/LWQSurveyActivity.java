package com.stanleyidesis.quotograph.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.stanleyidesis.quotograph.AnalyticsUtils;
import com.stanleyidesis.quotograph.R;
import com.stanleyidesis.quotograph.api.misc.UserSurveyController;
import com.stanleyidesis.quotograph.ui.debug.DebuggableActivity;

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
 * LWQSurveyActivity.java
 * @author Stanley Idesis
 *
 * From Quotograph
 * https://github.com/stanidesis/quotograph
 *
 * Please report any issues
 * https://github.com/stanidesis/quotograph/issues
 *
 * Date: 09/12/2016
 */
public class LWQSurveyActivity extends DebuggableActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);
        // Track screen
        AnalyticsUtils.trackScreenView(AnalyticsUtils.SCREEN_SURVEY_POPUP);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.btn_lwq_survey_negative,
            R.id.btn_lwq_survey_neutral,
            R.id.btn_lwq_survey_positive})
    void onClick(View button) {
        int id = button.getId();
        int which = UserSurveyController.RESPONSE_NEVER;
        if (id == R.id.btn_lwq_survey_neutral) {
            which = UserSurveyController.RESPONSE_LATER;
        } else if (id == R.id.btn_lwq_survey_positive) {
            which = UserSurveyController.RESPONSE_OKAY;
        }
        UserSurveyController.handleResponse(which);
        finish();
        overridePendingTransition(0, android.R.anim.fade_out);
    }

    @Override
    public void onBackPressed() {
        UserSurveyController.handleResponse(UserSurveyController.RESPONSE_LATER);
        finish();
        overridePendingTransition(0, android.R.anim.fade_out);
    }

}
