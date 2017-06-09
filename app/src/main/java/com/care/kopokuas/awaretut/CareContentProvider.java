package com.care.kopokuas.awaretut;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import android.util.Log;

import com.aware.Aware;
import com.aware.utils.DatabaseHelper;

import java.io.File;
import java.util.HashMap;

/**
 * Created by kopokuas on 05/06/2017.
 */

public class CareContentProvider extends ContentProvider {


    public static String AUTHORITY = "com.care.kopokuas.awaretut.provider.syctut";

    public static final int DATABASE_VERSION = 37;
    public static final String DATABASE_NAME = "plugin_care.db";

    public static final String DB_TBL_SLEEP = "sleep_data";

    //For each table, add two indexes: DIR and ITEM. The index needs to always increment. Next one is 3, and so on.
    private static final int SLEEP_ONE_DIR = 1;
    private static final int SLEEP_ONE_ITEM = 2;

    //Put tables names in this array so AWARE knows what you have on the database
    public static final String[] DATABASE_TABLES = {
            DB_TBL_SLEEP
    };

    //These are columns that we need to sync data, don't change this!
    public interface AWAREColumns extends BaseColumns {
        String _ID = "_id";
        String TIMESTAMP = "timestamp";
        String DEVICE_ID = "device_id";
    }

    /**
     * Create one of these per database table
     * In this example, we are adding example columns
     */
    public static final class Sleep_Data implements AWAREColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + DB_TBL_SLEEP);
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE+"/vnd.com.care.kopokuas.awaretut.provider.sleep_data";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE+"/vnd.com.care.kopokuas.awaretut.provider.sleep_data";

        public static final String PERSON = "person";
        public static final String SLEEP_AT = "sleep_at";
        public static final String WAKE_AT = "wake_at";
    }

    public static final String DB_TBL_SLEEP_FIELDS =
                    Sleep_Data._ID + " integer primary key autoincrement," +
                    Sleep_Data.TIMESTAMP + " real default 0," +
                    Sleep_Data.DEVICE_ID + " text default ''," +
                    Sleep_Data.SLEEP_AT + " real default 0," +
                    Sleep_Data.WAKE_AT + " real default 0," +
                    Sleep_Data.PERSON + " text default ''";


    /**
         * Share the fields with AWARE so we can replicate the table schema on the server
     */
    public static final String[] TABLES_FIELDS = {
            DB_TBL_SLEEP_FIELDS
    };

    //Helper variables for ContentProvider - DO NOT CHANGE
    private UriMatcher sUriMatcher;
    private DatabaseHelper dbHelper;
    private static SQLiteDatabase database;

    /**
     * initialise the content provider
     *
     * @return
     */
    private void initialiseDatabase() {
        if (dbHelper == null)
            dbHelper = new DatabaseHelper(getContext(), DATABASE_NAME, null, DATABASE_VERSION, DATABASE_TABLES, TABLES_FIELDS);
        if (database == null)
            database = dbHelper.getWritableDatabase();
    }

    //For each table, create a hashmap needed for database queries
    private HashMap<String, String> sleepeHash;


    @Override
    public boolean onCreate() {
        //This is a hack to allow providers to be reusable in any application/plugin by making the authority dynamic using the package name of the parent app
        AUTHORITY = getContext().getPackageName() + ".provider.syctut"; //make sure xxx matches the first string in this class

        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        //For each table, add indexes DIR and ITEM
        sUriMatcher.addURI(AUTHORITY, DATABASE_TABLES[0], SLEEP_ONE_DIR);
        sUriMatcher.addURI(AUTHORITY, DATABASE_TABLES[0] + "/#", SLEEP_ONE_ITEM);


        //Create each table hashmap so Android knows how to insert data to the database. Put ALL table fields.
        sleepeHash = new HashMap<>();
        sleepeHash.put(Sleep_Data._ID, Sleep_Data._ID);
        sleepeHash.put(Sleep_Data.TIMESTAMP, Sleep_Data.TIMESTAMP);
        sleepeHash.put(Sleep_Data.DEVICE_ID, Sleep_Data.DEVICE_ID);
        sleepeHash.put(Sleep_Data.SLEEP_AT, Sleep_Data.SLEEP_AT);
        sleepeHash.put(Sleep_Data.WAKE_AT, Sleep_Data.WAKE_AT);
        sleepeHash.put(Sleep_Data.PERSON, Sleep_Data.PERSON);
        return true;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        initialiseDatabase();

        database.beginTransaction();

        int count;
        switch (sUriMatcher.match(uri)) {

            //Add each table DIR case, increasing the index accordingly
            case SLEEP_ONE_DIR:
                count = database.delete(DATABASE_TABLES[0], selection, selectionArgs);
                break;

            default:
                database.endTransaction();
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        database.setTransactionSuccessful();
        database.endTransaction();

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {

        initialiseDatabase();

        ContentValues values = (initialValues != null) ? new ContentValues(initialValues) : new ContentValues();

        database.beginTransaction();

        switch (sUriMatcher.match(uri)) {

            //Add each table DIR case
            case SLEEP_ONE_DIR:
                long _id = database.insert(DATABASE_TABLES[0], Sleep_Data.DEVICE_ID, values);
                database.setTransactionSuccessful();
                database.endTransaction();
                if (_id > 0) {
                    Uri dataUri = ContentUris.withAppendedId(Sleep_Data.CONTENT_URI, _id);
                    getContext().getContentResolver().notifyChange(dataUri, null);
                    return dataUri;
                }
                database.endTransaction();
                throw new SQLException("Failed to insert row into " + uri);

            default:
                database.endTransaction();
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }


    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        initialiseDatabase();

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        switch (sUriMatcher.match(uri)) {

            //Add all tables' DIR entries, with the right table index
            case SLEEP_ONE_DIR:
                qb.setTables(DATABASE_TABLES[0]);
                qb.setProjectionMap(sleepeHash); //the hashmap of the table
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        //Don't change me
        try {
            Cursor c = qb.query(database, projection, selection, selectionArgs,
                    null, null, sortOrder);
            c.setNotificationUri(getContext().getContentResolver(), uri);
            return c;
        } catch (IllegalStateException e) {
            if (Aware.DEBUG)
                Log.e(Aware.TAG, e.getMessage());
            return null;
        }
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {

            //Add each table indexes DIR and ITEM
            case SLEEP_ONE_DIR:
                return Sleep_Data.CONTENT_TYPE;
            case SLEEP_ONE_ITEM:
                return Sleep_Data.CONTENT_ITEM_TYPE;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        initialiseDatabase();

        database.beginTransaction();

        int count;
        switch (sUriMatcher.match(uri)) {

            //Add each table DIR case
            case SLEEP_ONE_DIR:
                count = database.update(DATABASE_TABLES[0], values, selection, selectionArgs);
                break;
            default:
                database.endTransaction();
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        database.setTransactionSuccessful();
        database.endTransaction();

        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }

}
