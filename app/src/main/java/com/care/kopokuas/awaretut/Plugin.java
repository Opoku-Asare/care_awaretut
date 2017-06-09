package com.care.kopokuas.awaretut;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;

import com.aware.Applications;
import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.utils.Aware_Plugin;

/**
 * Created by kopokuas on 06/06/2017.
 */

public class Plugin extends Aware_Plugin {
    @Override
    public void onCreate() {
        super.onCreate();

        TAG = "AWARE::"+getResources().getString(R.string.app_name);

        //To sync data to the server, you'll need to set this variables from your ContentProvider
        DATABASE_TABLES = CareContentProvider.DATABASE_TABLES;
        TABLES_FIELDS = CareContentProvider.TABLES_FIELDS;
        CONTEXT_URIS = new Uri[]{ CareContentProvider.Sleep_Data.CONTENT_URI };
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Aware.stopAWARE(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        //Sync data to server every 1 minute
        Aware.setSetting(this, Aware_Preferences.FREQUENCY_WEBSERVICE, 1);
        //Clear old data monthly
        Aware.setSetting(this, Aware_Preferences.FREQUENCY_CLEAN_OLD_DATA, 2);

        Applications.isAccessibilityServiceActive(getApplicationContext());

        if (!Aware.isStudy(this)) Aware.joinStudy(getApplicationContext(), "https://api.awareframework.com/index.php/webservice/index/1319/JtQrj7MQCJoK");
        Aware.startAWARE(this);


        return START_STICKY;
    }
}
