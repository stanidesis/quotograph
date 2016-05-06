package com.stanleyidesis.quotograph.api.event;

import android.net.Uri;

import com.stanleyidesis.quotograph.api.LWQError;

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
 * ImageSaveEvent.java
 * @author Stanley Idesis
 *
 * From Quotograph
 * https://github.com/stanidesis/quotograph
 *
 * Please report any issues
 * https://github.com/stanidesis/quotograph/issues
 *
 * Date: 09/26/2015
 */
public class ImageSaveEvent extends FailableEvent {

    public static ImageSaveEvent success(Uri fileUri, Uri contentUri) {
        return new ImageSaveEvent(fileUri, contentUri);
    }

    public static ImageSaveEvent failure(LWQError error) {
        return new ImageSaveEvent(error);
    }

    Uri fileUri;
    Uri contentUri;

    ImageSaveEvent(LWQError error) {
        super(error);
    }

    ImageSaveEvent(Uri fileUri, Uri contentUri) {
        this.fileUri = fileUri;
        this.contentUri = contentUri;
    }

    public Uri getFileUri() {
        return fileUri;
    }

    public Uri getContentUri() {
        return contentUri;
    }
}
