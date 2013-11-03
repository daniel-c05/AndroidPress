package com.thoughts.apps.reader.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.thoughts.apps.reader.Constants;
import com.thoughts.apps.reader.WordPressHelper;
import com.thoughts.apps.reader.fragment.SinglePostFragment;
import com.thoughts.apps.reader.model.SinglePost;

import java.util.List;

/**
 * Created by Daniel on 8/23/13.
 */
public class DataManager {

    private static DataHelper mHelper;
    private static SQLiteDatabase mDatabase;

    private static void open (Context context) {
        if (mDatabase != null && mDatabase.isOpen()) {
            return;
        }
        try {
            mHelper = new DataHelper(context);
            mDatabase = mHelper.getWritableDatabase();
        } catch (Exception e) {
            Constants.logMessage(e.toString());
        }
    }

    private static void close () {
        if (mDatabase != null) {
            mDatabase.close();
        }
    }

    public static void destroyDatabase(final Context context) {
        if (mDatabase == null || !mDatabase.isOpen())
            open(context);

        Constants.logMessage("Deleting posts");
        mDatabase.delete(DataHelper.TABLE_POSTS, null, null);
    }

    public static void savePost (final Context context, final SinglePost post, int displayMode) {
        if (mDatabase == null || !mDatabase.isOpen())
            open(context);

        String where = DataHelper.POST_ID + " = '" + post.getPostID() + "'";
        Cursor cursor = mDatabase.query(DataHelper.TABLE_POSTS, new String [] {DataHelper.POST_ID}, where, null, null, null, null);
        ContentValues values = new ContentValues();

        values.put(DataHelper.POST_CONTENT, post.getPostContent());

        String videos = "";
        int videoCount = 0;

        if (post.getVideos() != null) {
            videoCount = post.getVideos().length;
            if (videoCount > 0) {
                for (int y = 0; y < videoCount; y++) {
                    videos = videos.concat(post.getVideos()[y]);
                    if (y < videoCount-1)
                        videos = videos.concat(",");
                }
            }
        }

        values.put(DataHelper.POST_VIDEOS, videos);

        if (cursor != null) {
            if (!cursor.moveToFirst()) {
                //Here, this item has never been stored
                values.put(DataHelper.POST_ID, post.getPostID());
                values.put(DataHelper.POST_TITLE, post.getPostTitle());
                values.put(DataHelper.POST_CAT_ID, post.getPostCategory());
                values.put(DataHelper.POST_IS_HOME, 0);
                values.put(DataHelper.POST_MODE, displayMode);
                values.put(DataHelper.POST_LANG, WordPressHelper.getPreferredLanguage());
                values.put(DataHelper.POST_SLUG, post.getPostSlug());
                values.put(DataHelper.POST_DATE, post.getPostDate());
                values.put(DataHelper.POST_FEATURED_IMG, post.getFeaturedImgUrl());
                values.put(DataHelper.POST_LINK, post.getPostLink());
                values.put(DataHelper.POST_TYPE, post.getPostType());
            }
            else {
                //Item was previously stored, update it.
                //Sometimes the banner image will be added later, as the article is updated, sometimes is removed, etc.
                mDatabase.update(DataHelper.TABLE_POSTS, values, where, null);
            }
        }

        Intent intent = new Intent(SinglePostFragment.ACTION_SINGLE_POST_STORED);
        context.sendBroadcast(intent);

    }

