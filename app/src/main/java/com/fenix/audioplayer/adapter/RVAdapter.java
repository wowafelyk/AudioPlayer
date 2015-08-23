package com.fenix.audioplayer.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fenix.audioplayer.R;
import com.fenix.audioplayer.data.DirectoryData;

import java.util.LinkedList;

/**
 * Created by fenix on 14.08.2015.
 */
public class RVAdapter extends RecyclerView.Adapter<RVAdapter.DirHolder> {


    private final static String TEST = "myLog-RecyclerAdapter";
    private OnItemClickListener mListener;
    private LinkedList<DirectoryData> mData;

    public RVAdapter(LinkedList<DirectoryData> data) {
        this.mData = data;
    }

    public interface OnItemClickListener {
        void onItemClick(View v, String s);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }


    @Override
    public DirHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.folder_layout, viewGroup, false);

        DirHolder dirHolder = new DirHolder(v);

        return dirHolder;
    }

    @Override
    public void onBindViewHolder(DirHolder dirHolder, int i) {

        DirectoryData d = mData.get(i);
        dirHolder.folderName.setText(d.getFolderName());
        dirHolder.duration.setText(""+d.getCount());
        dirHolder.rootOnClick.setOnClickListener(new Listener(d.getFolderName()));
    }


    @Override
    public int getItemCount() {
        Log.d(TEST, "count=" + (mData.size()));
        return (mData.size());
    }

    public static class DirHolder extends RecyclerView.ViewHolder {

        TextView folderName, duration;
        RelativeLayout rootOnClick;

        public DirHolder(View itemView) {
            super(itemView);
            folderName = (TextView) itemView.findViewById(R.id.element_folderName);
            duration = (TextView) itemView.findViewById(R.id.element_fileCount);
            rootOnClick = (RelativeLayout) itemView.findViewById(R.id.folder_relativ_root);
        }
    }
    class Listener implements View.OnClickListener {
        private String folderName;
        Listener(String folderName){
            this.folderName = folderName;
        }

        @Override
        public void onClick(View v) {
            mListener.onItemClick(v,folderName);
        }
    }

    //TODO:change delete if not need
    /*public void setData(LinkedList<DirectoryData> d,boolean b){
        if(b==true) {
            mData.clear();
            this.mData = d;
            notifyDataSetChanged();
        }else{
            mData.addAll(d);
            notifyDataSetChanged();
        }
    } */
}
