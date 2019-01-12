package xyz.klinker.giphy;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

class DownloadGif extends AsyncTask<Void, Void, Uri> {

    Activity activity;
    String gifURL;
    String name;
    String saveLocation;
    ProgressDialog dialog;
    GifSelectedCallback callback;

    DownloadGif(Activity activity, String gifURL, String name, String saveLocation) {
        this(activity, gifURL, name, saveLocation, null);
    }

    DownloadGif(Activity activity, String gifURL, String name, String saveLocation, GifSelectedCallback callback) {
        this.activity = activity;
        this.gifURL = gifURL;
        this.name = name;
        this.saveLocation = saveLocation;
        this.callback = callback;
    }

    @Override
    public void onPreExecute() {
        dialog = new ProgressDialog(activity);
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.setMessage(activity.getString(R.string.downloading));
        dialog.show();
    }

    @Override
    protected Uri doInBackground(Void... arg0) {
        try {
            return saveGiffy(activity, gifURL, name, saveLocation);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(Uri downloadedTo) {
        try {
            if (callback != null) {
                callback.onGifSelected(downloadedTo);
                dialog.dismiss();
            } else if (downloadedTo != null) {
                activity.setResult(Activity.RESULT_OK, new Intent().setData(downloadedTo));
                activity.finish();

                try {
                    dialog.dismiss();
                } catch (Exception e) {
                    Log.e("Exception", String.valueOf(e));
                }
            } else {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    Toast.makeText(activity, R.string.error_downloading_gif,
                            Toast.LENGTH_SHORT).show();
                    activity.finish();
                } else {
                    Toast.makeText(activity, R.string.error_downloading_gif_permission,
                            Toast.LENGTH_SHORT).show();
                    activity.finish();
                }
            }
        } catch (IllegalStateException e) {
            Log.e("Exception", String.valueOf(e));
        }
    }

    private Uri saveGiffy(Context context, String gifURL, String name, String saveLocation) throws Exception {
        name = name + ".gif";

        //Default save location to internal storage if no location set.
        if (saveLocation == null) {
            saveLocation = context.getFilesDir().getPath();
        }

        //Create save location if not exist.
        File dir = new File(saveLocation);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File saveGif = new File(saveLocation, name);
        if (!saveGif.createNewFile()) {
            //File exists, return existing File URI.
            return Uri.fromFile(saveGif);
        } else {
            //Download GIF via Glide, then save to specified location.
            File gifDownload = Glide.with(context).downloadOnly().load(gifURL).submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).get();
            FileInputStream inStream = new FileInputStream(gifDownload);
            FileOutputStream outStream = new FileOutputStream(saveGif);
            FileChannel inChannel = inStream.getChannel();
            FileChannel outChannel = outStream.getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
            inStream.close();
            outStream.close();
        }

        return Uri.fromFile(saveGif);
    }
}