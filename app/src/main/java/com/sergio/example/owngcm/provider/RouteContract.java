package com.sergio.example.owngcm.provider;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import com.sergio.example.owngcm.utils.AccountUtils;

/**
 * Defines table and column names for the Routes database.
 */
public class RouteContract {

    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "com.sergio.example.owngcm";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    // For instance, content://com.example.android.sunshine.app/weather/ is a valid path for
    // looking at weather data. content://com.example.android.sunshine.app/givemeroot/ will fail,
    // as the ContentProvider hasn't been given any information on what to do with "givemeroot".
    // At least, let's hope not.  Don't be that dev, reader.  Don't be that dev.
    public static final String PATH_ROUTE = "chat";


    /* Inner class that defines the table contents of the route table */
    public static final class RouteEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ROUTE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ROUTE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ROUTE;

        public static final String TABLE_NAME = "chat";

        public static final String COLUMN_REGISTERED = "registered";
        public static final String COLUMN_NAME_ROUTE = "name_chat";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_TOPICS = "topics";
        public static final String COLUMN_CITY_NAME_INIT = "city_name_init";
        public static final String COLUMN_START_DATE = "start_date";
        public static final String COLUMN_MAX_ATTENDEES = "max_attendes";
        public static final String COLUMN_SEATS_AVAILABLE = "seats_available";
        public static final String COLUMN_WEBSAFE_KEY = "websafe_key";
        public static final String COLUMN_ORGANIZER_DISPLAY_NAME = "organizer_display_name";
        public static final String COLUMN_URL_CHAT_COVER = "url_cover";
        public static final String COLUMN_TOPIC_GCM = "topic_gcm";


        public static Uri buildRouteUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static String getRouteIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

    }
}