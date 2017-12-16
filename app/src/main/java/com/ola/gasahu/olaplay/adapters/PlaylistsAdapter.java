package com.ola.gasahu.olaplay.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ola.gasahu.olaplay.R;
import com.ola.gasahu.olaplay.beans.Playlist;
import com.ola.gasahu.olaplay.utilities.SQLConstants;

import java.util.List;

public class PlaylistsAdapter extends RecyclerView.Adapter<PlaylistsAdapter.Holder> {
    private List<Playlist> playlistInfoList;
    private static LayoutInflater inflater = null;

    public PlaylistsAdapter(Context context, List<Playlist> playlistInfoList) {
        this.playlistInfoList = playlistInfoList;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public PlaylistsAdapter.Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowView = inflater.inflate(R.layout.item_playlist, parent, false);
        return new PlaylistsAdapter.Holder(rowView);
    }

    @Override
    public void onBindViewHolder(PlaylistsAdapter.Holder holder, int position) {
        Playlist playlist = playlistInfoList.get(position);
        int playlistSize = playlist.getPlaylistSize();
        String text = (playlistSize == SQLConstants.ONE) ? " song " : " songs ";
        String infoText = playlistSize + text;
        holder.playlistTitle.setText(playlistInfoList.get(position).getPlaylistName());
        holder.playlistInfo.setText(infoText);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return playlistInfoList.size();
    }

    class Holder extends RecyclerView.ViewHolder {
        TextView playlistTitle, playlistInfo;

        Holder(View itemView) {
            super(itemView);
            playlistTitle = (TextView) itemView.findViewById(R.id.playlistTitle);
            playlistInfo = (TextView) itemView.findViewById(R.id.playlistInfo);
        }
    }
}
