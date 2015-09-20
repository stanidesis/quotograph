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
                final Elements elements = document.select("a[href^=\"/quotes/topics/\"]");
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
        final Connection connection = Jsoup.connect(finalUrl);
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
        final Elements elements = document.select("div.boxyPaddingBig");
        for (final Element quoteAndAuthor : elements) {
            final Elements possibleQuote = quoteAndAuthor.select("a[href^=\"/quotes/quotes\"]");
            final Elements possibleAuthor = quoteAndAuthor.select("a[href^=\"/quotes/authors\"]");
            if (possibleQuote == null || possibleAuthor == null || possibleQuote.isEmpty() || possibleAuthor.isEmpty()) {
                continue;
            }
            BrainyQuote brainyQuote = new BrainyQuote();
            brainyQuote.quote = possibleQuote.text().replace("\"", "");
            brainyQuote.author = possibleAuthor.text();
            brainyQuotes.add(brainyQuote);
        }
        return brainyQuotes;
    }

    public void getQuotesAsync(final String topicDisplayName,
                          final int pageIndex, final Callback<List<BrainyQuote>> callback) {
        submit(new Runnable() {
            @Override
            public void run() {
                final Object returnObject = getQuotes(topicDisplayName, pageIndex);
                if (returnObject instanceof String) {
                    callback.onError((String) returnObject, null);
                } else {
                    callback.onSuccess((List<BrainyQuote>) returnObject);
                }
            }
        });
    }

    public class BrainyTopic {
        public String displayName;
        public String urlShortName;
    }

    public class BrainyQuote {
        public String author;
        public String quote;
    }
}
