package edu.cmu.ece18549.little_brother.littlebrother.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

import edu.cmu.ece18549.little_brother.littlebrother.R;
import edu.cmu.ece18549.little_brother.littlebrother.adapter.DeviceInfoAdapter;
import edu.cmu.ece18549.little_brother.littlebrother.adapter.SensorRegisterAdapter;
import edu.cmu.ece18549.little_brother.littlebrother.data_component.Device;
import edu.cmu.ece18549.little_brother.littlebrother.data_component.Sensor;

public class DeviceRegisterActivity extends AppCompatActivity {
    public static final String TAG = "DeviceRegister";
    Device mDevice;
    SensorRegisterAdapter mAdapter;
    ArrayList<Sensor> mSensors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDevice = (Device) getIntent().getSerializableExtra(DeviceInfoAdapter.DEVICE_TAG);
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

    public void registerDevice(View v) {

        //TODO get device data, sensor names, fill error messages

    }

}
