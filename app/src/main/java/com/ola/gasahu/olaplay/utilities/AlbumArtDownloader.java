package com.ola.gasahu.olaplay.utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;

import static com.ola.gasahu.olaplay.utilities.MediaPlayerConstants.LOG_TAG_EXCEPTION;

public class AlbumArtDownloader implements Response.Listener<Bitmap>, Response.ErrorListener {
    public void startDownload(Context context, String url) {
        RequestQueue queue = Volley.newRequestQueue(context);
        ImageRequest imageRequest = new ImageRequest(url, this, 200, 200, ImageView.ScaleType.CENTER,
                                                    Bitmap.Config.ARGB_8888, this);

        queue.add(imageRequest);
        Log.d("Downloading image from", url);
    }

    @Override
    public void onResponse(Bitmap response) {
        Log.d("DownloadTask", "Image downloaded");
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.e(LOG_TAG_EXCEPTION, error.getMessage());
    }
}
