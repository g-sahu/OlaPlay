package com.ola.gasahu.olaplay.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import android.widget.Toast;

import com.ola.gasahu.olaplay.beans.Playlist;
import com.ola.gasahu.olaplay.beans.Track;
import com.ola.gasahu.olaplay.utilities.MediaLibraryManager;
import com.ola.gasahu.olaplay.utilities.MediaPlayerConstants;
import com.ola.gasahu.olaplay.utilities.MessageConstants;
import com.ola.gasahu.olaplay.utilities.SQLConstants;
import com.ola.gasahu.olaplay.utilities.Utilities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MediaPlayerDAO {
    private static SQLiteDatabase db;
    private MediaPlayerDBHelper mDbHelper;
    private Context context;

    public MediaPlayerDAO(Context context) {
        this.context = context;
        mDbHelper = new MediaPlayerDBHelper(context);
        db = mDbHelper.getWritableDatabase();
    }

    //Closes SQLite database connection
    public void closeConnection() {
        if(mDbHelper != null) {
            mDbHelper.close();
        }

        if(db != null) {
            db.close();
        }
    }

    //Add to playlist
    public void addToPlaylists(List<Playlist> selectedPlaylists, Track selectedTrack) {
        SQLiteStatement insertStmt, updateStmt;
        int trackID, playlistID, playlistSize, newPlaylistSize, increment = SQLConstants.ONE;
        Playlist playlist;
        String toastText;

        try {
            insertStmt = db.compileStatement(SQLConstants.SQL_INSERT_PLAYLIST_DETAIL);

            //Retrieving selected track from trackInfoList
            trackID = selectedTrack.getTrackID();

            Iterator<Playlist> selectedPlaylistsIterator = selectedPlaylists.iterator();
            while(selectedPlaylistsIterator.hasNext()) {
                //Fetching current values for the selected playlist
                playlist = selectedPlaylistsIterator.next();
                playlistID = playlist.getPlaylistID();
                playlistSize = playlist.getPlaylistSize();
                newPlaylistSize = playlistSize + increment;

                //Making an entry in table 'Playlist_Detail' for the selected playlist
                insertStmt.clearBindings();
                insertStmt.bindLong(1, playlistID);
                insertStmt.bindLong(2, trackID);
                Log.d(MediaPlayerConstants.LOG_TAG_SQL, insertStmt.toString());
                insertStmt.execute();

                //Updating table 'Playlists' for the selected playlist with new values of 'playlistSize' and 'playlistDuration'
                updateStmt = db.compileStatement(SQLConstants.SQL_UPDATE_PLAYLIST);
                updateStmt.bindLong(1, newPlaylistSize);
                updateStmt.bindString(2, Utilities.getCurrentDate());
                updateStmt.bindLong(3, playlistID);
                Log.d(MediaPlayerConstants.LOG_TAG_SQL, updateStmt.toString());
                updateStmt.execute();

                //Setting new values for 'playlistSize' and 'playlistDuration' for selected playlist
                playlist.setPlaylistSize(newPlaylistSize);
                MediaLibraryManager.getPlaylistInfoList().set(playlist.getPlaylistIndex(), playlist);

                //If selected playlist is default playlist 'Favourites' then updating fav_sw in table 'Tracks' and trackInfoList
                if(playlistID == SQLConstants.PLAYLIST_ID_FAVOURITES) {
                    //Updating fav_sw in table Tracks
                    updateStmt = db.compileStatement(SQLConstants.SQL_UPDATE_TRACK_FAV_SW);
                    updateStmt.bindLong(1, SQLConstants.FAV_SW_YES);
                    updateStmt.bindLong(2, trackID);
                    Log.d(MediaPlayerConstants.LOG_TAG_SQL, updateStmt.toString());
                    updateStmt.execute();

                    //Setting favSw in trackInfoList
                    selectedTrack.setFavSw(SQLConstants.FAV_SW_YES);
                    MediaLibraryManager.getTrackInfoList().set(selectedTrack.getTrackIndex(), selectedTrack);
                }
            }

            //Setting success toast message
            if(selectedPlaylists.contains(MediaLibraryManager.getPlaylistByIndex(SQLConstants.PLAYLIST_INDEX_FAVOURITES))) {
                toastText = MessageConstants.ADDED_TO_FAVOURITES;
            } else {
                toastText = MessageConstants.ADDED_TO_PLAYLISTS;
            }
        } catch(SQLiteConstraintException sqle) {
            Log.e(MediaPlayerConstants.LOG_TAG_EXCEPTION, sqle.getMessage());
            toastText = MessageConstants.ERROR_DUPLICATE_TRACK_FAVOURITES;
        } catch(Exception e) {
            Log.e(MediaPlayerConstants.LOG_TAG_EXCEPTION, e.getMessage());
            //Setting error toast message
            toastText = MessageConstants.ERROR;
        }

        //Displaying toast message to user
        Toast toast = Toast.makeText(context, toastText, Toast.LENGTH_SHORT);
        toast.show();
    }

    public void removeFromPlaylist(Playlist selectedPlaylist, Track selectedTrack) {
        SQLiteStatement updateStmt;
        String toastText;
        int trackID, playlistID, playlistSize, newPlaylistSize, playlistDuration, newPlaylistDuration;

        try {
            //Fetching existing values for selected playlist
            trackID = selectedTrack.getTrackID();
            playlistID = selectedPlaylist.getPlaylistID();
            playlistSize = selectedPlaylist.getPlaylistSize();

            //Deleting record from table 'Playlist_Detail'
            String args[] = {String.valueOf(playlistID), String.valueOf(trackID)};
            db.execSQL(SQLConstants.SQL_DELETE_TRACK_FROM_PLAYLIST, args);
            Log.d(MediaPlayerConstants.LOG_TAG_SQL, SQLConstants.SQL_DELETE_TRACK_FROM_PLAYLIST);

            newPlaylistSize = playlistSize - 1;

            //Updating table 'Playlists' for the selected playlist with new values of 'playlistSize' and 'playlistDuration'
            updateStmt = db.compileStatement(SQLConstants.SQL_UPDATE_PLAYLIST);
            updateStmt.bindLong(1, newPlaylistSize);
            updateStmt.bindString(2, Utilities.getCurrentDate());
            updateStmt.bindLong(3, playlistID);
            Log.d(MediaPlayerConstants.LOG_TAG_SQL, updateStmt.toString());
            updateStmt.execute();

            //Setting new values for 'playlistSize' and 'playlistDuration' for selected playlist
            selectedPlaylist.setPlaylistSize(newPlaylistSize);
            MediaLibraryManager.getPlaylistInfoList().set(selectedPlaylist.getPlaylistIndex(), selectedPlaylist);

            //If selected playlist is default playlist 'Favourites' then updating fav_sw in table 'Tracks' and trackInfoList
            if(playlistID == SQLConstants.PLAYLIST_ID_FAVOURITES) {
                //Updating fav_sw to 0 in table 'Tracks'
                updateStmt = db.compileStatement(SQLConstants.SQL_UPDATE_TRACK_FAV_SW);
                updateStmt.bindLong(1, SQLConstants.FAV_SW_NO);
                updateStmt.bindLong(2, trackID);
                Log.d(MediaPlayerConstants.LOG_TAG_SQL, updateStmt.toString());
                updateStmt.execute();

                //Setting favSw in trackInfoList
                selectedTrack.setFavSw(SQLConstants.FAV_SW_NO);
                MediaLibraryManager.getTrackInfoList().set(selectedTrack.getTrackIndex(), selectedTrack);

                //Setting success toast message
                toastText = MessageConstants.REMOVED_FROM_FAVOURITES;
            } else {
                //Setting success toast message
                toastText = MessageConstants.REMOVED_FROM_PLAYLIST;
            }
        } catch(Exception e) {
            Log.e(MediaPlayerConstants.LOG_TAG_EXCEPTION, e.getMessage());
            //Setting error toast message
            toastText = MessageConstants.ERROR;
        }

        //Displaying toast message to user
        Toast toast = Toast.makeText(context, toastText, Toast.LENGTH_SHORT);
        toast.show();
    }

    public void createPlaylist(Playlist playlist) {
        SQLiteStatement insertStmt;
        int playlistID;
        String toastText;

        try {
            //Inserting new playlist into table 'Playlists'
            insertStmt = db.compileStatement(SQLConstants.SQL_INSERT_PLAYLIST);

            //Setting playlist_index to current max index + 1 initially
            insertStmt.bindLong(1, MediaLibraryManager.getPlaylistInfoListSize());
            insertStmt.bindString(2, playlist.getPlaylistName());
            insertStmt.bindLong(3, playlist.getPlaylistSize());
            insertStmt.bindString(4, Utilities.getCurrentDate());
            Log.d(MediaPlayerConstants.LOG_TAG_SQL, insertStmt.toString());
            playlistID = (int) insertStmt.executeInsert();

            //Setting playlistID of newly created playlist
            playlist.setPlaylistID(playlistID);

            //Adding new playlist to playlistInfoList
            MediaLibraryManager.addPlaylist(playlist);

            //Updating indices of all the playlists in table 'Playlists'
            updatePlaylistIndices();

            //Setting success toast message
            toastText = MessageConstants.PLAYLIST_CREATED;
        } catch(Exception e) {
            Log.e(MediaPlayerConstants.LOG_TAG_EXCEPTION, e.getMessage());
            //Setting error toast message
            toastText = MessageConstants.ERROR;
        }

        Toast toast = Toast.makeText(context, toastText, Toast.LENGTH_SHORT);
        toast.show();
    }

    public void renamePlaylist(Playlist selectedPlaylist) {
        SQLiteStatement updateStmt;
        String toastText;

        try {
            //Updating the title of the renamed playlist
            updateStmt = db.compileStatement(SQLConstants.SQL_UPDATE_PLAYLIST_TITLE);
            updateStmt.bindString(1, selectedPlaylist.getPlaylistName());
            updateStmt.bindString(2, Utilities.getCurrentDate());
            updateStmt.bindLong(3, selectedPlaylist.getPlaylistID());
            Log.d(MediaPlayerConstants.LOG_TAG_SQL, updateStmt.toString());
            updateStmt.execute();

            //Updating indices of all the playlists in table 'Playlists'
            updatePlaylistIndices();

            //Setting success toast message
            toastText = MessageConstants.PLAYLIST_RENAMED;
        } catch(Exception e) {
            Log.e(MediaPlayerConstants.LOG_TAG_EXCEPTION, e.getMessage());
            

            //Setting error toast message
            toastText = MessageConstants.ERROR;
        }

        //Displaying toast message to user
        Toast toast = Toast.makeText(context, toastText, Toast.LENGTH_SHORT);
        toast.show();
    }

    private void updatePlaylistIndices() {
        SQLiteStatement updateStmt;
        updateStmt = db.compileStatement(SQLConstants.SQL_UPDATE_PLAYLIST_INDICES);
        Iterator<Playlist> playlistIterator = MediaLibraryManager.getPlaylistInfoList().iterator();

        while(playlistIterator.hasNext()) {
            Playlist playlist = playlistIterator.next();

            //Updating the indices of all the playlists
            updateStmt.bindLong(1, playlist.getPlaylistIndex());
            updateStmt.bindString(2, Utilities.getCurrentDate());
            updateStmt.bindLong(3, playlist.getPlaylistID());
            Log.d(MediaPlayerConstants.LOG_TAG_SQL, updateStmt.toString());
            updateStmt.execute();
            updateStmt.clearBindings();
        }
    }

    public void deletePlaylist(Playlist playlist) {
        SQLiteStatement deleteStmt;
        String toastText;
        int playlistID = playlist.getPlaylistID();

        try {
            //Deleting playlist from table 'Playlist_Detail'
            deleteStmt = db.compileStatement(SQLConstants.SQL_DELETE_PLAYLIST_FROM_PLAYLIST_DETAIL);
            deleteStmt.bindLong(1, playlistID);
            Log.d(MediaPlayerConstants.LOG_TAG_SQL, deleteStmt.toString());
            deleteStmt.executeUpdateDelete();

            //Deleting playlist from table 'Playlist'
            deleteStmt = db.compileStatement(SQLConstants.SQL_DELETE_FROM_PLAYLISTS);
            deleteStmt.bindLong(1, playlistID);
            Log.d(MediaPlayerConstants.LOG_TAG_SQL, deleteStmt.toString());
            deleteStmt.executeUpdateDelete();

            //Removing playlist from playlistInfoList
            MediaLibraryManager.removePlaylist(playlist.getPlaylistIndex());

            //Updating playlist_index for all playlists
            updatePlaylistIndices();

            //Setting success toast message
            toastText = MessageConstants.PLAYLIST_DELETED;
        } catch(Exception e) {
            Log.e(MediaPlayerConstants.LOG_TAG_EXCEPTION, e.getMessage());
            //Setting error toast message
            toastText = MessageConstants.ERROR;
        }

        //Displaying toast message to user
        Toast toast = Toast.makeText(context, toastText, Toast.LENGTH_SHORT);
        toast.show();
    }

    public void addTracks(List<Track> selectedTracks, Playlist selectedPlaylist) {
        SQLiteStatement insertStmt, updateStmt = null;
        String toastText;
        int trackID, trackDuration, trackIndex, playlistID, playlistIndex, playlistSize, playlistDuration;
        Track track;

        try {
            //Retrieving current values for selected playlist
            playlistID = selectedPlaylist.getPlaylistID();
            playlistIndex = selectedPlaylist.getPlaylistIndex();
            playlistSize = selectedPlaylist.getPlaylistSize();
            insertStmt = db.compileStatement(SQLConstants.SQL_INSERT_PLAYLIST_DETAIL);

            //Checking if selected playlist is default playlist 'Favourites'
            if(playlistID == SQLConstants.PLAYLIST_ID_FAVOURITES) {
                updateStmt = db.compileStatement(SQLConstants.SQL_UPDATE_TRACK_FAV_SW);
            }

            Iterator<Track> trackIterator = selectedTracks.iterator();
            while(trackIterator.hasNext()) {
                track = trackIterator.next();
                trackID = track.getTrackID();
                trackIndex = track.getTrackIndex();

                //Inserting in table 'Playlist_Detail'
                insertStmt.clearBindings();
                insertStmt.bindLong(1, playlistID);
                insertStmt.bindLong(2, trackID);
                Log.d(MediaPlayerConstants.LOG_TAG_SQL, insertStmt.toString());
                insertStmt.executeInsert();

                //If selected playlist is 'Favourites', updating 'fav_sw' in table 'Tracks'
                if(playlistID == SQLConstants.PLAYLIST_ID_FAVOURITES) {
                    updateStmt.clearBindings();
                    updateStmt.bindLong(1, SQLConstants.FAV_SW_YES);
                    updateStmt.bindLong(2, trackID);
                    Log.d(MediaPlayerConstants.LOG_TAG_SQL, updateStmt.toString());
                    updateStmt.executeUpdateDelete();

                    //Updating trackInfoList with new value if favSw
                    track.setFavSw(SQLConstants.FAV_SW_YES);
                    MediaLibraryManager.updateTrackInfoList(trackIndex, track);
                }

                //Calculating new values for selected playlist
                playlistSize++;
            }

            //Updating selected playlist with new values of playlist_size and playlist_duration
            updateStmt = db.compileStatement(SQLConstants.SQL_UPDATE_PLAYLIST);
            updateStmt.bindLong(1, playlistSize);
            updateStmt.bindString(2, Utilities.getCurrentDate());
            updateStmt.bindLong(3, playlistID);
            Log.d(MediaPlayerConstants.LOG_TAG_SQL, updateStmt.toString());
            updateStmt.executeUpdateDelete();

            //Updating playlistInfoList with new values of playlistSize and playlistDuration
            selectedPlaylist.setPlaylistSize(playlistSize);
            MediaLibraryManager.updatePlaylistInfoList(playlistIndex, selectedPlaylist);

            //Set success toast message
            toastText = MessageConstants.ADDED_TRACKS;
        } catch(SQLiteConstraintException sqle) {
            Log.e(MediaPlayerConstants.LOG_TAG_SQL, sqle.getMessage());
            toastText = MessageConstants.ERROR_DUPLICATE_TRACK_FAVOURITES;
        } catch(Exception e) {
            Log.e(MediaPlayerConstants.LOG_TAG_SQL, e.getMessage());
            //Setting error toast message
            toastText = MessageConstants.ERROR;
        }

        //Displaying toast message to user
        Toast toast = Toast.makeText(context, toastText, Toast.LENGTH_SHORT);
        toast.show();
    }

    public ArrayList<Track> getTracksForPlaylist(int playlistID) {
        ArrayList<Track> trackList = new ArrayList<Track>();
        String args[] = {String.valueOf(playlistID)};
        Cursor playlistsCursor = null;

        try {
            Log.d(MediaPlayerConstants.LOG_TAG_SQL, SQLConstants.SQL_SELECT_ALL_TRACKS_FOR_PLAYLIST);
            playlistsCursor = db.rawQuery(SQLConstants.SQL_SELECT_ALL_TRACKS_FOR_PLAYLIST, args);
            playlistsCursor.moveToFirst();
            int c;

            while(!playlistsCursor.isAfterLast()) {
                Track track = new Track();
                c = SQLConstants.ZERO;

                track.setTrackID(playlistsCursor.getInt(c++));
                track.setSong(playlistsCursor.getString(c++));
                track.setTrackIndex(playlistsCursor.getInt(c++));
                track.setUrl(playlistsCursor.getString(c++));
                track.setArtists(playlistsCursor.getString(c++));
                track.setAlbumArt(playlistsCursor.getBlob(c++));
                track.setFavSw(playlistsCursor.getInt(c));

                trackList.add(track);
                playlistsCursor.moveToNext();
            }
        } catch(Exception e) {
            Log.e(MediaPlayerConstants.LOG_TAG_EXCEPTION, e.getMessage());
        } finally {
            if(playlistsCursor != null) {
                playlistsCursor.close();
            }
        }

        return trackList;
    }

    public ArrayList<Integer> getTrackIDsForPlaylist(int playlistID) {
        ArrayList<Integer> trackList = null;
        Cursor cursor = null;
        String args[] = {String.valueOf(playlistID)};

        try {
            Log.d(MediaPlayerConstants.LOG_TAG_SQL, SQLConstants.SQL_SELECT_PLAYLISTS_FOR_TRACK);
            cursor = db.rawQuery(SQLConstants.SQL_SELECT_TRACK_IDS_FOR_PLAYLIST, args);

            if(cursor != null && cursor.getCount() > 0) {
                trackList = new ArrayList<Integer>();
                cursor.moveToFirst();

                while (!cursor.isAfterLast()) {
                    trackList.add(cursor.getInt(SQLConstants.ZERO));
                    cursor.moveToNext();
                }
            }
        } catch(Exception e) {
            Log.e(MediaPlayerConstants.LOG_TAG_EXCEPTION, e.getMessage());
        } finally {
            if(cursor != null) {
                cursor.close();
            }
        }

        return trackList;
    }

    /**
     * Method to get the list of playlists from database
     * @return Sorted list of Playlists
     */
    public ArrayList<Playlist> getPlaylists() {
        ArrayList<Playlist> playlistInfoList = new ArrayList<Playlist>();

        Log.d(MediaPlayerConstants.LOG_TAG_SQL, SQLConstants.SQL_SELECT_PLAYLISTS);
        Cursor playlistsCursor = db.rawQuery(SQLConstants.SQL_SELECT_PLAYLISTS, null);
        playlistsCursor.moveToFirst();

        while(!playlistsCursor.isAfterLast()) {
            Playlist playlist = new Playlist();
            playlist.setPlaylistID(playlistsCursor.getInt(0));
            playlist.setPlaylistIndex(playlistsCursor.getInt(1));
            playlist.setPlaylistName(playlistsCursor.getString(2));
            playlist.setPlaylistSize(playlistsCursor.getInt(3));

            playlistInfoList.add(playlist);
            playlistsCursor.moveToNext();
        }

        playlistsCursor.close();
        return playlistInfoList;
    }

    //Instantiates a new track list and populates it from table 'Tracks'
    public ArrayList<Track> getTracks() {
        ArrayList<Track> trackInfoList = null;
        Track track;
        int c;

        Log.d(MediaPlayerConstants.LOG_TAG_SQL, SQLConstants.SQL_SELECT_TRACKS);
        Cursor tracksCursor = db.rawQuery(SQLConstants.SQL_SELECT_TRACKS, null);

        if(tracksCursor.getCount() > 0) {
            tracksCursor.moveToFirst();
            trackInfoList = new ArrayList<Track>();

            while(!tracksCursor.isAfterLast()) {
                track = new Track();
                c = SQLConstants.ZERO;

                track.setTrackID(tracksCursor.getInt(c++));
                track.setSong(tracksCursor.getString(c++));
                track.setTrackIndex(tracksCursor.getInt(c++));
                track.setUrl(tracksCursor.getString(c++));
                track.setArtists(tracksCursor.getString(c++));
                track.setAlbumArt(tracksCursor.getBlob(c++));
                track.setFavSw(tracksCursor.getInt(c));

                trackInfoList.add(track);
                tracksCursor.moveToNext();
            }
        }

        tracksCursor.close();
        return trackInfoList;
    }

    public ArrayList<Integer> getPlaylistsForTrack(int trackID) {
        ArrayList<Integer> playlist = null;
        Cursor cursor = null;

        try {
            String args[] = {String.valueOf(trackID)};

            //Fetching existing values for selected playlist
            Log.d(MediaPlayerConstants.LOG_TAG_SQL, SQLConstants.SQL_SELECT_PLAYLISTS_FOR_TRACK);
            cursor = db.rawQuery(SQLConstants.SQL_SELECT_PLAYLISTS_FOR_TRACK, args);

            if(cursor != null && cursor.getCount() > 0) {
                playlist = new ArrayList<Integer>();
                cursor.moveToFirst();

                while(!cursor.isAfterLast()) {
                    playlist.add(cursor.getInt(SQLConstants.ZERO));
                    cursor.moveToNext();
                }
            }
        } catch(Exception e) {
            Log.e(MediaPlayerConstants.LOG_TAG_EXCEPTION, e.getMessage());
        } finally {
            if(cursor != null) {
                cursor.close();
            }
        }

        return playlist;
    }
}