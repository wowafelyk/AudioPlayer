package com.fenix.audioplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.LinkedList;

import static com.fenix.audioplayer.data.HelperClass.timeFormat;

public class MyService extends Service {

    private NotificationManager mNM;
    private MediaPlayer mMediaPlayer;



    private LinkedList<String> mListOfSong;
    private Integer mPosition;
    private Integer mProgress;


    private int NOTIFICATION = R.string.local_service_started;
    private boolean mPlay=false;
    private boolean mLooping=false;


    private final String TEST = "myService";




    public MyService() {
    }

    public class LocalBinder extends Binder {
        MyService getService(){
            return MyService.this;
        }
    }

    @Override
    public void onCreate() {
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        // Display a notification about us starting.  We put an icon in the status bar.
        showNotification();
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
    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.local_service_started);

        //Notification notification = new Notification.Builder(this)
        //        .setContentTitle()


        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(R.drawable.play_action, text,
                System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, "some text46536",
                text, contentIntent);

        // Send the notification.
        mNM.notify(NOTIFICATION, notification);
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

        Log.d(TEST, "start play");
        if(position!=null){
            mPosition=position;
            resetMP();
            try{
                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setDataSource(mListOfSong.get(position));
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.prepare();
                mMediaPlayer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            if (mMediaPlayer != null) mMediaPlayer.start();
        }
        mPlay = true;


        mMediaPlayer.setLooping(mLooping);
        //TODO: set onCompletionListener
        //mMediaPlayer.setOnCompletionListener(this);
    }

    public void stopPlay() {
        mMediaPlayer.pause();
        mMediaPlayer.seekTo(0);
        mPlay = false;
    }

    public void pausePlay(){
        mMediaPlayer.pause();
        mPlay = false;
    }

    public void setSongList(LinkedList<String> list) {
        mListOfSong=list;
    }

    public void setProgress(int i){
        mProgress = i;
    }

    public void getProgress(int i){
        mProgress = i;
    }
}
