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
    private static final long SCAN_PERIOD = 5000;
    private static final UUID DEVICE_INFO_SERVICE_UUID = new UUID(0x4f701111290bac89L,0x5444795490294698L);
    private static final UUID NAME_CHARACTERISTIC_UUID = new UUID(0x4f700001290bac89L,0x5444795490294698L);
    private static final UUID ID_CHARACTERISTIC_UUID = new UUID(0x4f700002290bac89L,0x5444795490294698L);
    private static final UUID SENSOR_CHARACTERISTIC_UUID = new UUID(0x4f700003290bac89L,0x5444795490294698L);

    private static final UUID LOG_SERVICE_UUID = new UUID(0xbd592222a2e1fe97L,0x3746bc007591507bL);
    private static final UUID LOG_CHARACTERISTIC_UUID = new UUID(0xbd590003a2e1fe97L,0x3746bc007591507bL);
    private static final UUID NUM_LOG_CHARACTERISTIC_UUID = new UUID(0xbd590002a2e1fe97L,0x3746bc007591507bL);
    private static final UUID WRITE_CHARACTERISTIC_UUID = new UUID(0xbd590001a2e1fe97L,0x3746bc007591507bL);

    //private static final ParcelUuid DEVICE_INFO_UUID_P = new ParcelUuid(DEVICE_INFO_SERVICE_UUID);

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

    private boolean bluetoothEnabled;
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG,"Received intent");
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
        //builder.setServiceUuid(DEVICE_INFO_UUID_P);
        builder.setDeviceName(DEVICE_NAME);
        filters.add(builder.build());
        return filters;
   }

    private ScanSettings setupSettings() {
        ScanSettings.Builder builder = new ScanSettings.Builder();
        //builder.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES);
        //builder.setMatchMode(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT);
        builder.setScanMode(ScanSettings.SCAN_MODE_BALANCED);
        return builder.build();
    }
    private class LogCount {
        private int count;

        public LogCount() {
            this.count = 0;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public int getCount() {
            return this.count;
        }

        public void decCount() {
            this.count = this.count - 1;
        }
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
        final List<Device> devices = Collections.synchronizedList(new LinkedList<Device>());
        if (!bluetoothEnabled) {
            Log.i(TAG,"Bluetooth disabled, returning empty list");
            return devices;
        }

        final LinkedList<BluetoothGattCharacteristic> deviceCharacteristics = new LinkedList<>();
        final LinkedList<BluetoothGattCharacteristic> logCharacteristics = new LinkedList<>();
        final LogCount numLogs = new LogCount();

        final NewDevice newDevice = new NewDevice();
        final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.i(TAG,"Gatt services discovered");
                    List<BluetoothGattService> services = gatt.getServices();
                    for(BluetoothGattService service : services) {
                        UUID uuid = service.getUuid();
                        Log.i(TAG,"Service UUID: " + uuid);
                        if (uuid.equals(DEVICE_INFO_SERVICE_UUID)) {
                            Log.i(TAG,"Found device info service");
                            List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                            Log.i(TAG, "Found " + characteristics.size() + " characteristics");

                            //sort characteristics so id characteristic comes first
                            for(int i = 0; i < characteristics.size(); i++) {
                                if (characteristics.get(i).getUuid().equals(ID_CHARACTERISTIC_UUID)) {
                                    characteristics.add(0,characteristics.remove(i));
                                    break;
                                }
                            }

                            for (BluetoothGattCharacteristic characteristic : characteristics) {
                                Log.i(TAG, "UUID:" + characteristic.getUuid());
                                deviceCharacteristics.add(characteristic);
                            }
                        } else if (uuid.equals(LOG_SERVICE_UUID)) {
                            Log.i(TAG,"Found log service");
                            List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                            Log.i(TAG, "Found " + characteristics.size() + " characteristics");
                            sortCharacteristics(characteristics);
                            for (BluetoothGattCharacteristic characteristic : characteristics) {
                                UUID charUUID = characteristic.getUuid();
                                Log.i(TAG, "UUID:" + charUUID);
                                logCharacteristics.add(characteristic);
                            }
                        }
                    }
                    gatt.readCharacteristic(deviceCharacteristics.pop());
                } else {
                    Log.w(TAG, "onServicesDiscovered received: " + status);
                }
            }

            private void sortCharacteristics(List<BluetoothGattCharacteristic> c) {
                for(int i = 0; i < 3; i++) {
                    BluetoothGattCharacteristic ch = c.get(i);
                    if (ch.getUuid().equals(NUM_LOG_CHARACTERISTIC_UUID)) {
                        c.remove(i);
                        c.add(0,ch);
                    } else if (ch.getUuid().equals(LOG_CHARACTERISTIC_UUID)) {
                        c.remove(i);
                        c.add(1, ch);
                    } else {
                        c.remove(i);
                        c.add(2,ch);
                    }
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
                }
            }

            @Override
            public void onCharacteristicWrite (BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt,characteristic,status);
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.i(TAG, "Write successful");
                    if (characteristic.getUuid().equals(WRITE_CHARACTERISTIC_UUID)) {
                        int logCount = numLogs.getCount();
                        if (logCount > 0) {
                            Log.i(TAG,"Reading another log, count remaining=" + logCount);
                            gatt.readCharacteristic(logCharacteristics.get(0));
                        } else {
                            Log.i(TAG,"No logs remaining");

                            //remove rest of log characteristics
                            logCharacteristics.pop();
                            logCharacteristics.pop();
                        }
                    } else {
                        Log.i(TAG, "Characteristic with uuid=" + characteristic.getUuid() + " not writeChar");
                    }
                } else {
                    Log.i(TAG,"Write completed with status " + status);
                }
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicRead(gatt, characteristic, status);
                boolean wantLog = false;
                boolean success = true;
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.i(TAG,"Characteristic Read Successful");
                    UUID characteristicUUID = characteristic.getUuid();
                    Log.i(TAG,"Characteristic UUID:"+characteristicUUID);
                    if (characteristicUUID.equals(NAME_CHARACTERISTIC_UUID)) {
                        Log.i(TAG,"Name Characteristic Read");
                        String name = characteristic.getStringValue(0);
                        Log.i(TAG, "Name=" + name);
                        newDevice.getDevice().setName(name);
                    } else if (characteristicUUID.equals(ID_CHARACTERISTIC_UUID)) {
                        Log.i(TAG,"Id Characteristic Read");
                        int id = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8,0);
                        Log.i(TAG,"Id=" + id);

                        boolean found = false;
                        for (Device d : devices) {
                            if (d.getId() == id) {
                                Log.i(TAG,"Found already existing device");
                                newDevice.setDevice(d);
                                found = true;
                                break;
                            }
                        }
                        if(!found) {
                            Log.i(TAG,"Did not find device");
                            Device d = new Device();
                            d.setId(id);
                            newDevice.setDevice(d);
                        }
                        newDevice.setFound(found);

                    } else if (characteristicUUID.equals(SENSOR_CHARACTERISTIC_UUID)) {
                        Log.i(TAG,"Sensor Characteristic Read");
                        String sensorInfo = characteristic.getStringValue(0);
                        int id = Character.getNumericValue(sensorInfo.charAt(0));
                        String sensorName = sensorInfo.substring(2);
                        Log.i(TAG,"Name=" + sensorName + " Id="+id);
                        Device device = newDevice.getDevice();
                        Sensor newSensor = new Sensor(id,sensorName,device);
                        try {
                            device.addSensor(newSensor);
                        } catch (DeviceException e) {
                            Log.i(TAG,e.getMessage());
                        }
                    } else if (characteristicUUID.equals(NUM_LOG_CHARACTERISTIC_UUID)) {
                        Log.i(TAG,"NumLog Characteristic Read");
                        int logCount = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16,0);
                        Log.i(TAG,"Count="+logCount);
                        numLogs.setCount(logCount);
                    } else if (characteristicUUID.equals(LOG_CHARACTERISTIC_UUID)) {
                        wantLog = true;
                        Log.i(TAG,"Log Characteristic Read");
                        Device d = newDevice.getDevice();
                        byte[] logStruct = characteristic.getValue();
                        int time = extractInt(logStruct,0);
                        int value = extractInt(logStruct,4);
                        int logId = extractInt(logStruct,8);
                        int sensorId = extractShort(logStruct,12);
                        Log.i(TAG,"time="+time);
                        Log.i(TAG,"value="+value);
                        Log.i(TAG,"logId="+logId);
                        Log.i(TAG,"sensorId="+sensorId);
                        DeviceLog log = new DeviceLog(logId,new Date(time),(double)value,new Date(),d.getSensor(sensorId));
                        try {
                            d.addLog(log);
                            numLogs.decCount();
                            notifyListeners(Notification.LOG_FOUND, null);
                            gatt.writeCharacteristic(logCharacteristics.get(1));
                        } catch (DeviceException e) {
                            success = false;
                            Log.i(TAG,e.getMessage());
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
                        devices.add(newDevice.getDevice());
                        notifyListeners(Notification.DEVICE_ADDED,newDevice);
                    }

                    if (logCharacteristics.size() == 3) {
                        gatt.readCharacteristic(logCharacteristics.pop());
                    } else if (wantLog && !success) {
                       gatt.readCharacteristic(logCharacteristics.get(0));
                    }  else if (logCharacteristics.size() == 0) {
                        gatt.disconnect();
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
                Log.i(TAG,"After connect");

            }
        };
        scanLeDevice(true,callback);
        return devices;
    }

    private int extractInt(byte[] b, int offset) {
        int first = (b[0 + offset] & 0x00ff) << 28;
        int second = (b[0 + offset] & 0xff00) << 24;
        int third = (b[1 + offset] & 0x00ff) << 20;
        int fourth = (b[1 + offset] & 0xff00) << 16;
        int fifth = (b[2 + offset] & 0x00ff) << 12;
        int sixth = (b[2 + offset] & 0xff00) << 8;
        int seventh = (b[3 + offset] & 0x00ff) << 4;
        int eigth = (b[3 + offset] & 0xff00);
        return first & second & third & fourth & fifth & sixth & seventh & eigth;
    }

    private int extractShort(byte[] b, int offset) {
        int fifth = (b[2 + offset] & 0x00ff) << 12;
        int sixth = (b[2 + offset] & 0xff00) << 8;
        int seventh = (b[3 + offset] & 0x00ff) << 4;
        int eigth = (b[3 + offset] & 0xff00);
        return fifth & sixth & seventh & eigth;
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
            mBLEScanner.startScan(scanFilters, scanSettings,callback);
            Log.i(TAG,"Scanning started");
        } else {
            mScanning = false;
            mBLEScanner.stopScan(callback);
            Log.i(TAG,"Scanning stopped");
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
}
