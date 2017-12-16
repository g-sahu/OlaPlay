package com.ola.gasahu.olaplay.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ola.gasahu.olaplay.R;
import com.ola.gasahu.olaplay.adapters.SongsListAdapter;
import com.ola.gasahu.olaplay.beans.Track;
import com.ola.gasahu.olaplay.utilities.MediaLibraryManager;

import java.util.List;

import static com.ola.gasahu.olaplay.utilities.MediaPlayerConstants.LOG_TAG_EXCEPTION;

public class SongsFragment extends Fragment {
    private Context context;
    public static RecyclerView trackListView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = null;
        TextView emptyLibraryMessage;
        List<Track> trackInfoList;
        RecyclerView.Adapter songsListAdapter;

        try {
            view = inflater.inflate(R.layout.fragment_songs, container, false);
            emptyLibraryMessage = (TextView) view.findViewById(R.id.emptyLibraryMessage);
            trackListView = (RecyclerView) view.findViewById(R.id.recycler_view);
            trackInfoList = MediaLibraryManager.getTrackInfoList();

            if(trackInfoList == null || trackInfoList.isEmpty()) {
                emptyLibraryMessage.setVisibility(View.VISIBLE);
                trackListView.setVisibility(View.GONE);
            } else {
                songsListAdapter = new SongsListAdapter(context, trackInfoList);
                trackListView.setAdapter(songsListAdapter);
                trackListView.setLayoutManager(new LinearLayoutManager(context));
            }
        } catch(Exception e) {
            Log.e(LOG_TAG_EXCEPTION, e.getMessage());
        }

        return view;
    }
}