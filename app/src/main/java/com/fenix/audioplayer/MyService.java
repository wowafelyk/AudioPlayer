package com.fenix.audioplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.LinkedList;

public class MyService extends Service implements MediaPlayer.OnCompletionListener {

    private NotificationManager mNM;
    private MediaPlayer mMediaPlayer;

    private LinkedList<String> mListOfSong;
    private Integer mPosition;
    private Integer mProgress;
    private String mPath;
    private String mQuery;
    private Uri mUri;
    private Notification mNotification;


    private int NOTIFICATION = R.string.local_service_started;
    private boolean mPlay = false;
    private boolean mLooping = false;


    private final String TEST = "myService";

    private WeakReference<OnCompletionListener> mListener;

    public MyService() {
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

        if (mPlay == true) {

            if ((mPosition + 1) < mListOfSong.size()) {
                Log.d(TEST, "onComplete" + mPosition.toString());
                Log.d(TEST, "listZise " + mListOfSong.size());
                startPlay(mPosition = mPosition + 1);
            } else if (mListOfSong.size() >= 2) {
                mPosition = 0;
                startPlay(mPosition);
            }
        }
        OnCompletionListener listener = mListener.get();
        if(listener!=null) {
            listener.onCompletion(mp);
        }
    }

    public class LocalBinder extends Binder {
        MyService getService() {
            return MyService.this;
        }
    }

    @Override
    public void onCreate() {
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mListOfSong = new LinkedList<String>();

        createNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        mMediaPlayer.release();
        mNM.cancel(NOTIFICATION);

        // Tell the user we stopped.
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Show a notification while this service is running.
     */
    private void createNotification() {
        CharSequence text = "Playing songs";
        Intent intent = new Intent(this, MainActivity.class)
                .putExtra("extra", true);
                //.putExtra(MainActivity.SEND_PATH,mPath)
                //.putExtra(MainActivity.SEND_QUERY,mQuery);
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                MainActivity.START_FROM_NOTIFICATION,intent, 0);
        mNotification = new Notification.Builder(getBaseContext())
                .setContentTitle("AudioPlayer")
                .setContentText(text)
                .setSmallIcon(R.drawable.play_action)
                .setLargeIcon(BitmapFactory.decodeResource(null,R.drawable.play_action))
                .setContentIntent(contentIntent)
                .build();
        ;
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

    public void startPlay(Integer position) {


        startForeground(NOTIFICATION, mNotification);
        mPlay = true;
        if (position != null) {
            mPosition = position;
            Log.d(TEST, mPosition.toString());
            resetMP();
            try {
                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setDataSource(mListOfSong.get(position));
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.prepare();
                mMediaPlayer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            if (mMediaPlayer != null) {
                mMediaPlayer.start();
            }
        }


        mMediaPlayer.setLooping(mLooping);
        mMediaPlayer.setOnCompletionListener(this);
    }

    public void stopPlay() {
        pausePlay();
        mMediaPlayer.seekTo(0);
    }

    public void pausePlay() {
        stopForeground(true);
        mMediaPlayer.pause();
        mPlay = false;
    }


    public void setSongList(LinkedList<String> list, String mQuery, String mPath) {
        mUri = null;
        this.mQuery = mQuery;
        this.mPath = mPath;
        mListOfSong = list;
    }

    public void setSong(String song, Uri mUri) {
        this.mUri = mUri;
        mListOfSong = new LinkedList<String>();
        mListOfSong.add(song);
    }

    public void setSong(String song, String mQuery, String mPath) {
        mUri = null;
        this.mQuery = mQuery;
        this.mPath = mPath;
        mListOfSong = new LinkedList<String>();
        mListOfSong.add(song);
    }

    public void setProgress(int i) {
        mProgress = i;
        mMediaPlayer.seekTo(mProgress);
    }

    public int getProgress() {
        return mMediaPlayer.getCurrentPosition();
    }

    public int getDuration() {
        return mMediaPlayer.getDuration();
    }

    public void setMPListener(MediaPlayer.OnCompletionListener listener){
        this.mListener = new WeakReference<MediaPlayer.OnCompletionListener>(listener);
    }

    public boolean isLooping() {
        return mLooping;
    }

    public boolean isPlay() {
        return mPlay;
    }

    public void setLoop(boolean mLooping) {
        this.mLooping = mLooping;
        Log.d(TEST, "loop" + mLooping);
        mMediaPlayer.setLooping(mLooping);
    }

    public Integer getPosition() {
        return mPosition;
    }

    public void setPosition(Integer mPosition) {
        this.mPosition = mPosition;
    }

    public String getPath() {
        return mPath;
    }

    public void setPath(String mPath) {
        this.mPath = mPath;
    }

    public String getQuery() {
        return mQuery;
    }

    public void setQuery(String mQuery) {
        this.mQuery = mQuery;
    }

    public Uri getUri() {
        return mUri;
    }
}
