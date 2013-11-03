package com.thoughts.apps.reader.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;
import com.thoughts.apps.reader.Constants;
import com.thoughts.apps.reader.R;
import com.thoughts.apps.reader.database.DataHelper;

public class PostsCursorAdapter extends SimpleCursorAdapter {

    private LayoutInflater mInflater;
    private int mRootLayout;
    Resources mResources;

    public PostsCursorAdapter(Context context, int layout, Cursor c,
                              String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRootLayout = layout;
        mResources = context.getResources();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View container = mInflater.inflate(mRootLayout, null);

        ViewHolder mViewHolder = new ViewHolder();

        mViewHolder.mPostDate = (TextView) container.findViewById(R.id.post_date);
        mViewHolder.mPostTitle = (TextView) container.findViewById(R.id.post_title);
        mViewHolder.mPostContent = (TextView) container.findViewById(R.id.post_content);
        mViewHolder.mPostImage = (ImageView) container.findViewById(R.id.post_image);

        container.setTag(mViewHolder);

        return container;
    }

    @Override
    public void bindView(View view, Context arg1, Cursor arg2) {
        ViewHolder mViewHolder = (ViewHolder) view.getTag();

        if (mViewHolder == null) {
            mViewHolder = new ViewHolder();
            mViewHolder.mPostDate = (TextView) view.findViewById(R.id.post_date);
            mViewHolder.mPostTitle = (TextView) view.findViewById(R.id.post_title);
            mViewHolder.mPostContent = (TextView) view.findViewById(R.id.post_content);
            mViewHolder.mPostImage = (ImageView) view.findViewById(R.id.post_image);
            view.setTag(mViewHolder);
        }

        String featuredImageUrl = mCursor.getString(mCursor.getColumnIndex(DataHelper.POST_FEATURED_IMG));
        if (featuredImageUrl != null && featuredImageUrl != "") {
            mViewHolder.mPostImage.setVisibility(View.VISIBLE);

            /* Out of Memory Error
            Ion.with(mContext, featuredImageUrl)
                    .withBitmap()
                    .intoImageView(mViewHolder.mPostImage);

            */

            Ion.with(mContext)
                    .load(featuredImageUrl)
                    .withBitmap()
                    .resize(128, 128)
                    .intoImageView(mViewHolder.mPostImage);
        }
        else {
            mViewHolder.mPostImage.setVisibility(View.GONE);
        }

        mViewHolder.mPostDate.setText(mCursor.getString(mCursor.getColumnIndex(DataHelper.POST_DATE)).toUpperCase());
        mViewHolder.mPostTitle.setText(mCursor.getString(mCursor.getColumnIndex(DataHelper.POST_TITLE)));
        mViewHolder.mPostContent.setText(mCursor.getString(mCursor.getColumnIndex(DataHelper.POST_EXCERPT)) + "...");
    }

    public String getString (int pos, String columnName) {
        if (mCursor == null || mCursor.getCount() == 0 || mCursor.getCount() < pos) {
            return "";
        }

        mCursor.moveToPosition(pos);
        return mCursor.getString(mCursor.getColumnIndex(columnName));
    }

    /**
     * Class used to store the references to Views in the container.
     * The usage of this ViewHolder patterns helps reduce the time in which the list is drawn.
     */
    private class ViewHolder {
        TextView mPostTitle;
        TextView mPostDate;
        TextView mPostContent;
        ImageView mPostImage;
    }
}
