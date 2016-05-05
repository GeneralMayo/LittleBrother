package edu.cmu.ece18549.little_brother.littlebrother.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import edu.cmu.ece18549.little_brother.littlebrother.R;
import edu.cmu.ece18549.little_brother.littlebrother.adapter.BluetoothScanner;
import edu.cmu.ece18549.little_brother.littlebrother.adapter.DevicesAroundAdapter;
import edu.cmu.ece18549.little_brother.littlebrother.adapter.ServerCommunicationException;
import edu.cmu.ece18549.little_brother.littlebrother.adapter.ServerCommunicator;
import edu.cmu.ece18549.little_brother.littlebrother.data_component.Device;
import edu.cmu.ece18549.little_brother.littlebrother.data_component.DeviceException;
import edu.cmu.ece18549.little_brother.littlebrother.data_component.Sensor;
import edu.cmu.ece18549.little_brother.littlebrother.service.DeviceFinderService;
import edu.cmu.ece18549.little_brother.littlebrother.test.IncrementalFakeDeviceFactory;

public class DevicesAroundActivity extends AppCompatActivity {
    private static final String TAG = "DevicesAroundActivity";
    public static final int REGISTER_REQUEST_CODE = 0;

    private class DevicesAroundBroadcastReceiver extends BroadcastReceiver{
        DevicesAroundActivity activity;
        DevicesAroundAdapter mAdapter;
        List<Device> mDevices;

        public DevicesAroundBroadcastReceiver(DevicesAroundActivity activity){
            this.activity = activity;
        }

        @Override
        public void onReceive(final Context context, final Intent intent) {
            Log.i(TAG, "BroadcastReceiver received intent");
            Serializable s = intent.getSerializableExtra(DeviceFinderService.BLE_CHANGE_EXTRA);

            if (s == null) {
                Log.i(TAG, "intent was not from the DeviceFinderService. Ignore.");
            }
            else {
                int id = (int) s;
                Log.i(TAG, "Received ID:" +id);
                updateDevices();
            }
        }
    };

    DevicesAroundAdapter mAdapter;
    private List<Device> mDevices;
    private RecyclerView mDeviceListView;
    private BroadcastReceiver receiver;
    private boolean mBound;
    private DeviceFinderService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "in DevicesAroundActivty");

        setContentView(R.layout.activity_devices_around);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDeviceListView = (RecyclerView) findViewById(R.id.deviceAroundList);

        mDevices = new ArrayList<>();
        mAdapter = new DevicesAroundAdapter(mDevices, this);
        mDeviceListView.setAdapter(mAdapter);
        mDeviceListView.setLayoutManager(new LinearLayoutManager(this));
        receiver = new DevicesAroundBroadcastReceiver(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Bind to LocalService
        Intent intent = new Intent(this, DeviceFinderService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            Log.i(TAG, "device finder service connected");

            DeviceFinderService.LocalBinder binder = (DeviceFinderService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            IntentFilter filter = new IntentFilter();
            filter.addAction(DeviceFinderService.BLE_CHANGE_ACTION);
            registerReceiver(receiver, filter);
            updateDevices();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    protected void onPause() {
        unregisterReceiver(receiver);
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REGISTER_REQUEST_CODE) {
            if(resultCode == DeviceRegisterActivity.RESULT_OK) {
                int deviceId = data.getIntExtra(DeviceRegisterActivity.DEVICE_ID_EXTRA, -1);
                String deviceName = data.getStringExtra(DeviceRegisterActivity.DEVICE_NAME_EXTRA);
                ArrayList<String> sensorNames = data.getStringArrayListExtra(DeviceRegisterActivity.SENSOR_NAME_EXTRAS);

                Device device = mDevices.get(deviceId);

                new Thread(new DeviceRegistrationRunnable(device, deviceName, sensorNames)).start();

                updateDevices();
            }
        }
    }

    private class DeviceRegistrationRunnable implements Runnable {
        private Device mDevice;
        private String mDeviceName;
        private List<String> mSensorNames;

        public DeviceRegistrationRunnable(Device device, String deviceName, List<String> sensorNames) {
            mDevice = device;
            mDeviceName = deviceName;
            mSensorNames = sensorNames;
        }

        @Override
        public void run() {
            boolean success = false;
            try {
                ServerCommunicator.registerDevice(mDevice, mDeviceName);
                int sensor_count = 0;
                for (String s : mSensorNames){
                    Sensor sensor = new Sensor(sensor_count, s, mDevice);
                    ServerCommunicator.registerSensor(sensor);
                    mDevice.addSensor(sensor);
                    sensor_count += 1;
                }
                success = true;
            } catch (Exception e) {
                //for any exception, should unregister and reset all
                e.printStackTrace();
            } catch (DeviceException e) {
                e.printStackTrace();
            }

            if (!success) {
                Snackbar.make(findViewById(R.id.deviceAroundList),
                        "There was a problem with the device registration", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            } else {
                Snackbar.make(findViewById(R.id.deviceAroundList),
                        "Success! Device registered as id: " + mDevice.getId(), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                BluetoothScanner scanner = mService.getScanner();
                scanner.registerDevice(mDevice);
            }
        }
    }

    public void updateDevices() {
        mDevices.clear();
        mDevices.addAll(mService.getDevices());
        mAdapter.notifyDataSetChanged();
    }
}
