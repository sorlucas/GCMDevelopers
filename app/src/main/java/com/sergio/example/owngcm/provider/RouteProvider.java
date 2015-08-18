package com.sergio.example.owngcm.provider;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.sergio.example.owngcm.utils.PrefUtils;

import static com.sergio.example.owngcm.utils.LogUtils.makeLogTag;

/**
 * Created by sergio on 28/04/15.
 */
public class RouteProvider extends ContentProvider {

    private static final String TAG = makeLogTag(RouteProvider.class);

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private RouteDbHelper mOpenHelper;

    public static final int ROUTE = 100;
    public static final int ROUTE_WITH_ID = 101;


    //route._id = ?
    private static final String sRouteWithIdSelection =
            RouteContract.RouteEntry.TABLE_NAME+
                    "." + RouteContract.RouteEntry._ID + " = ? ";


    /*  This UriMatcher willmatch each URI to the ROUTE, ROUTE_WITH_ID, ROUTE_SHORT_ASC,
        and LOCATION integer constants defined above.     */
    public static UriMatcher buildUriMatcher() {
        // I know what you're thinking.  Why create a UriMatcher when you can use regular
        // expressions instead?  Because you're not crazy, that's why.

        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = RouteContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, RouteContract.PATH_ROUTE, ROUTE);
        matcher.addURI(authority, RouteContract.PATH_ROUTE + "/*", ROUTE_WITH_ID);

        return matcher;
    }
    @Override
    public boolean onCreate() {
        mOpenHelper = new RouteDbHelper(getContext());
        return true;
    }
    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case ROUTE:
                return RouteContract.RouteEntry.CONTENT_TYPE;
            case ROUTE_WITH_ID:
                return RouteContract.RouteEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case ROUTE: {
                retCursor =  mOpenHelper.getReadableDatabase().query(
                        RouteContract.RouteEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        null
                );
                break;
            }
            case ROUTE_WITH_ID: {
                String routeId = RouteContract.RouteEntry.getRouteIdFromUri(uri);
                retCursor = mOpenHelper.getReadableDatabase().query(
                        RouteContract.RouteEntry.TABLE_NAME,
                        projection,
                        sRouteWithIdSelection,
                        new String[]{routeId},
                        null,
                        null,
                        null
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    /*
        Student: Add the ability to insert Locations to the implementation of this function.
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case ROUTE: {
                long _id = db.insertWithOnConflict(RouteContract.RouteEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                if (_id == -1){
                    _id = db.update(RouteContract.RouteEntry.TABLE_NAME, values, RouteContract.RouteEntry._ID + " = ?", new String[]{values.get(RouteContract.RouteEntry._ID).toString()});
                }
                returnUri = RouteContract.RouteEntry.buildRouteUri(_id);

                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // mark that conference data sync succeeded
        PrefUtils.markSyncSucceededNow(getContext());
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ROUTE:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insertWithOnConflict(RouteContract.RouteEntry.TABLE_NAME, null, value, SQLiteDatabase.CONFLICT_IGNORE);
                        if (_id != -1){
                            returnCount++;

                        } else {
                            db.update(RouteContract.RouteEntry.TABLE_NAME, value, RouteContract.RouteEntry._ID + " = ?", new String[]{value.get(RouteContract.RouteEntry._ID).toString()});
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";
        else {
            selection = selection + " = ?";

        }
        switch (match) {
            case ROUTE:
                rowsDeleted = db.delete(
                        RouteContract.RouteEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case ROUTE:
                rowsUpdated = db.update(RouteContract.RouteEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
