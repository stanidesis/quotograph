package com.stanleyidesis.quotograph.api.controller;

import com.stanleyidesis.quotograph.api.Callback;
import com.stanleyidesis.quotograph.api.db.Author;
import com.stanleyidesis.quotograph.api.db.Category;
import com.stanleyidesis.quotograph.api.db.Quote;

import java.util.List;

/**
 * Copyright (c) 2016 Stanley Idesis
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
 * LWQQuoteController.java
 * @author Stanley Idesis
 *
 * From Quotograph
 * https://github.com/stanidesis/quotograph
 *
 * Please report any issues
 * https://github.com/stanidesis/quotograph/issues
 *
 * Date: 07/14/2015
 */

public interface LWQQuoteController {
    /**
     * Recover all categories available from the quote source.
     * This method will return newly-recovered categories to the callback.
     *
     * Query for existing categories using the Category class.
     *
     * @param callback
     */
    void fetchCategories(Callback<List<Category>> callback);

    /**
     * This method recovers new quotes from the source within the given category.
     * Newly recovered quotes are served back to the caller.
     *
     * @param callback
     */
    void fetchNewQuotes(Category category, Callback<List<Quote>> callback);

    /**
     * Use the string query to search for quotes online.
     *
     * @param searchQuery
     * @param callback
     */
    void fetchQuotes(String searchQuery, Callback<List<Object>> callback);

    /**
     * This will look for unused quotes within the database of the matching category.
     * If not found, it will attempt to fetch additional quotes.
     *
     * @param category
     * @param callback
     */
    void fetchUnusedQuotes(Category category, Callback<List<Quote>> callback);

    /**
     * Find unused Quotes written by the given author. If one is not found locally, attempt
     * to find unused quotes online
     *
     * @param author
     * @param callback
     */
    void fetchUnusedQuotesBy(Author author, Callback<List<Quote>> callback);
}
