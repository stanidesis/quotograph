package com.stanleyidesis.quotograph.api.network;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.stanleyidesis.quotograph.api.db.UnsplashCategory;
import com.stanleyidesis.quotograph.api.db.UnsplashPhoto;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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

    private static String APP_ID = "d1759c7e191a4e170b2b64ddcdd555d82dd65085ccc4f0e7c0165c031b64cbd0";

    private interface Endpoints {
        String UNSPLASH_API_URL= "https://api.unsplash.com/";
        String UNSPLASH_CATEGORIES = "categories";
        String UNSPLASH_RANDOM_PHOTO = "photos/random";
    }

    private interface Parameters {
        String PAGE_PARAM = "page=%d&";
        String QUERY_PARAM = "query=%s&";
        String CATEGORIES_PARAM = "category=%s&";
        String FEATURED_PARAM = "featured=%s&";
    }

    private interface JSONCategoryKeys {
        String KEY_ID = "id";
        String KEY_TITLE = "title";
    }

    private interface JSONPhotoKeys {
        String KEY_ID = "id";
        String KEY_URLS = "urls";
        String KEY_URL_FULL = "full";
        String KEY_URL_REGULAR = "regular";
        String KEY_URL_SMALL = "small";
        String KEY_URL_THUMB = "thumb";
    }

    private OkHttpClient client = new OkHttpClient();

    Object fetchResponse(String urlWithParams) {
        try {
            Request request = new Request.Builder()
                    .url(urlWithParams)
                    .addHeader("Accept-Version", "v1")
                    .addHeader("Authorization", "Client-ID " + APP_ID)
                    .build();
            return client.newCall(request).execute().body().string();
        } catch (Exception e) {
            e.printStackTrace();
            return e;
        }
    }

    public Object fetchAllCategories() {
        Object result = fetchResponse(Endpoints.UNSPLASH_API_URL.concat(Endpoints.UNSPLASH_CATEGORIES));
        if (!(result instanceof String)) {
            // Error
            return ((Exception)result).getLocalizedMessage();
        }
        List<UnsplashCategory> unsplashCategories = new ArrayList<>();
        try {
            JSONArray categoriesArray = new JSONArray((String) result);
            for (int i = 0; i < categoriesArray.length(); i++) {
                JSONObject categoryJSON = categoriesArray.getJSONObject(i);
                UnsplashCategory unsplashCategory = UnsplashCategory.find(categoryJSON.getInt(JSONCategoryKeys.KEY_ID));
                if (unsplashCategory == null) {
                    unsplashCategory = new UnsplashCategory(categoryJSON.getInt(JSONCategoryKeys.KEY_ID),
                            categoryJSON.getString(JSONCategoryKeys.KEY_TITLE));
                    unsplashCategory.save();
                }
                unsplashCategories.add(unsplashCategory);
            }
            return unsplashCategories;
        } catch (Exception e) {
            e.printStackTrace();
            return e.getLocalizedMessage();
        }
    }

    public Object fetchRandomPhoto(final UnsplashCategory unsplashCategory, final boolean featured,
                                 final String optionalQuery) {
        String urlWithParams = Endpoints.UNSPLASH_API_URL.concat(Endpoints.UNSPLASH_RANDOM_PHOTO).concat("?");
        if (unsplashCategory != null) {
            urlWithParams = urlWithParams.concat(String.format(Parameters.CATEGORIES_PARAM, String.valueOf(unsplashCategory.unsplashId)));
        }
        if (featured) {
            urlWithParams = urlWithParams.concat(String.format(Parameters.FEATURED_PARAM, Boolean.toString(true)));
        }
        if (optionalQuery != null) {
            try {
                urlWithParams = urlWithParams.concat(String.format(Parameters.QUERY_PARAM, URLEncoder.encode(optionalQuery, "UTF-8")));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        try {
            Object result = fetchResponse(urlWithParams);
            if (!(result instanceof String)) {
                // Error
                return ((Exception)result).getLocalizedMessage();
            }
            JSONObject randomPhotoJSON = new JSONObject((String) result);
            String unsplashId = randomPhotoJSON.getString(JSONPhotoKeys.KEY_ID);
            UnsplashPhoto unsplashPhoto = UnsplashPhoto.find(unsplashId);
            if (unsplashPhoto != null) {
                return unsplashPhoto;
            }
            JSONObject urls = randomPhotoJSON.getJSONObject(JSONPhotoKeys.KEY_URLS);
            unsplashPhoto = new UnsplashPhoto(unsplashId,
                    urls.getString(JSONPhotoKeys.KEY_URL_FULL),
                    urls.getString(JSONPhotoKeys.KEY_URL_REGULAR),
                    urls.getString(JSONPhotoKeys.KEY_URL_SMALL),
                    urls.getString(JSONPhotoKeys.KEY_URL_THUMB));
            unsplashPhoto.save();
            return unsplashPhoto;
        } catch (Exception e) {
            return e.getLocalizedMessage();
        }
    }
}