    public static void savePosts(final Context context, final List<SinglePost> posts, int displayMode, String language, String intentAction) {
        if (mDatabase == null || !mDatabase.isOpen())
            open(context);

        int length = posts.size();

        int isHome;

        if (displayMode == WordPressHelper.DISPLAY_HOME) {
            displayMode = -1;
            isHome = 1;
        }
        else {
            isHome = 0;
        }

        String where;
        Cursor mCursor;
        ContentValues mValues;

        for (int i = 0; i < length; i++) {
            where = DataHelper.POST_ID + " = '" + posts.get(i).getPostID() + "' AND "
                    + DataHelper.POST_LANG + " = '" + language + "'";
            mCursor = mDatabase.query(DataHelper.TABLE_POSTS, new String [] {DataHelper.POST_ID}, where, null, null, null, null);
            mValues = new ContentValues();
            mValues.put(DataHelper.POST_ID, posts.get(i).getPostID());
            mValues.put(DataHelper.POST_PARENT, posts.get(i).getPostParent());
            mValues.put(DataHelper.POST_TITLE, posts.get(i).getPostTitle());
            mValues.put(DataHelper.POST_CAT_ID, posts.get(i).getPostCategory());
            mValues.put(DataHelper.POST_MODE, displayMode);
            mValues.put(DataHelper.POST_LANG, language);
            mValues.put(DataHelper.POST_SLUG, posts.get(i).getPostSlug());
            mValues.put(DataHelper.POST_DATE, posts.get(i).getPostDate());
            mValues.put(DataHelper.POST_EXCERPT, posts.get(i).getPostContent());
            mValues.put(DataHelper.POST_FEATURED_IMG, posts.get(i).getFeaturedImgUrl());
            mValues.put(DataHelper.POST_LINK, posts.get(i).getPostLink());
            mValues.put(DataHelper.POST_TYPE, posts.get(i).getPostType());

            if (!mCursor.moveToFirst()) {
                //Here, this item has never been stored, insert it.
                //Only save this when it's a new item.
                mValues.put(DataHelper.POST_IS_HOME, isHome);
                mDatabase.insert(DataHelper.TABLE_POSTS, null, mValues);
            }
            else {
                //Just update stuff
                mDatabase.update(DataHelper.TABLE_POSTS, mValues, where, null);
            }
        }

        Intent intent = new Intent(intentAction);
        context.sendBroadcast(intent);
    }

    public static void saveAttachments(final Context context, final List<SinglePost> posts, int displayMode, String language, String intentAction) {
        if (mDatabase == null || !mDatabase.isOpen())
            open(context);

        int length = posts.size();

        Constants.logMessage("Saving " + length + " images.");

        int isHome;

        if (displayMode == WordPressHelper.DISPLAY_HOME) {
            displayMode = -1;
            isHome = 1;
        }
        else {
            isHome = 0;
        }

        String where;
        Cursor mCursor;
        ContentValues mValues;
        String link; //Will need to replace a string on each link so we need this.

        for (int i = 0; i < length; i++) {
            link = getBiggerImageLink(posts.get(i).getPostLink());
            where = DataHelper.POST_LINK + " = '" + link + "' AND "
                    + DataHelper.POST_LANG + " = '" + language + "'";
            mCursor = mDatabase.query(DataHelper.TABLE_POSTS, new String [] {DataHelper.POST_PARENT}, where, null, null, null, null);
            mValues = new ContentValues();
            mValues.put(DataHelper.POST_ID, posts.get(i).getPostID());
            mValues.put(DataHelper.POST_PARENT, posts.get(i).getPostParent());
            mValues.put(DataHelper.POST_TITLE, posts.get(i).getPostTitle());
            mValues.put(DataHelper.POST_CAT_ID, posts.get(i).getPostCategory());
            mValues.put(DataHelper.POST_MODE, displayMode);
            mValues.put(DataHelper.POST_LANG, language);
            mValues.put(DataHelper.POST_SLUG, posts.get(i).getPostSlug());
            mValues.put(DataHelper.POST_DATE, posts.get(i).getPostDate());
            mValues.put(DataHelper.POST_EXCERPT, posts.get(i).getPostContent());
            mValues.put(DataHelper.POST_LINK, link);
            mValues.put(DataHelper.POST_TYPE, posts.get(i).getPostType());

            if (!mCursor.moveToFirst()) {
                //Here, this item has never been stored, insert it.
                //Only save this when it's a new item.
                mValues.put(DataHelper.POST_IS_HOME, isHome);
                mDatabase.insert(DataHelper.TABLE_POSTS, null, mValues);
            }
            else {
                //Just update stuff
                mDatabase.update(DataHelper.TABLE_POSTS, mValues, where, null);
            }
        }

        Intent intent = new Intent(intentAction);
        context.sendBroadcast(intent);
    }

    private static String getBiggerImageLink (String link) {
        if (link.contains("-150x150")) {
            return link.replace("-150x150", "");
        }
        return link;
    }

