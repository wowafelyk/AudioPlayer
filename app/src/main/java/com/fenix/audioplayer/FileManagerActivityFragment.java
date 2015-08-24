package com.fenix.audioplayer;

import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fenix.audioplayer.adapter.RVAdapter;
import com.fenix.audioplayer.adapter.RecyclerCursorAdapter;
import com.fenix.audioplayer.data.DirectoryData;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import static com.fenix.audioplayer.data.HelperClass.*;

/**
 * A placeholder fragment containing a simple view.
 */
public class FileManagerActivityFragment extends Fragment implements View.OnClickListener {


    private ImageButton mBackButton;
    private Button mAcceptButton, mCancelButton;
    private TextView mFolderName;
    private LinearLayout mSelectControl;
    private RecyclerView mItemRecyclerView;

    private Cursor mCursor;
    private RVAdapter mDataAdapter;
    private RecyclerCursorAdapter mCursorAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private static String sSortingOrder;
    private String mPath;

    private LinkedHashSet<String> folderSet = new LinkedHashSet<String>();
    private LinkedList<DirectoryData> mDirectoryList = new LinkedList<>();
    private final Uri mUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

    private final String[] projection = new String[]{
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DISPLAY_NAME
    };


    public FileManagerActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_file_manager, container, false);

        mBackButton = (ImageButton) mView.findViewById(R.id.backButton);
        mCancelButton = (Button) mView.findViewById(R.id.button_Accept);
        mAcceptButton = (Button) mView.findViewById(R.id.button_Cancel);
        mFolderName = (TextView) mView.findViewById(R.id.folder_name);
        mBackButton.setOnClickListener(this);
        mAcceptButton.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);
        mSelectControl = (LinearLayout) mView.findViewById(R.id.root_fragment_LL);
        mItemRecyclerView = (RecyclerView) mView.findViewById(R.id.item_recyclerView);

        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mItemRecyclerView.setLayoutManager(mLinearLayoutManager);
        mCursorAdapter = new RecyclerCursorAdapter(getActivity(), mCursor);
        mSelectControl.setVisibility(View.GONE);
        mDataAdapter = new RVAdapter(mDirectoryList);
        mItemRecyclerView.setAdapter(mDataAdapter);


        mDataAdapter.setOnItemClickListener(new RVAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, String s) {
                mPath = s;
                mCursorAdapter.swapCursor(searchMedia(getArgs(s), null));

                mFolderName.setText(mPath);
                mSelectControl.setVisibility(View.VISIBLE);
                mItemRecyclerView.setAdapter(mCursorAdapter);
            }
        });

        return mView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
            }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCursor = searchMedia(null, null);
        countFolder();
        setRetainInstance(true);
    }


    public void countFolder() {
        //список папок
        do {
            folderSet.add(getFolder(mCursor.getString(mCursor
                    .getColumnIndex(MediaStore.Audio.Media.DATA))));
        } while (mCursor.moveToNext());
        //обчислення кількості
        Iterator<String> iter = folderSet.iterator();
        while (iter.hasNext()) {
            mPath = iter.next();
            mCursor = searchMedia(getArgs(mPath), null);
            mDirectoryList.add(new DirectoryData(mPath.toString(), mCursor.getCount()));
        }

    }

    public Cursor searchMedia(LinkedList<String> path, String s) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean asc = prefs.getBoolean(getString(R.string.pref_sort_order), true);
        String m = prefs.getString(getString(R.string.pref_list_sort), "1");
        if(m.equals("1")) {
            sSortingOrder = asc ? " " + MediaStore.Audio.Media.DISPLAY_NAME + " " + "ASC" :
                    " " + MediaStore.Audio.Media.DISPLAY_NAME + " " + "DESC";
        }else if(m.equals("0")) {
            sSortingOrder = asc ? " " + MediaStore.Audio.Media.ARTIST + " " + " ASC" :
                    " " + MediaStore.Audio.Media.ARTIST + " " + " DESC";
        }else if(m.equals("2")){

            sSortingOrder= asc ?" "+MediaStore.Audio.Media.ALBUM+" "+" ASC":
                    " "+MediaStore.Audio.Media.ALBUM+" "+" DESC";
        }

        Cursor cursor;
        ContentResolver contentResolver = getActivity().getContentResolver();
        String selection = null;
        if (path != null) {
            selection = " ( " + MediaStore.Audio.Media.DATA + "  LIKE ? AND "
                    + MediaStore.Audio.Media.DATA + " NOT LIKE ? )";
            cursor = contentResolver.query(mUri, projection, selection,
                    path.toArray(new String[path.size()]), sSortingOrder);
        } else {
            cursor = contentResolver.query(mUri, projection, null, null, sSortingOrder);
        }

        if (cursor == null) {
            Toast.makeText(getActivity(), "Error. Музику не знайдено", Toast.LENGTH_SHORT).show();
            // query failed, handle error.
        } else if (!cursor.moveToFirst()) {
            Toast.makeText(getActivity(), "По вашому запиту нічого не знайдено", Toast.LENGTH_LONG).show();
            // no media on the device
        } else {
            return cursor;
        }
        return null;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backButton:
                mSelectControl.setVisibility(View.GONE);
                mItemRecyclerView.setAdapter(mDataAdapter);
                break;
            case R.id.button_Accept:
                getActivity().setResult(getActivity().RESULT_OK,
                        new Intent().putExtra(MainActivity.FOLDER_NAME, mPath));
                getActivity().finish();
                break;
            case R.id.button_Cancel:
                getActivity().setResult(getActivity().RESULT_CANCELED);
                getActivity().finish();
                break;
            default:
        }
        return;
    }

}

