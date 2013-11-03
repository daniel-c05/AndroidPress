package com.thoughts.apps.reader;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;

import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.thoughts.apps.reader.adapter.DrawerAdapter;
import com.thoughts.apps.reader.database.DataManager;
import com.thoughts.apps.reader.fragment.PostListFragment;
import com.thoughts.apps.reader.fragment.SinglePostFragment;
import com.thoughts.apps.reader.gcm.GcmHelper;
import com.thoughts.apps.reader.model.SinglePost;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends Activity
        implements PostListFragment.OnPostSelectedListener{

    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    /**
     * Substitute you own sender ID here. This is the project number you got
     * from the API Console, as described in "Getting Started."
     */
    String SENDER_ID = "316846442455";

    private DrawerLayout mDrawerLayout;
    private ExpandableListView mDrawerList;
    private DrawerAdapter mDrawerAdapter;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mSlidingOptions;
    private boolean mShowingPostList = true;

    private Fragment mPostsListFragment, mSinglePostFragment;

    /* For saving and restoring state */
    SharedPreferences mPreferences;
    SharedPreferences.Editor mEditor;

    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    SharedPreferences prefs;
    String regid;
    Context context;

    private AdView mAdView;

    /**
     * Checks if we are on landscape mode or not.
     */
    public static boolean isDoublePane = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTitle = mDrawerTitle = getTitle();
        mSlidingOptions = getResources().getStringArray(R.array.all_drawer_items);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ExpandableListView) findViewById(R.id.left_drawer);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener
        mDrawerAdapter = new DrawerAdapter(this);
        mDrawerList.setAdapter(mDrawerAdapter);
        mDrawerList.setOnGroupClickListener(new DrawerGroupClickListener());
        mDrawerList.setOnChildClickListener(new DrawerChildClickListener());

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            selectItem(0, 0, false);
        }

        setupContent();

        context = getApplicationContext();

        // Check device for Play Services APK. If check succeeds, proceed with GCM registration.
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);

            if (regid.isEmpty()) {
                Constants.logMessage("Registering App to GCM on the background");
                registerInBackground();
            }
        } else {
           Constants.logMessage("No valid Google Play Services APK found.");
        }

        setupAds();
    }

    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Constants.logMessage("Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Constants.logMessage("App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Constants.logMessage("This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and the app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;
                    Constants.logMessage(msg);

                    // You should send the registration ID to your server over HTTP, so it
                    // can use GCM/HTTP or CCS to send messages to your app.
                    sendRegistrationIdToBackend();

                    // For this demo: we don't need to send it because the device will send
                    // upstream messages to a server that echo back the message using the
                    // 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                Constants.logMessage("Response: " + msg);
            }
        }.execute(null, null, null);
    }

    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
     * or CCS to send messages to your app. Not needed for this demo since the
     * device sends upstream messages to a server that echoes back the message
     * using the 'from' address in the message.
     */
    private void sendRegistrationIdToBackend() {
        String regUrl = GcmHelper.GCM_SERVER + regid;
        Constants.logMessage("Registration URL is: ");
        Constants.logMessage(regUrl);
        try {
            Ion.with(this, regUrl)
                    .asString()
                    .setCallback(new FutureCallback<String>() {
                        @Override
                        public void onCompleted(Exception e, String s) {
                            if (e != null) {
                                Constants.logMessage("Error storing reg ID on server: " + e.toString());
                            }
                            Constants.logMessage("Completed Post to Server: " + s);
                        }
                    });
        }
        catch (Exception exception) {
            Constants.logMessage("Failed to fetch posts: " + exception.toString());
        }
    }

    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Constants.logMessage("Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);
    }

    private void setupContent() {
        if (findViewById(R.id.content_frame_two) != null) {
            isDoublePane = true;
        }
        else {
            isDoublePane = false;
        }

        showPostsForMode(getLastDisplayMode());
    }

    private void setupAds() {
        // Look up the AdView as a resource and load a request.
        mAdView = (AdView)this.findViewById(R.id.adView);
        mAdView.loadAd(new AdRequest());
    }

    @Override
    public void onBackPressed() {

        if (!mShowingPostList && !isDoublePane) {
            showPostsForMode(getLastDisplayMode());
            mShowingPostList = true;
            return;
        }
        if (mShowingPostList && getLastDisplayMode() != WordPressHelper.DISPLAY_HOME) {
            showPostsForMode(WordPressHelper.DISPLAY_HOME);
            return;
        }

        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private int getLastDisplayMode() {
        if (mPreferences == null) {
            mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        }

        return mPreferences.getInt(WordPressHelper.KEY_DISPLAY_MODE, WordPressHelper.DISPLAY_HOME);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent settings = new Intent(this, Settings.class);
                startActivity(settings);
                return true;
            default:
                return false;
        }
    }

    private void selectItem(int position, int childPositon, boolean isChild) {


        if (isChild) {
            switch (position) {
                case 1:
                    position = childPositon + WordPressHelper.MAIN_NAV_ITEMS_COUNT;
                    break;
                case 2:
                    position = childPositon + WordPressHelper.MAIN_NAV_ITEMS_COUNT + WordPressHelper.MAIN_NAV_ITEMS_COUNT;
                    break;
            }
        }

        mDrawerLayout.closeDrawer(mDrawerList);
        setTitle(mSlidingOptions[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
        switch (position) {
            case WordPressHelper.DISPLAY_ABOUT:
            case WordPressHelper.DISPLAY_CONTACT:
                showSinglePostFragment(position, WordPressHelper.getPageId(position), WordPressHelper.TYPE_PAGE);
                return;
            default:
                showPostsForMode(position);
                return;
        }
    }

    private void showSinglePostFragment(int position, String slug, String postType) {
        //What container should we use?
        int containerId = isDoublePane ? R.id.content_frame_two: R.id.content_frame;

        mSinglePostFragment = new SinglePostFragment();

        Bundle args = new Bundle();
        args.putInt(WordPressHelper.KEY_DISPLAY_MODE, position);
        args.putString(WordPressHelper.PARAM_SLUG, slug);
        args.putString(WordPressHelper.PARAM_TYPE, postType);
        mSinglePostFragment.setArguments(args);

        getFragmentManager().beginTransaction().replace(containerId, mSinglePostFragment, "SinglePost").commit();
        mShowingPostList = false;
    }

    private void showSinglePostFragment(int position, int postId, String postType) {
        //What container should we use?
        int containerId = isDoublePane ? R.id.content_frame_two: R.id.content_frame;

        mSinglePostFragment = new SinglePostFragment();

        Bundle args = new Bundle();
        args.putInt(WordPressHelper.KEY_DISPLAY_MODE, position);
        args.putInt(WordPressHelper.PARAM_POST_ID, postId);
        args.putString(WordPressHelper.PARAM_TYPE, postType);
        mSinglePostFragment.setArguments(args);

        getFragmentManager().beginTransaction().replace(containerId, mSinglePostFragment, "SinglePost").commit();
        mShowingPostList = false;
    }

    private void showPostsForMode(int position) {

        mPostsListFragment = new PostListFragment();

        Bundle args = new Bundle();
        args.putInt(WordPressHelper.KEY_DISPLAY_MODE, position);
        mPostsListFragment.setArguments(args);

        getFragmentManager().beginTransaction().replace(R.id.content_frame, mPostsListFragment, "PostList").commit();

        mShowingPostList = true;
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onPostSelected(String slug) {
        showSinglePostFragment(WordPressHelper.DISPLAY_SINGLE_POST, slug, WordPressHelper.TYPE_POST);
    }

    @Override
    public void onPostsLoaded(String firstPostSlug) {
        if (isDoublePane && firstPostSlug != null) {
            showSinglePostFragment(WordPressHelper.DISPLAY_SINGLE_POST, firstPostSlug, WordPressHelper.TYPE_POST);
        }
    }

    private class DrawerGroupClickListener implements ExpandableListView.OnGroupClickListener {

        @Override
        public boolean onGroupClick(ExpandableListView expandableListView, View view, int position, long l) {
            if (position != 1 && position != 2)
                selectItem(position, 0, false);
            return false;
        }
    }

    private class DrawerChildClickListener implements ExpandableListView.OnChildClickListener {

        @Override
        public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPosition, int childPosition, long l) {
            selectItem(groupPosition, childPosition, true);
            return false;
        }
    }


}