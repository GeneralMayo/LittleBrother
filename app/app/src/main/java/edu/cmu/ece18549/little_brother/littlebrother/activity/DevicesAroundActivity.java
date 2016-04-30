package edu.cmu.ece18549.little_brother.littlebrother.activity;

import android.content.Intent;
import android.os.Bundle;
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

public class DevicesAroundActivity extends AppCompatActivity {

    DeviceInfoAdapter mAdapter;
    private ExpandableListView mDeviceListView;

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

        List<Device> devices = new ArrayList<Device>(Device.devices.values());
        for (Device d : devices) {
            Log.i("DevicesAround", d.toString());
        }

        mDeviceListView = (ExpandableListView) findViewById(R.id.deviceAroundList);
        DeviceInfoImporter importer = new DeviceInfoImporter();
        mAdapter = new DeviceInfoAdapter(this, importer);
        mDeviceListView.setAdapter(mAdapter);
    }

}
