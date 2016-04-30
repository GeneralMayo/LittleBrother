package edu.cmu.ece18549.little_brother.littlebrother.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
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
import edu.cmu.ece18549.little_brother.littlebrother.data_component.DeviceException;
import edu.cmu.ece18549.little_brother.littlebrother.data_component.Sensor;

public class DeviceRegisterActivity extends AppCompatActivity {
    public static final String SUCCESS_TAG = "SUCCESS";
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

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean wifi_only = prefs.getBoolean("use_wifi_only", true);
        if (wifi_only) {
            WifiManager manager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
            if (!manager.isWifiEnabled()) {
                AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setTitle("Alert");
                alertDialog.setMessage("Wi-Fi is currently disabled, please enable Wi-Fi or disable " +
                                        "the \"only use Wi-Fi\" option in data sync/settings before " +
                                        "registering devices");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        });
                alertDialog.show();
            }
        }
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
            int success;
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
                    Device.devices.get(mDevice_id).addSensor(s);
                }
            } catch (RuntimeException e) {
                Log.e(TAG, e.getMessage());
                Device.devices.get(mDevice_id).clearSensors();
            } catch (DeviceException e) {
                //undo all sensors added
                Device.devices.get(mDevice_id).clearSensors();
            }
            success = Device.devices.get(mDevice_id).getId();
            Intent intent = new Intent(context, DevicesAroundActivity.class);
            intent.putExtra(SUCCESS_TAG, success);
            context.startActivity(intent);
        }
    }

}
