package edu.cmu.ece18549.little_brother.littlebrother.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.ece18549.little_brother.littlebrother.R;
import edu.cmu.ece18549.little_brother.littlebrother.adapter.DeviceInfoAdapter;
import edu.cmu.ece18549.little_brother.littlebrother.adapter.SensorRegisterAdapter;
import edu.cmu.ece18549.little_brother.littlebrother.adapter.ServerCommunicationException;
import edu.cmu.ece18549.little_brother.littlebrother.adapter.ServerCommunicator;
import edu.cmu.ece18549.little_brother.littlebrother.data_component.Device;
import edu.cmu.ece18549.little_brother.littlebrother.data_component.Sensor;

public class DeviceRegisterActivity extends AppCompatActivity {
    public static final String TAG = "DeviceRegister";
    private int mDevice_id;
    private SensorRegisterAdapter mAdapter;
    private ArrayList<Sensor> mSensors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDevice_id = (int) getIntent().getSerializableExtra(DeviceInfoAdapter.DEVICE_TAG);
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

        new Thread(new RunnableWithContext(this)).start();
        Log.i(TAG, "Registration started");
    }

    private class RunnableWithContext implements Runnable {
        Context context;

        public RunnableWithContext(Context context) {
            this.context = context;
        }

        @Override
        public void run() {
            EditText deviceNameView = (EditText) findViewById(R.id.deviceNameEditText);
            String deviceName = deviceNameView.getText().toString();
            Device device = Device.devices.get(mDevice_id);
            try {
                ServerCommunicator.registerDevice(device, deviceName);
            } catch (ServerCommunicationException e) {
                Log.e(TAG, e.getMessage());
            }
            try {
                //configure sensors
                List<Sensor> sensors = new ArrayList<>();
                for (Sensor s : mSensors){
                    Sensor sensor = new Sensor(sensors.size(), s.getName(), Device.devices.get(mDevice_id));
                    sensors.add(sensor);
                    Log.i(TAG, "current device id: " + device.getId());
                    ServerCommunicator.registerSensor(sensor);
                }
            } catch (ServerCommunicationException e) {
                Log.e(TAG, e.getMessage());
            }

            Intent intent = new Intent(context, DevicesAroundActivity.class);
            context.startActivity(intent);
        }
    }

}
