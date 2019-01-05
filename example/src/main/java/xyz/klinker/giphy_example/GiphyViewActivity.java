package xyz.klinker.giphy_example;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import androidx.appcompat.app.AppCompatActivity;
import xyz.klinker.giphy.GifSelectedCallback;
import xyz.klinker.giphy.GiphyView;

public class GiphyViewActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_giphy_view);

        final ImageView image = (ImageView) findViewById(R.id.image);
        final GiphyView giphy = (GiphyView) findViewById(R.id.giphy);

        giphy.setSelectedCallback(new GifSelectedCallback() {
            @Override
            public void onGifSelected(Uri uri) {
                Log.v("Giphy", "image uri: " + uri.toString());
                Glide.with(GiphyViewActivity.this).asGif().load(uri).into(image);
            }
        });

        giphy.initializeView("dc6zaTOxFJmzC", 5 * 1024 * 1024); // 5mb
    }
}
