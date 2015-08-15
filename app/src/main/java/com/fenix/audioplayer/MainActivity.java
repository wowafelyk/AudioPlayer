package com.fenix.audioplayer;

import android.app.Activity;
import android.content.ContentResolver;
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
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity implements View.OnClickListener,MediaPlayer.OnCompletionListener {

    private final static String TEST="myTEST";
    private Cursor mCursor;
    private static MediaPlayer mMediaPlayer;
    private boolean mPlay = false;
    private static final int TICK_WHAT = 2;
    private ImageButton songButton;
    private ImageButton prevButton, nextButton, playButton, stopButton;
    private ToggleButton loopButton, randomButton;
    private TextView textProgress, textDuration, songName, authorName, albumName;
    private SeekBar seekBar;
    private LinearLayout playerPult;
    private LinkedHashSet<String> folderSet = new LinkedHashSet<String>();
    private LinkedList<DirectoryData> list = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        serchMedia(null);
        countFolder();



        //find view elements
        prevButton = (ImageButton)findViewById(R.id.move_prev);
        nextButton = (ImageButton)findViewById(R.id.move_next);
        playButton = (ImageButton)findViewById(R.id.move_play);
        stopButton = (ImageButton)findViewById(R.id.move_stop);
        loopButton = (ToggleButton)findViewById(R.id.move_loop);
        randomButton = (ToggleButton)findViewById(R.id.move_random);
        seekBar = (SeekBar)findViewById(R.id.seekBar);
        textDuration = (TextView)findViewById(R.id.text_duration);
        textProgress = (TextView)findViewById(R.id.text_progress);
        songName = (TextView)findViewById(R.id.songName);
        authorName = (TextView)findViewById(R.id.authorName);
        albumName = (TextView)findViewById(R.id.albumName);
        playerPult = (LinearLayout)findViewById(R.id.playerPult);

        //Changes in view
        playerPult.setVisibility(View.GONE);


        //init RecyclerView
        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(mCursor);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v,SongData data) {
                if(songButton !=null) {
                    songButton.setImageResource(R.drawable.music_action);
                }
                songButton = (ImageButton)v;
                songButton.setImageResource(R.drawable.play_action);
                startPlay(data);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
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

    public void startPlay(SongData data){
        Log.d(TEST, "start play");

        if(data!=null) {
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
        }else{
            if(mMediaPlayer!=null)  mMediaPlayer.start();
        }
        mPlay=true;
        mHandler.sendMessageDelayed(Message.obtain(mHandler, TICK_WHAT), 900);
        //init player control panel
        seekBar.setMax(mMediaPlayer.getDuration());
        seekBar.setProgress(mMediaPlayer.getCurrentPosition());
        textDuration.setText(timeFormat(mMediaPlayer.getDuration()));
        Log.d(TEST, "time" + mMediaPlayer.getDuration());
        textProgress.setText(timeFormat(mMediaPlayer.getCurrentPosition()));
        playButton.setImageResource(R.drawable.pause_action);
        songName.setText(data.getSongName());
        authorName.setText(data.getAutor());
        albumName.setText(data.getAlbum());
        mMediaPlayer.setLooping(loopButton.isChecked());

        //must be last string
        playerPult.setVisibility(View.VISIBLE);
    }

    public void countFolder(){

        //список папок
        do {
            folderSet.add(getFolder(mCursor.getString(mCursor
                    .getColumnIndex(MediaStore.Audio.Media.DATA))));
            Log.d(TEST,mCursor.getString(mCursor
                    .getColumnIndex(MediaStore.Audio.Media.DATA)));
        } while (mCursor.moveToNext());
        //DELETEвивід списку
        Iterator<String> iter =folderSet.iterator();
        while(iter.hasNext()){
            Log.d(TEST, iter.next());
        }
        //обчислення кількості
        iter =folderSet.iterator();
        while(iter.hasNext()){
            String j = iter.next();
            String []i= {("%/"+j+"/%"),("%/"+j+"/%/%")};
            serchMedia(i);
            list.add(new DirectoryData(i[0],mCursor.getCount()));
            Log.d(TEST, i[0]+" = "+mCursor.getCount());
        }

    }

    public void serchMedia(String[] path){
        ContentResolver contentResolver = getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[]{
                MediaStore.Audio.Media._COUNT,
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
        if(path!=null){
            selection=MediaStore.Audio.Media.DATA+" LIKE ? AND "
                    + MediaStore.Audio.Media.DATA+" NOT LIKE ?";
            Log.d(TEST, selection+"   "+path[0]);
        }

        mCursor = contentResolver.query(uri, null, selection, path, null);
        if (mCursor == null) {
            Toast.makeText(this,"По вашому запиту нічого не знайдено",Toast.LENGTH_SHORT).show();
            // query failed, handle error.
        } else if (!mCursor.moveToFirst()) {
            Toast.makeText(this,"Нема музики на девайсі",Toast.LENGTH_LONG).show();
            // no media on the device
        } else {

            //TODO: Change block


        }
    }


    /**Return directory name from file path*/
    public String getFolder(String s){
        String path = new File(s).getParent();
        return path.substring(path.lastIndexOf("/")+1);
    }

    public static String timeFormat(int millis) {
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
        long minutes = seconds / 60;
        seconds = seconds - minutes * 60;
        return String.format("%01d:%02d", minutes, seconds);
    }

    public static String timeFormat(String millis) {
        int value = 0;
        try {
            value = Integer.valueOf(millis);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TEST, e.toString());
        }
        return timeFormat(value);
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message m) {
            if (mPlay==true) {
                //TODO: program update Player progress
                setProgress();
                sendMessageDelayed(Message.obtain(this, TICK_WHAT), 900);
            }
        }
    };


    @Override
    public void onClick(View v) {
        switch (v.getId()){
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
                setProgress();
                mPlay=false;
                break;
            case R.id.move_play:
                if(mMediaPlayer.isPlaying()){
                    mMediaPlayer.pause();
                    playButton.setImageResource(R.drawable.play_action);
                    setProgress();
                    mPlay=false;

                }else{
                    mMediaPlayer.start();
                    playButton.setImageResource(R.drawable.pause_action);
                    mPlay=true;
                    mHandler.sendMessageDelayed(Message.obtain(mHandler, TICK_WHAT), 900);
                }
                break;

        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(mPlay==true){

        }else{
            playButton.setImageResource(R.drawable.play_action);
        }
    }

    private void setProgress(){
        int i = mMediaPlayer.getCurrentPosition();
        textProgress.setText(timeFormat(i));
        seekBar.setProgress(i);
    }

}
