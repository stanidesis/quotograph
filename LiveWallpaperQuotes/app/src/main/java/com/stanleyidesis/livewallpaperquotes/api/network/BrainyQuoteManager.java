package com.stanleyidesis.livewallpaperquotes.api.network;

import android.util.Log;

import com.stanleyidesis.livewallpaperquotes.api.Callback;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

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
 * BrainyQuoteManager.java
 * @author Stanley Idesis
 *
 * From Live-Wallpaper-Quotes
 * https://github.com/stanidesis/live-wallpaper-quotes
 *
 * Please report any issues
 * https://github.com/stanidesis/live-wallpaper-quotes/issues
 *
 * Date: 08/14/2015
 */

public class BrainyQuoteManager {

    private interface Endpoints {
        String BRAINY_QUOTE_TOPICS = "https://www.brainyquote.com/quotes/topics.html";
        String BRAINY_QUOTE_TOPIC_PAGE_1 = "https://www.brainyquote.com/quotes/topics/%s.html";
        String BRAINY_QUOTE_TOPIC_PAGE_NUMBER = "https://www.brainyquote.com/quotes/topics/%s%d.html";
        String BRAINY_QUOTE_AUTHOR_PAGE_1 = "https://www.brainyquote.com/quotes/authors/%s/%s.html";
        String BRAINY_QUOTE_AUTHOR_PAGE_NUMBER = "https://www.brainyquote.com/quotes/authors/%s/%s_%d.html";
    }

    private ScheduledExecutorService scheduledExecutorService;

    private void submit(final Runnable runnable) {
        scheduledExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } catch (Exception e) {
                    Log.e(getClass().getSimpleName(), "Error in executor service task", e);
                }
            }
        });
    }

    private String convertToURLName(String topicDisplayName) {
        final String cleanedUp = topicDisplayName.replaceAll("[^a-zA-Z0-9]", "");
        final String lowerCase = cleanedUp.toLowerCase();
        return "topic_" + lowerCase;
    }

    private String convertAuthorToURLName(String authorDisplayName) {
        final String maxLength = authorDisplayName.substring(0, Math.min(authorDisplayName.length(), 25));
        final String noPunctuation = maxLength.replaceAll("[,.'-]", "");
        final String underscores = noPunctuation.replaceAll(" ", "_");
        return underscores.toLowerCase();
    }

    public BrainyQuoteManager() {
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    }

    public void getTopics(final Callback<List<BrainyTopic>> callback) {
        submit(new Runnable() {
            @Override
            public void run() {
                final Connection connection = Jsoup.connect(Endpoints.BRAINY_QUOTE_TOPICS);
                final Connection.Response response;
                final Document document;
                try {
                    response = connection.execute();
                    document = response.parse();
                } catch (IOException e) {
                    e.printStackTrace();
                    callback.onError(e.getMessage(), e);
                    return;
                }

                List<BrainyTopic> topics = new ArrayList<>();
                final Elements elements = document.select("a[href^=/quotes/topics/]");
                for (Element element : elements) {
                    BrainyTopic brainyTopic = new BrainyTopic();
                    brainyTopic.displayName = element.text();

                    final String href = element.attr("href");
                    brainyTopic.urlShortName = href.substring(href.lastIndexOf('/') + 1);
                    topics.add(brainyTopic);
                }
                callback.onSuccess(topics);
            }
        });
    }

    public Object getQuotes(String topicDisplayName, int pageIndex) {
        final String urlTopicName = convertToURLName(topicDisplayName);
        String baseUrl = pageIndex > 1 ?
                Endpoints.BRAINY_QUOTE_TOPIC_PAGE_NUMBER
                : Endpoints.BRAINY_QUOTE_TOPIC_PAGE_1;
        String finalUrl;
        if (pageIndex > 1) {
            finalUrl = String.format(baseUrl, urlTopicName, pageIndex);
        } else {
            finalUrl = String.format(baseUrl, urlTopicName);
        }
        return parseForQuotes(finalUrl);
    }

    public Object getQuotesBy(String authorDisplayName, int pageIndex) {
        final String urlAuthorName = convertAuthorToURLName(authorDisplayName);
        String baseUrl = pageIndex > 1 ?
                Endpoints.BRAINY_QUOTE_AUTHOR_PAGE_NUMBER
                : Endpoints.BRAINY_QUOTE_AUTHOR_PAGE_1;
        String finalUrl;
        if (pageIndex > 1) {
            finalUrl = String.format(baseUrl, urlAuthorName.substring(0, 1), urlAuthorName, pageIndex);
        } else {
            finalUrl = String.format(baseUrl, urlAuthorName.substring(0, 1), urlAuthorName);
        }
        return parseForQuotes(finalUrl);
    }

    private Object parseForQuotes(String url) {
        final Connection connection = Jsoup.connect(url);
        final Connection.Response response;
        try {
            response = connection.execute();
        } catch (IOException e) {
            e.printStackTrace();
            return e.getLocalizedMessage();
        }

        final Document document;
        try {
            document = response.parse();
        } catch (IOException e) {
            e.printStackTrace();
            return e.getLocalizedMessage();
        }

        List<BrainyQuote> brainyQuotes = new ArrayList<>();
        final Elements elements = document.select(".boxy");
        for (final Element quoteAuthorCategories : elements) {
            final Elements quotes = quoteAuthorCategories.select("span.bqQuoteLink > a");
            if (quotes == null || quotes.size() == 0) {
                continue;
            }
            final Elements authors = quoteAuthorCategories.select("div.bq-aut > a");
            if (authors == null || authors.size() == 0) {
                continue;
            }
            BrainyQuote brainyQuote = new BrainyQuote();
            brainyQuote.quote = quotes.get(0).text().replace("\"", "");
            brainyQuote.author = authors.get(0).text();
            brainyQuotes.add(brainyQuote);
            final Elements category = quoteAuthorCategories.select("a[href^=/quotes/topics]");
            if (category == null || category.size() == 0) {
                continue;
            }
            brainyQuote.category = category.get(0).text();
        }
        return brainyQuotes;
    }

    public class BrainyTopic {
        public String displayName;
        public String urlShortName;
    }

    public class BrainyQuote {
        public String author;
        public String quote;
        public String category;
    }
}
