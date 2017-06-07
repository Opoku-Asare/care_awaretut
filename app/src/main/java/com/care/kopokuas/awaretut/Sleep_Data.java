package com.care.kopokuas.awaretut;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by kopokuas on 06/06/2017.
 */

public class Sleep_Data implements BaseColumns {

    public static final String TABLE_NAME = "sleep_data";
    public static final String _ID = "_id";
    public static final String TIMESTAMP = "timestamp";
    public static final String DEVICE_ID="device_id";
    public static final String SLEPT_AT = "slept_at";
    public static final String WAKE_AT = "wake_at";

    public static final Uri CONTENT_URI=Uri.parse("content://"+CareContentProvider.AUTHORITY+"/Sleep_Data");
    public static final String CONTENT_TYPE="vnd.android.cursor.dir/vnd."+CareContentProvider.AUTHORITY+"."+TABLE_NAME;
    public static final String CONTENT_ITEM_TYPE="vnd.android.cursor.item/vnd."+CareContentProvider.AUTHORITY+"."+TABLE_NAME;

    public static final int COLLECTION_INDEX=1;
    public static final int ITEM_INDEX=2;

}
