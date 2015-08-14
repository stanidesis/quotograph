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
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by stanleyidesis on 7/28/15.
 */
public class UnsplashManager {
    public enum UnsplashCategory {
        BUILDINGS(2),
        FOOD_DRINK(3),
        NATURE(4),
        OBJECTS(8),
        PEOPLE(6),
        TECHNOLOGY(7);

        int identifier;

        UnsplashCategory(int identifier) {
            this.identifier = identifier;
        }

        public static UnsplashCategory random() {
            final int randomIndex = new Random().nextInt(UnsplashCategory.values().length);
            return UnsplashCategory.values()[randomIndex];
        }
    }

    public static String appendJPGFormat(String unsplashUri) {
        return unsplashUri + "?fm=jpg";
    }

    public UnsplashManager() {
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    }

    public void getPhotoURLs(final int pageIndex, final Set<UnsplashCategory> unsplashCategorySet,
                             final boolean featured, final Callback<List<LWQUnsplashImage>> callback) {
        submit(new Runnable() {
            @Override
            public void run() {
                StringBuilder urlBuilder = new StringBuilder(Endpoints.UNSPLASH_URL);
                urlBuilder.append(String.format(PAGE_PARAM, pageIndex));
                urlBuilder.append(String.format(FEATURED_PARAM, featured ? 1 : 0));
                for (UnsplashCategory unsplashCategory : UnsplashCategory.values()) {
                    urlBuilder.append(String.format(CATEGORY_PARAM, unsplashCategory.identifier, 0));
                    if (unsplashCategorySet.contains(unsplashCategory)) {
                        urlBuilder.append(String.format(CATEGORY_PARAM, unsplashCategory.identifier, 1));
                    }
                }
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
        String UNSPLASH_URL = "https://unsplash.com/filter?";
    }

    private static final String CATEGORY_PARAM = "category[%d]=%d&";
    private static final String KEYWORD_PARAM = "search[keyword]=%s&";
    private static final String FEATURED_PARAM = "scope[featured]=%d&";
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
