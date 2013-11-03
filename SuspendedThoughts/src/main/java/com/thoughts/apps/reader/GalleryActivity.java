package com.thoughts.apps.reader;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;
import com.thoughts.apps.reader.database.DataHelper;
import com.thoughts.apps.reader.database.DataManager;
import com.thoughts.apps.reader.fragment.SinglePostFragment;

public class GalleryActivity extends Activity {

    String mImageLink, mTitle;

    Cursor mCursor;
    TextView mTitleView;
    ViewPager mViewPager;

    boolean isOldApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        Bundle extras = getIntent().getExtras();

        mViewPager = (ViewPager) findViewById(R.id.pager);

        if (extras != null) {
            mCursor = DataManager.getAttachments(this, extras.getInt(SinglePostFragment.EXTRA_ID));
            mTitle = extras.getString(SinglePostFragment.EXTRA_TITLE);
            getActionBar().setTitle(mTitle);
            setGallery(mCursor);
        }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gallery, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.action_download_photo:
                downloadPhoto();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void downloadPhoto () {
        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request mRequest = new DownloadManager.Request(Uri.parse(mImageLink));
        mRequest.setDescription(getString(R.string.download_description));
        mRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        downloadManager.enqueue(mRequest);
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

            Ion.with(GalleryActivity.this)
                    .load(mAttachments.getString(mAttachments.getColumnIndex(DataHelper.POST_LINK)))
                    .withBitmap()
                    .intoImageView(mGalleryItem);

            // Now just add PhotoView to ViewPager and return it
            container.addView(mGalleryItem, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

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
