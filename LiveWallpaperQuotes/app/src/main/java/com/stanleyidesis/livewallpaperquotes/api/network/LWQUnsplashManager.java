package com.stanleyidesis.livewallpaperquotes.api.network;

import android.util.Log;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by stanleyidesis on 7/28/15.
 */
public class LWQUnsplashManager {
    public enum Category {
        BUILDINGS(2),
        FOOD_DRINK(3),
        NATURE(4),
        OBJECTS(8),
        PEOPLE(6),
        TECHNOLOGY(7);

        int identifier;

        Category(int identifier) {
            this.identifier = identifier;
        }
    }

    public static LWQUnsplashManager getInstance() {
        if (sUnsplashManager == null) {
            sUnsplashManager = new LWQUnsplashManager();
        }
        return sUnsplashManager;
    }

    public void getPhotoURLs(final int pageIndex, final Set<Category> categorySet,
                             final boolean featured, final Callback<List<String>> callback) {
        submit(new Runnable() {
            @Override
            public void run() {
                StringBuilder urlBuilder = new StringBuilder(Endpoints.UNSPLASH_URL);
                urlBuilder.append(String.format(PAGE_PARAM, pageIndex));
                urlBuilder.append(String.format(FEATURED_PARAM, featured ? 1 : 0));
                for (Category category : Category.values()) {
                    urlBuilder.append(String.format(CATEGORY_PARAM, category.identifier, 0));
                    if (categorySet.contains(category)) {
                        urlBuilder.append(String.format(CATEGORY_PARAM, category.identifier, 1));
                    }
                }
                Log.v(LWQUnsplashManager.class.getSimpleName(), "URL: " + urlBuilder.toString());

                final Connection connection = Jsoup.connect(urlBuilder.toString());
                final Connection.Response response;
                try {
                    response = connection.execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    callback.onError(e.getMessage());
                    return;
                }

                Log.v(LWQUnsplashManager.class.getSimpleName(), "Response: " + response.statusMessage());

                final Document document;
                try {
                    document = response.parse();
                } catch (IOException e) {
                    e.printStackTrace();
                    callback.onError(e.getMessage());
                    return;
                }

                List<String> returnURLs = new ArrayList<String>();

                final Elements elements = document.select("div.photo-grid img");
                for (final Element element : elements) {
                    if (element.tagName().equals("img")) {
                        final String fullSrc = element.attr("src");
                        final String src = fullSrc.substring(0, fullSrc.indexOf("?"));
                        final String byLine = element.attr("alt");
                        final String dataWidth = element.attr("data-width");
                        final String dataHeight = element.attr("data-height");

                        // TODO use that data?
                        returnURLs.add(fullSrc + "?fm=jpg");
                    }
                }
                callback.onSuccess(returnURLs);
            }
        });
    }

    private interface Endpoints {
        String UNSPLASH_URL = "https://unsplash.com/filter?";
    }

    private static final String CATEGORY_PARAM = "category[%d]=%d&";
    private static final String KEYWORD_PARAM = "search[keyword]=%s&";
    private static final String FEATURED_PARAM = "scope[featured]=%d&";
    private static final String PAGE_PARAM = "page=%d&";

    private static LWQUnsplashManager sUnsplashManager;

    private ScheduledExecutorService scheduledExecutorService;

    private LWQUnsplashManager() {
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    }

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
}
