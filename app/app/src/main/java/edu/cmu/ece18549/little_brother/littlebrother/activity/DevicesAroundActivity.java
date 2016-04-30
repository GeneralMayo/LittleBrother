package edu.cmu.ece18549.little_brother.littlebrother.activity;

import android.content.ComponentName;
import android.content.Intent;
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
import java.util.ArrayList;
import java.util.List;

import edu.cmu.ece18549.little_brother.littlebrother.adapter.DeviceInfoAdapter;
import edu.cmu.ece18549.little_brother.littlebrother.adapter.DeviceInfoImporter;
import edu.cmu.ece18549.little_brother.littlebrother.R;
import edu.cmu.ece18549.little_brother.littlebrother.data_component.Device;
import edu.cmu.ece18549.little_brother.littlebrother.service.DeviceFinderService;
import edu.cmu.ece18549.little_brother.littlebrother.service.Notification;
import edu.cmu.ece18549.little_brother.littlebrother.service.Observer;

public class DevicesAroundActivity extends AppCompatActivity implements Observer {

    DeviceInfoAdapter mAdapter;
    DeviceInfoImporter mDeviceInfo;
    private ExpandableListView mDeviceListView;
    boolean mBound;
    DeviceFinderService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, DeviceFinderService.class);
        bindService(intent, mConnection, this.BIND_AUTO_CREATE);

        mDeviceListView = (ExpandableListView) findViewById(R.id.deviceAroundList);
        DeviceInfoImporter importer = new DeviceInfoImporter();
        mAdapter = new DeviceInfoAdapter(this, importer);
        mDeviceListView.setAdapter(mAdapter);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service.  We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.
            DeviceFinderService.LocalBinder binder = (DeviceFinderService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
            mBound = false;
        }
    };


    @Override
    public void notifyChange(Notification n, Object o) {
        switch (n) {
            case DEVICE_ADDED:
                mDeviceInfo.addDevice((Device) o);
        }
    }
}
