package com.stanleyidesis.livewallpaperquotes.api.db;

import com.orm.StringUtil;
import com.orm.SugarRecord;
import com.orm.query.Condition;
import com.orm.query.Select;

import java.util.ArrayList;
import java.util.List;

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
 * Playlist.java
 * @author Stanley Idesis
 *
 * From Live-Wallpaper-Quotes
 * https://github.com/stanidesis/live-wallpaper-quotes
 *
 * Please report any issues
 * https://github.com/stanidesis/live-wallpaper-quotes/issues
 *
 * Date: 10/03/2015
 */
public class Playlist extends SugarRecord<Playlist> {
    public String name;
    public boolean active;

    public Playlist() {}

    public Playlist(String name, boolean active) {
        this.name = name;
        this.active = active;
    }

    public List<Quote> quotes() {
        final List<PlaylistQuote> playlistQuotes = Select.from(PlaylistQuote.class)
                .where(Condition.prop(StringUtil.toSQLName("playlist")).eq(getId())).list();
        final List<Quote> quotes = new ArrayList<>(playlistQuotes.size());
        for (PlaylistQuote playlistQuote : playlistQuotes) {
            quotes.add(playlistQuote.quote);
        }
        return quotes;
    }

    public List<Author> authors() {
        final List<PlaylistAuthor> playlistAuthors = Select.from(PlaylistAuthor.class)
                .where(Condition.prop(StringUtil.toSQLName("playlist")).eq(getId())).list();
        final List<Author> authors = new ArrayList<>(playlistAuthors.size());
        for (PlaylistAuthor playlistAuthor: playlistAuthors) {
            authors.add(playlistAuthor.author);
        }
        return authors;
    }

    public List<Category> categories() {
        final List<PlaylistCategory> playlistCategories = Select.from(PlaylistCategory.class)
                .where(Condition.prop(StringUtil.toSQLName("playlist")).eq(getId())).list();
        final List<Category> categories = new ArrayList<>(playlistCategories.size());
        for (PlaylistCategory playlistCategory: playlistCategories) {
            categories.add(playlistCategory.category);
        }
        return categories;
    }

    public static Playlist active() {
        return Select.from(Playlist.class).where(Condition.prop(StringUtil.toSQLName("active")).eq("1")).first();
    }
}
