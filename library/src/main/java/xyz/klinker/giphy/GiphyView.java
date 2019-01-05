package xyz.klinker.giphy;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class GiphyView extends FrameLayout {

    private GifSelectedCallback callback = null;

    private GiphyApiHelper helper;
    private RecyclerView recycler;
    private GiphyAdapter adapter;
    private View progressSpinner;
    private EditText searchView;

    public GiphyView(@NonNull Context context) {
        super(context);
        init();
    }

    public GiphyView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GiphyView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        View gifView = LayoutInflater.from(getContext()).inflate(R.layout.giphy_search_activity, this, false);
        addView(gifView);

        recycler = (RecyclerView) findViewById(R.id.recycler_view);
        progressSpinner = findViewById(R.id.list_progress);

        searchView = (EditText) findViewById(R.id.search_view);
        searchView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    executeQuery(searchView.getText().toString());
                    return true;
                }
                return false;
            }
        });
    }

    public void initializeView(String apiKey, long sizeLimit) {
        initializeView(apiKey, sizeLimit, false);
    }

    public void initializeView(String apiKey, long sizeLimit, boolean useStickers) {
        helper = new GiphyApiHelper(apiKey, 100, Giphy.PREVIEW_SMALL, sizeLimit);
        helper.useStickers(useStickers);

        loadTrending();

        if (useStickers) {
            searchView.setHint(R.string.find_a_sticker);
        }
    }

    public void setSelectedCallback(GifSelectedCallback callback) {
        this.callback = callback;
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
                new DownloadGif((Activity) getContext(), item.gifUrl, item.name, getContext().getCacheDir().getAbsolutePath(), callback).execute();
            }
        }, true);

        recycler.setLayoutManager(new GridLayoutManager(getContext(), getContext().getResources().getInteger(R.integer.grid_count)));
        recycler.setAdapter(adapter);
    }

    private void dismissKeyboard() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
    }
}
