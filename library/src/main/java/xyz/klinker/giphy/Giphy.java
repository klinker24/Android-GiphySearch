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

import android.app.Activity;
import android.content.Intent;

/**
 * Entry class for creating a new GiphyActivity and downloading gifs from the service.
 * <p>
 * Simply create a new Giphy object and call start. Then in your activity, look for the results in
 * onActivityResult(). The uri to the downloaded image will be available at intent.getData() if the
 * result is set to Activity.RESULT_OK.
 */
public class Giphy {

	public static final int REQUEST_GIPHY = 10012;
    public static final int PREVIEW_SMALL = 0;
    public static final int PREVIEW_MEDIUM = 1;
    public static final int PREVIEW_LARGE = 2;

    private Activity activity;
    private String apiKey;
    private String saveLocation;
    private int limit;
    private int previewSize;
    private long maxFileSize;
    private boolean useStickers;

    private Giphy(Activity activity, String apiKey) {
        this.activity = activity;
        this.apiKey = apiKey;
    }

    public void start(int requestCode) {
        Intent intent = new Intent(activity, GiphyActivity.class);
        intent.putExtra(GiphyActivity.EXTRA_API_KEY, apiKey);
        intent.putExtra(GiphyActivity.EXTRA_GIF_LIMIT, limit);
        intent.putExtra(GiphyActivity.EXTRA_PREVIEW_SIZE, previewSize);
        intent.putExtra(GiphyActivity.EXTRA_SIZE_LIMIT, maxFileSize);
        intent.putExtra(GiphyActivity.EXTRA_SAVE_LOCATION, saveLocation);
        intent.putExtra(GiphyActivity.EXTRA_USE_STICKERS, useStickers);
        activity.startActivityForResult(intent, requestCode);
    }

    public static class Builder {

        private Giphy giphy;

        public Builder(Activity activity, String apiKey) {
            this.giphy = new Giphy(activity, apiKey);
        }

        public Giphy.Builder setSaveLocation(String saveLocation) {
            giphy.saveLocation = saveLocation;
            return this;
        }

        public Giphy.Builder maxFileSize(long maxFileSize) {
            giphy.maxFileSize = maxFileSize;
            return this;
        }

        public Giphy.Builder setQueryLimit(int limit) {
            giphy.limit = limit;
            return this;
        }

        public Giphy.Builder setPreviewSize(int previewSize) {
            giphy.previewSize = previewSize;
            return this;
        }

        public Giphy.Builder useStickers(boolean useStickers) {
            giphy.useStickers = useStickers;
            return this;
        }

        public Giphy build() {
            return giphy;
        }

        public void start() {
            build().start(REQUEST_GIPHY);
        }
    }

}
