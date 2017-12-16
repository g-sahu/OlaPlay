package com.ola.gasahu.olaplay.dao;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.ola.gasahu.olaplay.beans.Track;
import com.ola.gasahu.olaplay.utilities.MediaLibraryManager;
import com.ola.gasahu.olaplay.utilities.MediaPlayerConstants;
import com.ola.gasahu.olaplay.utilities.SQLConstants;
import com.ola.gasahu.olaplay.utilities.Utilities;

import java.util.Iterator;
import java.util.List;

class MediaPlayerDBHelper extends SQLiteOpenHelper {
    private Context context;

    MediaPlayerDBHelper(Context context) {
        super(context, MediaPlayerContract.DATABASE_NAME, null, MediaPlayerContract.DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        SQLiteStatement insertStmt = null;
        List<Track> trackList;
        Iterator<Track> trackIterator;
        Track track;
        int c, tracksInserted = 0;

        try {
            Log.d(MediaPlayerConstants.LOG_TAG_SQL, SQLConstants.SQL_CREATE_TRACKS);
            db.execSQL(SQLConstants.SQL_CREATE_TRACKS);

            Log.d(MediaPlayerConstants.LOG_TAG_SQL, SQLConstants.SQL_CREATE_PLAYLISTS);
            db.execSQL(SQLConstants.SQL_CREATE_PLAYLISTS);

            Log.d(MediaPlayerConstants.LOG_TAG_SQL, SQLConstants.SQL_CREATE_PLAYLIST_DETAIL);
            db.execSQL(SQLConstants.SQL_CREATE_PLAYLIST_DETAIL);

            //Creating default playlist 'Favourites'
            insertStmt = db.compileStatement(SQLConstants.SQL_INSERT_PLAYLIST);

            insertStmt.bindLong(1, SQLConstants.PLAYLIST_INDEX_FAVOURITES);
            insertStmt.bindString(2, SQLConstants.PLAYLIST_TITLE_FAVOURITES);
            insertStmt.bindLong(3, SQLConstants.ZERO);
            insertStmt.bindString(4, Utilities.getCurrentDate());

            Log.d(MediaPlayerConstants.LOG_TAG_SQL, insertStmt.toString());
            insertStmt.execute();

            //Fetching tracks from Web Service
            trackList = MediaLibraryManager.populateTrackInfoList(context);

            //Inserting tracks in table 'Tracks'
            if(trackList != null && !trackList.isEmpty()) {
                insertStmt = db.compileStatement(SQLConstants.SQL_INSERT_TRACK);
                trackIterator = trackList.iterator();

                while(trackIterator.hasNext()) {
                    track = trackIterator.next();
                    c = SQLConstants.ONE;

                    insertStmt.bindString(c++, track.getSong());
                    insertStmt.bindLong(c++, track.getTrackIndex());
                    insertStmt.bindString(c++, track.getUrl());
                    insertStmt.bindString(c++, track.getArtists());
                    //insertStmt.bindBlob(c++, track.getAlbumArt());
                    insertStmt.bindLong(c++, track.getFavSw());
                    insertStmt.bindString(c, Utilities.getCurrentDate());

                    Log.d(MediaPlayerConstants.LOG_TAG_SQL, insertStmt.toString());

                    try {
                        insertStmt.executeInsert();
                        ++tracksInserted;
                    } catch(SQLException sqle) {
                        Log.e(MediaPlayerConstants.LOG_TAG_EXCEPTION, sqle.getMessage());
                    }
                }

                Log.d("Tracks added to library", String.valueOf(tracksInserted));
            }
        } catch(Exception e) {
            Log.e(MediaPlayerConstants.LOG_TAG_EXCEPTION, e.getMessage());
        } finally {
            if(insertStmt != null) {
                insertStmt.close();
            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }
}
