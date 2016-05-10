package edu.cmu.ece18549.little_brother.littlebrother.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;

import edu.cmu.ece18549.little_brother.littlebrother.R;
import edu.cmu.ece18549.little_brother.littlebrother.adapter.DevicesAroundAdapter;
import edu.cmu.ece18549.little_brother.littlebrother.adapter.SensorRegisterAdapter;
import edu.cmu.ece18549.little_brother.littlebrother.data_component.Sensor;

public class DeviceRegisterActivity extends AppCompatActivity {
    public static final String SUCCESS_TAG = "SUCCESS";
    public static final String SENSOR_NAME_EXTRAS = "DeviceRegisterActivity.sensor_name_extras";
    public static final String DEVICE_NAME_EXTRA = "DeviceRegisterActivity.device_name_extra";
    public static final String DEVICE_ID_EXTRA = "DeviceRegisterActivity.device_id_extra";
    public static final String WIFI = "Wi-Fi";
    public static final String ANY = "Any";
    public static final String TAG = "DeviceRegister";
    private int mDevice_id;
    private SensorRegisterAdapter mAdapter;
    private ArrayList<Sensor> mSensors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDevice_id = (int) getIntent().getSerializableExtra(DevicesAroundAdapter.DEVICE_ID_EXTRA);
        setContentView(R.layout.activity_device_register);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecyclerView rvSensors = (RecyclerView) findViewById(R.id.sensor_list);

        // Initialize contacts
        mSensors = new ArrayList<>();
        mSensors.add(new Sensor(0, "", null));
        mSensors.add(new Sensor(0, "", null));
        mSensors.add(new Sensor(0, "", null));

        // Create adapter passing in the sample user data
        mAdapter = new SensorRegisterAdapter(mSensors);
        // Attach the adapter to the recyclerview to populate items
        rvSensors.setAdapter(mAdapter);
        // Set layout manager to position the items
        rvSensors.setLayoutManager(new LinearLayoutManager(this));
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
                    "the \"Network\" option in data sync/Network Preferences before " +
                    "registering devices";
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
                            finish();
                        }
                    });
            alertDialog.show();
        }
    }

    public void registerDevice(View v) {
        EditText deviceNameView = (EditText) findViewById(R.id.deviceNameEditText);
        String deviceName = deviceNameView.getText().toString();

        ArrayList<String> sensorNames = new ArrayList<>();
        for (Sensor s : mSensors){
            Log.i(TAG, s.getName());
            if (!(s.getName() == "")) {
                sensorNames.add(s.getName());
            }
        }

        Intent returnIntent = new Intent();
        returnIntent.putExtra(DEVICE_ID_EXTRA, mDevice_id);
        returnIntent.putExtra(DEVICE_NAME_EXTRA, deviceName);
        returnIntent.putStringArrayListExtra(SENSOR_NAME_EXTRAS, sensorNames);
        setResult(this.RESULT_OK,returnIntent);
        finish();
    }

}
