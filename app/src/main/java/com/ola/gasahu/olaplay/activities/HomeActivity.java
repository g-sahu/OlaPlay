package com.ola.gasahu.olaplay.activities;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;

import com.ola.gasahu.olaplay.R;
import com.ola.gasahu.olaplay.adapters.HomePagerAdapter;
import com.ola.gasahu.olaplay.adapters.PlaylistsAdapter;
import com.ola.gasahu.olaplay.adapters.SongsListAdapter;
import com.ola.gasahu.olaplay.beans.Playlist;
import com.ola.gasahu.olaplay.beans.Track;
import com.ola.gasahu.olaplay.dao.MediaPlayerDAO;
import com.ola.gasahu.olaplay.fragments.AboutUsDialogFragment;
import com.ola.gasahu.olaplay.fragments.CreatePlaylistDialogFragment;
import com.ola.gasahu.olaplay.fragments.PlaylistsFragment;
import com.ola.gasahu.olaplay.fragments.SelectPlaylistDialogFragment;
import com.ola.gasahu.olaplay.fragments.SelectTrackDialogFragment;
import com.ola.gasahu.olaplay.fragments.SongsFragment;
import com.ola.gasahu.olaplay.services.MediaPlayerService;
import com.ola.gasahu.olaplay.utilities.MediaLibraryManager;
import com.ola.gasahu.olaplay.utilities.MediaPlayerConstants;
import com.ola.gasahu.olaplay.utilities.SQLConstants;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.ola.gasahu.olaplay.utilities.MediaPlayerConstants.LOG_TAG_EXCEPTION;

