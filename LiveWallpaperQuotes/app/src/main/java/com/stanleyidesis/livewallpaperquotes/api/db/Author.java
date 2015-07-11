package com.stanleyidesis.livewallpaperquotes.api.db;

import com.orm.SugarRecord;

/**
 * Created by stanleyidesis on 7/11/15.
 */
public class Author extends SugarRecord<Author> {

    String firstName;
    String lastName;

    public Author() {}

    public Author(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
