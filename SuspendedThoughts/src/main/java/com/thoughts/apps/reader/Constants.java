package com.thoughts.apps.reader;

import android.util.Log;

/**
 * Created by Daniel on 8/17/13.
 */
public class Constants {

    private static final String LOGTAG = "Tiempocio Android";

    public static boolean isTablet = false;

    public static void logMessage (String message) {
        if(BuildConfig.DEBUG) {
            Log.v(LOGTAG, message);
        }
    }
}
