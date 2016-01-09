package com.stanleyidesis.quotograph.api.controller;

import android.util.Log;

import com.orm.StringUtil;
import com.orm.query.Condition;
import com.orm.query.Select;
import com.stanleyidesis.quotograph.api.Callback;
import com.stanleyidesis.quotograph.api.db.Author;
import com.stanleyidesis.quotograph.api.db.Category;
import com.stanleyidesis.quotograph.api.db.Quote;
import com.stanleyidesis.quotograph.api.db.Wallpaper;
import com.stanleyidesis.quotograph.api.network.BrainyQuoteManager;

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
 * LWQQuoteControllerBrainyQuoteImpl.java
 * @author Stanley Idesis
 *
 * From Live-Wallpaper-Quotes
 * https://github.com/stanidesis/live-wallpaper-quotes
 *
 * Please report any issues
 * https://github.com/stanidesis/live-wallpaper-quotes/issues
 *
 * Date: 08/15/2015
 */

public class LWQQuoteControllerBrainyQuoteImpl implements LWQQuoteController {

    static int MAX_PAGE_QUERY = 40;

    BrainyQuoteManager brainyQuoteManager;

    public LWQQuoteControllerBrainyQuoteImpl() {
        brainyQuoteManager = new BrainyQuoteManager();
    }

    @Override
    public void fetchCategories(final Callback<List<Category>> callback) {
        brainyQuoteManager.getTopics(new Callback<List<BrainyQuoteManager.BrainyTopic>>() {
            @Override
            public void onSuccess(List<BrainyQuoteManager.BrainyTopic> brainyTopics) {
                List<Category> newCategories = new ArrayList<>();
                for (BrainyQuoteManager.BrainyTopic brainyTopic : brainyTopics) {
                    if (Category.hasCategory(brainyTopic.name, Category.Source.BRAINY_QUOTE)) {
                        continue;
                    }
                    Category newCategory = new Category(brainyTopic.name, Category.Source.BRAINY_QUOTE);
                    newCategory.save();
                    newCategories.add(newCategory);
                }
                callback.onSuccess(newCategories);
            }

            @Override
            public void onError(String errorMessage, Throwable throwable) {
                callback.onError(errorMessage, throwable);
            }
        });
    }

