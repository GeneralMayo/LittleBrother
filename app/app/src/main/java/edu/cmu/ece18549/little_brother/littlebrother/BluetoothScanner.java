package edu.cmu.ece18549.little_brother.littlebrother;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Ramsey on 4/2/2016.
 */
public class BluetoothScanner {
    private static final long SCAN_PERIOD = 10000;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBLEScanner;
    private Handler handler;
    private boolean mScanning;

    private final static String TAG = "BLUETOOTH_SCANNER";

    //private List<ScanFilter> mFilters;
    //private ScanSettings mScanSettings;

    public BluetoothScanner(Context context) {
        final BluetoothManager bluetoothManager =
                (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mBLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
        handler = new Handler();
        //mFilters = new LinkedList<ScanFilter>();
        //setupFilters();
        //mScanSettings = getScanSettings();
    }

    private void setupFilters() {
        //ScanFilter.Builder builder = new ScanFilter.Builder();
        //builder.set
    }

    public Collection<Device> getDevices() {
        final List<BluetoothDevice> bluetoothDevices = Collections.synchronizedList(new LinkedList<BluetoothDevice>());
        ScanCallback callback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                BluetoothDevice device = result.getDevice();
                bluetoothDevices.add(device);
                Log.i(TAG,"Device Found:\nName="+device.getName());
            }
        };

        return null;
    }

    private void scanLeDevice(final boolean enable, final ScanCallback callback) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBLEScanner.stopScan(callback);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBLEScanner.startScan(callback);
            Log.i(TAG,"Scanning started");
        } else {
            mScanning = false;
            mBLEScanner.stopScan(callback);
        }

    }
}
