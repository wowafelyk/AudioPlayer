package com.fenix.audioplayer.data;

/**
 * Created by fenix on 15.08.2015.
 */
public class SongData {
    private String autor;
    private String songName;
    private String album;
    private String DATA;
    private String Title;
    private Integer mPosition;

    public SongData(String autor, String songName, String album, String DATA, String Title) {
        this.autor = autor;
        this.songName = songName;
        this.album = album;
        this.DATA = DATA;
        this.Title=Title;

    }

    public SongData() {
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

    public String getDATA() {
        return DATA;
    }

    public void setDATA(String DATA) {
        this.DATA = DATA;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public Integer getPosition() {
        return mPosition;
    }

    public void setPosition(Integer mPosition) {
        this.mPosition = mPosition;
    }
}