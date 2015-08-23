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
import android.os.Environment;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;

import static com.fenix.audioplayer.data.HelperClass.*;

public class MainActivity extends Activity implements View.OnClickListener,
        SearchView.OnQueryTextListener {

    private final static String TEST = "mySerActivity";
    public final static int REQUEST_FOLDER = 101;
    public final static int SEND_SONG_DATA = 102;
    public final static int GET_SONG_DATA = 103;
    public final static int STOP_SERVICE = 103;
    public final static String FOLDER_NAME = "folder_name";
    public final static String SEND_DATA = "send_data";
    public final static String QUERY = "query";
    public final static String PATH = "path";
    //private final String SAVE_BUNDLE_SONG_STARTED = "bundle_song_started";
    public Integer intTEST = 1;


    private Cursor mCursor;
    private MyService mBoundService;
    private boolean mPlay = false;
    private boolean mIsBound = false;
    private static final int TICK_WHAT = 2;
    private static String sPath;
    private static String sQuery;
    private static Integer mPosition;
    private static String sSortingOrder;
    private String mData; //for storing playing song
    private ImageButton songButton;
    private ImageButton playButton;
    private ToggleButton loopButton, randomButton;
    private TextView textProgress, textDuration, songName, authorName, albumName;
    private SeekBar seekBar;
    private SearchView mSearchView;
    private RecyclerCursorAdapter mAdapter;
    private LinearLayout playerPult;


    private String[] mProjection = new String[]{
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.TITLE_KEY,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_KEY,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ARTIST_KEY,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DISPLAY_NAME
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Log.e(TEST,"Check1");

        /**     Start and bind to service    */
        doBindService();

        /**     woks with data from intent-filter */
        final String dataString = Uri.decode(getIntent().getDataString());
        if (dataString != null) {
            if (dataString.contains("content://media")) {
                final Uri uri = getIntent().getData();
                final Cursor c = getContentResolver().query(uri, null, null, null, null);
                mCursor = c;
                if (savedInstanceState == null) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            c.moveToFirst();
                            mBoundService.setSong(c.getString(
                                    c.getColumnIndex(MediaStore.Audio.Media.DATA)), uri);
                            startPlay(new SongData(c, 0));
                        }
                    }, 100);
                }
            } else if (dataString.contains("file:")) {
                sPath = getFolder(dataString);
                sQuery = dataString.substring(dataString.lastIndexOf("/") + 1);
                searchMedia(getArgs(sPath),sQuery);
                if (savedInstanceState == null) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mCursor.moveToFirst();
                            mBoundService.setSong(mCursor.getString(
                                    mCursor.getColumnIndex(MediaStore.Audio.Media.DATA)), sPath, sQuery);
                            startPlay(new SongData(mCursor, 0));
                        }
                    }, 100);
                }
            } else Toast.makeText(this, "ПОМИЛКА - файл не знайдено", Toast.LENGTH_LONG).show();
            ;
        } else {
            searchMedia(getArgs(sPath), sQuery);
        }

        //find view elements
        //prevButton = (ImageButton) findViewById(R.id.move_prev);
        //nextButton = (ImageButton) findViewById(R.id.move_next);
        playButton = (ImageButton) findViewById(R.id.move_play);
        //stopButton = (ImageButton) findViewById(R.id.move_stop);
        loopButton = (ToggleButton) findViewById(R.id.move_loop);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        textDuration = (TextView) findViewById(R.id.text_duration);
        textProgress = (TextView) findViewById(R.id.text_progress);
        songName = (TextView) findViewById(R.id.songName);
        authorName = (TextView) findViewById(R.id.authorName);
        albumName = (TextView) findViewById(R.id.albumName);
        playerPult = (LinearLayout) findViewById(R.id.playerPult);

        //Changes in view
        //TODO: check if need
        /*if (!getIntent().hasExtra("extra")) {
            playerPult.setVisibility(View.GONE);
            mHandler.sendMessageDelayed(Message.obtain(mHandler, TICK_WHAT), 500);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {

                }
            }, 600);
        }*/

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
                mAdapter.setmData(data.getData());
                mPosition = data.getPosition();
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
                mBoundService.setProgress(seekBar.getProgress());
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) searchItem.getActionView();
        mSearchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
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

    public void startPlay(SongData data) {

        if (data != null) {
            mBoundService.startPlay(data.getPosition());
        } else {
            mBoundService.startPlay(null);
        }
        mPlay = true;
        mHandler.sendMessageDelayed(Message.obtain(mHandler, TICK_WHAT), 500);
        //init player control panel
        seekBar.setMax(mBoundService.getDuration());
        seekBar.setProgress(mBoundService.getProgress());
        textDuration.setText(timeFormat(mBoundService.getDuration()));
        textProgress.setText(timeFormat(mBoundService.getProgress()));
        playButton.setImageResource(R.drawable.pause_action);
        songName.setText(data.getData());
        authorName.setText("Autor: " + data.getAutor());
        albumName.setText("Album: " + data.getAlbum());

        mBoundService.setLoop(loopButton.isChecked());
        //must be last string
        playerPult.setVisibility(View.VISIBLE);
    }


    public void searchMedia(LinkedList<String> path, String quest) {

        //reading preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean asc = prefs.getBoolean(getString(R.string.pref_sort_order), true);
        String m = prefs.getString(getString(R.string.pref_list_sort), "1");

        //set sSortingOrder
        if (m.equals("1")) {
            Log.d(TEST, "sort " + m + "   " + asc);
            sSortingOrder = asc ? " " + MediaStore.Audio.Media.DISPLAY_NAME + " " + "ASC" :
                    " " + MediaStore.Audio.Media.DISPLAY_NAME + " " + "DESC";
        } else if (m.equals("0")) {
            sSortingOrder = asc ? " " + MediaStore.Audio.Media.ARTIST + " " + " ASC" :
                    " " + MediaStore.Audio.Media.ARTIST + " " + " DESC";
        } else if (m.equals("2")) {
            sSortingOrder = asc ? " " + MediaStore.Audio.Media.ALBUM + " " + " ASC" :
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
                    path.toArray(new String[path.size()]), sSortingOrder);
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
                    path.toArray(new String[path.size()]), sSortingOrder);
        }
        if (cursor == null) {
            Toast.makeText(this, "По вашому запиту нічого не знайдено", Toast.LENGTH_SHORT).show();
            // query failed, handle error.
        } else if (!cursor.moveToFirst()) {
            Toast.makeText(this, "Нема музики на девайсі", Toast.LENGTH_LONG).show();
            // no media on the device
        } else {

            //if (mCursor == null) mCursor = cursor;
            mCursor = cursor;
            if (mAdapter != null) {
                mAdapter.swapCursor(cursor);
                mAdapter.notifyDataSetChanged();
            }

            cursor.moveToFirst();
            final LinkedList<String> songList = new LinkedList<String>();
            do {
                songList.add(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
            } while (cursor.moveToNext());

            if (mBoundService == null) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mBoundService.setSongList(songList, sPath, sQuery);
                    }
                }, 200);
            } else mBoundService.setSongList(songList, sPath, sQuery);
        }
    }


    private Handler mHandler = new Handler() {
        public void handleMessage(Message m) {
            if (mBoundService.getPosition() != mPosition) {
                Log.d(TEST, "activity = " + mPosition);
                Log.d(TEST, "service = " + mBoundService.getPosition());
                mPosition = mBoundService.getPosition();
                Log.d(TEST, "service2 = " + mPosition);
                seekBar.setMax(mBoundService.getDuration());
                seekBar.setProgress(mBoundService.getProgress());
                textDuration.setText(timeFormat(mBoundService.getDuration()));
                textProgress.setText(timeFormat(mBoundService.getProgress()));
                playButton.setImageResource(R.drawable.pause_action);

                Log.d(TEST, "service3 = " + mPosition);
                mCursor.moveToPosition(mPosition);
                Log.d(TEST, "service4 = " + mCursor.getPosition());
                songName.setText(mCursor.getString(mCursor.
                        getColumnIndex(MediaStore.Audio.Media.TITLE)));
                authorName.setText("Autor: " + mCursor.getString(mCursor.
                        getColumnIndex(MediaStore.Audio.Media.ARTIST)));
                albumName.setText("Album: " + mCursor.getString(mCursor.
                        getColumnIndex(MediaStore.Audio.Media.ALBUM)));
                mAdapter.setmData(mCursor.getString(mCursor.
                        getColumnIndex(MediaStore.Audio.Media.DATA)));
                mAdapter.notifyDataSetChanged();
                mBoundService.setLoop(loopButton.isChecked());
                playerPult.setVisibility(View.VISIBLE);

            }
            if (mBoundService.isPlay()) {
                //TODO: program update Player progress

                setProgress();
                sendMessageDelayed(Message.obtain(this, TICK_WHAT), 900);
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
                Log.d(TEST, "pos = " + mPosition);
                cursorMoveTo(mPosition + 1);

                playButton.setImageResource(R.drawable.pause_action);
                mPlay = true;
                mHandler.sendMessageDelayed(Message.obtain(mHandler, TICK_WHAT), 400);
                break;
            case R.id.move_prev:
                Log.d(TEST, "pos = " + mPosition);
                ;
                cursorMoveTo(mPosition - 1);

                playButton.setImageResource(R.drawable.pause_action);
                mPlay = true;
                mHandler.sendMessageDelayed(Message.obtain(mHandler, TICK_WHAT), 400);
                break;
            case R.id.move_stop:
                mBoundService.stopPlay();
                //mMediaPlayer.seekTo(0);
                playButton.setImageResource(R.drawable.play_action);
                setProgress();
                mPlay = false;
                break;
            case R.id.move_play:
                if (mBoundService.isPlay()) {
                    mBoundService.pausePlay();
                    playButton.setImageResource(R.drawable.play_action);
                    setProgress();
                    mPlay = false;

                } else {
                    mBoundService.startPlay(null);
                    playButton.setImageResource(R.drawable.pause_action);
                    mPlay = true;
                    mHandler.sendMessageDelayed(Message.obtain(mHandler, TICK_WHAT), 400);
                }
                break;

        }
    }

    private void cursorMoveTo(int i) {
        if (i < 0) {
            int j = mCursor.getCount() - 1;
            mCursor.moveToPosition(j);
            startPlay(new SongData(
                    mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)),
                    mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)),
                    mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)),
                    mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.DATA)),
                    mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)), j));
        } else if (mCursor.getCount() == i) {
            int j = 0;
            mCursor.moveToPosition(j);
            startPlay(new SongData(
                    mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)),
                    mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)),
                    mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)),
                    mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.DATA)),
                    mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)), j));
        } else {

            mCursor.moveToPosition(i);
            startPlay(new SongData(
                    mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)),
                    mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)),
                    mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)),
                    mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.DATA)),
                    mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)), i));
        }

    }

    private void setProgress() {
        int i = mBoundService.getProgress();
        textProgress.setText(timeFormat(i));
        seekBar.setProgress(i);
    }

    private LinkedList<String> getArgs(String localPath) {
        LinkedList<String> args = new LinkedList<String>();
        if (localPath != null) {
            args.add("%/" + sPath + "/%");
            args.add("%/" + sPath + "/%/%");
            return args;
        } else {
            return null;
        }
    }


    public boolean onQueryTextChange(String newText) {
        //TODO:is reserved
        return false;
    }

    public boolean onQueryTextSubmit(String query) {
        if (query.length() > 0) {

            searchMedia(getArgs(sPath), "%" + query + "%");

            sQuery = "%" + query + "%";
        } else searchMedia(null, null);
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e(TEST, "Check2");
        switch (requestCode) {
            case REQUEST_FOLDER:
                if (resultCode == RESULT_OK) {
                    sPath = data.getStringExtra(FOLDER_NAME);
                    searchMedia(getArgs(sPath), null);
                }
                break;
        }
    }


    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mBoundService = ((MyService.LocalBinder) service).getService();
            mIsBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            mBoundService = null;
            mIsBound = false;
        }
    };


    void doBindService() {
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
        outState.putInt("playerPult", playerPult.getVisibility());
        //outState.putBoolean(SAVE_BUNDLE_SONG_STARTED,false);
    }


    @Override
    protected void onRestoreInstanceState(Bundle onRestore) {
        super.onRestoreInstanceState(onRestore);
        if (onRestore.getInt("playerPult", View.VISIBLE) == View.VISIBLE) {
            playerPult.setVisibility(View.VISIBLE);
        } else playerPult.setVisibility(View.GONE);

        mHandler.sendMessageDelayed(Message.obtain(mHandler, TICK_WHAT), 500);

        mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {

                mPosition = mBoundService.getPosition();
                if (mBoundService.getPosition() != null) {
                    seekBar.setMax(mBoundService.getDuration());
                    seekBar.setProgress(mBoundService.getProgress());
                    textDuration.setText(timeFormat(mBoundService.getDuration()));
                    textProgress.setText(timeFormat(mBoundService.getProgress()));
                    playButton.setImageResource(R.drawable.pause_action);
                    mCursor.moveToPosition(mPosition);
                    songName.setText(mCursor.getString(mCursor.
                            getColumnIndex(MediaStore.Audio.Media.TITLE)));
                    authorName.setText("Autor: " + mCursor.getString(mCursor.
                            getColumnIndex(MediaStore.Audio.Media.ARTIST)));
                    albumName.setText("Album: " + mCursor.getString(mCursor.
                            getColumnIndex(MediaStore.Audio.Media.ALBUM)));
                    mAdapter.setmData(mCursor.getString(mCursor.
                            getColumnIndex(MediaStore.Audio.Media.DATA)));
                    mAdapter.notifyDataSetChanged();
                    mBoundService.setLoop(loopButton.isChecked());
                }
            }

        }, 500);
    }


}


