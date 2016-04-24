package edu.cmu.ece18549.little_brother.littlebrother;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import java.util.HashMap;
import java.util.List;

import android.widget.ExpandableListView;

public class DevicesAroundActivity extends AppCompatActivity {

    DeviceDetailListAdapter listAdapter;
    private ExpandableListView mDeviceListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices_around);

        mDeviceListView = (ExpandableListView) findViewById(R.id.deviceAroundList);
        listAdapter = new DeviceDetailListAdapter(this);
        mDeviceListView.setAdapter(listAdapter);
    }
}
