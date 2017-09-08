package xyz.klinker.giphy;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.util.List;

public class GiphyAdapter extends RecyclerView.Adapter<GiphyAdapter.GifViewHolder> {

    private static final int VIEW_TYPE_HEADER = 1;
    private static final int VIEW_TYPE_NORMAL = 2;

    interface Callback {
        void onClick(GiphyApiHelper.Gif item);
    }

    private List<GiphyApiHelper.Gif> gifs;
    private GiphyAdapter.Callback callback;

    GiphyAdapter(List<GiphyApiHelper.Gif> gifs, GiphyAdapter.Callback callback) {
        this.gifs = gifs;
        this.callback = callback;
    }

    @Override
    public GifViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(viewType == VIEW_TYPE_HEADER ?
                        R.layout.adapter_item_gif_header :
                        R.layout.adapter_item_gif, parent, false);
        return new GifViewHolder(v);
    }

    @Override
    public void onBindViewHolder(GifViewHolder holder, int position) {
        if (position != 0) {
            holder.bind(gifs.get(position - 1));
        }
    }

    @Override
    public int getItemCount() {
        return gifs.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? VIEW_TYPE_HEADER : VIEW_TYPE_NORMAL;
    }

    class GifViewHolder extends RecyclerView.ViewHolder {

        private ImageView gifIv;
        private ImageView gifPreview;
        private boolean previewDownloaded;
        private boolean gifDownloaded;

        GifViewHolder(View itemView) {
            super(itemView);
            gifIv = (ImageView) itemView.findViewById(R.id.gif);
            gifPreview = (ImageView) itemView.findViewById(R.id.gifpreview);
        }

        private void bind(final GiphyApiHelper.Gif gif) {
            previewDownloaded = gif.getPreviewDownloaded();
            gifDownloaded = gif.getGifDownloaded();
            gifPreview.setVisibility(View.VISIBLE);

            Glide.with(itemView.getContext())
                    .asGif()
                    .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.DATA).centerCrop())
                    .load(Uri.parse(gif.previewGif))
                    .listener(new RequestListener<GifDrawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GifDrawable resource, Object model, Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource) {
                            gif.setGifDownloaded(true);
                            gifPreview.setVisibility(View.GONE);
                            return false;
                        }
                    }).into(gifIv);

            if (!previewDownloaded) {
                Glide.with(itemView.getContext())
                        .load(Uri.parse(gif.previewImage))
                        .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.DATA).centerCrop())
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                gif.setPreviewDownloaded(true);
                                return false;
                            }
                        }).into(gifPreview);
            } else {

                if (!gifDownloaded) {
                    Glide.with(itemView.getContext()).load(Uri.parse(gif.previewImage)).into(gifPreview);
                } else {
                    gifPreview.setVisibility(View.GONE);
                }
            }

            gifIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    callback.onClick(gif);
                }
            });
        }
    }
}