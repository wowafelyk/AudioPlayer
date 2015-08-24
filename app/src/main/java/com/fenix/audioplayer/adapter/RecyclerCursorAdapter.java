package com.fenix.audioplayer.adapter;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.fenix.audioplayer.R;
import com.fenix.audioplayer.data.SongData;

import lib.external.CursorRecyclerViewAdapter;

import static com.fenix.audioplayer.data.HelperClass.*;

/**
 * Created by fenix on 14.08.2015.
 */
public class RecyclerCursorAdapter extends CursorRecyclerViewAdapter<RecyclerCursorAdapter.SongHolder> {

    private OnItemClickListener mListener;
    private Cursor cursor;
    private String mData;

    public void setmData(String mData) {
        this.mData = mData;
        notifyDataSetChanged();
    }

    public RecyclerCursorAdapter(Context context,Cursor cursor) {
        super(context,cursor);
        this.cursor = cursor;
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
    public void onBindViewHolder(SongHolder songHolder, Cursor cursor) {

        SongData data = new SongData(
                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)),
                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)),
                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)),
                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)),
                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)),
                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));
        data.setPosition(cursor.getPosition());
        if(data.getData().equals(mData)) {
            songHolder.button.setImageResource(R.drawable.play_action);
        }else songHolder.button.setImageResource(R.drawable.music_action);

        songHolder.songName.setText(data.getSongName());
        songHolder.autor.setText(data.getAutor());
        songHolder.album.setText(data.getAlbum());
        songHolder.duration.setText(timeFormatMillis(cursor.getString(
                cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))));

        if(mListener!=null) {
            songHolder.button.setOnClickListener(new Listener(data));
        }else songHolder.button.setClickable(false);
    }

    public static class SongHolder extends RecyclerView.ViewHolder {

        ImageButton button;
        TextView songName, autor, duration,album;


        public SongHolder(View itemView) {
            super(itemView);
            button = (ImageButton) itemView.findViewById(R.id.startButton);
            songName = (TextView) itemView.findViewById(R.id.textSongName);
            autor = (TextView) itemView.findViewById(R.id.textAuthor);
            duration = (TextView) itemView.findViewById(R.id.textDuration);
            album = (TextView) itemView.findViewById(R.id.textAlbum);
            album = (TextView) itemView.findViewById(R.id.textAlbum);
        }
    }
    class Listener implements View.OnClickListener {
        private SongData data;
        Listener(SongData d){
            this.data = d;
        }

        @Override
        public void onClick(View v) {
            if(mListener!=null) {
                mListener.onItemClick(v, data);
            }

        }
    }
}
