package com.ola.gasahu.olaplay.beans;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Comparator;

public class Track implements Serializable, Comparator<Object> {
    private int trackID;
    @SerializedName("song")
    private String song;
    private int trackIndex;
    private int currentTrackIndex;
    @SerializedName("url")
    private String url;
    @SerializedName("artists")
    private String artists;
    @SerializedName("cover_image")
    private String cover_image;
    private byte[] albumArt;
    private int favSw;

    public String getCover_image() {
        return cover_image;
    }

    public void setCover_image(String cover_image) {
        this.cover_image = cover_image;
    }

    public int getTrackID() {
        return trackID;
    }

    public void setTrackID(int trackID) {
        this.trackID = trackID;
    }

    public String getSong() {
        return song;
    }

    public void setSong(String song) {
        this.song = song;
    }

    public int getTrackIndex() {
        return trackIndex;
    }

    public void setTrackIndex(int trackIndex) {
        this.trackIndex = trackIndex;
    }

    public int getCurrentTrackIndex() {
        return currentTrackIndex;
    }

    public void setCurrentTrackIndex(int currentTrackIndex) {
        this.currentTrackIndex = currentTrackIndex;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getArtists() {
        return artists;
    }

    public void setArtists(String artists) {
        this.artists = artists;
    }

    public byte[] getAlbumArt() {
        return albumArt;
    }

    public void setAlbumArt(byte[] albumArt) {
        this.albumArt = albumArt;
    }

    public int getFavSw() {
        return favSw;
    }

    public void setFavSw(int favSw) {
        this.favSw = favSw;
    }

    @Override
    public int compare(Object lhs, Object rhs) {
        Track track1 = (Track) lhs;
        Track track2 = (Track) rhs;

        return track1.getSong().compareToIgnoreCase(track2.getSong());
    }
}
