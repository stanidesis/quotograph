package com.stanleyidesis.livewallpaperquotes.api.db;

import com.orm.StringUtil;
import com.orm.SugarRecord;
import com.orm.query.Condition;
import com.orm.query.Select;

import java.util.Random;

/**
 * Copyright (c) 2015 Stanley Idesis
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
 * Quote.java
 * @author Stanley Idesis
 *
 * From Live-Wallpaper-Quotes
 * https://github.com/stanidesis/live-wallpaper-quotes
 *
 * Please report any issues
 * https://github.com/stanidesis/live-wallpaper-quotes/issues
 *
 * Date: 07/11/2015
 */

public class Quote extends SugarRecord<Quote> {
    public String text;

    public Author author;
    public Category category;

    public Quote() {}

    public Quote(String text, Author author, Category category) {
        this.text = text;
        this.author = author;
        this.category = category;
    }

    @Override
    public String toString() {
        return String.format("\"%s\" by %s", text, author.name);
    }

    public static Quote random() {
        final long count = Select.from(Quote.class).count();
        final int offset = new Random().nextInt((int) count);
        return Quote.findWithQuery(Quote.class, "Select * from Quote LIMIT 1 OFFSET " + offset, null).get(0);
    }

    public static Quote find(String text, Author author) {
        return Select.from(Quote.class).where(Condition.prop(StringUtil.toSQLName("text")).eq(text),
                Condition.prop(StringUtil.toSQLName("author")).eq(author.getId())).first();
    }
}
