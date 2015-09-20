package com.stanleyidesis.livewallpaperquotes.api.network;

import android.util.Log;

import com.orm.StringUtil;
import com.stanleyidesis.livewallpaperquotes.api.Callback;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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
 * UnsplashManager.java
 * @author Stanley Idesis
 *
 * From Live-Wallpaper-Quotes
 * https://github.com/stanidesis/live-wallpaper-quotes
 *
 * Please report any issues
 * https://github.com/stanidesis/live-wallpaper-quotes/issues
 *
 * Date: 07/28/2015
 */
public class UnsplashManager {
    public enum UnsplashCategory {
        BUILDINGS(2, "Buildings"),
        FOOD_DRINK(3, "Food and Drink"),
        NATURE(4, "Nature"),
        OBJECTS(8, "Objects"),
        PEOPLE(6, "People"),
        TECHNOLOGY(7, "Technology");

        public int identifier;
        public String prettyName;

        UnsplashCategory(int identifier, String prettyName) {
            this.identifier = identifier;
            this.prettyName = prettyName;
        }

        public String sqlName() {
            return StringUtil.toSQLName(name());
        }

        public static UnsplashCategory random() {
            final int randomIndex = new Random().nextInt(UnsplashCategory.values().length);
            return UnsplashCategory.values()[randomIndex];
        }

        public static UnsplashCategory fromName(String name) {
            for (UnsplashCategory unsplashCategory : UnsplashCategory.values()) {
                if (unsplashCategory.name().equalsIgnoreCase(name) || unsplashCategory.prettyName.equalsIgnoreCase(name)) {
                    return unsplashCategory;
                }
            }
            Log.e(UnsplashCategory.class.getSimpleName(), "Defaulting to Nature category", new Throwable());
            // Defaults to nature if not found
            return NATURE;
        }
    }

    public static String appendJPGFormat(String unsplashUri) {
        return unsplashUri + "?fm=jpg";
    }

    public UnsplashManager() {
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    }

    public void getPhotoURLs(final int pageIndex, final UnsplashCategory unsplashCategory,
                             final Callback<List<LWQUnsplashImage>> callback) {
        submit(new Runnable() {
            @Override
            public void run() {
                StringBuilder urlBuilder = new StringBuilder(Endpoints.UNSPLASH_SEARCH_URL);
                urlBuilder.append(String.format(PAGE_PARAM, pageIndex));
                urlBuilder.append(String.format(KEYWORD_PARAM, unsplashCategory.name().toLowerCase()));
                Log.v(UnsplashManager.class.getSimpleName(), "URL: " + urlBuilder.toString());

                final Connection connection = Jsoup.connect(urlBuilder.toString());
                final Connection.Response response;
                try {
                    response = connection.execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    callback.onError(e.getMessage());
                    return;
                }

                Log.v(UnsplashManager.class.getSimpleName(), "Response: " + response.statusMessage());

                final Document document;
                try {
                    document = response.parse();
                } catch (IOException e) {
                    e.printStackTrace();
                    callback.onError(e.getMessage());
                    return;
                }

                List<LWQUnsplashImage> returnImages = new ArrayList<>();

                final Elements elements = document.select("div.photo-grid img");
                for (final Element element : elements) {
                    if (element.tagName().equals("img")) {
                        final String fullSrc = element.attr("src");
                        final String src = fullSrc.substring(0, fullSrc.indexOf("?"));
                        final String byLine = element.attr("alt");
                        final String dataWidth = element.attr("data-width");
                        final String dataHeight = element.attr("data-height");
                        returnImages.add(new LWQUnsplashImage(src, byLine, dataWidth, dataHeight));
                    }
                }
                callback.onSuccess(returnImages);
            }
        });
    }

    private interface Endpoints {
        String UNSPLASH_SEARCH_URL= "https://unsplash.com/search?utf8=✓";
    }

    private static final String KEYWORD_PARAM = "keyword=%s&";
    private static final String PAGE_PARAM = "page=%d&";

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

    public class LWQUnsplashImage {
        public String url;
        public String altText;
        public String width;
        public String height;

        public LWQUnsplashImage(String url, String altText, String width, String height) {
            this.url = url;
            this.altText = altText;
            this.width = width;
            this.height = height;
        }
    }
}
