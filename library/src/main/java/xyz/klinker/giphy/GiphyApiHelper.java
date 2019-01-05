/*
 * Copyright (C) 2016 Jacob Klinker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.klinker.giphy;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Helper for working with Giphy data. To use, create a new object and pass in your api key and
 * max size, then call search() or trends().
 * <p>
 * new GiphyHelper(apiKey, 1024 * 1024)
 * .search(this);
 */
class GiphyApiHelper {

    public static final int NO_SIZE_LIMIT = -1;

    private String apiKey;
    private int limit;
    private int previewSize;
    private long maxSize;
    private boolean useStickers = false;

    GiphyApiHelper(String apiKey, int limit, int previewSize, long maxSize) {
        this.apiKey = apiKey;
        this.limit = limit;
        this.previewSize = previewSize;
        this.maxSize = maxSize;
    }

    public void useStickers(boolean useStickers) {
        this.useStickers = useStickers;
    }

    private static final String[] PREVIEW_SIZE = new String[]{"fixed_width_downsampled", "fixed_width", "downsized"};

    private static final String[] SIZE_OPTIONS = new String[]{"original", "downsized_large", "downsized_medium",
            "downsized", "fixed_height", "fixed_width", "fixed_height_small", "fixed_width_small"};

    interface Callback {
        void onResponse(List<Gif> gifs);
    }

    void search(String query, Callback callback) {
        new SearchGiphy(apiKey, limit, previewSize, maxSize, query, callback, useStickers).execute();
    }

    void trends(Callback callback) {
        new GiphyTrends(apiKey, previewSize, maxSize, callback, useStickers).execute();
    }

    private static class GiphyTrends extends SearchGiphy {

        GiphyTrends(String apiKey, int previewSize, long maxSize, Callback callback, boolean useStickers) {
            super(apiKey, -1, previewSize, maxSize, null, callback, useStickers);
        }

        @Override
        protected String buildSearchUrl(String query) throws UnsupportedEncodingException {
            return "https://api.giphy.com/v1/" + (useStickers ? "stickers" : "gifs") + "/trending?api_key=" + getApiKey();
        }
    }

    private static class SearchGiphy extends AsyncTask<Void, Void, List<Gif>> {

        private String apiKey;
        private int limit;
        private int previewSize;
        private long maxSize;
        private String query;
        private Callback callback;
        protected boolean useStickers;

        SearchGiphy(String apiKey, int limit, int previewSize, long maxSize, String query, Callback callback, boolean useStickers) {
            this.apiKey = apiKey;
            this.limit = limit;
            this.previewSize = previewSize;
            this.maxSize = maxSize;
            this.query = query;
            this.callback = callback;
            this.useStickers = useStickers;
        }

        String getApiKey() {
            return apiKey;
        }

        @Override
        protected List<Gif> doInBackground(Void... arg0) {
            List<Gif> gifList = new ArrayList<>();

            try {
                // create the connection
                URL urlToRequest = new URL(buildSearchUrl(query));
                HttpURLConnection urlConnection = (HttpURLConnection)
                        urlToRequest.openConnection();

                // create JSON object from content
                InputStream in = new BufferedInputStream(
                        urlConnection.getInputStream());
                JSONObject root = new JSONObject(getResponseText(in));
                JSONArray data = root.getJSONArray("data");

                try {
                    in.close();
                } catch (Exception e) { }

                try {
                    urlConnection.disconnect();
                } catch (Exception e) { }

                for (int i = 0; i < data.length(); i++) {
                    JSONObject gif = data.getJSONObject(i);
                    String name = gif.getString("slug");
                    Log.d("GIF Name", name);
                    JSONObject images = gif.getJSONObject("images");
                    JSONObject previewImage = images.getJSONObject("downsized_still");
                    JSONObject previewGif = images.getJSONObject(PREVIEW_SIZE[previewSize]);
                    JSONObject originalSize = images.getJSONObject("original");
                    JSONObject downsized = null;

                    // Return the highest quality GIF under MaxSizeLimit.
                    for (String size : SIZE_OPTIONS) {
                        downsized = images.getJSONObject(size);
                        Log.v("giphy", size + ": " + downsized.getString("size") + " bytes");

                        if (Long.parseLong(downsized.getString("size")) < maxSize ||
                                maxSize == NO_SIZE_LIMIT) {
                            break;
                        } else {
                            downsized = null;
                        }
                    }

                    if (downsized != null) {
                        gifList.add(
                                new Gif(name,
                                        previewImage.getString("url"),
                                        previewGif.getString("url"),
                                        downsized.getString("url"),
                                        originalSize.getString("mp4"))
                        );
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return gifList;
        }

        @Override
        protected void onPostExecute(List<Gif> result) {
            if (callback != null) {
                callback.onResponse(result);
            }
        }

        protected String buildSearchUrl(String query) throws UnsupportedEncodingException {
            return "https://api.giphy.com/v1/" + (useStickers ? "stickers" : "gifs") + "/search?q=" + URLEncoder.encode(query, "UTF-8") + "&limit=" + limit + "&api_key=" + apiKey;
        }

        private String getResponseText(InputStream inStream) {
            return new Scanner(inStream).useDelimiter("\\A").next();
        }
    }

    static class Gif {
        String name;
        String previewImage;
        String previewGif;
        String gifUrl;
        String mp4Url;
        boolean previewDownloaded = false;
        boolean gifDownloaded = false;

        Gif(String name, String previewImage, String previewGif, String gifUrl, String mp4Url) {
            try {
                this.name = URLDecoder.decode(name, "UTF-8");
                this.previewImage = URLDecoder.decode(previewImage, "UTF-8");
                this.previewGif = URLDecoder.decode(previewGif, "UTF-8");
                this.gifUrl = URLDecoder.decode(gifUrl, "UTF-8");
                this.mp4Url = URLDecoder.decode(mp4Url, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        public boolean getPreviewDownloaded() {
            return previewDownloaded;
        }

        public void setPreviewDownloaded(boolean previewDownloaded) {
            this.previewDownloaded = previewDownloaded;
        }

        public boolean getGifDownloaded() {
            return gifDownloaded;
        }

        public void setGifDownloaded(boolean gifDownloaded) {
            this.gifDownloaded = gifDownloaded;
        }
    }
}
