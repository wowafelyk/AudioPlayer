package com.fenix.audioplayer;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by fenix on 18.08.2015.
 */
public class SettingFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preference);
    }

}
