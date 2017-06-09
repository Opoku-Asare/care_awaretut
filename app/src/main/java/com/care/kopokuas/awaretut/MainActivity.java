package com.care.kopokuas.awaretut;

import android.content.ContentValues;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.aware.Aware;
import com.aware.Aware_Preferences;

import java.util.Calendar;
import java.util.Date;

import static com.care.kopokuas.awaretut.CareContentProvider.*;

public class MainActivity extends AppCompatActivity {
    public EditText txtPersonName;
    public DatePicker dpkSleepAt, dpkWakeAt;
    public Button btnSave;

    public String deviceID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtPersonName = (EditText) findViewById(R.id.txtPerson);
        dpkSleepAt = (DatePicker) findViewById(R.id.dpkSleepAt);
        dpkWakeAt = (DatePicker) findViewById(R.id.dpkWakeAt);
        btnSave = (Button) findViewById(R.id.btnSave);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                String person = txtPersonName.getText().toString();
                calendar.set(dpkSleepAt.getYear(), dpkSleepAt.getMonth(), dpkSleepAt.getDayOfMonth());
                long sleepAt = calendar.getTimeInMillis();

                calendar.set(dpkWakeAt.getYear(), dpkWakeAt.getMonth(), dpkSleepAt.getDayOfMonth());
                long wakeAt = calendar.getTimeInMillis();

                ContentValues new_data = new ContentValues();
                if (deviceID.equals(""))
                    deviceID = Aware.getSetting(getApplicationContext(), Aware_Preferences.DEVICE_ID);
                new_data.put(Sleep_Data.DEVICE_ID, deviceID);
                new_data.put(Sleep_Data.TIMESTAMP, System.currentTimeMillis());
                new_data.put(CareContentProvider.Sleep_Data.SLEEP_AT, sleepAt);
                new_data.put(CareContentProvider.Sleep_Data.WAKE_AT, wakeAt);
                new_data.put(CareContentProvider.Sleep_Data.PERSON, person);

                //put the rest of the columns you defined

                //Insert the data to the ContentProvider
                getContentResolver().insert(CareContentProvider.Sleep_Data.CONTENT_URI, new_data);

                Intent startSync = new Intent(getApplicationContext(), Plugin.class);
                startSync.setAction(Aware.ACTION_AWARE_SYNC_DATA);
                startService(startSync);
            }
        });

        //Initialise AWARE
        Intent aware = new Intent(this, Aware.class);
        startService(aware);


        Intent startPlugin = new Intent(this, Plugin.class);
        startService(startPlugin);


        deviceID = Aware.getSetting(getApplicationContext(), Aware_Preferences.DEVICE_ID);

    }
}