    public static Cursor getPosts(final Context context, int displayMode, String lang) {
        if (mDatabase == null || !mDatabase.isOpen())
            open(context);

        String where = displayMode == WordPressHelper.DISPLAY_HOME? DataHelper.POST_TYPE + " = 'post' AND " + DataHelper.POST_IS_HOME
                + " = '1' AND " + DataHelper.POST_LANG + " = '" + lang + "'" : DataHelper.POST_TYPE + " = 'post' AND " + DataHelper.POST_MODE + " = '" + displayMode
                + "' AND " + DataHelper.POST_LANG + " = '" + lang + "'";

        return mDatabase.query(DataHelper.TABLE_POSTS,
                DataHelper.PROJECTION_POSTS, where, null, null, null, DataHelper.POST_DATE + " DESC");
    }

    public static Cursor getAttachments(final Context context, int postParent) {
        if (mDatabase == null || !mDatabase.isOpen())
            open(context);

        String where = DataHelper.POST_TYPE + " = 'attachment' AND " + DataHelper.POST_PARENT + " = '" + postParent + "'";

        return mDatabase.query(DataHelper.TABLE_POSTS,
                DataHelper.PROJECTION_POSTS, where, null, null, null, DataHelper.POST_PARENT + " DESC");
    }

    public static boolean arePostsAvailable (final Context context, int displayMode, String lang) {

        if (mDatabase == null || !mDatabase.isOpen())
            open(context);

        String where = displayMode == WordPressHelper.DISPLAY_HOME? DataHelper.POST_TYPE + " = 'post' AND " + DataHelper.POST_IS_HOME
                + " = '1' AND " + DataHelper.POST_LANG + " = '" + lang + "'" : DataHelper.POST_TYPE + " = 'post' AND " + DataHelper.POST_MODE + " = '" + displayMode
                + "' AND " + DataHelper.POST_LANG + " = '" + lang + "'";

        Cursor mCursor = mDatabase.query(DataHelper.TABLE_POSTS, new String [] {DataHelper._ID}, where, null, null, null, DataHelper.POST_DATE + " DESC");

        if (mCursor == null || mCursor.getCount() == 0) {
            return false;
        }

        else {
            mCursor.close();
            return true;
        }
    }

    public static boolean isPostAvailable (final Context context, String postSlug, String lang) {

        if (mDatabase == null || !mDatabase.isOpen())
            open(context);

        String where = DataHelper.POST_SLUG + " = '" + postSlug
                + "' AND " + DataHelper.POST_LANG + " = '" + lang
                + "' AND " + DataHelper.POST_CONTENT + " <> ''";

        Cursor mCursor = mDatabase.query(DataHelper.TABLE_POSTS, new String [] {DataHelper.POST_CONTENT}, where, null, null, null, DataHelper.POST_DATE + " DESC");

        if (mCursor == null || mCursor.getCount() == 0) {
            return false;
        }

        else {
            mCursor.close();
            return true;
        }
    }

    public static boolean isPostAvailable (final Context context, int postId, String lang) {

        if (mDatabase == null || !mDatabase.isOpen())
            open(context);

        String where = DataHelper.POST_ID + " = '" + postId
                + "' AND " + DataHelper.POST_LANG + " = '" + lang
                + "' AND " + DataHelper.POST_CONTENT + " <> ''";

        Cursor mCursor = mDatabase.query(DataHelper.TABLE_POSTS, new String [] {DataHelper.POST_CONTENT}, where, null, null, null, DataHelper.POST_DATE + " DESC");

        if (mCursor == null || mCursor.getCount() == 0) {
            return false;
        }

        else {
            mCursor.close();
            return true;
        }
    }

    public static Cursor getPost (final Context context, String postSlug, String lang, String postType) {

        if (mDatabase == null || !mDatabase.isOpen())
            open(context);

        String where = DataHelper.POST_SLUG + " = '" + postSlug + "' AND " + DataHelper.POST_LANG + " = '" + lang + "' AND " + DataHelper.POST_TYPE + " = '" + postType + "'";
        return mDatabase.query(DataHelper.TABLE_POSTS,
                DataHelper.PROJECTION_POSTS, where, null, null, null, DataHelper.POST_DATE + " DESC");
    }

    public static Cursor getPost (final Context context, int postId, String lang, String postType) {

        if (mDatabase == null || !mDatabase.isOpen())
            open(context);

        String where = DataHelper.POST_ID + " = '" + postId + "' AND " + DataHelper.POST_LANG + " = '" + lang + "' AND " + DataHelper.POST_TYPE + " = '" + postType + "'";
        return mDatabase.query(DataHelper.TABLE_POSTS,
                DataHelper.PROJECTION_POSTS, where, null, null, null, DataHelper.POST_DATE + " DESC");
    }
}
