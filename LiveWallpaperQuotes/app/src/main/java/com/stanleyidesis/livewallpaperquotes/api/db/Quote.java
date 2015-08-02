package com.stanleyidesis.livewallpaperquotes.api.db;

import com.orm.SugarRecord;
import com.orm.query.Select;

import java.util.Random;

/**
 * Created by stanleyidesis on 7/11/15.
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

    public static final Quote random() {
        final long count = Select.from(Quote.class).count();
        final int offset = new Random().nextInt((int) count);
        return Quote.findWithQuery(Quote.class, "Select * from Quote LIMIT 1 OFFSET " + offset, null).get(0);
    }
}
