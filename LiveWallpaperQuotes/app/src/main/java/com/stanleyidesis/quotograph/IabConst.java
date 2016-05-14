package com.stanleyidesis.quotograph;

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
 * IabConst.java
 * @author Stanley Idesis
 *
 * From Quotograph
 * https://github.com/stanidesis/quotograph
 *
 * Please report any issues
 * https://github.com/stanidesis/quotograph/issues
 *
 * Date: 03/26/2016
 */
public class IabConst {

    public enum Product {

        FONTS("fonts",
                R.string.iap_fonts_title,
                R.string.iap_fonts,
                "https://storage.googleapis.com/quotograph-android-assets/images/store/img_fonts.png"),
        IMAGES("images",
                R.string.iap_photos_title,
                R.string.iap_photos,
                "https://storage.googleapis.com/quotograph-android-assets/images/store/img_photos.png"),
        FONTS_IMAGES("fonts_and_images",
                R.string.iap_fonts_photos_title,
                R.string.iap_fonts_photos,
                "https://storage.googleapis.com/quotograph-android-assets/images/store/img_fonts_photos.png"),
        QUOTOGRAPH_INSPIRED("quotograph_inspired",
                R.string.iap_inspired_title,
                R.string.iap_inspired,
                "https://storage.googleapis.com/quotograph-android-assets/images/store/img_inspired.png");

        Product(String sku, int titleRes, int descriptionRes, String imgSource) {
            this.sku = sku;
            this.titleRes = titleRes;
            this.descriptionRes = descriptionRes;
            this.imgSource = imgSource;
        }

        public String sku;
        public int titleRes;
        public int descriptionRes;
        public String imgSource;

    }

    public static final int PURCHASE_REQUEST_CODE = 1001;

}