public class HomeActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    private final static String LOG_TAG = "HomeActivity";
    private static Track selectedTrack;
    private static Playlist selectedPlaylist, favouritesPlaylist;
    private FragmentManager supportFragmentManager;
    private static Context context;
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Log.d(LOG_TAG, "HomeActivity created");

        context = this;
        supportFragmentManager = getSupportFragmentManager();
        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        FragmentPagerAdapter adapterViewPager = new HomePagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapterViewPager);

        //Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);
        favouritesPlaylist = MediaLibraryManager.getPlaylistByIndex(SQLConstants.PLAYLIST_INDEX_FAVOURITES);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) findViewById(R.id.searchView);
        int id = searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        TextView textView = (TextView) searchView.findViewById(id);
        textView.setTextColor(Color.WHITE);
        searchView.setOnQueryTextListener(this);
        ComponentName componentName = getComponentName();
        SearchableInfo searchableInfo = searchManager.getSearchableInfo(componentName);
        searchView.setSearchableInfo(searchableInfo);
    }

    public static Playlist getSelectedPlaylist() {
        return selectedPlaylist;
    }

    public static Context getContext() {
        return context;
    }

    //Show Songs pop-up menu options
    public void showSongsPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        Menu menu = popup.getMenu();
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_song_options, menu);
        MenuItem menuItem = menu.findItem(R.id.addToFavourites);

        RecyclerView recyclerView = SongsFragment.trackListView;
        View parent = (View) view.getParent();
        position = recyclerView.getChildLayoutPosition(parent);
        selectedTrack = MediaLibraryManager.getTrackByIndex(MediaPlayerConstants.TAG_PLAYLIST_LIBRARY, position);

        //Checking if song is added to default playlist 'Favourites'
        if(selectedTrack != null && selectedTrack.getFavSw() == SQLConstants.FAV_SW_YES) {
            menuItem.setTitle(MediaPlayerConstants.TITLE_REMOVE_FROM_FAVOURITES);
        }

        popup.show();
    }

    //Show dialog to select playlists
    public void addToPlaylist(MenuItem menuItem) {
        DialogFragment selectPlaylistDialogFragment = new SelectPlaylistDialogFragment();
        Bundle args = new Bundle();

        args.putSerializable(MediaPlayerConstants.KEY_SELECTED_TRACK, selectedTrack);
        selectPlaylistDialogFragment.setArguments(args);
        selectPlaylistDialogFragment.show(supportFragmentManager, MediaPlayerConstants.TAG_ADD_TO_PLAYLIST);
    }

    //Add or remove from favourites menu option
    public void addRemoveFavourites(MenuItem menuItem) {
        if (menuItem.getTitle().equals(MediaPlayerConstants.TITLE_ADD_TO_FAVOURITES)) {
            addToFavourites();
        } else {
            removeFromFavourites();
        }
    }

    //Add to favourites menu option
    private void addToFavourites() {
        MediaPlayerDAO dao = null;
        ArrayList<Playlist> selectedPlaylists = new ArrayList<>();

        try {
            selectedPlaylists.add(favouritesPlaylist);
            dao = new MediaPlayerDAO(this);
            dao.addToPlaylists(selectedPlaylists, selectedTrack);

            //Updating list view adapter
            updatePlaylistsAdapter();
        } catch(Exception e) {
            Log.e(LOG_TAG_EXCEPTION, e.getMessage());
        } finally {
            if(dao != null) {
                dao.closeConnection();
            }
        }
    }

    //Remove from favourites menu option
    private void removeFromFavourites() {
        MediaPlayerDAO dao = null;

        try {
            dao = new MediaPlayerDAO(this);
            dao.removeFromPlaylist(favouritesPlaylist, selectedTrack);

            //Updating list view adapter
            updatePlaylistsAdapter();
        } catch(Exception e) {
            Log.e(LOG_TAG_EXCEPTION, e.getMessage());
        } finally {
            if(dao != null) {
                dao.closeConnection();
            }
        }
    }

    //Show playlists pop-up menu options
    public void showPlaylistsPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        Menu menu = popup.getMenu();
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_playlist_options, menu);
        MenuItem renamePlaylist = menu.findItem(R.id.renamePlaylist);
        MenuItem deletePlaylist = menu.findItem(R.id.deletePlaylist);

        RecyclerView listView = PlaylistsFragment.recyclerView;
        View parent = (View) view.getParent();
        position = listView.getChildLayoutPosition(parent);
        selectedPlaylist = MediaLibraryManager.getPlaylistByIndex(position);

        if(selectedPlaylist.getPlaylistID() == SQLConstants.PLAYLIST_ID_FAVOURITES) {
            renamePlaylist.setEnabled(false);
            deletePlaylist.setEnabled(false);
        }

        popup.show();
    }

    public void addTracksToPlaylist(MenuItem menuItem) {
        DialogFragment selectTrackDialogFragment = new SelectTrackDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(MediaPlayerConstants.KEY_SELECTED_PLAYLIST, selectedPlaylist);
        selectTrackDialogFragment.setArguments(args);
        selectTrackDialogFragment.show(supportFragmentManager, MediaPlayerConstants.TAG_ADD_TRACKS);
    }

    //Rename playlist menu option
    public void renamePlaylist(MenuItem menuItem) {
        DialogFragment newFragment = new CreatePlaylistDialogFragment();
        Bundle args = new Bundle();
        args.putString(MediaPlayerConstants.KEY_PLAYLIST_TITLE, selectedPlaylist.getPlaylistName());
        args.putInt(MediaPlayerConstants.KEY_PLAYLIST_INDEX, selectedPlaylist.getPlaylistIndex());
        newFragment.setArguments(args);
        newFragment.show(getSupportFragmentManager(), MediaPlayerConstants.TAG_RENAME_PLAYLIST);

        //Updating list view adapter
        updatePlaylistsAdapter();
    }

    //Delete playlist menu option
    public void deletePlaylist(MenuItem menuItem) {
        MediaPlayerDAO dao = null;

        try {
            dao = new MediaPlayerDAO(this);
            dao.deletePlaylist(selectedPlaylist);

            //Updating list view adapter
            updatePlaylistsAdapter();
        } catch(Exception e) {
            Log.e(LOG_TAG_EXCEPTION, e.getMessage());
        } finally {
            if(dao != null) {
                dao.closeConnection();
            }
        }
    }

    public void callMediaplayerActivity(View view) {
        RecyclerView recyclerView = SongsFragment.trackListView;
        position = recyclerView.getChildLayoutPosition(view);
        selectedTrack = MediaLibraryManager.getTrackByIndex(MediaPlayerConstants.TAG_PLAYLIST_LIBRARY,  position);

        Intent intent = new Intent(this, MediaPlayerActivity.class);
        intent.setAction(MediaPlayerConstants.PLAY);
        intent.putExtra(MediaPlayerConstants.KEY_SELECTED_TRACK, selectedTrack);
        intent.putExtra(MediaPlayerConstants.KEY_SELECTED_PLAYLIST, MediaPlayerConstants.TAG_PLAYLIST_LIBRARY);
        intent.putExtra(MediaPlayerConstants.KEY_PLAYLIST_TITLE, MediaPlayerConstants.TITLE_LIBRARY);
        intent.putExtra(MediaPlayerConstants.KEY_TRACK_ORIGIN, MediaPlayerConstants.TAG_SONGS_LIST_VIEW);
        startActivity(intent);
    }

    public void callPlaylistActivity(View view) {
        RecyclerView listView = PlaylistsFragment.recyclerView;
        position = listView.getChildLayoutPosition(view);
        selectedPlaylist = MediaLibraryManager.getPlaylistByIndex(position);
        int playlistID = selectedPlaylist.getPlaylistID();

        Intent intent = new Intent(this, PlaylistActivity.class);
        intent.putExtra(MediaPlayerConstants.KEY_PLAYLIST_ID, playlistID);
        intent.putExtra(MediaPlayerConstants.KEY_PLAYLIST_INDEX, position);
        startActivity(intent);
    }

    private void updatePlaylistsAdapter() {
        PlaylistsAdapter adapter = new PlaylistsAdapter(this, MediaLibraryManager.getPlaylistInfoList());
        RecyclerView listView = PlaylistsFragment.recyclerView;
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public void showAppPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        Menu menu = popup.getMenu();
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_app_options, menu);
        popup.show();
    }

    public void aboutUs(MenuItem item) {
        DialogFragment aboutUsDialogFragment = new AboutUsDialogFragment();
        aboutUsDialogFragment.show(supportFragmentManager, MediaPlayerConstants.TAG_ABOUT_US);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Log.d("Search query", query);
        updateSongsList(query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        Log.d("Search query", newText);
        updateSongsList(newText);
        return true;
    }

    private void updateSongsList(String query) {
        List<Track> trackList = MediaLibraryManager.getTrackInfoList();
        List<Track> resultList = new ArrayList<>();
        Iterator<Track> trackIterator = trackList.iterator();
        Track track;
        String trackTitle;
        query = query.toLowerCase();

        while(trackIterator.hasNext()) {
            track = trackIterator.next();
            trackTitle = track.getSong();

            if(trackTitle.toLowerCase().contains(query)) {
                resultList.add(track);
            }
        }

        SongsListAdapter songsListAdapter = new SongsListAdapter(this, resultList);
        SongsFragment.trackListView.setAdapter(songsListAdapter);
        songsListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        Log.d(LOG_TAG, "Back button pressed");
        moveTaskToBack(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent(this, MediaPlayerService.class);
        stopService(intent);
        Log.d(LOG_TAG, "HomeActivity destroyed");
    }
}