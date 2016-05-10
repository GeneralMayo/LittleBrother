package edu.cmu.ece18549.little_brother.littlebrother.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.net.Uri;

import edu.cmu.ece18549.little_brother.littlebrother.R;
import edu.cmu.ece18549.little_brother.littlebrother.service.DeviceFinderService;

import android.preference.PreferenceManager;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String webappURI = "http://ec2-52-90-105-31.compute-1.amazonaws.com/";
    private static final String WIFI = "Wi-Fi";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        startService(new Intent(this,DeviceFinderService.class));
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Gets the user's network preference settings
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        String sPref = sharedPrefs.getString("networkPref", "Wi-Fi");
        boolean wifiConnected;
        boolean mobileConnected;

        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if (activeInfo != null && activeInfo.isConnected()) {
            wifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
            mobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
        } else {
            wifiConnected = false;
            mobileConnected = false;
        }
        if (wifiConnected)
            Log.i(TAG, "WIFI CONNECTED");
        if (mobileConnected)
            Log.i(TAG, "MOBILE CONNECTED");
        Log.i(TAG, sPref.toString());
        Log.i(TAG, "alert text" + (sPref.equals(WIFI) && mobileConnected && !wifiConnected));

        String alertText = null;
        if (sPref.equals(WIFI) && mobileConnected && !wifiConnected) {
            alertText = "Wi-Fi is currently disabled, please enable Wi-Fi or disable " +
                    "the \"Network\" option in data sync/Network Preferences to upload logs";
        } else if (!mobileConnected && !wifiConnected){
            alertText = "No network available";
        }
        if (alertText != null) {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Alert");
            alertDialog.setMessage(alertText);
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
            alertDialog.show();
        }
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
