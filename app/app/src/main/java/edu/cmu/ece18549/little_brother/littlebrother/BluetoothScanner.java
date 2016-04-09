package edu.cmu.ece18549.little_brother.littlebrother;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
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
    private static final long SCAN_PERIOD = 5000;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBLEScanner;
    private Handler handler;
    private boolean mScanning;

    private final static String TAG = "BLUETOOTH_SCANNER";
    private final Context mContext;
    //private List<ScanFilter> mFilters;
    //private ScanSettings mScanSettings;

    public BluetoothScanner(Context context) {
        final BluetoothManager bluetoothManager =
                (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mBLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
        handler = new Handler();
        mContext = context;
        //mFilters = new LinkedList<ScanFilter>();
        //setupFilters();
        //mScanSettings = getScanSettings();
    }

    private void setupFilters() {
        //ScanFilter.Builder builder = new ScanFilter.Builder();
        //builder.set
    }

    public Collection<Device> getDevices() {
        //final List<Device> devices = Collections.synchronizedList(new LinkedList<Device>());

        final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.i(TAG,"Gatt services discovered");
                    List<BluetoothGattService> services = gatt.getServices();
                    for(BluetoothGattService service : services) {
                        Log.i(TAG,service.toString());
                        Log.i(TAG,"UUID: "+service.getUuid());
                        List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                        if (characteristics != null) {
                            Log.i(TAG,"Found " + characteristics.size() + " characteristics");
                            for (BluetoothGattCharacteristic characteristic : characteristics) {
                                Log.i(TAG, "Data: " + characteristic.getStringValue(0));
                            }
                        } else {
                            Log.i(TAG,"Found no characteristics");
                        }
                    }

                } else {
                    Log.w(TAG, "onServicesDiscovered received: " + status);
                }
            }

            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.i(TAG, "Connected to GATT server.");
                    Log.i(TAG, "Attempting to start service discovery:" +
                            gatt.discoverServices());

                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.i(TAG, "Disconnected from GATT server.");
                }
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicRead(gatt, characteristic, status);
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.i(TAG,"Characteristic Read Successful");
                } else {
                    Log.i(TAG,"onCharacteristicRead recieved " + status);
                }

            }
        };
        ScanCallback callback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                BluetoothDevice device = result.getDevice();
                Log.i(TAG,"Device Found:\nName="+device.getName());
                BluetoothGatt gattConnection = device.connectGatt(mContext,false,mGattCallback);
                Log.i(TAG,"After connect");

            }
        };
        scanLeDevice(true,callback);
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
                    Log.i(TAG, "Scanning stopped");
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBLEScanner.startScan(callback);
            Log.i(TAG,"Scanning started");
        } else {
            mScanning = false;
            mBLEScanner.stopScan(callback);
            Log.i(TAG,"Scanning stopped");
        }

    }
}
