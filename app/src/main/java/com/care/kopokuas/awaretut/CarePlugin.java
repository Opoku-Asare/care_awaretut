package com.care.kopokuas.awaretut;

import android.net.Uri;

import com.aware.utils.Aware_Plugin;

/**
 * Created by kopokuas on 06/06/2017.
 */

public class CarePlugin extends Aware_Plugin {
    @Override
    public void onCreate() {
        super.onCreate();

        //To sync data to the server, you'll need to set this variables from your ContentProvider
        DATABASE_TABLES = CareContentProvider.DATABASE_TABLES;
        TABLES_FIELDS = CareContentProvider.TABLES_FIELDS;
        CONTEXT_URIS = new Uri[]{ CareContentProvider.Sleep_Data.CONTENT_URI };
    }


}
