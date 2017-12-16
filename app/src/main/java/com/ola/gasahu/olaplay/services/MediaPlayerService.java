package com.ola.gasahu.olaplay.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.ola.gasahu.olaplay.R;
import com.ola.gasahu.olaplay.activities.MediaPlayerActivity;
import com.ola.gasahu.olaplay.beans.Track;
import com.ola.gasahu.olaplay.utilities.MediaPlayerConstants;
import com.ola.gasahu.olaplay.utilities.MediaPlayerStateManager;
import com.ola.gasahu.olaplay.utilities.SQLConstants;

import java.io.IOException;

import static com.ola.gasahu.olaplay.utilities.MediaPlayerConstants.LOG_TAG_EXCEPTION;

public class MediaPlayerService extends IntentService
        implements AudioManager.OnAudioFocusChangeListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {
    private static final String LOG_TAG = "MediaPlayerService";
    private static MediaPlayer mp;
    public static boolean isServiceRunning;

    private IBinder mBinder = new MyBinder();
    private AudioManager am;

    public MediaPlayerService() {
        super("MediaPlayerService");
    }

    public static MediaPlayer getMp() {
        return mp;
    }

    public static void setMp(MediaPlayer mp) {
        MediaPlayerService.mp = mp;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "Service created");
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(LOG_TAG, "Starting service...");
        isServiceRunning = true;

        while(isServiceRunning) {}

        Log.d("isServiceRunning: ", String.valueOf(isServiceRunning));
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "Binding activity to service...");

        Track selectedTrack = (Track) intent.getSerializableExtra(MediaPlayerConstants.KEY_SELECTED_TRACK);
        String selectedPlaylist = intent.getStringExtra(MediaPlayerConstants.KEY_SELECTED_PLAYLIST);
        playSong(selectedTrack);
        Notification notification = createNotification(selectedTrack, selectedPlaylist);
        startForeground(1, notification);
        Log.d(LOG_TAG, "Is foreground?: true");
        Log.d(LOG_TAG, "Service bound to activity");
        return mBinder;
    }

    // Play the requested track
    public void playSong(Track selectedTrack) {
        Log.d(LOG_TAG, "START: The playSong() event");
        String url = selectedTrack.getUrl();

        if(mp == null) {
            mp = new MediaPlayer();
        }

        try {
            mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mp.setOnPreparedListener(this);
            mp.setDataSource(url);
            mp.prepareAsync();
        } catch(IOException | IllegalStateException e) {
            e.printStackTrace();
        }

        Log.d(LOG_TAG, "END: The playSong() event");
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //Requesting audio focus
        int result = am.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        //Checking if audio focus is granted
        if(result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            setMp(mp);
            mp.start();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return true;
    }

    public Notification createNotification(Track selectedTrack, String selectedPlaylist) {
        Notification notification = null;
        Notification.Action prevAction = null, pauseAction = null, playAction = null, nextAction = null;
        Bitmap bm;
        int zero = SQLConstants.ZERO;
        int flag = PendingIntent.FLAG_CANCEL_CURRENT;
        String keySelectedTrack = MediaPlayerConstants.KEY_SELECTED_TRACK;
        String keySelectedPlaylist = MediaPlayerConstants.KEY_SELECTED_PLAYLIST;
        String keyTrackOrigin = MediaPlayerConstants.KEY_TRACK_ORIGIN;
        String origin = MediaPlayerConstants.TAG_NOTIFICATION;
        byte data[] = selectedTrack.getAlbumArt();

        try {
            Notification.Builder builder = new Notification.Builder(this);
            Notification.MediaStyle mediaStyle = new Notification.MediaStyle();
            MediaSession mMediaSession = new MediaSession(this, MediaPlayerConstants.TAG_MEDIA_SESSION);

            //Setting mediastyle attributes
            mediaStyle.setShowActionsInCompactView(0, 1, 2);
            mediaStyle.setMediaSession(mMediaSession.getSessionToken());

            //Setting builder attributes
            builder.setStyle(mediaStyle);
            builder.setContentTitle(selectedTrack.getSong());
            builder.setContentText(selectedTrack.getArtists());
            builder.setVisibility(Notification.VISIBILITY_PUBLIC);
            builder.setSmallIcon(R.mipmap.ic_launcher);
            builder.setShowWhen(false);

            if(data != null && data.length != 0) {
                bm = BitmapFactory.decodeByteArray(data, SQLConstants.ZERO, data.length);

                if(bm != null) {
                    builder.setLargeIcon(bm);
                } else {
                    bm = BitmapFactory.decodeResource(getResources(), R.drawable.img_default_album_art_thumb);
                    builder.setLargeIcon(bm);
                }
            } else {
                bm = BitmapFactory.decodeResource(getResources(), R.drawable.img_default_album_art_thumb);
                builder.setLargeIcon(bm);
            }

            //Creating intents
            Intent prevIntent = new Intent(this, MediaPlayerActivity.class);
            Intent pauseIntent = new Intent(this, MediaPlayerActivity.class);
            Intent playIntent = new Intent(this, MediaPlayerActivity.class);
            Intent nextIntent = new Intent(this, MediaPlayerActivity.class);
            Intent deleteIntent = new Intent(this, MediaPlayerService.class);
            Intent openIntent = new Intent(this, MediaPlayerActivity.class);

            //Setting actions and extras for intents
            prevIntent.setAction(MediaPlayerConstants.PREVIOUS)
                    .putExtra(keySelectedTrack, selectedTrack)
                    .putExtra(keySelectedPlaylist, selectedPlaylist)
                    .putExtra(keyTrackOrigin, origin);
            pauseIntent.setAction(MediaPlayerConstants.PAUSE)
                    .putExtra(keySelectedTrack, selectedTrack)
                    .putExtra(keySelectedPlaylist, selectedPlaylist)
                    .putExtra(keyTrackOrigin, origin);
            playIntent.setAction(MediaPlayerConstants.PLAY)
                    .putExtra(keySelectedTrack, selectedTrack)
                    .putExtra(keySelectedPlaylist, selectedPlaylist)
                    .putExtra(keyTrackOrigin, origin);
            nextIntent.setAction(MediaPlayerConstants.NEXT)
                    .putExtra(keySelectedTrack, selectedTrack)
                    .putExtra(keySelectedPlaylist, selectedPlaylist)
                    .putExtra(keyTrackOrigin, origin);
            deleteIntent.setAction(MediaPlayerConstants.STOP);
            openIntent.setAction(MediaPlayerConstants.OPEN)
                    .putExtra(keySelectedTrack, selectedTrack)
                    .putExtra(keySelectedPlaylist, selectedPlaylist)
                    .putExtra(keyTrackOrigin, origin);

            //Creating pending intents
            PendingIntent prevPendingIntent = PendingIntent.getActivity(this, zero, prevIntent, flag);
            PendingIntent pausePendingIntent = PendingIntent.getActivity(this, zero, pauseIntent, flag);
            PendingIntent playPendingIntent = PendingIntent.getActivity(this, zero, playIntent, flag);
            PendingIntent nextPendingIntent = PendingIntent.getActivity(this, zero, nextIntent, flag);
            PendingIntent deletePendingIntent = PendingIntent.getService(this, zero, deleteIntent, flag);
            PendingIntent openPendingIntent = PendingIntent.getActivity(this, zero, openIntent, flag);

            //Checking OS build version for notification compatibility
            if(android.os.Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP ||
                    android.os.Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP_MR1) {
                //Creating notification actions
                prevAction = new Notification.Action.Builder(R.drawable.ic_skip_previous_white_36dp, MediaPlayerConstants.PREVIOUS, prevPendingIntent).build();
                pauseAction = new Notification.Action.Builder(R.drawable.ic_pause_white_36dp, MediaPlayerConstants.PAUSE, pausePendingIntent).build();
                playAction = new Notification.Action.Builder(R.drawable.ic_play_arrow_white_36dp, MediaPlayerConstants.PLAY, playPendingIntent).build();
                nextAction = new Notification.Action.Builder(R.drawable.ic_skip_next_white_36dp, MediaPlayerConstants.NEXT, nextPendingIntent).build();
            } else if(android.os.Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
                //Creating Icons for actions
                Icon prevIcon = Icon.createWithResource(this, R.drawable.ic_skip_previous_white_36dp);
                Icon pauseIcon = Icon.createWithResource(this, R.drawable.ic_pause_white_36dp);
                Icon playIcon = Icon.createWithResource(this, R.drawable.ic_play_arrow_white_36dp);
                Icon nextIcon = Icon.createWithResource(this, R.drawable.ic_skip_next_white_36dp);

                //Creating notification actions
                prevAction = new Notification.Action.Builder(prevIcon, MediaPlayerConstants.PREVIOUS, prevPendingIntent).build();
                pauseAction = new Notification.Action.Builder(pauseIcon, MediaPlayerConstants.PAUSE, pausePendingIntent).build();
                playAction = new Notification.Action.Builder(playIcon, MediaPlayerConstants.PLAY, playPendingIntent).build();
                nextAction = new Notification.Action.Builder(nextIcon, MediaPlayerConstants.NEXT, nextPendingIntent).build();
            }

            //Adding notification actions to the builder
            builder.addAction(prevAction);

            if(mp.isPlaying()) {
                builder.addAction(pauseAction);
                builder.setOngoing(true);
            } else {
                builder.addAction(playAction);
                builder.setOngoing(false);
            }

            builder.addAction(nextAction);
            builder.setDeleteIntent(deletePendingIntent);
            builder.setContentIntent(openPendingIntent);

            //Building notification
            notification = builder.build();
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(SQLConstants.ONE, notification);
        } catch(Exception e) {
            Log.e(LOG_TAG_EXCEPTION, e.getMessage());
            
        }

        Log.d(LOG_TAG, "Notification created");
        return notification;
    }

    @Override
    public void onDestroy() {
        //Abandoning audio focus
        am.abandonAudioFocus(this);

        if(mp != null) {
            mp.release();
        }

        isServiceRunning = false;
        Log.d(LOG_TAG, "MediaPlayerService destroyed");
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS:
                if (mp != null && mp.isPlaying()) {
                    MediaPlayerStateManager mpStateManager = ((MediaPlayerStateManager) getApplicationContext());
                    mp.pause();
                }

                break;

            case AudioManager.AUDIOFOCUS_GAIN:
                if (mp != null) {
                    mp.start();
                }

                break;
        }
    }

    public class MyBinder extends Binder {
        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }
}
