package com.fenix.audioplayer;

/**
 * Created by fenix on 15.08.2015.
 */
public class SongData {
    private String autor;
    private String songName;
    private String album;
    private String DATA;

    public SongData(String autor, String songName, String album, String DATA) {
        this.autor = autor;
        this.songName = songName;
        this.album = album;
        this.DATA = DATA;
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
}
