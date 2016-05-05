package edu.cmu.ece18549.little_brother.littlebrother.adapter;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import edu.cmu.ece18549.little_brother.littlebrother.data_component.Device;
import edu.cmu.ece18549.little_brother.littlebrother.data_component.DeviceException;
import edu.cmu.ece18549.little_brother.littlebrother.data_component.DeviceLog;
import edu.cmu.ece18549.little_brother.littlebrother.data_component.Sensor;
import edu.cmu.ece18549.little_brother.littlebrother.service.Notification;
import edu.cmu.ece18549.little_brother.littlebrother.service.Observer;

/**
 * Created by Ramsey on 4/2/2016.
 */
public class BluetoothScanner {

    private static final boolean DEVICE_DEBUG = false;
    private static final boolean SCAN_DEBUG = false;

    private static final long SCAN_PERIOD = 3000;
    private static final UUID DEVICE_INFO_SERVICE_UUID = new UUID(0x4f701111290bac89L,0x5444795490294698L);
    private static final UUID NAME_CHARACTERISTIC_UUID = new UUID(0x4f700001290bac89L,0x5444795490294698L);
    private static final UUID ID_CHARACTERISTIC_UUID = new UUID(0x4f700002290bac89L,0x5444795490294698L);
    private static final UUID SENSOR_CHARACTERISTIC_UUID = new UUID(0x4f700003290bac89L,0x5444795490294698L);

    private static final UUID LOG_SERVICE_UUID = new UUID(0xbd592222a2e1fe97L,0x3746bc007591507bL);
    private static final UUID LOG_CHARACTERISTIC_UUID = new UUID(0xbd590003a2e1fe97L,0x3746bc007591507bL);
    private static final UUID LOG_DESCRIPTOR_UUID = new UUID(0x0000290200001000L,0x800000805f9b34fbL);

    private static final String DEVICE_NAME = "Little_Brother";
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBLEScanner;
    private Handler handler;
    private boolean mScanning;
    private final List<ScanFilter> scanFilters;
    private final ScanSettings scanSettings;
    private final List<Observer> listeners;

    private final static String TAG = "BLUETOOTH_SCANNER";
    private final Context mContext;

    private final List<Device> devices = Collections.synchronizedList(new LinkedList<Device>());

