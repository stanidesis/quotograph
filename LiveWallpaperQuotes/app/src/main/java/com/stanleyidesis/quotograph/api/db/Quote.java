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
 * Quote.java
 * @author Stanley Idesis
 *
 * From Quotograph
 * https://github.com/stanidesis/quotograph
 *
 * Please report any issues
 * https://github.com/stanidesis/quotograph/issues
 *
 * Date: 07/11/2015
 */
public class Quote extends SugarRecord {
    public String text;
    public Author author;
    public Category category;
    public boolean used;

    public Quote() {}

    public Quote(String text, Author author, Category category) {
        this(text, author, category, false);
    }

    public Quote(String text, Author author, Category category, boolean used) {
        this.text = text;
        this.author = author;
        this.category = category;
        this.used = used;
    }

    @Override
    public String toString() {
        return String.format("\"%s\" by %s", text, author.name);
    }

    public static Quote random() {
        final long count = Select.from(Quote.class).count();
        final int offset = new Random().nextInt((int) count);
        return Quote.findWithQuery(Quote.class, "Select * from Quote LIMIT 1 OFFSET " + offset, (String []) null).get(0);
    }

    public static List<Quote> allFromCategory(Category category) {
        return Select.from(Quote.class).where(Condition.prop(NamingHelper.toSQLNameDefault("category")).eq(category)).list();
    }

    public static Quote randomFromCategory(Category category) {
        final List<Quote> quotes = allFromCategory(category);
        return quotes.get(new Random().nextInt(quotes.size()));
    }

    public static List<Quote> allFromAuthor(Author author) {
        return Select.from(Quote.class).where(Condition.prop(NamingHelper.toSQLNameDefault("author")).eq(author)).list();
    }

    public static Quote randomFromAuthor(Author author) {
        final List<Quote> quotes = allFromAuthor(author);
        return quotes.get(new Random().nextInt(quotes.size()));
    }

    public static Quote find(String text, Author author) {
        return Select.from(Quote.class).where(Condition.prop(NamingHelper.toSQLNameDefault("text")).eq(text),
                Condition.prop(NamingHelper.toSQLNameDefault("author")).eq(author.getId())).first();
    }
}
