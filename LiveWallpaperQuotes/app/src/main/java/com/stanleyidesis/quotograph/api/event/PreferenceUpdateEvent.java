package com.stanleyidesis.quotograph.api.event;

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
 * PreferenceUpdateEvent.java
 * @author Stanley Idesis
 *
 * From Quotograph
 * https://github.com/stanidesis/quotograph
 *
 * Please report any issues
 * https://github.com/stanidesis/quotograph/issues
 *
 * Date: 09/20/2015
 */
public class PreferenceUpdateEvent {

    public static PreferenceUpdateEvent preferenceUpdated(int preferenceKeyId, Object newValue) {
        return new PreferenceUpdateEvent(preferenceKeyId, newValue);
    }

    private int preferenceKeyId;
    private Object newValue;

    PreferenceUpdateEvent(int preferenceKeyId, Object newValue) {
        this.preferenceKeyId = preferenceKeyId;
        this.newValue = newValue;
    }

    public int getPreferenceKeyId() {
        return preferenceKeyId;
    }

    public Object getNewValue() {
        return newValue;
    }
}