    private volatile boolean bluetoothEnabled;
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                int newState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,BluetoothAdapter.ERROR);

                if (newState == BluetoothAdapter.STATE_ON) {
                    Log.i(TAG,"Bluetooth turned on");
                    bluetoothEnabled = true;
                    mBLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
                } else if (newState == BluetoothAdapter.STATE_OFF) {
                    Log.i(TAG,"Bluetooth turned off");
                    bluetoothEnabled = false;
                }
            }
        }
    };

    public BluetoothScanner(Context context) {
        final BluetoothManager bluetoothManager =
                (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter.isEnabled()) {
            mBLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
            bluetoothEnabled = true;
        } else {
            bluetoothEnabled = false;
        }

        handler = new Handler();
        mContext = context;
        scanFilters = setupFilters();
        scanSettings = setupSettings();
        listeners = new LinkedList<Observer>();
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        context.registerReceiver(receiver, filter);
    }

    private List<ScanFilter> setupFilters() {
        ScanFilter.Builder builder = new ScanFilter.Builder();
        List<ScanFilter> filters = new LinkedList<ScanFilter>();
        builder.setDeviceName(DEVICE_NAME);
        filters.add(builder.build());
        return filters;
   }

    private ScanSettings setupSettings() {
        ScanSettings.Builder builder = new ScanSettings.Builder();
        builder.setScanMode(ScanSettings.SCAN_MODE_BALANCED);
        return builder.build();
    }

    private class NewDevice {
        private Device device;
        private boolean found;

        public void setDevice(Device d) {
            device = d;
        }

        public Device getDevice() {
            return device;
        }

        public void setFound(boolean found) {
            this.found = found;
        }

        public boolean getFound() {
            return found;
        }
    }

    public Collection<Device> getDevices() {

        if (!bluetoothEnabled) {
            Log.i(TAG,"Bluetooth disabled, returning");
            return devices;
        }

        final LinkedList<BluetoothGattCharacteristic> deviceCharacteristics = new LinkedList<>();
        final LinkedList<BluetoothGattCharacteristic> logCharacteristics = new LinkedList<>();

        final NewDevice newDevice = new NewDevice();
        final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    if (DEVICE_DEBUG) Log.i(TAG,"Gatt services discovered");
                    List<BluetoothGattService> services = gatt.getServices();
                    for(BluetoothGattService service : services) {
                        UUID uuid = service.getUuid();
                        if (DEVICE_DEBUG) Log.i(TAG,"Service UUID: " + uuid);
                        if (uuid.equals(DEVICE_INFO_SERVICE_UUID)) {
                            if (DEVICE_DEBUG) Log.i(TAG,"Found device info service");
                            List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                            //Log.i(TAG, "Found " + characteristics.size() + " characteristics");

                            //sort characteristics so id characteristic comes first
                            for(int i = 0; i < characteristics.size(); i++) {
                                if (characteristics.get(i).getUuid().equals(ID_CHARACTERISTIC_UUID)) {
                                    characteristics.add(0,characteristics.remove(i));
                                    break;
                                }
                            }

                            for (BluetoothGattCharacteristic characteristic : characteristics) {
                                if (DEVICE_DEBUG) Log.i(TAG, "Device characteristic:UUID=" + characteristic.getUuid());
                                deviceCharacteristics.add(characteristic);
                            }
                        } else if (uuid.equals(LOG_SERVICE_UUID)) {
                            if (DEVICE_DEBUG) Log.i(TAG,"Found log service");
                            List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                            if (DEVICE_DEBUG) Log.i(TAG, "Found " + characteristics.size() + " characteristics");
                            for (BluetoothGattCharacteristic characteristic : characteristics) {
                                UUID charUUID = characteristic.getUuid();
                                if (DEVICE_DEBUG) Log.i(TAG, "UUID:" + charUUID);
                                if (charUUID.equals(LOG_CHARACTERISTIC_UUID)) {
                                    Log.i(TAG,"Log characteristic found");
                                    if (!gatt.setCharacteristicNotification(characteristic, true)) {
                                        Log.i(TAG,"Set characteristic to notify returned false");
                                    }
                                    logCharacteristics.add(characteristic);
                                } else {
                                    Log.i(TAG,"Unknown log service characteristic");
                                }

                            }
                        }
                    }
                    //Log.i(TAG,"Initiating device info read");
                    gatt.readCharacteristic(deviceCharacteristics.pop());
                } else {
                    Log.w(TAG, "onServicesDiscovered received: " + status);
                }
            }

            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.i(TAG, "Connected to GATT server.");
                    Log.i(TAG, "Attempting to start service discovery:");
                    gatt.discoverServices();
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.i(TAG, "Disconnected from GATT server.");
                    gatt.close();
                    Log.i(TAG, "GATT Server Closed");
                    notifyListeners(Notification.DEVICE_DONE,null);
                }
            }

            @Override
            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                super.onDescriptorWrite(gatt,descriptor,status);
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        Log.i(TAG,"Descriptor write successful");
                    } else {
                        Log.i(TAG,"Descriptor write returned status="+status);
                    }
            }



            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt,characteristic);
                UUID charUUID = characteristic.getUuid();
                if (charUUID.equals(LOG_CHARACTERISTIC_UUID)) {
                    Log.i(TAG,"Log characteristic changed");
                    DeviceLog d = parseLog(characteristic,newDevice.getDevice());
                    if (d.getId() == 0) {
                        Log.i(TAG,"Stop Log received");
                        gatt.disconnect();
                    } else {
                        try {
                            newDevice.getDevice().addLog(d);
                            notifyListeners(Notification.LOG_FOUND,d);
                        } catch (DeviceException e) {
                            Log.i(TAG,e.getMessage());
                        }
                    }
                } else {
                    Log.i(TAG,"Unknown characteristic changed with UUID="+charUUID);
                }

            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicRead(gatt, characteristic, status);
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    if (DEVICE_DEBUG) Log.i(TAG, "Characteristic Read Successful");
                    UUID characteristicUUID = characteristic.getUuid();
                    //Log.i(TAG, "Characteristic UUID:" + characteristicUUID);
                    if (characteristicUUID.equals(NAME_CHARACTERISTIC_UUID)) {
                        //Log.i(TAG,"Name Characteristic Read");
                        String name = characteristic.getStringValue(0);
                        Log.i(TAG, "Name=" + name);
                        newDevice.getDevice().setName(name);
                    } else if (characteristicUUID.equals(ID_CHARACTERISTIC_UUID)) {
                        //Log.i(TAG,"Id Characteristic Read");
                        int id = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 0);
                        Log.i(TAG, "Id=" + id);

                        boolean found = false;
                        for (Device d : devices) {
                            if (d.getId() == id) {
                                Log.i(TAG, "Found already existing device");
                                newDevice.setDevice(d);
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            Log.i(TAG, "Did not find device");
                            Device d = new Device();
                            d.setId(id);
                            newDevice.setDevice(d);
                        }
                        newDevice.setFound(found);

                    } else if (characteristicUUID.equals(SENSOR_CHARACTERISTIC_UUID)) {
                        //Log.i(TAG,"Sensor Characteristic Read");
                        String sensorInfo = characteristic.getStringValue(0);
                        int id = Character.getNumericValue(sensorInfo.charAt(0));
                        String sensorName = sensorInfo.substring(2);
                        Log.i(TAG,"Name=" + sensorName + " Id="+id);
                        Device device = newDevice.getDevice();
                        Sensor newSensor = new Sensor(id, sensorName, device);
                        try {
                            device.addSensor(newSensor);
                        } catch (DeviceException e) {
                            Log.i(TAG, e.getMessage());
                        }
                    } else if (characteristicUUID.equals(LOG_CHARACTERISTIC_UUID)) {
                        if (DEVICE_DEBUG) Log.i(TAG, "Log Characteristic Read");
                        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(LOG_DESCRIPTOR_UUID);
                        if (descriptor != null) {
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            gatt.writeDescriptor(descriptor);
                            if (DEVICE_DEBUG) Log.i(TAG, "Wrote notify descriptor");
                        } else {
                            Log.i(TAG, "Log characteristic descriptor is null");
                        }
                    } else {
                        Log.i(TAG,"Received unknown characteristic with UUID " + characteristic.getUuid());
                    }
                } else {
                    Log.i(TAG,"onCharacteristicRead not successful with status " + status);
                }

                if (deviceCharacteristics.size() > 0) {
                    gatt.readCharacteristic(deviceCharacteristics.pop());
                } else {
                    if (!newDevice.getFound()) {
                        Log.i(TAG,"Device not in list, adding...");
                        Device d = newDevice.getDevice();
                        devices.add(d);
                        newDevice.setFound(true);
                        notifyListeners(Notification.DEVICE_ADDED,d);
                    }

                    if (logCharacteristics.size() == 1) {
                        gatt.readCharacteristic(logCharacteristics.pop());
                    }
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
                refreshDeviceCache(gattConnection);
                //Log.i(TAG,"After connect");

            }
        };
        scanLeDevice(true, callback);
        return devices;
    }

    private DeviceLog parseLog(BluetoothGattCharacteristic characteristic,Device device) {
        int time = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32,0);
        Log.i(TAG,"time="+String.format("%x",time));
        int value = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32,4);
        Log.i(TAG,"value="+String.format("%x",value));
        int logId = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32,8);
        Log.i(TAG,"logId="+String.format("%x",logId));
        int sensorId = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16,12);
        Log.i(TAG, "sensorId=" + String.format("%x", sensorId));
        return new DeviceLog(logId, new Date(time), (double) value, new Date(), device.getSensor(sensorId));
    }

    private void scanLeDevice(final boolean enable, final ScanCallback callback) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    if (mBluetoothAdapter.isEnabled()) {
                        mBLEScanner.stopScan(callback);
                        if (SCAN_DEBUG) Log.i(TAG, "Scanning stopped");
                    } else {
                        Log.i(TAG,"Scanning not stopped, adapter is off");
                    }
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBLEScanner.startScan(scanFilters, scanSettings, callback);
            if (SCAN_DEBUG) Log.i(TAG,"Scanning started");
        } else {
            mScanning = false;
            if (mBluetoothAdapter.isEnabled()) {
                mBLEScanner.stopScan(callback);
                if (SCAN_DEBUG) Log.i(TAG, "Scanning stopped");
            } else {
                Log.i(TAG,"Scanning not stopped, adapter turned off");
            }
        }

    }

    public void registerListener(Observer obs) {
        listeners.add(obs);
    }

    private void notifyListeners(Notification n, Object o) {
        for(Observer obs : listeners) {
            obs.notifyChange(n,o);
        }
    }

    public void delete() {
        mContext.unregisterReceiver(receiver);
    }

    public BluetoothAdapter getAdapter() {
        return mBluetoothAdapter;
    }

    private boolean refreshDeviceCache(BluetoothGatt gatt){
        try {
            BluetoothGatt localBluetoothGatt = gatt;
            Method localMethod = localBluetoothGatt.getClass().getMethod("refresh", new Class[0]);
            if (localMethod != null) {
                boolean bool = ((Boolean) localMethod.invoke(localBluetoothGatt, new Object[0])).booleanValue();
                Log.i(TAG,"Device cache refreshed="+bool);
                return bool;
            }
        }
        catch (Exception localException) {
            Log.e(TAG, "An exception occurred while refreshing device");
        }
        return false;
    }

}
