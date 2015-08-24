package com.fenix.audioplayer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.fenix.audioplayer.adapter.RecyclerCursorAdapter;
import com.fenix.audioplayer.data.SongData;

import java.util.LinkedList;

import static com.fenix.audioplayer.data.HelperClass.*;

public class MainActivity extends Activity implements View.OnClickListener,
        SearchView.OnQueryTextListener, MyService.OnServiceListener {

    private final static String TEST = "MainActivity";
    public final static int REQUEST_FOLDER = 101;
    //public final static int SONG_CHANGE = 102;
    public final static int START_FROM_NOTIFICATION = 103;
    //public final static int STOP_SERVICE = 103;
    public final static String FOLDER_NAME = "folder_name";
    //public final static String SEND_PATH = "send_path";
    //public final static String SEND_QUERY = "send_query";
    public final static String PATH = "path";
    //private final String SAVE_BUNDLE_SONG_STARTED = "bundle_song_started";
    private static final int TICK_WHAT = 2;

    private final String BUNDLE_PLAY = "bundle_play";
    private final String BUNDLE_PLAYER_CONTROL = "bundle_player_control";
    private final String BUNDLE_SONG_TIMER = "bundle_song_timer";
    private final String BUNDLE_SONG_DURATION = "bundle_song_duration";


    private boolean mPlay = false;
    private boolean mIsBound = false;
    private boolean mIsNotificationStart = false;
    private boolean mIsIntentStart = false;
    private static String sPath;
    private static String sQuery;
    private static Integer sPosition = 0;
    private static Uri sUri;
    //private String mData; //for storing playing song
    private Cursor mCursor;
    private MyService mBoundService;
    private ImageButton songButton;
    private ImageButton playButton;
    private ToggleButton loopButton;
    private TextView textProgress, textDuration, songName, authorName, albumName;
    private SeekBar seekBar;
    private SearchView mSearchView;
    private RecyclerCursorAdapter mAdapter;
    private LinearLayout mPlayerControl;
    private int mSongTimer;
    private int mSongDuration;
    private String mSortingOrder;

    private LinkedList<String> mSongList = new LinkedList<>();
    private String[] mProjection = new String[]{
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DISPLAY_NAME
    };
    //private String localPath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        /**     Start and bind to service    */
        doBindService();

        /**     Init view elements     */
        playButton = (ImageButton) findViewById(R.id.move_play);
        loopButton = (ToggleButton) findViewById(R.id.move_loop);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        textDuration = (TextView) findViewById(R.id.text_duration);
        textProgress = (TextView) findViewById(R.id.text_progress);
        songName = (TextView) findViewById(R.id.songName);
        authorName = (TextView) findViewById(R.id.authorName);
        albumName = (TextView) findViewById(R.id.albumName);
        mPlayerControl = (LinearLayout) findViewById(R.id.playerPult);

        Log.d(TEST, "myrestart1");
        /**     woks with data from intent      */
        final String dataString = Uri.decode(getIntent().getDataString());
        if (dataString != null) {                   //Start from intent-filter
            Log.d(TEST, "myrestart2");
            if (dataString.contains("content://media")) {
                sUri = getIntent().getData();
                mCursor = getContentResolver().query(sUri, mProjection, null, null, null);
                mCursor.moveToFirst();
                mSongList.add(mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
                mIsIntentStart = true;
            } else if (dataString.contains("file:")) {
                sPath = getFolder(dataString);
                sQuery = dataString.substring(dataString.lastIndexOf("/") + 1);
                searchMedia(getArgs(sPath), sQuery);
                mCursor.moveToFirst();
                mSongList.add(mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
                mIsIntentStart = true;
            } else Toast.makeText(this, "ПОМИЛКА - файл не знайдено", Toast.LENGTH_LONG).show();

        } else if (getIntent().hasExtra("extra")) {  //start from NOTIFICATION
            Log.d(TEST, "myrestart3");
            mIsNotificationStart = true;
            mPlay = true;
        } else {
            Log.d(TEST, "myrestart4");
            if (sUri == null) {
                searchMedia(getArgs(sPath), sQuery);
            } else {
                mCursor = getContentResolver().
                        query(sUri, mProjection, null, null, null);
            }
        }

        //Clear intent after using
        setIntent(new Intent());

        //init RecyclerView
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        mAdapter = new RecyclerCursorAdapter(this, mCursor);
        recyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new RecyclerCursorAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, SongData data) {
                if (songButton != null) {
                    songButton.setImageResource(R.drawable.music_action);
                }
                sPosition = data.getPosition();
                songButton = (ImageButton) v;
                songButton.setImageResource(R.drawable.play_action);
                startPlay(data);
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mSongTimer = seekBar.getProgress();
                setProgress();
                mBoundService.setProgress(mSongTimer);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) searchItem.getActionView();
        mSearchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.select_folder:
                intent = new Intent(this, FileManagerActivity.class);
                startActivityForResult(intent, REQUEST_FOLDER);
                break;
            case R.id.action_all_file:
                searchMedia(null, null);
                sQuery = null;
                break;
        }
        return false;
    }

    /**
     * Start play music
     */
    void startPlay(SongData data) {

        if (data != null) {
            mBoundService.startPlay(data.getPosition());
            mSongTimer = 1000;
        } else {
            mBoundService.startPlay(null);
        }
        mPlay = true;

        mSongTimer = mBoundService.getProgress();
        //init mPlayerControl control panel
        if (data != null) {
            mSongDuration = Integer.parseInt(data.getDuration());
            initPlayerControl(data);
        }
        mBoundService.setLoop(loopButton.isChecked());
    }

    private void initPlayerControl(SongData data) {

        mHandler.removeMessages(TICK_WHAT);
        mHandler.sendMessageDelayed(Message.obtain(mHandler, TICK_WHAT), 1000);
        Log.e(TEST, "it works");
        seekBar.setMax(Integer.parseInt(data.getDuration()));
        seekBar.setProgress(mSongTimer);
        textDuration.setText(timeFormatMillis(Integer.parseInt(data.getDuration())));
        textProgress.setText(timeFormatMillis(mSongTimer));
        playButton.setImageResource(R.drawable.pause_action);
        songName.setText(data.getSongName());
        authorName.setText("Author: " + data.getAutor());
        albumName.setText("Album: " + data.getAlbum());
        mAdapter.setmData(data.getData());
        //must be last string
        mPlayerControl.setVisibility(View.VISIBLE);
    }

    public void searchMedia(LinkedList<String> path, String quest) {

        sUri = null;
        //reading preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean asc = prefs.getBoolean(getString(R.string.pref_sort_order), true);
        String m = prefs.getString(getString(R.string.pref_list_sort), "1");

        //set mSortingOrder
        if (m.equals("1")) {
            Log.d(TEST, "sort " + m + "   " + asc);
            mSortingOrder = asc ? " " + MediaStore.Audio.Media.DISPLAY_NAME + " " + "ASC" :
                    " " + MediaStore.Audio.Media.DISPLAY_NAME + " " + "DESC";
        } else if (m.equals("0")) {
            mSortingOrder = asc ? " " + MediaStore.Audio.Media.ARTIST + " " + " ASC" :
                    " " + MediaStore.Audio.Media.ARTIST + " " + " DESC";
        } else if (m.equals("2")) {
            mSortingOrder = asc ? " " + MediaStore.Audio.Media.ALBUM + " " + " ASC" :
                    " " + MediaStore.Audio.Media.ALBUM + " " + " DESC";
        }

        Cursor cursor;
        ContentResolver contentResolver = getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = null;
        if (path != null) {
            selection = " ( " + MediaStore.Audio.Media.DATA + "  LIKE ? AND "
                    + MediaStore.Audio.Media.DATA + " NOT LIKE ? )";
            Log.d(TEST, selection + "   " + path.get(0));
            if (quest != null) {
                selection += " AND ((" + MediaStore.Audio.Media.DISPLAY_NAME + " LIKE ? "
                        + " ) OR ( " + MediaStore.Audio.Media.ARTIST + " LIKE ? "
                        + " ) OR ( " + MediaStore.Audio.Media.ALBUM + " LIKE ? )) ";
                path.add(quest);
                path.add(quest);
                path.add(quest);
            }
            cursor = contentResolver.query(uri, mProjection, selection,
                    path.toArray(new String[path.size()]), mSortingOrder);
        } else {
            path = new LinkedList<String>();
            if (quest != null) {
                selection = " ( " + MediaStore.Audio.Media.DISPLAY_NAME + " LIKE ?  )" +
                        " OR ( " + MediaStore.Audio.Media.ARTIST + " LIKE ? " + " ) " +
                        " OR ( " + MediaStore.Audio.Media.ALBUM + " LIKE ? ) ";

                path.add(quest);
                path.add(quest);
                path.add(quest);
            }
            cursor = contentResolver.query(uri, mProjection, selection,
                    path.toArray(new String[path.size()]), mSortingOrder);
        }

        if (cursor == null) {
            Toast.makeText(this, "Error. Музику не знайдено", Toast.LENGTH_SHORT).show();
            // query failed, handle error.
        } else if (!cursor.moveToFirst()) {
            Toast.makeText(this, "По вашому запиту нічого не знайдено", Toast.LENGTH_LONG).show();
            // no media on the device
        } else {

            //if (mCursor == null) mCursor = cursor;
            mCursor = cursor;
            if (mAdapter != null) {
                mAdapter.swapCursor(cursor);
                mAdapter.notifyDataSetChanged();
            }

            cursor.moveToFirst();
            mSongList = new LinkedList<String>();
            do {
                mSongList.add(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
            } while (cursor.moveToNext());

            if (mBoundService != null) {
                mBoundService.setSongList(mSongList, sPath, sQuery);
            }
        }
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message m) {
            if (mPlay == true) {

                setProgress();
                mSongTimer = mSongTimer + 1000;
                if (mSongTimer >= mSongDuration){
                    mSongTimer = mBoundService.getProgress();
                }
                sendMessageDelayed(Message.obtain(this, TICK_WHAT), 1000);
            }
        }
    };


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.move_loop:
                mBoundService.setLoop(loopButton.isChecked());
                break;
            case R.id.move_next:
                Log.d(TEST, "pos = " + sPosition);
                cursorMoveTo(sPosition + 1);
                startPlay(new SongData(mCursor, sPosition));
                playButton.setImageResource(R.drawable.pause_action);
                break;
            case R.id.move_prev:
                Log.d(TEST, "pos = " + sPosition);
                cursorMoveTo(sPosition - 1);
                startPlay(new SongData(mCursor, sPosition));
                playButton.setImageResource(R.drawable.pause_action);
                break;
            case R.id.move_stop:
                mBoundService.stopPlay();
                playButton.setImageResource(R.drawable.play_action);
                mSongTimer=0;
                setProgress();
                mHandler.removeMessages(TICK_WHAT);
                mPlay = false;
                break;
            case R.id.move_play:
                if (mPlay) {
                    mBoundService.pausePlay();
                    playButton.setImageResource(R.drawable.play_action);
                    setProgress();

                    mPlay = false;
                } else {
                    startPlay(null);
                    playButton.setImageResource(R.drawable.pause_action);
                    mPlay = true;
                    mHandler.sendMessageDelayed(Message.obtain(mHandler, TICK_WHAT), 400);
                }
                break;

        }
    }

    /**
     * realise move one step forward or backward
     */
    private void cursorMoveTo(int i) {
        Log.d(TEST, "size = " + mCursor.getCount() + "  " + i);
        if (i < 0) {
            i = mCursor.getCount() - 1;
        } else if (mCursor.getCount() == i) {
            i = 0;
        }
        sPosition = i;
        mCursor.moveToPosition(sPosition);
    }

    /**
     * Sets MediaPlayer progress
     */
    private void setProgress() {
        textProgress.setText(timeFormatMillis(mSongTimer));
        seekBar.setProgress(mSongTimer);
    }

    /**
     * Not used - reserved
     */
    public boolean onQueryTextChange(String newText) {
        //TODO:is reserved
        return false;
    }

    public boolean onQueryTextSubmit(String query) {
        if (query.length() > 0) {
            sQuery = "%" + query + "%";
            searchMedia(getArgs(sPath), sQuery);
        } //else searchMedia(null, null);
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e(TEST, "Check2");
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_FOLDER:
                    sPath = data.getStringExtra(FOLDER_NAME);
                    searchMedia(getArgs(sPath), null);
                    mHandler.removeMessages(TICK_WHAT);
                    mAdapter.setmData(null);
                    mPlayerControl.setVisibility(View.GONE);
                    mBoundService.stopPlay();
                    mBoundService.setPosition(0);
                    break;
            }
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mBoundService = ((MyService.LocalBinder) service).getService();
            mBoundService.setMPListener(MainActivity.this);
            mIsBound = true;

            if (mIsNotificationStart == true) {
                mIsNotificationStart = false;
                if (mBoundService.getUri() == null) {
                    sPath = mBoundService.getPath();
                    sQuery = mBoundService.getQuery();
                    sPosition = mBoundService.getPosition();
                    searchMedia(getArgs(sPath), sQuery);
                    Log.d(TEST, "sPosition = " + sPath + "  " + sQuery);
                    Log.d(TEST, "sPosition = " + sPosition + "  " + mCursor.getCount());
                    mCursor.moveToPosition(sPosition);
                    SongData data = new SongData(mCursor, sPosition);
                    mAdapter.setmData(data.getData());
                    initPlayerControl(data);
                } else {
                    sUri = mBoundService.getUri();
                    Cursor c = getContentResolver().
                            query(sUri, mProjection, null, null, null);
                    mCursor = c;
                    mAdapter.swapCursor(c);
                    c.moveToFirst();
                    SongData data = new SongData(mCursor, sPosition);
                    mAdapter.setmData(data.getData());
                    initPlayerControl(data);
                }

            } else if (mIsIntentStart == true) {
                if (sUri != null) {
                    mBoundService.setSongList(mSongList, sUri);
                } else {
                    mBoundService.setSongList(mSongList, sPath, sQuery);
                }
                startPlay(new SongData(mCursor, 0));
            } else {
                mBoundService.setSongList(mSongList, sPath, sQuery);
            }

        }


        public void onServiceDisconnected(ComponentName className) {
            mBoundService = null;
            mIsBound = false;
        }
    };

    void doBindService() {
        //PendingIntent pi = createPendingResult()
        Intent intent = new Intent(this, MyService.class);
        startService(intent);
        boolean b = bindService(intent, mConnection, Context.BIND_ABOVE_CLIENT);
        mIsBound = true;
    }

    void doUnbindService() {
        if (mIsBound) {
            unbindService(mConnection);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeMessages(TICK_WHAT);
        doUnbindService();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(BUNDLE_PLAYER_CONTROL, mPlayerControl.getVisibility());
        outState.putBoolean(BUNDLE_PLAY, mPlay);
        outState.putInt(BUNDLE_SONG_TIMER, mSongTimer);
        outState.putInt(BUNDLE_SONG_DURATION,mSongDuration);
    }


    @Override
    protected void onRestoreInstanceState(Bundle onRestore) {
        super.onRestoreInstanceState(onRestore);
        mPlay = onRestore.getBoolean(BUNDLE_PLAY, false);
        mSongTimer = onRestore.getInt(BUNDLE_SONG_TIMER,0);
        mSongDuration = onRestore.getInt(BUNDLE_SONG_DURATION);
        Log.d(TEST, "myrestart");

        if (onRestore.getInt(BUNDLE_PLAYER_CONTROL, View.GONE) == View.VISIBLE) {
            Log.d(TEST, "myrestart5");
            mCursor.moveToPosition(sPosition);
            SongData data = new SongData(mCursor, sPosition);
            mAdapter.setmData(data.getData());
            initPlayerControl(data);
        } else mPlayerControl.setVisibility(View.GONE);
    }

    /** implementation MyService.onServiceListener */
    @Override
    public void onActionStart() {
        mPlay = true;
        mSongTimer = 0;
        cursorMoveTo(sPosition + 1);
        SongData data = new SongData(mCursor, sPosition);
        initPlayerControl(data);
    }

}


