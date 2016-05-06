package com.stanleyidesis.quotograph.api.db;

import com.orm.SugarRecord;
import com.orm.query.Condition;
import com.orm.query.Select;
import com.orm.util.NamingHelper;

import java.util.List;
import java.util.Random;

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
 * UnsplashPhoto.java
 * @author Stanley Idesis
 *
 * From Quotograph
 * https://github.com/stanidesis/quotograph
 *
 * Please report any issues
 * https://github.com/stanidesis/quotograph/issues
 *
 * Date: 11/29/2015
 */
public class UnsplashPhoto extends SugarRecord {

    public String unsplashId;
    public String fullURL;
    public String regularURL;
    public String smallURL;
    public String thumbURL;

    public UnsplashPhoto() {}

    public UnsplashPhoto(String unsplashId, String fullURL, String regularURL, String smallURL, String thumbURL) {
        this.unsplashId = unsplashId;
        this.fullURL = fullURL;
        this.regularURL = regularURL;
        this.smallURL = smallURL;
        this.thumbURL = thumbURL;
    }

    public static UnsplashPhoto find(String unsplashId) {
        return Select.from(UnsplashPhoto.class).where(Condition.prop(NamingHelper.toSQLNameDefault("unsplashId")).eq(unsplashId)).first();
    }

    public static UnsplashPhoto random() {
        List<UnsplashPhoto> unsplashPhotos = Select.from(UnsplashPhoto.class).limit("100").list();
        return unsplashPhotos.get(new Random().nextInt(unsplashPhotos.size()));
    }
}
