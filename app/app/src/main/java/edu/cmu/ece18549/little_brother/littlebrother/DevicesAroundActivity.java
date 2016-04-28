package edu.cmu.ece18549.little_brother.littlebrother;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import android.support.v7.widget.Toolbar;
import android.widget.ExpandableListView;

public class DevicesAroundActivity extends AppCompatActivity {

    DataImporterAdapter mAdapter;
    private ExpandableListView mDeviceListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices_around);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDeviceListView = (ExpandableListView) findViewById(R.id.deviceAroundList);
        DeviceInfoImporter deviceInfo = new DeviceInfoImporter();
        mAdapter = new DataImporterAdapter(this, deviceInfo);
        mDeviceListView.setAdapter(mAdapter);
    }
}
