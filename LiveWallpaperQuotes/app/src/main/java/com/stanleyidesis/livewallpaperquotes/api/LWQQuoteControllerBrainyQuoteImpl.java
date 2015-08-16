package com.stanleyidesis.livewallpaperquotes.api;

import android.util.Log;

import com.orm.StringUtil;
import com.orm.query.Condition;
import com.orm.query.Select;
import com.stanleyidesis.livewallpaperquotes.api.db.Author;
import com.stanleyidesis.livewallpaperquotes.api.db.Category;
import com.stanleyidesis.livewallpaperquotes.api.db.Quote;
import com.stanleyidesis.livewallpaperquotes.api.db.Wallpaper;
import com.stanleyidesis.livewallpaperquotes.api.network.BrainyQuoteManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
                    if (Category.hasCategory(brainyTopic.displayName, Category.Source.BRAINY_QUOTE)) {
                        continue;
                    }
                    Category newCategory = new Category(brainyTopic.displayName, Category.Source.BRAINY_QUOTE);
                    newCategory.save();
                    newCategories.add(newCategory);
                }
                callback.onSuccess(newCategories);
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    @Override
    public void fetchQuotes(final Category category, final Callback<List<Quote>> callback) {
        if (category.source != Category.Source.BRAINY_QUOTE) {
            Log.w(getClass().getSimpleName(), "Cannot fetch quotes for categories not found in BrainyQuote");
            callback.onError("Required BrainyQuote category");
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
                        callback.onError((String) returnObject);
                        return;
                    }
                    List<BrainyQuoteManager.BrainyQuote> brainyQuotes = (List<BrainyQuoteManager.BrainyQuote>) returnObject;
                    for (BrainyQuoteManager.BrainyQuote brainyQuote : brainyQuotes) {
                        Author author = Author.findAuthor(brainyQuote.author);
                        if (author == null) {
                            author = new Author(brainyQuote.author);
                            author.save();

                            Quote newQuote = new Quote(brainyQuote.quote, author, category);
                            newQuote.save();
                            recoveredQuotes.add(newQuote);
                            continue;
                        }
                        Quote quote = Quote.find(brainyQuote.quote, author);
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
    public void fetchUnusedQuote(final Category category, final Callback<Quote> callback) {
        final List<Wallpaper> allWallpapers = Select.from(Wallpaper.class).list();
        Condition [] conditions = new Condition[allWallpapers.size() + 1];
        for (int i = 0; i < allWallpapers.size(); i++) {
            conditions[i] = Condition.prop("id").notEq(allWallpapers.get(i).quote.getId());
        }
        conditions[conditions.length - 1] = Condition.prop(StringUtil.toSQLName("category")).eq(category.getId());
        final List<Quote> unusedQuotesFromCategory = Select.from(Quote.class).where(conditions).list();
        if (unusedQuotesFromCategory != null && !unusedQuotesFromCategory.isEmpty()) {
            callback.onSuccess(unusedQuotesFromCategory.get(new Random().nextInt(unusedQuotesFromCategory.size())));
        } else if (category.source == Category.Source.BRAINY_QUOTE) {
            fetchQuotes(category, new Callback<List<Quote>>() {
                @Override
                public void onSuccess(List<Quote> quotes) {
                    if (quotes.isEmpty()) {
                        // TODO wut?
                        Log.w(getClass().getSimpleName(), "No new quotes retrieved");
                        callback.onSuccess(Quote.random());
                    } else {
                        callback.onSuccess(quotes.get(new Random().nextInt(quotes.size())));
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    callback.onError(errorMessage);
                }
            });
        } else {
            callback.onError("Cannot find unused quote from " + category.source.name());
        }
    }
}
