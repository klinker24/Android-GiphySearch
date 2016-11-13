package xyz.klinker.giphy;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

class DownloadGif extends AsyncTask<Void, Void, Uri> {

    Activity activity;
    String video;
    ProgressDialog dialog;

    DownloadGif(Activity activity, String videoLink) {
        this.activity = activity;
        this.video = videoLink;
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
            return saveGiffy(activity, video);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(Uri downloadedTo) {
        if (downloadedTo != null) {
            activity.setResult(Activity.RESULT_OK, new Intent().setData(downloadedTo));
            activity.finish();

            try {
                dialog.dismiss();
            } catch (Exception e) {
            }
        } else {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                Toast.makeText(activity, R.string.error_downloading_gif,
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(activity, R.string.error_downloading_gif_permission,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Uri saveGiffy(Context context, String videoUrl) throws Exception {
        final File file = new File(context.getFilesDir().getPath(),
                "giphy_" + System.currentTimeMillis() + ".gif");
        if (!file.createNewFile()) {
            // file already exists
        }

        URL url = new URL(videoUrl);
        URLConnection connection = url.openConnection();
        connection.setReadTimeout(5000);
        connection.setConnectTimeout(30000);

        InputStream is = connection.getInputStream();
        BufferedInputStream inStream = new BufferedInputStream(is, 1024 * 5);
        FileOutputStream outStream = new FileOutputStream(file);

        byte[] buffer = new byte[1024 * 5];
        int len;
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }

        outStream.flush();
        outStream.close();
        inStream.close();
        is.close();

        return Uri.fromFile(file);
    }
}