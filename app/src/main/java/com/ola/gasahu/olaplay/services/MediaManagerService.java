package com.ola.gasahu.olaplay.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.ola.gasahu.olaplay.utilities.MediaLibraryManager;
import com.ola.gasahu.olaplay.utilities.MediaPlayerConstants;

public class MediaManagerService extends IntentService {

    public MediaManagerService() {
        super("MediaManagerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("MediaManagerService", "Inside MediaManagerService");

        //Initialising Mediaplayer library
        MediaLibraryManager.init(this);

        //Sending broadcast indicating that MediaManagerService has finished initiliasing the library
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MediaPlayerConstants.ACTION_INIT_COMPLETE);
        sendBroadcast(broadcastIntent);
    }
}