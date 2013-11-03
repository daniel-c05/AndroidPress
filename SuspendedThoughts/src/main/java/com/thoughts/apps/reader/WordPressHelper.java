package com.thoughts.apps.reader;

import android.content.Context;

import java.util.Locale;

/**
 * Created by Daniel on 8/17/13.
 */
public class WordPressHelper {

    public static final String BASE_REQUEST_URI = "http://tiempocio.com/api/request.php?";
    public static final String ATTACHMENTS_URI = "http://tiempocio.com/api/attachments.php?";
    public static final String POSTS_URI = "http://tiempocio.com/api/posts.php?";
    public static final String PAGES_URI = "http://tiempocio.com/api/pages.php?";

    private static final int CAT_ID_INFORMACION_GENERAL = 2;
    private static final int CAT_ID_JUEGOS_DE_COMPUTADORA = 3;
    private static final int CAT_ID_TURISMO = 4;
    private static final int CAT_ID_VIDEO_JUEGOS = 5;
    private static final int CAT_ID_GUIA_TURISTICA = 18;
    private static final int CAT_ID_TOURIST_GUIDE = 19;
    private static final int CAT_ID_RECOMENDACION_DEL_MES = 24;
    private static final int CAT_ID_RECOMMENDATION_OF_THE_MONTH = 26;
    private static final int CAT_ID_RESENA_JUEGOS = 32;
    private static final int CAT_ID_GAME_REVIEWS = 36;
    private static final int CAT_ID_MEDIO_AMBIENTE = 50;
    private static final int CAT_ID_RECOMENDACION_RETRO = 88;
    private static final int CAT_ID_RETRO_RECOMMENDATION = 91;
    private static final int CAT_ID_LIBRO_DE_ARTE = 102;
    private static final int CAT_ID_ART_BOOKS = 104;
    private static final int CAT_ID_BANDAS_SONORAS = 103;
    private static final int CAT_ID_SOUNTRACKS = 105;

    public static final String KEY_DISPLAY_MODE ="display_mode";
    public static final String PARAM_TYPE = "type=";
    public static final String PARAM_SLUG = "slug=";
    public static final String PARAM_POST_ID = "id=";
    public static final String PARAM_CATEGORY = "catid=";
    public static final String PARAM_FEAT_IMG_SIZE = "size=";
    public static final String PARAM_LANG = "lang=";
    public static final String PARAM_OFFSET = "offset=";
    public static final String TYPE_POST = "post";
    public static final String TYPE_PAGE = "page";
    public static final String TYPE_ATTACHMENTS = "attachment";
    public static final String SIZE_THUMBNAIL = "thumbnail";
    public static final String SIZE_SMALL = "small";

    public static final int DISPLAY_HOME = 0;
    public static final int DISPLAY_VIDEO_GAMES = 1;
    public static final int DISPLAY_TOURISM = 2;
    public static final int DISPLAY_ABOUT = 3;
    public static final int DISPLAY_CONTACT = 4;
    public static final int DISPLAY_RECOMMENDATIONS = 5;
    public static final int DISPLAY_REVIEWS = 6;
    public static final int DISPLAY_RETRO = 7;
    public static final int DISPLAY_ART_BOOKS = 8;
    public static final int DISPLAY_SOUNDTRACKS = 9;
    public static final int DISPLAY_TOURIST_GUIDE = 10;
    public static final int DISPLAY_SINGLE_POST = 11;

    public static final int MAIN_NAV_ITEMS_COUNT = 5;

    private static final int POST_ID_ABOUT_US = 14;
    private static final int POST_ID_ACERCA_DE_NOSOTROS = 31;

    private static final int POST_ID_CONTACT_US = 59;
    private static final int POST_ID_CONTACTENOS = 66;

    private static int mAboutUs = POST_ID_ABOUT_US;
    private static int mContactUs = POST_ID_CONTACT_US;
    private static String mFeaturedImageSize = SIZE_THUMBNAIL;

