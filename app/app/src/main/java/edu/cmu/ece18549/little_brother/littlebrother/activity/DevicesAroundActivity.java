package edu.cmu.ece18549.little_brother.littlebrother.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;

import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;

import java.io.Serializable;

import edu.cmu.ece18549.little_brother.littlebrother.adapter.DeviceInfoAdapter;
import edu.cmu.ece18549.little_brother.littlebrother.adapter.DeviceInfoImporter;
import edu.cmu.ece18549.little_brother.littlebrother.R;
import edu.cmu.ece18549.little_brother.littlebrother.data_component.Device;
import edu.cmu.ece18549.little_brother.littlebrother.service.DeviceFinderService;
import edu.cmu.ece18549.little_brother.littlebrother.service.Notification;
import edu.cmu.ece18549.little_brother.littlebrother.service.Observer;

public class DevicesAroundActivity extends AppCompatActivity {
    private static final String TAG = "DevicesAroundActivity";

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            Log.i(TAG, "BroadcastReceiver received intent");
            Serializable s = intent.getSerializableExtra(DeviceFinderService.BLE_CHANGE_EXTRA);
            if (s == null) {
                Log.i(TAG, "intent was not from the DeviceFinderService. Ignore.");
            }
            else {
                int id = (int) s;
                Log.i(TAG, "Received ID:" +id);
            }
        }
    };

    DeviceInfoAdapter mAdapter;
    DeviceInfoImporter mDeviceInfo;
    private ExpandableListView mDeviceListView;
    boolean mBound;
    DeviceFinderService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "in DevicesAroundActivty");

        setContentView(R.layout.activity_devices_around);

        Serializable extra = getIntent().getSerializableExtra(DeviceRegisterActivity.SUCCESS_TAG);
        if (extra != null){
            int success = (int) extra;
            if (success < 0) {
                Snackbar.make(findViewById(R.id.deviceAroundList),
                        "There was a problem with the device registration", Snackbar.LENGTH_LONG)
                                                .setAction("Action", null).show();
            } else {
                Snackbar.make(findViewById(R.id.deviceAroundList),
                        "Success! Device registered as id: " + success, Snackbar.LENGTH_LONG)
                                                .setAction("Action", null).show();
            }
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDeviceListView = (ExpandableListView) findViewById(R.id.deviceAroundList);
        DeviceInfoImporter importer = new DeviceInfoImporter();
        mAdapter = new DeviceInfoAdapter(this, importer);
        mDeviceListView.setAdapter(mAdapter);
    }

    @Override
    protected void onResume() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(DeviceFinderService.BLE_CHANGE_ACTION);
        registerReceiver(receiver, filter);
        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(receiver);
        super.onPause();
    }

}
