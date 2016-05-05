package edu.cmu.ece18549.little_brother.littlebrother.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.bignerdranch.expandablerecyclerview.Model.ParentObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import edu.cmu.ece18549.little_brother.littlebrother.R;
import edu.cmu.ece18549.little_brother.littlebrother.adapter.DevicesAroundAdapter;
import edu.cmu.ece18549.little_brother.littlebrother.data_component.Device;
import edu.cmu.ece18549.little_brother.littlebrother.service.DeviceFinderService;
import edu.cmu.ece18549.little_brother.littlebrother.test.IncrementalFakeDeviceFactory;

public class DevicesAroundActivity extends AppCompatActivity {
    private static final String TAG = "DevicesAroundActivity";

    private class DevicesAroundBroadcastReceiver extends BroadcastReceiver{
        DevicesAroundAdapter mAdapter;
        List<ParentObject> mObjectList;

        public DevicesAroundBroadcastReceiver(DevicesAroundAdapter adapter,
                                              List<ParentObject> parentList){
            mAdapter = adapter;
            mObjectList = parentList;
        }

        @Override
        public void onReceive(final Context context, final Intent intent) {
            Log.i(TAG, "BroadcastReceiver received intent");
            Serializable s = intent.getSerializableExtra(DeviceFinderService.BLE_CHANGE_EXTRA);
            Log.i(TAG, mParentList.toString());
            mParentList.add(new DevicesAroundAdapter.DeviceObject(ifdf.getNewDevice()));
            Log.i(TAG, "SIZE HERE: " + mParentList.size());
            mAdapter.notifyDataSetChanged();
            if (s == null) {
                Log.i(TAG, "intent was not from the DeviceFinderService. Ignore.");
            }
            else {
                int id = (int) s;
                Log.i(TAG, "Received ID:" +id);
            }
        }
    };

    IncrementalFakeDeviceFactory ifdf = new IncrementalFakeDeviceFactory();
    DevicesAroundAdapter mAdapter;
    private List<Device> mDevices;
    private RecyclerView mDeviceListView;
    private List<ParentObject> mParentList;
    private BroadcastReceiver receiver;

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

        mDeviceListView = (RecyclerView) findViewById(R.id.deviceAroundList);

        mParentList =
                DevicesAroundAdapter.createParentList(new ArrayList<Device>(Device.devices.values()));
        mParentList.add(new DevicesAroundAdapter.DeviceObject(ifdf.getNewDevice()));

        mAdapter = new DevicesAroundAdapter(this,mParentList);
        mDeviceListView.setAdapter(mAdapter);
        mDeviceListView.setLayoutManager(new LinearLayoutManager(this));
        receiver = new DevicesAroundBroadcastReceiver(mAdapter, mParentList);
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

    public void notifyDataReset(View v) {
        mAdapter.notifyDataSetChanged();
    }
}
