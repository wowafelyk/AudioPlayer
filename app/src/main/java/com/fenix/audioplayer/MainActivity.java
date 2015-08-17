package com.fenix.audioplayer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
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


    private Cursor mCursor;
    private MyService mBoundService;
    private boolean mPlay = false;
    private boolean mIsBound = false;
    private static final int TICK_WHAT = 2;
    private String mPath;
    private Integer mPosition;
    private LinkedList<String> mArgs = new LinkedList<String>();
    private String mData; //for storing playing song
    private ImageButton songButton;
    private ImageButton prevButton, nextButton, playButton, stopButton;
    private ToggleButton loopButton, randomButton;
    private TextView textProgress, textDuration, songName, authorName, albumName;
    private SeekBar seekBar;
    private SearchView mSearchView;
    private RecyclerCursorAdapter mAdapter;
    private LinearLayout playerPult;
    //private LinkedHashSet<String> folderSet = new LinkedHashSet<String>();
    //private LinkedList<DirectoryData> mDirectoryList = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        doBindService();
        searchMedia(null, null);

        //find view elements
        prevButton = (ImageButton) findViewById(R.id.move_prev);
        nextButton = (ImageButton) findViewById(R.id.move_next);
        playButton = (ImageButton) findViewById(R.id.move_play);
        stopButton = (ImageButton) findViewById(R.id.move_stop);
        loopButton = (ToggleButton) findViewById(R.id.move_loop);
        randomButton = (ToggleButton) findViewById(R.id.move_random);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        textDuration = (TextView) findViewById(R.id.text_duration);
        textProgress = (TextView) findViewById(R.id.text_progress);
        songName = (TextView) findViewById(R.id.songName);
        authorName = (TextView) findViewById(R.id.authorName);
        albumName = (TextView) findViewById(R.id.albumName);
        playerPult = (LinearLayout) findViewById(R.id.playerPult);

        //Changes in view
        playerPult.setVisibility(View.GONE);

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
                mAdapter.setmData(data.getDATA());
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


        Intent intent = getIntent();
        Uri data = intent.getData();
        if (data != null) {
            Log.d(TEST, "URI = " + data.getPath());

            Cursor c = getContentResolver().query(data, null, null, null, null);
            mAdapter.swapCursor(c);
            mAdapter.notifyDataSetChanged();
            c.moveToFirst();
            startPlay(new SongData(
                    c.getString(c.getColumnIndex(MediaStore.Audio.Media.ARTIST)),
                    c.getString(c.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)),
                    c.getString(c.getColumnIndex(MediaStore.Audio.Media.ALBUM)),
                    c.getString(c.getColumnIndex(MediaStore.Audio.Media.DATA)),
                    c.getString(c.getColumnIndex(MediaStore.Audio.Media.TITLE)), 0));

        }


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
                break;
            case R.id.action_search:
                break;
            case R.id.select_folder:
                Intent intent = new Intent(this, FileManagerActivity.class);
                startActivityForResult(intent, REQUEST_FOLDER);
                break;
            case R.id.action_all_file:
                searchMedia(null, null);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void startPlay(SongData data) {
        Log.d(TEST, "start play");

        if (data != null) {
             //mBoundService.setSong(data.getDATA());
             Log.d(TEST, " "+data.getPosition());
             mBoundService.startPlay(data.getPosition());
        } else {
            mBoundService.startPlay(null);
        }
        mPlay = true;
        mHandler.sendMessageDelayed(Message.obtain(mHandler, TICK_WHAT), 900);
        //init player control panel
        seekBar.setMax(mBoundService.getDuration());
        seekBar.setProgress(mBoundService.getProgress());
        textDuration.setText(timeFormat(mBoundService.getDuration()));
        Log.d(TEST, "time" + mBoundService.getDuration());
        textProgress.setText(timeFormat(mBoundService.getProgress()));
        playButton.setImageResource(R.drawable.pause_action);
        songName.setText(data.getTitle());
        authorName.setText("Autor: " + data.getAutor());
        albumName.setText("Album: " + data.getAlbum());

        mBoundService.setLooping(loopButton.isChecked());

        //must be last string
        playerPult.setVisibility(View.VISIBLE);
    }

    public void searchMedia(LinkedList<String> path, String s) {
        Cursor cursor;
        ContentResolver contentResolver = getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[]{
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
        String selection = null;
        if (path != null) {
            selection = " ( " + MediaStore.Audio.Media.DATA + "  LIKE ? AND "
                    + MediaStore.Audio.Media.DATA + " NOT LIKE ? )";
            Log.d(TEST, selection + "   " + path.get(0));
            if (s != null) {
                selection += " AND " + MediaStore.Audio.Media.TITLE + " LIKE ? ";
                path.add(s);
            }
        } else {
            if (s != null) {
                selection = MediaStore.Audio.Media.TITLE + " LIKE ? ";
                path = new LinkedList<String>();
                path.add(s);
            }
        }

        if (path != null) {
            Log.e(TEST, "path = " + path.toArray(new String[path.size()]).toString());
            Log.e(TEST, "size = " + selection);
            cursor = contentResolver.query(uri, null, selection, path.toArray(new String[path.size()]), null);
        } else {
            cursor = contentResolver.query(uri, null, selection, null, null);

        }
        if (cursor == null) {
            Toast.makeText(this, "По вашому запиту нічого не знайдено", Toast.LENGTH_SHORT).show();
            // query failed, handle error.
        } else if (!cursor.moveToFirst()) {
            Toast.makeText(this, "Нема музики на девайсі", Toast.LENGTH_LONG).show();
            // no media on the device
        } else {
            //TODO: Change block
            if (mCursor == null) mCursor = cursor;
            //mCursor=cursor;
            if (mAdapter != null) {
                mAdapter.swapCursor(cursor);
                mAdapter.notifyDataSetChanged();
            }
            cursor.moveToFirst();
            final LinkedList<String> songList = new LinkedList<String>();
            do{
                songList.add(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
            }while(cursor.moveToNext());

            if(mBoundService==null){
                mHandler.postDelayed(new Runnable(){

                    @Override
                    public void run() {
                        mBoundService.setSongList(songList);
                    }
                },2000);
            }else mBoundService.setSongList(songList);



        }
    }


    private Handler mHandler = new Handler() {
        public void handleMessage(Message m) {
            if(mBoundService.getPosition()!=mPosition){
                Log.d(TEST, "activity = " + mPosition);
                Log.d(TEST, "service = " + mBoundService.getPosition());
                mPosition=mBoundService.getPosition();
                seekBar.setMax(mBoundService.getDuration());
                seekBar.setProgress(mBoundService.getProgress());
                textDuration.setText(timeFormat(mBoundService.getDuration()));
                textProgress.setText(timeFormat(mBoundService.getProgress()));
                playButton.setImageResource(R.drawable.pause_action);

                mCursor.move(mPosition);
                songName.setText(mCursor.getString(mCursor.
                        getColumnIndex(MediaStore.Audio.Media.TITLE)));
                authorName.setText("Autor: " + mCursor.getString(mCursor.
                        getColumnIndex(MediaStore.Audio.Media.ARTIST)));
                albumName.setText("Album: " + mCursor.getString(mCursor.
                        getColumnIndex(MediaStore.Audio.Media.ALBUM)));

                mBoundService.setLooping(loopButton.isChecked());
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
                mBoundService.setLooping(loopButton.isChecked());
                break;
            case R.id.move_next:
                break;
            case R.id.move_prev:
                break;
            case R.id.move_random:
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
                    mHandler.sendMessageDelayed(Message.obtain(mHandler, TICK_WHAT), 900);
                }
                break;

        }
    }

    private void setProgress() {
        int i = mBoundService.getProgress();
        textProgress.setText(timeFormat(i));
        seekBar.setProgress(i);
    }


    public boolean onQueryTextChange(String newText) {
        //TODO:is reserved
        /*
        if(!newText.isEmpty()) {
            if(mPath!=null) {
                mArgs.clear();
                mArgs.add("%/" + mPath + "/%");
                mArgs.add("%/" + mPath + "/%/%");
                searchMedia(mArgs, "%"+newText+"%");
            }else {
                searchMedia(null, "%"+newText+"%");
            }
        }*/
        return false;
    }

    public boolean onQueryTextSubmit(String query) {
        if (query.length() > 1) {
            if (mPath != null) {
                mArgs.clear();
                mArgs.add("%/" + mPath + "/%");
                mArgs.add("%/" + mPath + "/%/%");
                searchMedia(mArgs, "%" + query + "%");
            } else {
                searchMedia(null, "%" + query + "%");
            }
        } else searchMedia(null, null);
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_FOLDER:
                if (resultCode == RESULT_OK) {
                    mPath = data.getStringExtra(FOLDER_NAME);
                    if (mPath != null) {
                        mArgs.clear();
                        mArgs.add("%/" + mPath + "/%");
                        mArgs.add("%/" + mPath + "/%/%");
                        searchMedia(mArgs, null);
                    }
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
        boolean b = bindService(new Intent(this,
                MyService.class), mConnection, Context.BIND_AUTO_CREATE);
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
        doUnbindService();
    }
}


