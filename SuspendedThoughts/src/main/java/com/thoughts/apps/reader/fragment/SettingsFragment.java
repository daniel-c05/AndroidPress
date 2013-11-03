package com.thoughts.apps.reader.fragment;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.thoughts.apps.reader.R;

/**
 * Created by Daniel on 8/19/13.
 */
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }
}
