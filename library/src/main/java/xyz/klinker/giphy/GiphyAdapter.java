package xyz.klinker.giphy;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

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

        GifViewHolder(View itemView) {
            super(itemView);
            gifIv = (ImageView) itemView.findViewById(R.id.gif);
        }

        private void bind(final GiphyApiHelper.Gif gif) {
            Glide.with(itemView.getContext()).asGif().load(Uri.parse(gif.gifUrl)).into(gifIv);
            gifIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    callback.onClick(gif);
                }
            });
        }
    }
}