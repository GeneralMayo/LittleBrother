package edu.cmu.ece18549.little_brother.littlebrother.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ExpandableListView;

import edu.cmu.ece18549.little_brother.littlebrother.adapter.DeviceInfoAdapter;
import edu.cmu.ece18549.little_brother.littlebrother.adapter.DeviceInfoImporter;
import edu.cmu.ece18549.little_brother.littlebrother.R;

public class DevicesAroundActivity extends AppCompatActivity {

    DeviceInfoAdapter mAdapter;
    private ExpandableListView mDeviceListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices_around);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDeviceListView = (ExpandableListView) findViewById(R.id.deviceAroundList);
        DeviceInfoImporter importer = new DeviceInfoImporter();
        mAdapter = new DeviceInfoAdapter(this, importer);
        mDeviceListView.setAdapter(mAdapter);
    }

    public void loadDeviceRegisterActivity(View v) {
        Intent intent = new Intent(this, DeviceRegisterActivity.class);
        startActivity(intent);
    }
}
