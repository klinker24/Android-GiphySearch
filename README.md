# Giphy Android Search Library

This library is a wrapper Giphy's wonderful [API](https://github.com/Giphy/GiphyAPI) and allows you to easily include animated GIFs in your projects.

The library will start by showing the trending GIFs thn allow the user to search through Giphy's vast amount of animations.

GIFs are automatically played when the user scrolls through them, then, when the user clicks a GIF, it will be downloaded and the resulting `file://` URI will be returned to your activity.

The library is extremely easy to implement, as shown below. Enjoy all the animations!

## Installation

Include the following in your gradle script:

```groovy
dependencies {
    compile 'com.klinkerapps:giphy:1.0.0'
}
```

and resync the project.

## Usage

To create a giphy search activity, you can use `Giphy.Builder`:

```java
new Giphy.Builder(activity, "dc6zaTOxFJmzC")    // Giphy's BETA key
    .maxFileSize(5 * 1024 * 1024)               // 5 mb
    .start(Giphy.REQUEST_GIPHY);
```

Max file size is optional. In your activity, listen for the results:

```java
@Override
public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == Giphy.REQUEST_GIPHY) {
        if (resultCode == Activity.RESULT_OK) {
            Uri gif = data.getData();
            // do something with the uri.
        }
    } else {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
```

### Obtaining an API Key

Giphy makes a public BETA API key available for any one to use during testing: `dc6zaTOxFJmzC`.

They say that this BETA key is rate limited, so I recommend [applying](http://api.giphy.com/submit) for a production key.

### Notes about the library

GIFs are always automatically played when scrolling the list, there isn't and won't be an option for this. The GIF file format, in general is very wasteful, for this reason, the GIFs that are being played on the list are not the actual `.gif` files. Giphy provides a `.mp4` version of all of their GIFs. These are much lighter weight, load quicker, and have better quality.

When a user does download the GIF to your app, it will download the `.gif` and not the `.mp4` video. To consume these GIFs within your app, I highly recommend the amazing [Glide](https://github.com/bumptech/glide) library. Glide is used in almost all of my applications. One thing to note about the library, when using it for animated GIFs, use `.diskCacheStrategy(DiskCacheStrategy.SOURCE)` to ensure load times are low.

## License

    Copyright (C) 2016 Jake Klinker, Luke Klinker

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
