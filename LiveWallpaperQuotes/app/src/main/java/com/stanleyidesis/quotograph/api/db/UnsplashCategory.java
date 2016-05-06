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
 * UnsplashCategory.java
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
public class UnsplashCategory extends SugarRecord {

    public int unsplashId;
    public String title;
    public boolean active;

    public UnsplashCategory() {}

    public UnsplashCategory(int unsplashId, String title, boolean active) {
        this.unsplashId = unsplashId;
        this.title = title;
        this.active = active;
    }

    public static UnsplashCategory find(int id) {
        return Select.from(UnsplashCategory.class).where(Condition.prop(NamingHelper.toSQLNameDefault("unsplashId")).eq(id)).first();
    }

    public static UnsplashCategory find(String title) {
        return Select.from(UnsplashCategory.class).where(Condition.prop(NamingHelper.toSQLNameDefault("title")).eq(title)).first();
    }

    public static List<UnsplashCategory> active() {
        return Select.from(UnsplashCategory.class).where(Condition.prop(NamingHelper.toSQLNameDefault("active")).eq("1")).list();
    }

    public static UnsplashCategory random() {
        final long count = Select.from(UnsplashCategory.class).count();
        final int offset = new Random().nextInt((int) count);
        return SugarRecord.findWithQuery(
                UnsplashCategory.class,
                "Select * from " + NamingHelper.toSQLName(UnsplashCategory.class) + " LIMIT 1 OFFSET " + offset,
                (String []) null).get(0);
    }
}
