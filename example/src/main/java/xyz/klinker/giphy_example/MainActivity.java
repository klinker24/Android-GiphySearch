/*
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */

package xyz.klinker.giphy_example;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import xyz.klinker.giphy.Giphy;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.image);

        findViewById(R.id.small).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Giphy.Builder(MainActivity.this, "dc6zaTOxFJmzC") //Giphy BETA API Key
                        .setPreviewSize(Giphy.PREVIEW_SMALL)
                        .maxFileSize(2 * 1024 * 1024) //2MB
                        .start();
            }
        });

        findViewById(R.id.medium).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Giphy.Builder(MainActivity.this, "dc6zaTOxFJmzC")
                        .setPreviewSize(Giphy.PREVIEW_MEDIUM)
                        .maxFileSize(5 * 1024 * 1024) //5MB
                        .start();
            }
        });

        findViewById(R.id.large).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Giphy.Builder(MainActivity.this, "dc6zaTOxFJmzC")
                        .setPreviewSize(Giphy.PREVIEW_LARGE)
                        .maxFileSize(8 * 1024 * 1024)
                        .start();
            }
        });

        findViewById(R.id.view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, GiphyViewActivity.class));
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Giphy.REQUEST_GIPHY) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                imageView.setVisibility(View.VISIBLE);
                Uri gif = data.getData();
                Glide.with(this).asGif().load(gif).into(imageView);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
