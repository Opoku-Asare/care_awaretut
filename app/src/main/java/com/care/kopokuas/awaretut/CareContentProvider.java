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


    public static final int COLLECTION_INDEX = 1;
    public static final int ITEM_INDEX = 2;


    public static final class Sleep_Data implements BaseColumns {

        public static final String TABLE_NAME = "sleep_data";
        public static final String _ID = "_id";
        public static final String TIMESTAMP = "timestamp";
        public static final String DEVICE_ID = "device_id";
        public static final String PERSON = "person";
        public static final String SLEEP_AT = "sleep_at";
        public static final String WAKE_AT = "wake_at";

        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE+"/vnd.com.care.kopokuas.awaretut.provider" + "." + TABLE_NAME;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE+"/vnd.com.care.kopokuas.awaretut.provider"+ "." +TABLE_NAME;

    }

    public static final String DATABASE_NAME = "plugin_care.db";
    public static final int DATABASE_VERSION = 2;
    public static final String[] DATABASE_TABLES = {Sleep_Data.TABLE_NAME};

    public static final String[] TABLES_FIELDS = {
                    Sleep_Data._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Sleep_Data.TIMESTAMP + " REAL DEFAULT 0," +
                    Sleep_Data.DEVICE_ID + " TEXT DEFAULT ''," +
                    Sleep_Data.SLEEP_AT + " REAL DEFAULT O," +
                    Sleep_Data.WAKE_AT + " REAL DEFAULT 0," +
                    Sleep_Data.PERSON + " TEXT DEFAULT ''," +
                    "UNIQUE (" + Sleep_Data.TIMESTAMP + "," + Sleep_Data.DEVICE_ID + ")"
    };

    private UriMatcher uriMatcher = null;
    private HashMap<String, String> tableMap = null;
    private static DatabaseHelper databaseHelper = null;
    private static SQLiteDatabase database = null;

    /**
     * initialise the content provider
     *
     * @return
     */
    private boolean initializeDB() {
        if (databaseHelper == null) {
            databaseHelper = new DatabaseHelper(getContext(), DATABASE_NAME, null, DATABASE_VERSION, DATABASE_TABLES, TABLES_FIELDS);
        }
        if (databaseHelper != null && (database == null || !database.isOpen())) {
            database = databaseHelper.getWritableDatabase();
        }
        return (database != null && databaseHelper != null);
    }

    /**
     * Allow resetting the ContentProvider when updating/reinstalling AWARE
     */
    public static void resetDB(Context c) {
        Log.d("AWARE", "Resetting " + DATABASE_NAME + "...");

        File db = new File(DATABASE_NAME);
        db.delete();
        databaseHelper = new DatabaseHelper(c, DATABASE_NAME, null, DATABASE_VERSION, DATABASE_TABLES, TABLES_FIELDS);
        if (databaseHelper != null) {
            database = databaseHelper.getWritableDatabase();
        }
    }

    @Override
    public boolean onCreate() {
        AUTHORITY = getContext().getPackageName() + ".provider.syctut"; //make AUTHORITY dynamic
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, DATABASE_TABLES[0], COLLECTION_INDEX); //URI for all records
        uriMatcher.addURI(AUTHORITY, DATABASE_TABLES[0] + "/#", ITEM_INDEX); //URI for a single record

        tableMap = new HashMap<String, String>();
        tableMap.put(Sleep_Data._ID, Sleep_Data._ID);
        tableMap.put(Sleep_Data.TIMESTAMP, Sleep_Data.TIMESTAMP);
        tableMap.put(Sleep_Data.DEVICE_ID, Sleep_Data.DEVICE_ID);
        tableMap.put(Sleep_Data.PERSON, Sleep_Data.PERSON);
        tableMap.put(Sleep_Data.SLEEP_AT, Sleep_Data.SLEEP_AT);
        tableMap.put(Sleep_Data.WAKE_AT, Sleep_Data.WAKE_AT);

        return true; //let Android know that the database is ready to be used.
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (!initializeDB()) {
            Log.w(AUTHORITY, "Database unavailable...");
            return null;
        }

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        switch (uriMatcher.match(uri)) {
            case COLLECTION_INDEX:
                qb.setTables(DATABASE_TABLES[0]);
                qb.setProjectionMap(tableMap);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        try {
            Cursor c = qb.query(database, projection, selection, selectionArgs, null, null, sortOrder);
            c.setNotificationUri(getContext().getContentResolver(), uri);
            return c;
        } catch (IllegalStateException e) {
            if (Aware.DEBUG) Log.e(Aware.TAG, e.getMessage());
            return null;
        }
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case COLLECTION_INDEX:
                return Sleep_Data.CONTENT_TYPE;
            case ITEM_INDEX:
                return Sleep_Data.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues new_values) {
        if (!initializeDB()) {
            Log.w(AUTHORITY, "Database unavailable...");
            return null;
        }

        ContentValues values = (new_values != null) ? new ContentValues(new_values) : new ContentValues();

        switch (uriMatcher.match(uri)) {
            case COLLECTION_INDEX:
                long _id = database.insert(DATABASE_TABLES[0], Sleep_Data.DEVICE_ID, values);
                if (_id > 0) {
                    Uri dataUri = ContentUris.withAppendedId(Sleep_Data.CONTENT_URI, _id);
                    getContext().getContentResolver().notifyChange(dataUri, null);
                    return dataUri;
                }
                throw new SQLException("Failed to insert row into " + uri);
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if (!initializeDB()) {
            Log.w(AUTHORITY, "Database unavailable...");
            return 0;
        }

        int count = 0;
        switch (uriMatcher.match(uri)) {
            case COLLECTION_INDEX:
                count = database.delete(DATABASE_TABLES[0], selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (!initializeDB()) {
            Log.w(AUTHORITY, "Database unavailable...");
            return 0;
        }

        int count = 0;
        switch (uriMatcher.match(uri)) {
            case COLLECTION_INDEX:
                count = database.update(DATABASE_TABLES[0], values, selection, selectionArgs);
                break;
            default:
                database.close();
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

}
