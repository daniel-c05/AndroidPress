package com.thoughts.apps.reader;

import android.app.Activity;
import android.os.Bundle;

import com.thoughts.apps.reader.fragment.SettingsFragment;

/**
 * Created by Daniel on 8/19/13.
 */
public class Settings extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}