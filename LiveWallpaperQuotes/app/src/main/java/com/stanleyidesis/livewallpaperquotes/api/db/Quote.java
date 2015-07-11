package com.stanleyidesis.livewallpaperquotes.api.db;

import com.orm.SugarRecord;

/**
 * Created by stanleyidesis on 7/11/15.
 */
public class Quote extends SugarRecord<Quote> {
    String text;
    long date;

    Author author;
    Category category;

    public Quote() {}

    public Quote(String text, long date, Author author, Category category) {
        this.text = text;
        this.date = date;
        this.author = author;
        this.category = category;
    }
}
