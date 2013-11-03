package com.thoughts.apps.reader.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.database.Cursor;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.text.method.LinkMovementMethod;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.thoughts.apps.reader.Constants;
import com.thoughts.apps.reader.GalleryActivity;
import com.thoughts.apps.reader.R;
import com.thoughts.apps.reader.WordPressHelper;
import com.thoughts.apps.reader.database.DataHelper;
import com.thoughts.apps.reader.database.DataManager;
import com.thoughts.apps.reader.model.SinglePost;
import com.thoughts.apps.reader.ui.HorizontalViewPager;

import java.util.List;

/**
 * Created by Daniel on 8/17/13.
 */
public class SinglePostFragment extends Fragment {

    public static final String ACTION_SINGLE_POST_STORED = "com.thoughts.apps.tiempocio.single_post_stored";
    public static final String ACTION_ATTACHMENTS_STORED = "com.thoughts.apps.tiempocio.attachments_stored";
    public static final String EXTRA_TITLE = "single:title";
    public static final String EXTRA_ID = "single:id";
    public static final String YOUTUBE_BASE = "http://www.youtube.com/watch?v=";
    public static final String THUMB_PREFIX = "http://img.youtube.com/vi/";
    public static final String THUMB_SUFFIX = "/default.jpg";
    private static final int RECOVERY_DIALOG_REQUEST = 1;

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ACTION_SINGLE_POST_STORED)) {
                //The posts have been saved, use the adapter now.
                isSupposedToBeRefreshing = false;
                //setRefreshing(false);
                if (mPostId != -1) {
                    setContent(DataManager.getPost(context, mPostId, mPostLang, mPostType));
                }
                else {
                    setContent(DataManager.getPost(context, mPostSlug, mPostLang, mPostType));
                }
                showProgress(mContext, mProgressContainer, mScrollView, false);
            }
            else if (action.equals(ACTION_ATTACHMENTS_STORED)) {
                Constants.logMessage("REfreshing Gallery");
                setGallery(DataManager.getAttachments(context, mPostId));
            }
        }
    };

    private String mContentUri, mPostUri, mPostSlug, mPostLang, mPostTitle;
    private int mPostId;
    private int mDisplayMode;
    private Context mContext;

    private TextView mPostTitleView, mPostContent, mPostDate;
    private HorizontalViewPager mViewPager, mThumbPager;
    private ScrollView mScrollView;
    private View mProgressContainer;
    private boolean isSupposedToBeRefreshing = false;
    String [] postThumbs;

    /**
     * This can be either a Post or an Attachment.
     */
    private String mPostType;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
        //register the broadcast receiver
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(ACTION_SINGLE_POST_STORED);
        mIntentFilter.addAction(ACTION_ATTACHMENTS_STORED);
        activity.registerReceiver(mBroadcastReceiver, mIntentFilter);
    }

    @Override
    public void onDetach() {
        //Detaching basically means we are no longer active, remove the receiver
        mContext.unregisterReceiver(mBroadcastReceiver);
        super.onDetach();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mDisplayMode = args.getInt(WordPressHelper.KEY_DISPLAY_MODE, WordPressHelper.DISPLAY_ABOUT);
        mPostSlug = args.getString(WordPressHelper.PARAM_SLUG);
        mPostId = args.getInt(WordPressHelper.PARAM_POST_ID, -1);
        mPostType = args.getString(WordPressHelper.PARAM_TYPE, WordPressHelper.TYPE_POST);
        mPostLang = WordPressHelper.getPreferredLanguage();
        mContentUri = mDisplayMode == WordPressHelper.DISPLAY_SINGLE_POST?
                WordPressHelper.getRequestUri(mPostSlug, mPostLang) : WordPressHelper.getRequestUri(mDisplayMode, mPostLang, 0);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_single_post, null);

        mPostTitleView = (TextView) view.findViewById(R.id.post_title);
        mPostContent = (TextView) view.findViewById(R.id.post_content);
        mPostDate = (TextView) view.findViewById(R.id.post_date);

        //handle <a> clicks
        mPostContent.setMovementMethod(LinkMovementMethod.getInstance());

        mViewPager = (HorizontalViewPager) view.findViewById(R.id.gallery_pager);

        mThumbPager = (HorizontalViewPager) view.findViewById(R.id.thumbs_pager);

        mProgressContainer = view.findViewById(R.id.progress_container);
        mScrollView = (ScrollView) view.findViewById(R.id.scrolling_container);


        if (mPostSlug != null) {
            if (DataManager.isPostAvailable(mContext, mPostSlug, mPostLang)) {
                Constants.logMessage("Posts was available, not refreshing.");
                setContent(DataManager.getPost(mContext, mPostSlug, mPostLang, mPostType));
                setGallery(DataManager.getAttachments(mContext, mPostId));
            }
            else {
                Constants.logMessage("Getting post from the internetz.");
                try {
                    getPost();
                }
                catch (Exception exception) {
                    Constants.logMessage(exception.toString());
                    Constants.logMessage("Failed to fetch posts: " + exception.toString());
                    showProgress(mContext, mProgressContainer, mScrollView, false);
                }
            }
        }
        else {
            if (DataManager.isPostAvailable(mContext, mPostId, mPostLang)) {
                setContent(DataManager.getPost(mContext, mPostId, mPostLang, mPostType));
                setGallery(DataManager.getAttachments(mContext, mPostId));
            }
            else {
                try {
                    getPost();
                }
                catch (Exception exception) {
                    Constants.logMessage(exception.toString());
                    Constants.logMessage("Failed to fetch posts: " + exception.toString());
                    showProgress(mContext, mProgressContainer, mScrollView, false);
                }
            }
        }

        return view;
    }

    private void getPost() {
        Constants.logMessage("Post URL: " + mContentUri);
        showProgress(mContext, mProgressContainer, mScrollView, true);
        Ion.with(mContext, mContentUri)
                .as(new TypeToken<List<SinglePost>>() {
                })
                .setCallback(new FutureCallback<List<SinglePost>>() {
                    @Override
                    public void onCompleted(Exception exception, List<SinglePost> posts) {
                        if (exception != null) {
                            Constants.logMessage(exception.toString());
                            Constants.logMessage("Failed to fetch posts: " + exception.toString());
                            showProgress(mContext, mProgressContainer, mScrollView, false);
                            return;
                        }
                        else if (posts.size() == 0) {
                            Constants.logMessage("No data received, check params supplied");
                            showProgress(mContext, mProgressContainer, mScrollView, false);
                            return;
                        }
                        getImages(posts.get(0).getPostID());
                        DataManager.savePost(mContext, posts.get(0), mDisplayMode);
                    }
                });
    }

    private void getImages (String postId) {

        mPostId = Integer.valueOf(postId);

        Ion.with(mContext, WordPressHelper.getAttachmentUri(postId, mPostSlug, mPostLang))
                .as(new TypeToken<List<SinglePost>>() {
                })
                .setCallback(new FutureCallback<List<SinglePost>>() {
                    @Override
                    public void onCompleted(Exception e, List<SinglePost> posts) {
                        if (e != null) {
                            Constants.logMessage(e.toString());
                            return;
                        }
                        Constants.logMessage("Post has " + posts.size() + " images.");
                        DataManager.saveAttachments(mContext, posts, mDisplayMode, mPostLang, ACTION_ATTACHMENTS_STORED);
                    }
                });
    }

    private void setGallery(Cursor attachments) {

        if (attachments == null || attachments.getCount() == 0){
            Constants.logMessage("No image attachments for this post");
            mViewPager.setVisibility(View.GONE);
            return;
        }

        Constants.logMessage("Setting Gallery with " + attachments.getCount() + " images.");

        attachments.moveToFirst();

        mViewPager.setPageTransformer(true, new DepthPageTransformer());
        mViewPager.setAdapter(new SamplePagerAdapter(attachments));

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.single_post_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share:
                startShareDialog();
                return true;
            case R.id.action_refresh:
                getPost();
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void startShareDialog() {

        //Track Share Event
        EasyTracker easyTracker = EasyTracker.getInstance(mContext);

        // MapBuilder.createEvent().build() returns a Map of event fields and values
        // that are set and sent with the hit.
        easyTracker.send(MapBuilder
                .createEvent("User Interaction",     // Event category (required)
                        "Share Article",  // Event action (required)
                        mPostUri,   // Event label
                        null)            // Event value
                .build()
        );

        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_SUBJECT, mPostTitleView.getText().toString());
        share.putExtra(Intent.EXTRA_TEXT, mPostUri);
        share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        startActivity(Intent.createChooser(share, getString(R.string.share_prompt)));

    }

    private void setContent(Cursor post) {
        //unhide container
        showProgress(mContext, mProgressContainer, mScrollView, false);

        if(post == null || post.getCount() == 0)
            return;

        post.moveToFirst();

        mPostId = Integer.valueOf(post.getString(post.getColumnIndex(DataHelper.POST_ID)));

        mPostTitle = post.getString(post.getColumnIndex(DataHelper.POST_TITLE));
        mPostTitleView.setText(mPostTitle);
        mPostContent.setText(Html.fromHtml(post.getString(post.getColumnIndex(DataHelper.POST_CONTENT))));
        mPostDate.setText(getString(R.string.posted_on) + " " + post.getString(post.getColumnIndex(DataHelper.POST_DATE)));
        mPostUri = post.getString(post.getColumnIndex(DataHelper.POST_LINK));

        postThumbs = getPostThumbs(post.getString(post.getColumnIndex(DataHelper.POST_VIDEOS)));
        if (postThumbs != null && postThumbs.length >= 1) {
            mThumbPager.setVisibility(View.VISIBLE);
            mThumbPager.setPageTransformer(true, new DepthPageTransformer());
            mThumbPager.setAdapter(new SampleThumbnailAdapter(postThumbs));
        }
        else {
            mThumbPager.setVisibility(View.GONE);
        }

    }

    private String [] getPostThumbs (String videoStr) {

        if (videoStr.length() < 12 || videoStr == null)
            return null;

        Constants.logMessage("Provided Video String is: " + videoStr);
        char [] chars = videoStr.toCharArray();
        int count = 1;
        for (char c : chars) {
            if (c == ',')
                count++;
        }

        postThumbs = new String[count];

        for (int i = 0; i < count; i++) {
            postThumbs[i] = videoStr.substring((0 + (i * 12)), (11+ (i * 12)));
            Constants.logMessage("Video ID: " + postThumbs[i]);
        }

        return postThumbs;
    }

    private void showProgress (final Context context, final View progress, final ScrollView results, final boolean show) {
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

    class SamplePagerAdapter extends PagerAdapter {

        private Cursor mAttachments;

        public SamplePagerAdapter (Cursor attachments) {
            this.mAttachments = attachments;
            Constants.logMessage("Gallery adapter created with " + mAttachments.getCount() + " items.");
            attachments.moveToFirst();
        }

        @Override
        public int getCount() {
            return mAttachments.getCount();
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {

            mAttachments.moveToPosition(position);

            ImageView mGalleryItem = new ImageView(container.getContext());
            /* This was too aggresive as far as memory usage
            Ion.with(mContext, mAttachments.getString(mAttachments.getColumnIndex(DataHelper.POST_LINK)))
                    .withBitmap()
                    .intoImageView(mGalleryItem);
            */

            Ion.with(mContext)
                    .load(mAttachments.getString(mAttachments.getColumnIndex(DataHelper.POST_LINK)))
                    .withBitmap()
                    .resize(256, 256)
                    .intoImageView(mGalleryItem);

            mGalleryItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Constants.logMessage("ViewPager item clicked");
                    Intent intent = new Intent(getActivity(), GalleryActivity.class);
                    intent.putExtra(EXTRA_ID, mPostId);
                    intent.putExtra(EXTRA_TITLE, mPostTitle);
                    startActivity(intent);
                }
            });

            // Now just add PhotoView to ViewPager and return it
            container.addView(mGalleryItem, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);

            return mGalleryItem;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }

    class SampleThumbnailAdapter extends PagerAdapter {

        private String [] thumbs;

        public SampleThumbnailAdapter (String [] thumbs) {
            this.thumbs = thumbs;
        }

        @Override
        public int getCount() {
            return thumbs.length;
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {

            FrameLayout thumbFrame = new FrameLayout(container.getContext());
            ImageView thumbnail = new ImageView(container.getContext());
            ImageView playOverlay = new ImageView(container.getContext());

            playOverlay.setImageResource(R.drawable.ic_av_play_over_video);
            FrameLayout.LayoutParams mLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER_HORIZONTAL|Gravity.CENTER_HORIZONTAL);
            playOverlay.setLayoutParams(mLayoutParams);

            Constants.logMessage("Setting Thumbnail Image with URL: " + THUMB_PREFIX + postThumbs[position] + THUMB_SUFFIX);

            Ion.with(mContext, THUMB_PREFIX + postThumbs[position] + THUMB_SUFFIX)
                    .withBitmap()
                    .intoImageView(thumbnail);
            thumbnail.setTag(postThumbs[position]);

            thumbFrame.addView(thumbnail, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            thumbFrame.addView(playOverlay, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            thumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String videoId = (String) view.getTag();
                    if (videoId != null && videoId != "") {
                        Intent youtubeIntent = new Intent();
                        youtubeIntent.setAction(Intent.ACTION_VIEW);
                        youtubeIntent.setData(Uri.parse(YOUTUBE_BASE + videoId));
                        youtubeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                        startActivity(youtubeIntent);
                    }
                }
            });

            container.addView(thumbFrame, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            return thumbFrame;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }

    public class DepthPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.75f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);

            } else if (position <= 0) { // [-1,0]
                // Use the default slide transition when moving to the left page
                view.setAlpha(1);
                view.setTranslationX(0);
                view.setScaleX(1);
                view.setScaleY(1);

            } else if (position <= 1) { // (0,1]
                // Fade the page out.
                view.setAlpha(1 - position);

                // Counteract the default slide transition
                view.setTranslationX(pageWidth * -position);

                // Scale the page down (between MIN_SCALE and 1)
                float scaleFactor = MIN_SCALE
                        + (1 - MIN_SCALE) * (1 - Math.abs(position));
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }
    }
}