    @Override
    public void fetchNewQuotes(final Category category, final Callback<List<Quote>> callback) {
        if (category.source != Category.Source.BRAINY_QUOTE) {
            Log.w(getClass().getSimpleName(), "Cannot fetch quotes for categories not found in BrainyQuote");
            callback.onError("Required BrainyQuote category", null);
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                int page = 1;
                List<Quote> recoveredQuotes = new ArrayList<>();
                while (page <= MAX_PAGE_QUERY && recoveredQuotes.size() == 0) {
                    final Object returnObject = brainyQuoteManager.getQuotes(category.name, page);
                    if (returnObject instanceof String) {
                        callback.onError((String) returnObject, null);
                        return;
                    }
                    List<BrainyQuoteManager.BrainyQuote> brainyQuotes = (List<BrainyQuoteManager.BrainyQuote>) returnObject;
                    for (BrainyQuoteManager.BrainyQuote brainyQuote : brainyQuotes) {
                        Author author = Author.findAuthor(brainyQuote.author.name);
                        Quote quote = null;
                        if (author == null) {
                            author = new Author(brainyQuote.author.name, false);
                            author.save();
                        } else {
                            quote = Quote.find(brainyQuote.quote, author);
                        }
                        if (quote != null) {
                            continue;
                        }
                        quote = new Quote(brainyQuote.quote, author, category);
                        quote.save();
                        recoveredQuotes.add(quote);
                    }
                    page++;
                }
                callback.onSuccess(recoveredQuotes);
            }
        }).start();
    }

    @Override
    public void fetchUnusedQuotes(final Category category, final Callback<List<Quote>> callback) {
        final List<Wallpaper> allWallpapers = Select.from(Wallpaper.class).list();
        Condition [] conditions = new Condition[allWallpapers.size() + 1];
        for (int i = 0; i < allWallpapers.size(); i++) {
            conditions[i] = Condition.prop("id").notEq(allWallpapers.get(i).quote.getId());
        }
        conditions[conditions.length - 1] = Condition.prop(StringUtil.toSQLName("category")).eq(category.getId());
        final List<Quote> unusedQuotesFromCategory = Select.from(Quote.class).where(conditions).list();
        if (unusedQuotesFromCategory != null && !unusedQuotesFromCategory.isEmpty()) {
            callback.onSuccess(unusedQuotesFromCategory);
        } else if (category.source == Category.Source.BRAINY_QUOTE) {
            fetchNewQuotes(category, new Callback<List<Quote>>() {
                @Override
                public void onSuccess(List<Quote> quotes) {
                    callback.onSuccess(quotes);
                }

                @Override
                public void onError(String errorMessage, Throwable throwable) {
                    callback.onError(errorMessage, throwable);
                }
            });
        } else {
            // It's not an error, just no return.
            callback.onSuccess(new ArrayList<Quote>());
        }
    }

    @Override
    public void fetchUnusedQuotesBy(final Author author, final Callback<List<Quote>> callback) {
        final List<Wallpaper> allWallpapers = Select.from(Wallpaper.class).list();
        Condition [] conditions = new Condition[allWallpapers.size() + 1];
        for (int i = 0; i < allWallpapers.size(); i++) {
            conditions[i] = Condition.prop("id").notEq(allWallpapers.get(i).quote.author.getId());
        }
        conditions[conditions.length - 1] = Condition.prop(StringUtil.toSQLName("author")).eq(author.getId());
        final List<Quote> unusedQuotesFromAuthor = Select.from(Quote.class).where(conditions).list();
        if (unusedQuotesFromAuthor != null && !unusedQuotesFromAuthor.isEmpty()) {
            callback.onSuccess(unusedQuotesFromAuthor);
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                int page = 1;
                List<Quote> recoveredQuotes = new ArrayList<>();
                while (page <= MAX_PAGE_QUERY && recoveredQuotes.size() == 0) {
                    final Object returnObject = brainyQuoteManager.getQuotesBy(author.name, page);
                    if (returnObject instanceof String) {
                        callback.onError((String) returnObject, null);
                        return;
                    }
                    List<BrainyQuoteManager.BrainyQuote> brainyQuotes = (List<BrainyQuoteManager.BrainyQuote>) returnObject;
                    for (BrainyQuoteManager.BrainyQuote brainyQuote : brainyQuotes) {
                        Quote quote = Quote.find(brainyQuote.quote, author);
                        if (quote != null) {
                            continue;
                        }
                        Category category = Category.findWithName(brainyQuote.topic.name);
                        if (category == null) {
                            category = new Category(brainyQuote.topic.name, Category.Source.BRAINY_QUOTE);
                            category.save();
                        }
                        quote = new Quote(brainyQuote.quote, author, category);
                        quote.save();
                        recoveredQuotes.add(quote);
                    }
                    page++;
                }
                callback.onSuccess(recoveredQuotes);
            }
        }).start();
    }

    @Override
    public void fetchQuotes(final String searchQuery, final Callback<List<Object>> callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Object returnObject = brainyQuoteManager.searchForQuotes(searchQuery);
                if (returnObject instanceof String) {
                    callback.onError((String) returnObject, null);
                    return;
                }
                List<Object> recoveredObjects = new ArrayList<>();
                List<Object> brainyResults = (List<Object>) returnObject;
                for (Object brainyResult : brainyResults) {
                    if (brainyResult instanceof BrainyQuoteManager.BrainyQuote) {
                       BrainyQuoteManager.BrainyQuote brainyQuote = (BrainyQuoteManager.BrainyQuote) brainyResult;
                        Category category = null;
                        if (brainyQuote.topic != null) {
                            category = Category.findWithName(brainyQuote.topic.name);
                            if (category == null) {
                                category = new Category(brainyQuote.topic.name, Category.Source.BRAINY_QUOTE);
                                category.save();
                            }
                        }
                        Author author = Author.findAuthor(brainyQuote.author.name);
                        if (author == null) {
                            author = new Author(brainyQuote.author.name, false);
                            author.save();
                        }
                        Quote quote = Quote.find(brainyQuote.quote, author);
                        if (quote == null) {
                            quote = new Quote(brainyQuote.quote, author, category);
                            quote.save();
                        }
                        recoveredObjects.add(quote);
                    } else if (brainyResult instanceof BrainyQuoteManager.BrainyAuthor) {
                        BrainyQuoteManager.BrainyAuthor brainyAuthor = (BrainyQuoteManager.BrainyAuthor) brainyResult;
                        Author author = Author.findAuthor(brainyAuthor.name);
                        if (author == null) {
                            author = new Author(brainyAuthor.name, false);
                            author.save();
                        }
                        recoveredObjects.add(author);
                    } else if (brainyResult instanceof BrainyQuoteManager.BrainyTopic) {
                        BrainyQuoteManager.BrainyTopic brainyTopic = (BrainyQuoteManager.BrainyTopic) brainyResult;
                        Category category = Category.findWithName(brainyTopic.name);
                        if (category == null) {
                            // Unlikely
                            category = new Category(brainyTopic.name, Category.Source.BRAINY_QUOTE);
                            category.save();
                        }
                        recoveredObjects.add(category);
                    }
                }
                callback.onSuccess(recoveredObjects);
            }
        }).start();
    }
}
