package com.thoughts.apps.reader.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Daniel on 8/23/13.
 */
public class DataHelper extends SQLiteOpenHelper  {

    public static final String DB_NAME = "clipper.db";

    private static final int DB_VERSION = 1;

    /**
     * The autoincrement key
     */
    public static final String _ID = "_id";

    public static final String TABLE_POSTS = "wp_posts";
    public static final String POST_ID = "post_id";
    public static final String POST_PARENT = "post_parent";
    public static final String POST_SLUG = "post_name";
    public static final String POST_IS_HOME = "post_is_home";
    public static final String POST_CAT_ID = "post_category";
    public static final String POST_LANG = "post_lang";
    public static final String POST_DATE = "post_date";
    public static final String POST_CONTENT = "post_content";
    public static final String POST_EXCERPT = "post_excerpt";
    public static final String POST_TITLE = "post_title";
    public static final String POST_LINK = "guid";
    public static final String POST_TYPE = "post_type";
    public static final String POST_MODE = "post_mode";
    public static final String POST_FEATURED_IMG = "featured_img";
    public static final String POST_VIDEOS = "videos";

    public static final String [] PROJECTION_POSTS = {
            _ID,
            POST_ID,
            POST_PARENT,
            POST_SLUG,
            POST_CAT_ID,
            POST_MODE,
            POST_IS_HOME,
            POST_LANG,
            POST_DATE,
            POST_EXCERPT,
            POST_CONTENT,
            POST_TITLE,
            POST_LINK,
            POST_TYPE,
            POST_VIDEOS,
            POST_FEATURED_IMG
    };

    private static final String DATABASE_CREATE = "create table " + TABLE_POSTS
            + "(" + _ID + " integer primary key autoincrement, "
            + POST_ID + " text, "
            + POST_PARENT + " text not null, "
            + POST_SLUG + " text, "
            + POST_CAT_ID + " text, "
            + POST_MODE + " text, "
            + POST_IS_HOME + " text, "
            + POST_LANG + " text not null, "
            + POST_DATE + " text, "
            + POST_EXCERPT + " text, "
            + POST_CONTENT + " text, "
            + POST_TITLE + " text, "
            + POST_LINK + " text not null, "
            + POST_TYPE + " text not null, "
            + POST_VIDEOS + " text, "
            + POST_FEATURED_IMG + " text);"
            ;

    public DataHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_POSTS);
        onCreate(sqLiteDatabase);
    }
}
