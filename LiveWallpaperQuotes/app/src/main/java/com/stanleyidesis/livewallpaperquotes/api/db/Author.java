package com.stanleyidesis.livewallpaperquotes.api.db;

import com.orm.SugarRecord;

/**
 * Created by stanleyidesis on 7/11/15.
 */
public class Author extends SugarRecord<Author> {

    public String name;

    public Author() {}

    public Author(String name) {
        this.name = name;
    }
}
