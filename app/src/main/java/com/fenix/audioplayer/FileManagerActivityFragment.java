package com.fenix.audioplayer;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
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
public class FileManagerActivityFragment extends Fragment implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TEST = "myFileManger";
    private static final int FOLDER_LAYOUT = 1;
    private static final int FILE_LAYOUT = 2;

    private SimpleCursorAdapter mAdapter;
    private ImageButton mBackButton, mSongButton;
    private Button acceptButton, cancelButton;
    private TextView folderName;
    private LinearLayout rootFragmentLL;
    private RecyclerView mItemRecyclerView;

    private Cursor mCursor;
    private RVAdapter mDataAdapter;
    private RecyclerCursorAdapter mCursorAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private LinkedList<String> mArgs = new LinkedList<String>();
    private String mPath;

    private LinkedHashSet<String> folderSet = new LinkedHashSet<String>();
    private LinkedList<DirectoryData> mDirectoryList = new LinkedList<>();
    private Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

    private final String[] projection = new String[]{
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.TITLE_KEY,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_KEY,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ARTIST_KEY,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DISPLAY_NAME
    };


    public FileManagerActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_file_manager, container, false);
        mBackButton = (ImageButton) mView.findViewById(R.id.backButton);
        cancelButton = (Button) mView.findViewById(R.id.button_Accept);
        acceptButton = (Button) mView.findViewById(R.id.button_Cancel);
        folderName = (TextView) mView.findViewById(R.id.folder_name);
        mBackButton.setOnClickListener(this);
        acceptButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
        //changing states (GONE/VISIBLE)
        rootFragmentLL = (LinearLayout) mView.findViewById(R.id.root_fragment_LL);
        //changing RecyclerViewADAPTER
        mItemRecyclerView = (RecyclerView) mView.findViewById(R.id.item_recyclerView);

        return mView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mItemRecyclerView.setLayoutManager(mLinearLayoutManager);

        mCursorAdapter = new RecyclerCursorAdapter(getActivity(), mCursor);

        rootFragmentLL.setVisibility(View.GONE);
        mDataAdapter = new RVAdapter(mDirectoryList);
        mItemRecyclerView.setAdapter(mDataAdapter);


        mDataAdapter.setOnItemClickListener(new RVAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, String s) {
                mPath = s;
                mArgs.clear();
                mArgs.add("%/" + s + "/%");
                mArgs.add("%/" + s + "/%/%");

                mCursorAdapter.swapCursor(searchMedia(mArgs, null));

                folderName.setText(mPath);
                rootFragmentLL.setVisibility(View.VISIBLE);
                mItemRecyclerView.setAdapter(mCursorAdapter);
            }
        });

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCursor = searchMedia(null, null);
        countFolder();
        //Setting "long lifecycle" for fragment
        setRetainInstance(true);
    }


    public void countFolder() {
        //список папок
        do {
            folderSet.add(getFolder(mCursor.getString(mCursor
                    .getColumnIndex(MediaStore.Audio.Media.DATA))));
            Log.d(TEST, mCursor.getString(mCursor
                    .getColumnIndex(MediaStore.Audio.Media.DATA)));
        } while (mCursor.moveToNext());
        //DELETEвивід списку
        Iterator<String> iter = folderSet.iterator();
        while (iter.hasNext()) {
            Log.d(TEST, iter.next());
        }
        //обчислення кількості
        iter = folderSet.iterator();
        while (iter.hasNext()) {
            mPath = iter.next();
            mArgs.clear();
            mArgs.add("%/" + mPath + "/%");
            mArgs.add("%/" + mPath + "/%/%");
            mCursor = searchMedia(mArgs, null);
            mDirectoryList.add(new DirectoryData(mPath.toString(), mCursor.getCount()));
            Log.d(TEST, mArgs.get(0) + " = " + mCursor.getCount());
        }

    }

    public Cursor searchMedia(LinkedList<String> path, String s) {
        Cursor cursor;
        ContentResolver contentResolver = getActivity().getContentResolver();
        String selection = null;
        if (path != null) {
            selection = " ( " + MediaStore.Audio.Media.DATA + "  LIKE ? AND "
                    + MediaStore.Audio.Media.DATA + " NOT LIKE ? )";
            Log.d(TEST, selection + "   " + path.get(0));
        } else {
            path = new LinkedList<String>();
        }
        if (s != null) {
            if (selection != null) {
                selection += " AND ";
            }

            selection += " " + MediaStore.Audio.Media.TITLE + " LIKE ? ";
            path.add(s);
        }
        if (path != null) {
            cursor = contentResolver.query(uri, projection, selection, path.toArray(new String[path.size()]), null);
        } else {
            cursor = contentResolver.query(uri, null, selection, null, null);

        }
        if (cursor == null) {
            Toast.makeText(getActivity(), "По вашому запиту нічого не знайдено", Toast.LENGTH_SHORT).show();
            // query failed, handle error.
        } else if (!cursor.moveToFirst()) {
            Toast.makeText(getActivity(), "Нема музики на девайсі", Toast.LENGTH_LONG).show();
            // no media on the device
        } else {
            //TODO: Change block
            return cursor;
        }
        return null;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backButton:
                rootFragmentLL.setVisibility(View.GONE);
                mItemRecyclerView.setAdapter(mDataAdapter);
                break;
            case R.id.button_Accept:
                getActivity().setResult(
                        getActivity().RESULT_OK,
                        new Intent().putExtra(MainActivity.FOLDER_NAME, mPath));
                getActivity().finish();
                break;
            case R.id.button_Cancel:
                getActivity().setResult(getActivity().RESULT_CANCELED);
                getActivity().finish();
                break;
            default:
                return;
        }
    }

    /*public SimpleAdapter createAdapter(Context context, LinkedList<DirectoryData> data) {
        final String ATTR_FOLDER_NAME = "attr_name";
        final String ATTR_FILE_COUNT = "attr_count";
        ArrayList<Map<String, Object>> mListData = new ArrayList<Map<String, Object>>(data.size());
        Map<String, Object> m;
        for (int i = 0; i < data.size(); i++) {
            m = new HashMap<String, Object>();
            m.put(ATTR_FOLDER_NAME, data.get(i).getFolderName());
            m.put(ATTR_FILE_COUNT, data.get(i).getCount());
            mListData.add(m);
        }

        String[] from = {ATTR_FOLDER_NAME, ATTR_FILE_COUNT};
        int[] to = {R.id.element_folderName, R.id.element_fileCount};
        return new SimpleAdapter(context, mListData, R.layout.folder_layout, from, to);
    }

    public SimpleCursorAdapter createAdapter(Context context, Cursor c) {
        final static String[] coumns = new String {
            MediaStore.Audio.Media.DISPLAY_NAME,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.DURATION
        } ;
    } */


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}

