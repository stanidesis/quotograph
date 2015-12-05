package com.stanleyidesis.livewallpaperquotes.api.network;

import android.util.Log;

import com.stanleyidesis.livewallpaperquotes.api.Callback;
import com.stanleyidesis.livewallpaperquotes.api.db.UnsplashCategory;
import com.stanleyidesis.livewallpaperquotes.api.db.UnsplashPhoto;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
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

    private static String APP_ID = "d1759c7e191a4e170b2b64ddcdd555d82dd65085ccc4f0e7c0165c031b64cbd0";

    private interface Endpoints {
        String UNSPLASH_SEARCH_URL= "https://unsplash.com/search?";
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

    private interface JSONPhotoKeys {
        String KEY_ID = "id";
        String KEY_URLS = "urls";
        String KEY_URL_FULL = "full";
        String KEY_URL_REGULAR = "regular";
        String KEY_URL_SMALL = "small";
        String KEY_URL_THUMB = "thumb";
    }

    URLConnection openConnection(String urlWithParams) {
        try {
            URL url = new URL(Endpoints.UNSPLASH_API_URL.concat(urlWithParams));
            final URLConnection connection = url.openConnection();
            connection.setRequestProperty("Accept-Version", "v1");
            connection.setRequestProperty("Authorization", "Client-ID " + APP_ID);
            return connection;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Object fetchRandomPhoto(final UnsplashCategory unsplashCategory, final boolean featured,
                                 final String optionalQuery) {
        String urlWithParams = Endpoints.UNSPLASH_API_URL.concat(Endpoints.UNSPLASH_RANDOM_PHOTO).concat("?");
        if (unsplashCategory != null) {
            urlWithParams = urlWithParams.concat(String.format(Parameters.CATEGORIES_PARAM, String.valueOf(unsplashCategory.unsplashId)));
        }
        if (featured) {
            urlWithParams = urlWithParams.concat(String.format(Parameters.FEATURED_PARAM, Boolean.toString(true))));
        }
        if (optionalQuery != null) {
            try {
                urlWithParams = urlWithParams.concat(String.format(Parameters.QUERY_PARAM, URLEncoder.encode(optionalQuery, "UTF-8")));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        try {
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(openConnection(urlWithParams).getInputStream()));
            StringBuilder jsonStringBuilder = new StringBuilder();
            String readLine = null;
            while ((readLine = bufferedReader.readLine()) != null) {
                jsonStringBuilder.append(readLine);
            }
            bufferedReader.close();
            JSONObject randomPhotoJSON = new JSONObject(jsonStringBuilder.toString());
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
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
