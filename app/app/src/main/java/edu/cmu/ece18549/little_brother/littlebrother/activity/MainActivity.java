package edu.cmu.ece18549.little_brother.littlebrother.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.net.Uri;

import edu.cmu.ece18549.little_brother.littlebrother.R;
import android.preference.PreferenceManager;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String webappURI = "http://ec2-52-90-105-31.compute-1.amazonaws.com/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //startService(new Intent(this,DeviceFinderService.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean b = prefs.getBoolean("use_wifi_only", true);
        Log.i(TAG, "value of use_wifi_only: " + b);
    }

    public void loadDevicesAroundActivity(View v) {
        Intent intent = new Intent(this, DevicesAroundActivity.class);
        startActivity(intent);
    }

    public void loadStoredDataActivity(View v) {
        Intent intent = new Intent(this, StoredDataActivity.class);
        startActivity(intent);
    }

    public void viewWebappActivity(View v) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(webappURI));
        startActivity(intent);
    }

    public void viewSettingsActivity(View v) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

}
