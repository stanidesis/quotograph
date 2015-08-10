package com.stanleyidesis.livewallpaperquotes.api.db;

import com.orm.SugarRecord;
import com.orm.query.Condition;
import com.orm.query.Select;

/**
 * Created by stanleyidesis on 8/1/15.
 */
public class Wallpaper extends SugarRecord<Wallpaper> {
    public Quote quote;
    public BackgroundImage backgroundImage;
    public boolean active;
    public long dateCreated;

    public Wallpaper() {}

    public Wallpaper(Quote quote, BackgroundImage backgroundImage, boolean active, long dateCreated) {
        this.quote = quote;
        this.backgroundImage = backgroundImage;
        this.active = active;
        this.dateCreated = dateCreated;
    }

    public static final Wallpaper active() {
        return Select.from(Wallpaper.class).where(Condition.prop("active").eq("1")).first();
    }
}
