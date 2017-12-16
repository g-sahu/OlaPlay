package com.ola.gasahu.olaplay.dao;

import android.provider.BaseColumns;

public final class MediaPlayerContract {
    static final String DATABASE_NAME = "OlaPlay";
    static final int DATABASE_VERSION = 1;

    public static abstract class Tracks implements BaseColumns {
        public static final String TABLE_NAME = "tracks";
        public static final String TRACK_ID = "track_id";
        public static final String TRACK_TITLE = "track_title";
        public static final String TRACK_INDEX = "track_index";
        public static final String URL = "url";
        public static final String ARTIST_NAME = "artist_name";
        public static final String ALBUM_ART = "album_art";
        public static final String FAV_SW = "fav_sw";
        public static final String CREATE_DT = "create_dt";
        public static final String UPDATE_DT = "update_dt";
    }

    public static abstract class Playlists implements BaseColumns {
        public static final String TABLE_NAME = "playlists";
        public static final String PLAYLIST_ID = "playlist_id";
        public static final String PLAYLIST_INDEX = "playlist_index";
        public static final String PLAYLIST_TITLE = "playlist_title";
        public static final String PLAYLIST_SIZE = "playlist_size";
        public static final String CREATE_DT = "create_dt";
        public static final String UPDATE_DT = "update_dt";
    }

    public static abstract class PlaylistDetail implements BaseColumns {
        public static final String TABLE_NAME = "playlist_detail";
        public static final String PLAYLIST_ID = "playlist_id";
        public static final String TRACK_ID = "track_id";
    }
}
