package com.fenix.audioplayer.data;

import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

/**
 * Created by fenix on 15.08.2015.
 */
public class SongData {
    private String autor;
    private String songName;
    private String album;
    private String data;
    private String title;
    private String duration;
    private Integer mPosition;

    private final static String TEST = "mySerActivity";

    public SongData(String autor, String songName, String album, String data, String title, String duration) {
        this.autor = autor;
        this.songName = songName;
        this.album = album;
        this.data = data;
        this.title = title;
        this.duration=duration;
    }

    public SongData(String autor, String songName, String album, String data, String Title,String duration,Integer mPosition) {
        this.autor = autor;
        this.songName = songName;
        this.album = album;
        this.data = data;
        this.title = Title;
        this.duration=duration;
    }

    //WARNING: MOVE CURSOR TO POSITION BEFORE USING CONSTRUCTOR
    public SongData(Cursor c, Integer mPosition) {
        this(c.getString(c.getColumnIndex(MediaStore.Audio.Media.ARTIST)),
                c.getString(c.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)),
                c.getString(c.getColumnIndex(MediaStore.Audio.Media.ALBUM)),
                c.getString(c.getColumnIndex(MediaStore.Audio.Media.DATA)),
                c.getString(c.getColumnIndex(MediaStore.Audio.Media.TITLE)),
                c.getString(c.getColumnIndex(MediaStore.Audio.Media.DURATION)));
        this.mPosition = mPosition;
    }


    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getPosition() {
        return mPosition;
    }

    public void setPosition(Integer mPosition) {
        this.mPosition = mPosition;
    }

    public String getDuration() {
        return duration;
    }
}
