package com.ola.gasahu.olaplay.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.ola.gasahu.olaplay.activities.HomeActivity;
import com.ola.gasahu.olaplay.adapters.PlaylistsAdapter;
import com.ola.gasahu.olaplay.beans.Playlist;
import com.ola.gasahu.olaplay.beans.Track;
import com.ola.gasahu.olaplay.dao.MediaPlayerDAO;
import com.ola.gasahu.olaplay.utilities.MediaLibraryManager;
import com.ola.gasahu.olaplay.utilities.MediaPlayerConstants;
import com.ola.gasahu.olaplay.utilities.MessageConstants;
import com.ola.gasahu.olaplay.utilities.SQLConstants;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SelectTrackDialogFragment extends DialogFragment {
    private Context context;
    private List<Track> selectedTracks;
    private List<Track> tracksInLibrary = MediaLibraryManager.getTrackInfoList();

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ArrayList<Integer> tracksInPlaylist;
        final ArrayList<Track> tracksToDisplay;
        Iterator<Track> tracksIterator;
        String list[];
        Track track;
        int trackID;
        AlertDialog.Builder builder = null;
        MediaPlayerDAO dao = null;
        int trackInPlaylistSize, c = 0, listLength;

        try {
            context = getContext();
            builder = new AlertDialog.Builder(getActivity());
            Bundle args = getArguments();
            Playlist selectedPlaylist = (Playlist) args.getSerializable(MediaPlayerConstants.KEY_SELECTED_PLAYLIST);

            //Checking if there are any tracks in the library
            if(tracksInLibrary != null && !tracksInLibrary.isEmpty()) {
                dao = new MediaPlayerDAO(context);
                tracksInPlaylist = dao.getTrackIDsForPlaylist(selectedPlaylist.getPlaylistID());

                if(tracksInPlaylist != null && !tracksInPlaylist.isEmpty()) {
                    trackInPlaylistSize = tracksInPlaylist.size();
                } else {
                    trackInPlaylistSize = 0;
                }

                //Creating list of tracks to display in multiselect dialog
                tracksToDisplay = new ArrayList<Track>();

                //Iterating tracks in library to remove tracks already added to playlist
                tracksIterator = tracksInLibrary.iterator();

                while(tracksIterator.hasNext()) {
                    track = tracksIterator.next();
                    trackID = track.getTrackID();

                    if(trackInPlaylistSize == SQLConstants.ZERO || !tracksInPlaylist.contains(trackID)) {
                        tracksToDisplay.add(track);
                    }
                }

                selectedTracks = new ArrayList<Track>();
                list = new String[tracksToDisplay.size()];
                listLength = list.length;

                //Setting the title of the dialog window
                builder.setTitle(MediaPlayerConstants.TITLE_SELECT_TRACKS);

                if(listLength != 0) {
                    tracksIterator = tracksToDisplay.iterator();

                    //Adding tracks to multichoice items list in dialog
                    while(tracksIterator.hasNext()) {
                        track = tracksIterator.next();
                        list[c++] = track.getSong();
                    }

                    builder.setMultiChoiceItems(list, null, new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            Track track = tracksToDisplay.get(which);

                            if(isChecked) {
                                selectedTracks.add(track);
                            } else if (selectedTracks.contains(track)) {
                                selectedTracks.remove(track);
                            }
                        }
                    });

                    builder.setPositiveButton(MediaPlayerConstants.OK, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            MediaPlayerDAO dao = null;

                            if(!selectedTracks.isEmpty()) {
                                try {
                                    dao = new MediaPlayerDAO(getContext());

                                    //Add track to selected playlists
                                    dao.addTracks(selectedTracks, HomeActivity.getSelectedPlaylist());
                                } catch(Exception e) {
                                    Log.e(MediaPlayerConstants.LOG_TAG_EXCEPTION, e.getMessage());
                                } finally {
                                    if(dao != null) {
                                        dao.closeConnection();
                                    }
                                }

                                //Updating list view adapter
                                updatePlaylistsAdapter();

                                //Removing added tracks from tracksInLibrary
                                tracksToDisplay.removeAll(selectedTracks);
                            }
                        }
                    });

                    builder.setNegativeButton(MediaPlayerConstants.CANCEL, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            //Do nothing
                        }
                    });
                } else {
                    builder.setMessage(MessageConstants.ERROR_NO_TRACK);
                    builder.setPositiveButton(MediaPlayerConstants.OK, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            //Do nothing
                        }
                    });
                }
            } else {
                builder.setTitle(MediaPlayerConstants.TITLE_ERROR);
                builder.setMessage(MessageConstants.ERROR_NO_TRACKS_ADDED);
                builder.setPositiveButton(MediaPlayerConstants.OK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //Do nothing
                    }
                });
            }
        } catch(Exception e) {
            Log.e(MediaPlayerConstants.LOG_TAG_EXCEPTION, e.getMessage());
        } finally {
            if(dao != null) {
                dao.closeConnection();
            }
        }

        return builder.create();
    }

    private void updatePlaylistsAdapter() {
        PlaylistsAdapter adapter = new PlaylistsAdapter(context, MediaLibraryManager.getPlaylistInfoList());
        RecyclerView listView = PlaylistsFragment.recyclerView;
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
}