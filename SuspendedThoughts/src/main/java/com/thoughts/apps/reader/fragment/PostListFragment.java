package com.thoughts.apps.reader.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.thoughts.apps.reader.Constants;
import com.thoughts.apps.reader.R;
import com.thoughts.apps.reader.WordPressHelper;
import com.thoughts.apps.reader.adapter.PostsCursorAdapter;
import com.thoughts.apps.reader.database.DataHelper;
import com.thoughts.apps.reader.database.DataManager;
import com.thoughts.apps.reader.model.SinglePost;

import java.util.List;

/**
 * Created by Daniel on 8/16/13.
 */
public class PostListFragment extends Fragment {

    public static final String ACTION_POSTS_STORED = "com.thoughts.apps.tiempocio.posts_stored";
    public static final String KEY_LIST_POSITION = "posts:position";

    private static final String [] FROM = {
            DataHelper.POST_TITLE,
            DataHelper.POST_DATE,
            DataHelper.POST_CONTENT
    };

    private static final int [] TO = {
            R.id.post_title,
            R.id.post_date,
            R.id.post_content
    };

    public interface OnPostSelectedListener {
        public void onPostSelected (String slug);
        public void onPostsLoaded (String firstPostSlug);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ACTION_POSTS_STORED)) {
                //The posts have been saved, use the adapter now.
                isSupposedToBeRefreshing = false;
                //setRefreshing(false);
                fillListView();
                showProgress(mContext, mProgressContainer, mListView, false);
            }
        }
    };

    ListView mListView;
    private View mProgressContainer;
    PostsCursorAdapter mPostsAdapter;
    private int mDisplayMode = 0;
    private String mRequestUri;
    private String mLang;
    private int mCategoryId;
    private Context mContext;
    private OnPostSelectedListener mCallback;
    private boolean isSupposedToBeRefreshing = false;
    private boolean isloading = false;
    private int mMinItemsOnPage = 3;

    /* For saving and restoring state */
    SharedPreferences mPreferences;
    SharedPreferences.Editor mEditor;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
        mCallback = (OnPostSelectedListener) activity;

        //register the broadcast receiver
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(ACTION_POSTS_STORED);
        activity.registerReceiver(mBroadcastReceiver, mIntentFilter);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
    }

    private void saveState () {
        mEditor = mPreferences.edit();
        mEditor.putInt(WordPressHelper.KEY_DISPLAY_MODE, mDisplayMode);
        mEditor.commit();
    }

    @Override
    public void onPause() {
        saveState();
        super.onPause();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mDisplayMode = args.getInt(WordPressHelper.KEY_DISPLAY_MODE, WordPressHelper.DISPLAY_HOME);
        mLang = WordPressHelper.getPreferredLanguage();
        mCategoryId = WordPressHelper.getCategoryId(mDisplayMode, mLang);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_list, null);

        mListView = (ListView) view.findViewById(R.id.posts_list);
        mListView.setEmptyView(view.findViewById(R.id.empty));

        mProgressContainer = view.findViewById(R.id.progress_container);

        if (DataManager.arePostsAvailable(mContext, mDisplayMode, mLang)) {
            Constants.logMessage("Posts are available offline, not refreshing.");
            fillListView();
        }
        else {
            Constants.logMessage("Getting stuff from the internetz.");
            mRequestUri = WordPressHelper.getRequestUri(mDisplayMode, mLang, 0);
            getPosts(mRequestUri, true);
        }

        return view;
    }

    @Override
    public void onDetach() {
        //Detaching basically means we are no longer active, remove the receiver
        mContext.unregisterReceiver(mBroadcastReceiver);
        super.onDetach();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.post_list_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                //No Offset since we are trying to get most recent posts
                getPosts(WordPressHelper.getRequestUri(mDisplayMode, mLang, 0), false);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void getPosts(String requestUri, final boolean showBigLoadingDrawable) {

        Constants.logMessage("Request URi: "  + requestUri);

        mListView.getEmptyView().setVisibility(View.GONE);
        isloading = true;
        if (showBigLoadingDrawable)
            showProgress(mContext, mProgressContainer, mListView, true);

        try {
            Ion.with(mContext, requestUri)
                    .as(new TypeToken<List<SinglePost>>() {
                    })
                    .setCallback(new FutureCallback<List<SinglePost>>() {
                        @Override
                        public void onCompleted(Exception e, List<SinglePost> posts) {
                            isloading = false;
                            if (e != null) {
                                Constants.logMessage("Failed to fetch posts: " + e.toString());
                                if (showBigLoadingDrawable)
                                    showProgress(mContext, mProgressContainer, mListView, false);
                                mListView.getEmptyView().setVisibility(View.VISIBLE);
                                return;
                            }
                            DataManager.savePosts(mContext, posts, mDisplayMode, mLang, ACTION_POSTS_STORED);
                        }
                    });
        }
        catch (Exception exception) {
            isloading = false;
            Constants.logMessage("Failed to fetch posts: " + exception.toString());
            showProgress(mContext, mProgressContainer, mListView, false);
            mListView.getEmptyView().setVisibility(View.VISIBLE);
        }
    }

    public void fillListView () {

        mListView.getEmptyView().setVisibility(View.VISIBLE);
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
                   //Do Nothing
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                //Refresh List
                int loadedItems = firstVisibleItem + visibleItemCount;
                if((loadedItems == totalItemCount) && !isloading && totalItemCount > mMinItemsOnPage){
                    Constants.logMessage("Refreshing Posts");
                    getPosts(WordPressHelper.getRequestUri(mDisplayMode, mLang, totalItemCount), false);
                }
            }
        });
        mPostsAdapter = new PostsCursorAdapter(mContext, R.layout.single_post, DataManager.getPosts(mContext, mDisplayMode, mLang), FROM, TO, 0);
        mListView.setAdapter(mPostsAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mCallback.onPostSelected(mPostsAdapter.getString(i, DataHelper.POST_SLUG));
            }
        });

        mCallback.onPostsLoaded(mPostsAdapter.getString(0, DataHelper.POST_SLUG));
        mRequestUri = WordPressHelper.getRequestUri(mDisplayMode, mLang, mPostsAdapter.getCount());
    }

    private void showProgress (final Context context, final View progress, final ListView results, final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = context.getResources().getInteger(
                    android.R.integer.config_shortAnimTime);

            progress.setVisibility(View.VISIBLE);
            progress.animate().setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            progress.setVisibility(show ? View.VISIBLE
                                    : View.GONE);
                        }
                    });

            results.setVisibility(View.VISIBLE);
            results.animate().setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            results.setVisibility(show ? View.GONE
                                    : View.VISIBLE);
                        }
                    });
        } else {
            progress.setVisibility(show ? View.VISIBLE : View.GONE);
            results.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

}
