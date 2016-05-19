package com.stanleyidesis.quotograph.api.network;

import com.stanleyidesis.quotograph.api.LWQError;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashSet;
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
 * BrainyQuoteManager.java
 * @author Stanley Idesis
 *
 * From Quotograph
 * https://github.com/stanidesis/quotograph
 *
 * Please report any issues
 * https://github.com/stanidesis/quotograph/issues
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
        String BRAINY_QUOTE_SEARCH_QUERY = "https://www.brainyquote.com/search_results.html?q=%s";
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

    public Object getTopics() {
        final Connection connection = Jsoup.connect(Endpoints.BRAINY_QUOTE_TOPICS);
        final Connection.Response response;
        final Document document;
        try {
            response = connection.execute();
            document = response.parse();
        } catch (IOException e) {
            return LWQError.create(e);
        }

        List<BrainyTopic> topics = new ArrayList<>();
        final Elements elements = document.select("a[href^=/quotes/topics/]");
        for (Element element : elements) {
            BrainyTopic brainyTopic = new BrainyTopic(element.text());
            topics.add(brainyTopic);
        }
        return topics;
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
        final Object parseDocument = parseDocument(finalUrl);
        if (parseDocument instanceof LWQError) {
            return parseDocument;
        }
        return parseForQuotes((Document) parseDocument);
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
        final Object o = parseDocument(finalUrl);
        if (o instanceof LWQError) {
            return o;
        }
        return parseForQuotes((Document) o);
    }

    public Object searchForQuotes(String searchQuery) {
        String escapedQuery = null;
        try {
            escapedQuery = URLEncoder.encode(searchQuery, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return LWQError.create(e);
        }
        String finalUrl = String.format(Endpoints.BRAINY_QUOTE_SEARCH_QUERY, escapedQuery);
        final Object parseDocument = parseDocument(finalUrl);
        if (parseDocument instanceof LWQError) {
            return parseDocument;
        }
        Document document = (Document) parseDocument;
        final List<BrainyQuote> brainyQuotes = parseForQuotes(document);
        final List<BrainyAuthor> brainyAuthors = parseForAuthors(document);
        final List<BrainyTopic> brainyTopics = parseForTopics(document);
        final List<Object> result = new ArrayList<>();
        result.addAll(brainyTopics);
        result.addAll(brainyAuthors);
        result.addAll(brainyQuotes);
        return result;
    }

    private Object parseDocument(String url) {
        final Connection connection = Jsoup.connect(url);
        final Connection.Response response;
        try {
            response = connection.execute();
        } catch (IOException e) {
            return LWQError.create(e);
        }

        final Document document;
        try {
            document = response.parse();
        } catch (IOException e) {
            return LWQError.create(e);
        }
        return document;
    }

    private List<BrainyQuote> parseForQuotes(Document document) {
        List<BrainyQuote> brainyQuotes = new ArrayList<>();
        final Elements elements = document.select(".boxy");
        for (final Element quoteAuthorCategories : elements) {
            final Elements quote = quoteAuthorCategories.select("span.bqQuoteLink > a");
            if (quote == null || quote.isEmpty() || quote.get(0).text() == null) {
                continue;
            }
            final Elements author = quoteAuthorCategories.select("div.bq-aut > a");
            if (author == null || author.isEmpty() || author.get(0).text() == null) {
                continue;
            }
            final Elements category = quoteAuthorCategories.select("a[href^=/quotes/topics]");
            if (category == null || category.isEmpty() || category.get(0).text() == null) {
                continue;
            }
            BrainyQuote brainyQuote = new BrainyQuote();
            brainyQuote.quote = quote.get(0).text().replace("\"", "");
            brainyQuote.author = new BrainyAuthor(author.get(0).text());
            brainyQuote.topic = new BrainyTopic(category.get(0).text());
            brainyQuotes.add(brainyQuote);
        }
        return new ArrayList<>(new LinkedHashSet<>(brainyQuotes));
    }

    private List<BrainyAuthor> parseForAuthors(Document document) {
        List<BrainyAuthor> brainyAuthors = new ArrayList<>();
        final Elements elements = document.select("a[href^=/quotes/authors]");
        if (elements == null || elements.isEmpty()) {
            return brainyAuthors;
        }
        for (Element authorElement : elements) {
            boolean exists = false;
            for (BrainyAuthor author : brainyAuthors) {
                exists = exists || author.name.equalsIgnoreCase(authorElement.text());
            }
            if (exists) {
                continue;
            }
            brainyAuthors.add(new BrainyAuthor(authorElement.text()));
        }
        return brainyAuthors;
    }

    private List<BrainyTopic> parseForTopics(Document document) {
        List<BrainyTopic> brainyTopics = new ArrayList<>();
        final Elements elements = document.select("div.bqLn > a[href^=/quotes/topics/topic_]");
        if (elements == null || elements.isEmpty()) {
            return brainyTopics;
        }
        for (Element topicElement : elements) {
            boolean exists = false;
            for (BrainyTopic topic : brainyTopics) {
                exists = exists || topic.name.equalsIgnoreCase(topicElement.text());
            }
            if (exists) {
                continue;
            }
            brainyTopics.add(new BrainyTopic(topicElement.text()));
        }
        return brainyTopics;
    }

    public class BrainyTopic {
        public String name;

        BrainyTopic(String name) {
            this.name = name;
        }
    }

    public class BrainyAuthor {
        public String name;

        BrainyAuthor(String name) {
            this.name = name;
        }
    }

    public class BrainyQuote {
        public String quote;
        public BrainyAuthor author;
        public BrainyTopic topic;

        BrainyQuote() {}

        BrainyQuote(String quote, BrainyAuthor author, BrainyTopic topic) {
            this.quote = quote;
            this.author = author;
            this.topic = topic;
        }
    }
}
