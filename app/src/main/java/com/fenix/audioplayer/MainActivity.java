package com.fenix.audioplayer;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
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

import java.io.IOException;
import java.util.LinkedList;

import static com.fenix.audioplayer.HelperClass.*;

public class MainActivity extends Activity implements View.OnClickListener,
        MediaPlayer.OnCompletionListener,SearchView.OnQueryTextListener {

    private final static String TEST = "myTEST";
    public final int REQUEST_FOLDER = 101;
    public final int SEND_SONG_DATA = 102;
    public final int GET_SONG_DATA = 103;



    private Cursor mCursor;
    private static MediaPlayer mMediaPlayer;
    private boolean mPlay = false;
    private static final int TICK_WHAT = 2;
    private StringBuilder mPath;
    private LinkedList<String> mArgs=new LinkedList<String>();
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
        searchMedia(null, null);
        //countFolder();

        //TODO: ActionBar FIX
        /*try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if(menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ex) {

        };*/


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
        mAdapter = new RecyclerCursorAdapter(this,mCursor);
        recyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new RecyclerCursorAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, SongData data) {
                if (songButton != null) {
                    songButton.setImageResource(R.drawable.music_action);
                }
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
                if (mMediaPlayer != null) {
                    mMediaPlayer.seekTo(seekBar.getProgress());
                }
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_settings:

                break;
            case R.id.action_search:

                break;
            case R.id.select_folder:
                Intent intent = new Intent(this,FileManagerActivity.class);
                startActivityForResult(intent, GET_SONG_DATA);
                break;
        }


        return super.onOptionsItemSelected(item);
    }

    private void resetMP() {
        if (mMediaPlayer != null) {
            try {
                mMediaPlayer.reset();
                mMediaPlayer = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void startPlay(SongData data) {
        Log.d(TEST, "start play");

        if (data != null) {
            resetMP();
            try {
                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setDataSource(data.getDATA());
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.prepare();
                mMediaPlayer.start();
            } catch (IOException e) {

                Log.e(TEST, e.toString());
                e.printStackTrace();
            }
        } else {
            if (mMediaPlayer != null) mMediaPlayer.start();
        }
        mPlay = true;
        mHandler.sendMessageDelayed(Message.obtain(mHandler, TICK_WHAT), 900);
        //init player control panel
        seekBar.setMax(mMediaPlayer.getDuration());
        seekBar.setProgress(mMediaPlayer.getCurrentPosition());
        textDuration.setText(timeFormat(mMediaPlayer.getDuration()));
        Log.d(TEST, "time" + mMediaPlayer.getDuration());
        textProgress.setText(timeFormat(mMediaPlayer.getCurrentPosition()));
        playButton.setImageResource(R.drawable.pause_action);
        songName.setText(data.getTitle());
        authorName.setText("Autor: " + data.getAutor());
        albumName.setText("Album: " + data.getAlbum());

        mMediaPlayer.setLooping(loopButton.isChecked());
        mMediaPlayer.setOnCompletionListener(this);

        //must be last string
        playerPult.setVisibility(View.VISIBLE);
    }

    /*public void countFolder() {

        //список папок
        do {
            folderSet.add(getFolder(mCursor.getString(mCursor
                    .getColumnIndex(MediaStore.Audio.Media.DATA))));
            Log.d(TEST, mCursor.getString(mCursor
                    .getColumnIndex(MediaStore.Audio.Media.DATA)));
        } while (mCursor.moveToNext());
        //DELETEвивід списку
        Iterator<String> iter = folderSet.iterator();
        while (iter.hasNext()) {
            Log.d(TEST, iter.next());
        }
        //обчислення кількості
        iter = folderSet.iterator();
        while (iter.hasNext()) {
            mPath = new StringBuilder();
            mPath.append(iter.next());
            mArgs.clear();
            mArgs.add("%/" + mPath + "/%");
            mArgs.add("%/" + mPath + "/%/%");
            searchMedia(mArgs, null);
            mDirectoryList.add(new DirectoryData(mPath.toString(), mCursor.getCount()));
            Log.d(TEST, mArgs.get(0) + " = " + mCursor.getCount());
        }

    }*/

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
            selection = " ( "+MediaStore.Audio.Media.DATA + "  LIKE ? AND "
                    + MediaStore.Audio.Media.DATA + " NOT LIKE ? )";
            Log.d(TEST, selection + "   " + path.get(0));
            if(s!=null){
                selection +=" AND "+MediaStore.Audio.Media.TITLE + " LIKE ? ";
                path.add(s);
            }
        }else{
            if (s!=null) {
                selection = MediaStore.Audio.Media.TITLE + " LIKE ? ";
                path = new LinkedList<String>();
                path.add(s);
            }
        }

        if(path!=null) {
            Log.e(TEST, "path = " + path.toArray(new String[path.size()]).toString());
            Log.e(TEST, "size = " + selection);
            cursor = contentResolver.query(uri, null, selection, path.toArray(new String[path.size()]), null);
        }else{
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
            if(mCursor==null)  mCursor=cursor;
           //mCursor=cursor;
            if(mAdapter!=null) {
                mAdapter.swapCursor(cursor);
                mAdapter.notifyDataSetChanged();
            }



        }
    }




    private Handler mHandler = new Handler() {
        public void handleMessage(Message m) {
            if (mPlay == true) {
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
                mMediaPlayer.setLooping(loopButton.isChecked());
                break;
            case R.id.move_next:
                break;
            case R.id.move_prev:
                break;
            case R.id.move_random:
                break;
            case R.id.move_stop:
                mMediaPlayer.pause();
                mMediaPlayer.seekTo(0);
                playButton.setImageResource(R.drawable.play_action);
                setProgress();
                mPlay = false;
                break;
            case R.id.move_play:
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                    playButton.setImageResource(R.drawable.play_action);
                    setProgress();
                    mPlay = false;

                } else {
                    mMediaPlayer.start();
                    playButton.setImageResource(R.drawable.pause_action);
                    mPlay = true;
                    mHandler.sendMessageDelayed(Message.obtain(mHandler, TICK_WHAT), 900);
                }
                break;

        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (mPlay == true) {

        } else {
            playButton.setImageResource(R.drawable.play_action);
        }
    }

    private void setProgress() {
        int i = mMediaPlayer.getCurrentPosition();
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
        if(!query.isEmpty()) {
            if(mPath!=null) {
                mArgs.clear();
                mArgs.add("%/" + mPath + "/%");
                mArgs.add("%/" + mPath + "/%/%");
                searchMedia(mArgs, "%"+query+"%");
            }else {
                searchMedia(null, "%"+query+"%");
            }
        }else searchMedia(null, null);
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent data){

    }
}


