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
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;

import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.List;

public class GiphyActivity extends AppCompatActivity {

    public static final String EXTRA_API_KEY = "api_key";
    public static final String EXTRA_GIF_LIMIT = "gif_limit";
    public static final String EXTRA_PREVIEW_SIZE = "preview_size";
    public static final String EXTRA_SIZE_LIMIT = "size_limit";
    public static final String EXTRA_SAVE_LOCATION = "save_location";

    private String saveLocation;
    private boolean queried = false;

    private GiphyApiHelper helper;
    private RecyclerView recycler;
    private GiphyAdapter adapter;
    private View progressSpinner;
    private MaterialSearchView searchView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().getExtras() == null ||
                !getIntent().getExtras().containsKey(EXTRA_API_KEY)) {
            throw new RuntimeException("EXTRA_API_KEY is required!");
        }

        helper = new GiphyApiHelper(getIntent().getExtras().getString(EXTRA_API_KEY),
                getIntent().getExtras().getInt(EXTRA_GIF_LIMIT, GiphyApiHelper.NO_SIZE_LIMIT),
                getIntent().getExtras().getInt(EXTRA_PREVIEW_SIZE, Giphy.PREVIEW_SMALL),
                getIntent().getExtras().getLong(EXTRA_SIZE_LIMIT, GiphyApiHelper.NO_SIZE_LIMIT));

        saveLocation = getIntent().getExtras().getString(EXTRA_SAVE_LOCATION, null);

        try {
            getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        } catch (Exception e) { }

        setContentView(R.layout.giffy_search_activity);

        recycler = (RecyclerView) findViewById(R.id.recycler_view);
        progressSpinner = findViewById(R.id.list_progress);

        searchView = (MaterialSearchView) findViewById(R.id.search_view);
        searchView.setVoiceSearch(false);

        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                executeQuery(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {

            }

            @Override
            public void onSearchViewClosed() {
                if (queried)
                {
                    queried = false;
                    searchView.setQuery("", false);
                    loadTrending();
                    //OnSearchViewClosed forces SearchView to close. Show SearchView again. 
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            searchView.showSearch(false);
                        }
                    }, 25);
                }
                else
                {
                    setResult(Activity.RESULT_CANCELED);
                    finish();
                }
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadTrending();
            }
        }, 250);
    }

    @Override
    public void onStart() {
        super.onStart();
        searchView.showSearch(false);
    }

    @Override
    public void onBackPressed() {
        if (queried)
        {
            queried = false;
            searchView.setQuery("", false);
            loadTrending();
        }
        else
        {
            setResult(Activity.RESULT_CANCELED);
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);

        return true;
    }

    private void loadTrending() {
        progressSpinner.setVisibility(View.VISIBLE);
        helper.trends(new GiphyApiHelper.Callback() {
            @Override
            public void onResponse(List<GiphyApiHelper.Gif> gifs) {
                setAdapter(gifs);
            }
        });
    }

    private void executeQuery(String query) {
        queried = true;
        progressSpinner.setVisibility(View.VISIBLE);
        dismissKeyboard();

        helper.search(query, new GiphyApiHelper.Callback() {
            @Override
            public void onResponse(List<GiphyApiHelper.Gif> gifs) {
                setAdapter(gifs);
            }
        });
    }

    private void setAdapter(List<GiphyApiHelper.Gif> gifs) {
        progressSpinner.setVisibility(View.GONE);
        adapter = new GiphyAdapter(gifs, new GiphyAdapter.Callback() {
            @Override
            public void onClick(final GiphyApiHelper.Gif item) {
                new DownloadGif(GiphyActivity.this, item.gifUrl, item.name, saveLocation).execute();
            }
        });

        recycler.setLayoutManager(new LinearLayoutManager(GiphyActivity.this));
        recycler.setAdapter(adapter);
    }

    private void dismissKeyboard() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
    }
}
