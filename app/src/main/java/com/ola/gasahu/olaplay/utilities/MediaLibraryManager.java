package com.ola.gasahu.olaplay.utilities;

import android.content.Context;
import android.util.Log;

import com.ola.gasahu.olaplay.beans.Playlist;
import com.ola.gasahu.olaplay.beans.Track;
import com.ola.gasahu.olaplay.dao.MediaPlayerDAO;
import com.ola.gasahu.olaplay.services.OlaPlayApiClient;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static com.ola.gasahu.olaplay.utilities.MediaPlayerConstants.LOG_TAG_EXCEPTION;

public class MediaLibraryManager {
    private static List<Track> trackInfoList;
    private static List<Track> selectedPlaylist;
    private static List<Playlist> playlistInfoList;
    private static int tracklistSize;

    public static void init(Context context) {
        MediaPlayerDAO dao = null;

        try {
            dao = new MediaPlayerDAO(context);

            //Getting list of all tracks from db
            trackInfoList = dao.getTracks();

            if(trackInfoList != null) {
                sortTracklist(MediaPlayerConstants.TAG_PLAYLIST_LIBRARY);
                tracklistSize = trackInfoList.size();
            }

            //Getting list of all playlist from db and sorting them
            playlistInfoList = dao.getPlaylists();
            sortPlaylists();
        } catch(Exception e) {
            Log.e(LOG_TAG_EXCEPTION, e.getMessage());
        } finally {
            if(dao != null) {
                dao.closeConnection();
            }
        }
    }

    /**
     * Method to create track list from tracks fetched from Web Service
     * @param context Reference to calling context
     * @return Track list
     */
    public static List<Track> populateTrackInfoList(Context context) {
        try {
            //Call Web Service to fetch track list and store it in 'trackInfoList'
            trackInfoList = OlaPlayApiClient.fetchTrackList();
        } catch(IOException e) {
            Log.e(MediaPlayerConstants.LOG_TAG_EXCEPTION, e.getMessage());
        }

        if(trackInfoList != null) {
            tracklistSize = trackInfoList.size();
            sortTracklist(MediaPlayerConstants.TAG_PLAYLIST_LIBRARY);
        } else {
            tracklistSize = 0;
        }

        return trackInfoList;
    }

    /**
     * Method to sort list of tracks in Media library
     */
    public static void sortTracklist(String playlistType) {
        Iterator<Track> tracklistIterator;
        Track track;
        int i = 0;

        switch(playlistType) {
            case MediaPlayerConstants.TAG_PLAYLIST_LIBRARY:
                Collections.sort(trackInfoList, new Track());
                tracklistIterator = trackInfoList.iterator();

                while(tracklistIterator.hasNext()) {
                    track = tracklistIterator.next();
                    track.setTrackIndex(i);
                    i++;
                }

                break;

            case MediaPlayerConstants.TAG_PLAYLIST_OTHER:
                Collections.sort(selectedPlaylist, new Track());
                tracklistIterator = selectedPlaylist.iterator();

                while(tracklistIterator.hasNext()) {
                    track = tracklistIterator.next();
                    track.setCurrentTrackIndex(i);
                    i++;
                }

                break;
        }
    }

    /**
     * Method to sort list of playlists in Media library
     */
    public static void sortPlaylists() {
        //Removing default playlist 'Favourites' from playlistInfoList to prevent it's index from changing
        Playlist fav =  playlistInfoList.remove(SQLConstants.PLAYLIST_INDEX_FAVOURITES);

        Collections.sort(playlistInfoList, new Playlist());
        playlistInfoList.add(SQLConstants.PLAYLIST_INDEX_FAVOURITES, fav);
        Iterator<Playlist> playlistIterator = playlistInfoList.iterator();
        Playlist playlist;
        int i = 0;

        while(playlistIterator.hasNext()) {
            playlist = playlistIterator.next();
            playlist.setPlaylistIndex(i);
            i++;
        }
    }

    public static List<Track> getTrackInfoList() {
        return trackInfoList;
    }

    public static List<Playlist> getPlaylistInfoList() {
        return playlistInfoList;
    }

    public static Playlist getPlaylistByIndex(int index) {
        return playlistInfoList.get(index);
    }

    public static Playlist getPlaylistByTitle(String title) {
        Playlist playlist;
        Iterator<Playlist> playlistIterator = playlistInfoList.iterator();

        while(playlistIterator.hasNext()) {
            playlist = playlistIterator.next();

            if(title.equals(playlist.getPlaylistName())) {
                return playlist;
            }
        }

        return null;
    }

    public static int getTrackInfoListSize() {
        if(trackInfoList != null) {
            return trackInfoList.size();
        } else {
            return SQLConstants.ZERO;
        }
    }

    public static int getPlaylistInfoListSize() {
        if(playlistInfoList != null) {
            return playlistInfoList.size();
        } else {
            return SQLConstants.ZERO;
        }
    }

    public static Track getTrackByIndex(String playlistType, int index) {
        switch (playlistType) {
            case MediaPlayerConstants.TAG_PLAYLIST_LIBRARY:
                return trackInfoList.get(index);

            case MediaPlayerConstants.TAG_PLAYLIST_OTHER:
                return selectedPlaylist.get(index);

            default:
                return null;
        }
    }

    public static Track getFirstTrack(String playlistType) {
        switch(playlistType) {
            case MediaPlayerConstants.TAG_PLAYLIST_LIBRARY:
                return trackInfoList.get(0);

            case MediaPlayerConstants.TAG_PLAYLIST_OTHER:
                return selectedPlaylist.get(0);

            default:
                return null;
        }
    }

    public static Track getLastTrack(String playlistType) {
        switch(playlistType) {
            case MediaPlayerConstants.TAG_PLAYLIST_LIBRARY:
                return trackInfoList.get(tracklistSize - 1);

            case MediaPlayerConstants.TAG_PLAYLIST_OTHER:
                return selectedPlaylist.get(selectedPlaylist.size() - 1);

            default:
                return null;
        }
    }

    public static boolean isFirstTrack(int index) {
        return (index == 0);
    }

    public static boolean isLastTrack(String playlistType, int index) {
        switch(playlistType) {
            case MediaPlayerConstants.TAG_PLAYLIST_LIBRARY:
                return (index == (tracklistSize - 1));

            case MediaPlayerConstants.TAG_PLAYLIST_OTHER:
                return (index == (selectedPlaylist.size() - 1));

            default:
                return false;
        }
    }

    public static void removePlaylist(int index) {
        playlistInfoList.remove(index);
    }

    public static void removeTrack(String playlistType, int index) {
        switch(playlistType) {
            case MediaPlayerConstants.TAG_PLAYLIST_LIBRARY:
                trackInfoList.remove(index);
                break;

            case MediaPlayerConstants.TAG_PLAYLIST_OTHER:
                selectedPlaylist.remove(index);
                break;
        }
    }

    public static void addPlaylist(Playlist playlist) {
        playlistInfoList.add(playlist);
    }

    public static void updateTrackInfoList(int index, Track track) {
        trackInfoList.set(index, track);
    }

    public static void updatePlaylistInfoList(int index, Playlist playlist) {
        playlistInfoList.set(index, playlist);
    }

    public static List<Track> getSelectedPlaylist() {
        return selectedPlaylist;
    }

    public static void setSelectedPlaylist(List<Track> selectedPlaylist) {
        MediaLibraryManager.selectedPlaylist = selectedPlaylist;
    }

    public static boolean isUserPlaylistEmpty() {
        return (selectedPlaylist == null || selectedPlaylist.isEmpty());
    }
}