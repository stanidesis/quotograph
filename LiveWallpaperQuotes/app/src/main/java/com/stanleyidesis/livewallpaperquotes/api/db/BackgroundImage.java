package com.stanleyidesis.livewallpaperquotes.api.db;

import com.orm.StringUtil;
import com.orm.SugarRecord;
import com.orm.query.Condition;
import com.orm.query.Select;

import java.util.Random;

/**
 * Created by stanleyidesis on 8/1/15.
 */
public class BackgroundImage extends SugarRecord<BackgroundImage> {

    public enum Source {
        UNSPLASH,
        USER_PHOTO;
    }

    public String uri;
    public Source source;

    public BackgroundImage() {}

    public BackgroundImage(String uri, Source source) {
        this.uri = uri;
        this.source = source;
    }

    public static BackgroundImage findImage(String uri) {
        return Select.from(BackgroundImage.class).where(Condition.prop("uri").eq(uri)).first();
    }

    public static final BackgroundImage random() {
        final long count = Select.from(BackgroundImage.class).count();
        final int offset = new Random().nextInt((int) count);
        return BackgroundImage.findWithQuery(BackgroundImage.class,
                "Select * from " + StringUtil.toSQLName("BackgroundImage") + " LIMIT 1 OFFSET " + offset, null).get(0);
    }
}
