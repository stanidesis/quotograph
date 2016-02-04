package com.stanleyidesis.quotograph.api.db;

import com.orm.StringUtil;
import com.orm.SugarRecord;
import com.orm.query.Condition;
import com.orm.query.Select;

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
 * Wallpaper.java
 * @author Stanley Idesis
 *
 * From Quotograph
 * https://github.com/stanidesis/quotograph
 *
 * Please report any issues
 * https://github.com/stanidesis/quotograph/issues
 *
 * Date: 08/01/2015
 */
public class Wallpaper extends SugarRecord<Wallpaper> {

    public static final short IMAGE_SOURCE_RESOURCE = 0;
    public static final short IMAGE_SOURCE_USER = 1;
    public static final short IMAGE_SOURCE_UNSPLASH = 2;

    public Quote quote;
    public boolean active;
    public long dateCreated;
    public short imageSource;
    public long imageId;

    public Wallpaper() {}

    public Wallpaper(Quote quote, boolean active, long dateCreated, short imageSource, long imageId) {
        this.quote = quote;
        this.active = active;
        this.dateCreated = dateCreated;
        this.imageSource = imageSource;
        this.imageId = imageId;
    }

    public UserPhoto recoverUserPhoto() {
        if (imageSource != IMAGE_SOURCE_USER) {
            return null;
        }
        return Select.from(UserPhoto.class).where(Condition.prop(StringUtil.toSQLName("id")).eq(imageId)).first();
    }

    public UnsplashPhoto recoverUnsplashPhoto() {
        if (imageSource != IMAGE_SOURCE_UNSPLASH) {
            return null;
        }
        return Select.from(UnsplashPhoto.class).where(Condition.prop(StringUtil.toSQLName("id")).eq(imageId)).first();
    }

    public static Wallpaper active() {
        return Select.from(Wallpaper.class).where(Condition.prop("active").eq("1")).first();
    }

    public static boolean exists(short source, long id) {
        return Select.from(Wallpaper.class).where(Condition.prop(StringUtil.toSQLName("imageSource")).eq(source),
                Condition.prop(StringUtil.toSQLName("imageId")).eq(id)).first() != null;
    }
}