    public static String getRequestUri (int whatToDisplay, String language, int offset) {
        //mFeaturedImageSize = MainActivity.isDoublePane? SIZE_SMALL : SIZE_THUMBNAIL;
        if (language.equals("en")) {
            mAboutUs = POST_ID_ABOUT_US;
            mContactUs = POST_ID_CONTACT_US;
            switch (whatToDisplay) {
                case DISPLAY_HOME:
                    return POSTS_URI + PARAM_LANG + language + "&" + PARAM_FEAT_IMG_SIZE + mFeaturedImageSize + "&" + PARAM_OFFSET + offset;
                case DISPLAY_VIDEO_GAMES:
                    return POSTS_URI + PARAM_CATEGORY + CAT_ID_VIDEO_JUEGOS + "&" + PARAM_LANG + language + "&" + PARAM_FEAT_IMG_SIZE + mFeaturedImageSize + "&" + PARAM_OFFSET + offset;
                case DISPLAY_TOURISM:
                    return POSTS_URI + PARAM_CATEGORY + CAT_ID_TURISMO + "&" + PARAM_LANG + language + "&" + PARAM_FEAT_IMG_SIZE + mFeaturedImageSize + "&" + PARAM_OFFSET + offset;
                case DISPLAY_RECOMMENDATIONS:
                    return POSTS_URI + PARAM_CATEGORY + CAT_ID_RECOMMENDATION_OF_THE_MONTH + "&" + PARAM_LANG + language + "&" + PARAM_FEAT_IMG_SIZE + mFeaturedImageSize + "&" + PARAM_OFFSET + offset;
                case DISPLAY_REVIEWS:
                    return POSTS_URI + PARAM_CATEGORY + CAT_ID_GAME_REVIEWS + "&" + PARAM_LANG + language + "&" +PARAM_FEAT_IMG_SIZE + mFeaturedImageSize + "&" + PARAM_OFFSET + offset;
                case DISPLAY_RETRO:
                    return POSTS_URI + PARAM_CATEGORY + CAT_ID_RETRO_RECOMMENDATION + "&" + PARAM_LANG + language + "&" + PARAM_FEAT_IMG_SIZE + mFeaturedImageSize + "&" + PARAM_OFFSET + offset;
                case DISPLAY_ART_BOOKS:
                    return POSTS_URI + PARAM_CATEGORY + CAT_ID_ART_BOOKS + "&" + PARAM_LANG + language + "&" + PARAM_FEAT_IMG_SIZE + mFeaturedImageSize + "&" + PARAM_OFFSET + offset;
                case DISPLAY_SOUNDTRACKS:
                    return POSTS_URI + PARAM_CATEGORY + CAT_ID_SOUNTRACKS + "&" + PARAM_LANG + language + "&" + PARAM_FEAT_IMG_SIZE + mFeaturedImageSize + "&" + PARAM_OFFSET + offset;
                case DISPLAY_TOURIST_GUIDE:
                    return POSTS_URI + PARAM_CATEGORY + CAT_ID_TOURIST_GUIDE + "&" + PARAM_LANG + language + "&" + PARAM_FEAT_IMG_SIZE + mFeaturedImageSize + "&" + PARAM_OFFSET + offset;
                case DISPLAY_ABOUT:
                    return PAGES_URI + PARAM_TYPE + TYPE_PAGE + "&" + PARAM_LANG + language + "&" + PARAM_POST_ID + mAboutUs;
                case DISPLAY_CONTACT:
                    return PAGES_URI + PARAM_TYPE + TYPE_PAGE + "&" + PARAM_LANG + language + "&" + PARAM_POST_ID + mContactUs;
                default:
                    return POSTS_URI + PARAM_LANG + language + "&" + PARAM_FEAT_IMG_SIZE + mFeaturedImageSize + "&"  + PARAM_OFFSET + offset;
            }
        }
        else {
            mAboutUs = POST_ID_ACERCA_DE_NOSOTROS;
            mContactUs = POST_ID_CONTACTENOS;
            switch (whatToDisplay) {
                case DISPLAY_HOME:
                    return POSTS_URI + PARAM_LANG + language + "&" + PARAM_FEAT_IMG_SIZE + mFeaturedImageSize + "&" + PARAM_OFFSET + offset;
                case DISPLAY_VIDEO_GAMES:
                    return POSTS_URI + PARAM_CATEGORY + CAT_ID_VIDEO_JUEGOS + "&" + PARAM_LANG + language + "&" + PARAM_FEAT_IMG_SIZE + mFeaturedImageSize + "&" + PARAM_OFFSET + offset;
                case DISPLAY_TOURISM:
                    return POSTS_URI + PARAM_CATEGORY + CAT_ID_TURISMO + "&" + PARAM_LANG + language + "&" + PARAM_FEAT_IMG_SIZE + mFeaturedImageSize + "&" +PARAM_OFFSET + offset;
                case DISPLAY_RECOMMENDATIONS:
                    return POSTS_URI + PARAM_CATEGORY + CAT_ID_RECOMENDACION_DEL_MES + "&" + PARAM_LANG + language + "&" + PARAM_FEAT_IMG_SIZE + mFeaturedImageSize + "&" +PARAM_OFFSET + offset;
                case DISPLAY_REVIEWS:
                    return POSTS_URI + PARAM_CATEGORY + CAT_ID_RESENA_JUEGOS + "&" + PARAM_LANG + language + "&" + PARAM_FEAT_IMG_SIZE + mFeaturedImageSize + "&" + PARAM_OFFSET + offset;
                case DISPLAY_RETRO:
                    return POSTS_URI + PARAM_CATEGORY + CAT_ID_RECOMENDACION_RETRO + "&" + PARAM_LANG + language + "&" + PARAM_FEAT_IMG_SIZE + mFeaturedImageSize + "&" +PARAM_OFFSET + offset;
                case DISPLAY_ART_BOOKS:
                    return POSTS_URI + PARAM_CATEGORY + CAT_ID_LIBRO_DE_ARTE + "&" + PARAM_LANG + language + "&" + PARAM_FEAT_IMG_SIZE + mFeaturedImageSize + "&" + PARAM_OFFSET + offset;
                case DISPLAY_SOUNDTRACKS:
                    return POSTS_URI + PARAM_CATEGORY + CAT_ID_BANDAS_SONORAS + "&" + PARAM_LANG + language + "&" + PARAM_FEAT_IMG_SIZE + mFeaturedImageSize + "&" + PARAM_OFFSET + offset;
                case DISPLAY_TOURIST_GUIDE:
                    return POSTS_URI + PARAM_CATEGORY + CAT_ID_GUIA_TURISTICA + "&" + PARAM_LANG + language + "&" + PARAM_FEAT_IMG_SIZE + mFeaturedImageSize + "&" + PARAM_OFFSET + offset;
                case DISPLAY_ABOUT:
                    return PAGES_URI + PARAM_TYPE + TYPE_PAGE + "&" + PARAM_LANG + language + "&" + PARAM_POST_ID + mAboutUs;
                case DISPLAY_CONTACT:
                    return PAGES_URI + PARAM_TYPE + TYPE_PAGE + "&" + PARAM_LANG + language + "&" + PARAM_POST_ID + mContactUs;
                default:
                    return POSTS_URI + PARAM_LANG + language + "&" + PARAM_FEAT_IMG_SIZE + mFeaturedImageSize + "&" + PARAM_OFFSET + offset;
            }
        }
    }

