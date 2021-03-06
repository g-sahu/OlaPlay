package com.ola.gasahu.olaplay.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ola.gasahu.olaplay.R;
import com.ola.gasahu.olaplay.beans.Track;

import java.util.List;

public class SongsListAdapter extends RecyclerView.Adapter<SongsListAdapter.Holder> {
    private List<Track> trackInfoList;
    private static LayoutInflater inflater = null;
    private Context context;

    public SongsListAdapter(Context context, List<Track> trackInfoList) {
        this.trackInfoList = trackInfoList;
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public SongsListAdapter.Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowView = inflater.inflate(R.layout.item_track, parent, false);
        return new SongsListAdapter.Holder(rowView);
    }

    @Override
    public void onBindViewHolder(SongsListAdapter.Holder holder, int position) {
        Track track = trackInfoList.get(position);
        byte data[] = track.getAlbumArt();

        if(data != null && data.length != 0) {
            Glide.with(context).load(data).into(holder.albumArt);
        } else {
            Glide.with(context).load(R.drawable.img_default_album_art_thumb).into(holder.albumArt);
        }

        holder.trackTitle.setText(track.getSong());
        holder.artistName.setText(track.getArtists());

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return trackInfoList.size();
    }

    class Holder extends RecyclerView.ViewHolder {
        ImageView albumArt;
        TextView trackTitle, artistName;

        Holder(View itemView) {
            super(itemView);
            albumArt = (ImageView) itemView.findViewById(R.id.albumThumbnail);
            trackTitle = (TextView) itemView.findViewById(R.id.song);
            artistName = (TextView) itemView.findViewById(R.id.artists);
        }
    }
}