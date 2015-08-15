package com.fenix.audioplayer;

import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * Created by fenix on 14.08.2015.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.SongHolder> {
    //TODO: add OnClickListener

    private final static String TEST = "myLog-RecyclerAdapter";
    private static OnItemClickListener mListener;
    Cursor cursor;

    RecyclerViewAdapter(Cursor c) {
        this.cursor = c;
    }

    public interface OnItemClickListener {
        void onItemClick(View v, SongData data);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }


    @Override
    public SongHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.songlayout, viewGroup, false);
        //TODO: set layout parameters
        SongHolder songHolder = new SongHolder(v);

        return songHolder;
    }

    @Override
    public void onBindViewHolder(SongHolder songHolder, int i) {
        cursor.moveToPosition(i);
        SongData data = new SongData(
                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)),
                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)),
                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)),
                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));

        songHolder.songName.setText(data.getSongName());
        songHolder.autor.setText(data.getAutor());
        songHolder.duration.setText(MainActivity.timeFormat(cursor.getString(
                cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))));

        songHolder.button.setOnClickListener(new Listener(data));
    }



    @Override
    public int getItemCount() {
        Log.d(TEST, "count=" + (cursor.getCount()));
        return (cursor.getCount());
    }

    public static class SongHolder extends RecyclerView.ViewHolder {

        ImageButton button;
        TextView songName, autor, duration;


        public SongHolder(View itemView) {
            super(itemView);
            button = (ImageButton) itemView.findViewById(R.id.startButton);
            songName = (TextView) itemView.findViewById(R.id.textSongName);
            autor = (TextView) itemView.findViewById(R.id.textAuthor);
            duration = (TextView) itemView.findViewById(R.id.textDuration);

        }
    }
    class Listener implements View.OnClickListener {
        private SongData data;
        Listener(SongData d){
            this.data = d;
        }

        @Override
        public void onClick(View v) {
            mListener.onItemClick(v,data);
        }
    }
}