    public static int getCategoryId (int whatToDisplay, String language) {

        if (language.equals("en")) {
            switch (whatToDisplay) {
                case DISPLAY_VIDEO_GAMES:
                    return CAT_ID_VIDEO_JUEGOS;
                case DISPLAY_TOURISM:
                    return CAT_ID_TURISMO;
                case DISPLAY_RECOMMENDATIONS:
                    return CAT_ID_RECOMMENDATION_OF_THE_MONTH;
                case DISPLAY_REVIEWS:
                    return CAT_ID_GAME_REVIEWS;
                case DISPLAY_RETRO:
                    return CAT_ID_RETRO_RECOMMENDATION;
                case DISPLAY_ART_BOOKS:
                    return CAT_ID_ART_BOOKS;
                case DISPLAY_SOUNDTRACKS:
                    return CAT_ID_SOUNTRACKS;
                case DISPLAY_TOURIST_GUIDE:
                    return CAT_ID_TOURIST_GUIDE;
                default:
                    return -1;
            }
        }
        else {
            switch (whatToDisplay) {
                case DISPLAY_VIDEO_GAMES:
                    return CAT_ID_VIDEO_JUEGOS;
                case DISPLAY_TOURISM:
                    return CAT_ID_TURISMO;
                case DISPLAY_RECOMMENDATIONS:
                    return CAT_ID_RECOMENDACION_DEL_MES;
                case DISPLAY_REVIEWS:
                    return CAT_ID_RESENA_JUEGOS;
                case DISPLAY_RETRO:
                    return CAT_ID_RECOMENDACION_RETRO;
                case DISPLAY_ART_BOOKS:
                    return CAT_ID_LIBRO_DE_ARTE;
                case DISPLAY_SOUNDTRACKS:
                    return CAT_ID_BANDAS_SONORAS;
                case DISPLAY_TOURIST_GUIDE:
                    return CAT_ID_GUIA_TURISTICA;
                default:
                    return -1;
            }
        }
    }

    public static int getPageId (int whatToDisplay) {
        String language = getPreferredLanguage();
        if (language.equals("en")) {
            switch (whatToDisplay) {
                case DISPLAY_ABOUT:
                    return POST_ID_ABOUT_US;
                case DISPLAY_CONTACT:
                    return POST_ID_CONTACT_US;
            }
        }
        else {
            switch (whatToDisplay) {
                case DISPLAY_ABOUT:
                    return POST_ID_ACERCA_DE_NOSOTROS;
                case DISPLAY_CONTACT:
                    return POST_ID_CONTACTENOS;
            }
        }
        return -1;
    }

    public static String getPreferredLanguage () {
        Locale mLocale = Locale.getDefault();
        if (mLocale.equals(Locale.ENGLISH) || mLocale.equals(Locale.US) || mLocale.equals(Locale.CANADA) || mLocale.equals(Locale.UK)) {
            return "en";
        }
        else {
            return "es";
        }
    }

    public static String getRequestUri (String slug, String language) {
        return POSTS_URI + PARAM_TYPE + TYPE_POST + "&" + PARAM_LANG + language + "&" + PARAM_SLUG + slug;
    }

    public static String getAttachmentUri (String postId, String slug, String language) {
        Constants.logMessage(ATTACHMENTS_URI + PARAM_LANG + language + "&" + PARAM_POST_ID + postId + "&" + PARAM_SLUG + slug);
        return ATTACHMENTS_URI + PARAM_LANG + language + "&" + PARAM_POST_ID + postId + "&" + PARAM_SLUG + slug;
    }

}