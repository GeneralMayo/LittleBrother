package edu.cmu.ece18549.little_brother.littlebrother.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import edu.cmu.ece18549.little_brother.littlebrother.R;
import edu.cmu.ece18549.little_brother.littlebrother.adapter.DeviceInfoAdapter;
import edu.cmu.ece18549.little_brother.littlebrother.adapter.SensorRegisterAdapter;
import edu.cmu.ece18549.little_brother.littlebrother.adapter.ServerCommunicationException;
import edu.cmu.ece18549.little_brother.littlebrother.adapter.ServerCommunicator;
import edu.cmu.ece18549.little_brother.littlebrother.data_component.Device;
import edu.cmu.ece18549.little_brother.littlebrother.data_component.DeviceLog;
import edu.cmu.ece18549.little_brother.littlebrother.data_component.Sensor;

public class DeviceRegisterActivity extends AppCompatActivity {
    public static final String TAG = "DeviceRegister";
    private Device mDevice;
    private SensorRegisterAdapter mAdapter;
    private ArrayList<Sensor> mSensors;

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
        new Thread(new Runnable() {
            @Override
            public void run() {
                EditText deviceNameView = (EditText) findViewById(R.id.deviceNameEditText);
                String deviceName = deviceNameView.getText().toString();
                try {
                    ServerCommunicator.registerDevice(mDevice, deviceName);
                } catch (ServerCommunicationException e) {
                    Log.e(TAG, e.getMessage());
                }
                try {
                    //configure sensors
                    List<Sensor> sensors = new ArrayList<>();
                    for (Sensor s : mSensors){
                        Sensor sensor = new Sensor(sensors.size(), s.getName(), mDevice);
                        sensors.add(sensor);
                        Log.i(TAG, "current device id: " + mDevice.getId());
                        ServerCommunicator.registerSensor(sensor);
                    }
                } catch (ServerCommunicationException e) {
                }
            }
        }).start();
        Log.i(TAG, "Registration started");
    }

}
