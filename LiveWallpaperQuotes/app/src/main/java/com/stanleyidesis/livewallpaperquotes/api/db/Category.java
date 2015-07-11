package com.stanleyidesis.livewallpaperquotes.api.db;

import com.orm.SugarRecord;

/**
 * Created by stanleyidesis on 7/11/15.
 */
public class Category extends SugarRecord<Category> {
    String name;

    public Category() {}

    public Category(String name) {
        this.name = name;
    }

}
