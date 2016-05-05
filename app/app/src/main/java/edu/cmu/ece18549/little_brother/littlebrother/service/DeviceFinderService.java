package edu.cmu.ece18549.little_brother.littlebrother.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import edu.cmu.ece18549.little_brother.littlebrother.adapter.BluetoothScanner;
import edu.cmu.ece18549.little_brother.littlebrother.adapter.ServerCommunicationException;
import edu.cmu.ece18549.little_brother.littlebrother.adapter.ServerCommunicator;
import edu.cmu.ece18549.little_brother.littlebrother.data_component.Device;
import edu.cmu.ece18549.little_brother.littlebrother.data_component.DeviceLog;
import edu.cmu.ece18549.little_brother.littlebrother.R;
import edu.cmu.ece18549.little_brother.littlebrother.test.IncrementalFakeDeviceFactory;

public class DeviceFinderService extends Service implements DeviceFinderServiceInterface, Observer{
    private final static int SERVICE_START_MODE = START_STICKY;
    private final static int INTERVAL = 5000;
    private final static String TAG = "DeviceFinder";
    private final LocalBinder mBinder = new LocalBinder();
    private BlockingQueue<DeviceLog> mLogs;
    private List<Device> mDevices;
    private BluetoothScanner mBluetoothScanner;
    private List<Observer> listeners;
    private Collection<Device> tempDevices;
    private Thread producerThread;

    public static final String BLE_CHANGE_ACTION =
            "edu.cmu.ece18549.little_brother.littlebrother.service.ble_change_action";
    public static final String BLE_CHANGE_EXTRA =
            "edu.cmu.ece18549.little_brother.littlebrother.service.ble_change_extra";
    public static final int DEVICE_FOUND = 0;
    public static final int LOG_FOUND = 1;
    public static final int DEVICE_DONE = 2;

    public Intent mBLEChangeIntent;

    @Override
    public void notifyChange(Notification n, Object o) {
        //Log.i(TAG,"Received notification");
        mBLEChangeIntent = new Intent(BLE_CHANGE_ACTION);
        switch(n) {
            case DEVICE_ADDED:
                Device o1 = (Device) o;
                //Log.i(TAG,"Device notified with id="+o1.getId());
                if (mDevices.contains(o1)) {
                    mDevices.remove(o1);
                    mDevices.add(o1);
                } else {
                    mDevices.add(o1);
                }
                mBLEChangeIntent.putExtra(BLE_CHANGE_EXTRA, DEVICE_FOUND);
                sendBroadcast(mBLEChangeIntent);
                break;
            case LOG_FOUND:
                DeviceLog o2 = (DeviceLog)o;
                //Log.i(TAG,"Log notified with id="+String.format("%x",o2.getId()));
                try {
                    mLogs.put(o2);
                    mBLEChangeIntent.putExtra(BLE_CHANGE_EXTRA, LOG_FOUND);
                    sendBroadcast(mBLEChangeIntent);
                } catch (InterruptedException e) {
                    Log.i(TAG,"NotifyChange interrupted on add");
                }
                break;
            case DEVICE_DONE:
                Log.i(TAG,"Done with device");
                mBLEChangeIntent.putExtra(BLE_CHANGE_EXTRA, DEVICE_DONE);
                sendBroadcast(mBLEChangeIntent);
                break;
            case NO_DEVICE:
                /*synchronized (monitor) {
                    monitor.notifyAll();
                }*/
                break;
        }
    }

    public class LocalBinder extends Binder {
        public DeviceFinderService getService() {
            // Return this instance of LocalService so clients can call public methods
            return DeviceFinderService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return SERVICE_START_MODE;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private IncrementalFakeDeviceFactory ifdf;

    @Override
    public void onCreate() {
        Log.i(TAG,"Service starting");
        mLogs = new LinkedBlockingQueue<DeviceLog>();
        mDevices = Collections.synchronizedList(new LinkedList<Device>());
//        mBluetoothScanner = new BluetoothScanner(this.getApplicationContext());
//        mBluetoothScanner.registerListener(this);
//        listeners = new LinkedList<Observer>();
        ifdf = new IncrementalFakeDeviceFactory();

        startProducerThread();
        //startConsumerThread();
    }

    @Override
    public void onDestroy() {
        //mBluetoothScanner.delete();
        Log.i(TAG, "Service ending");
    }

    private void startProducerThread() {
        this.producerThread = new Thread(new Runnable() {

            @Override
            public void run() {
                int i = 0;
                while (true) {
                    if (i < 5) {
                        Log.i(TAG, "Initiating device scan");
                        Device device = ifdf.getNewDevice();
                        mDevices.add(device);
                        mLogs.addAll(device.getLogs());
                        mBLEChangeIntent = new Intent(BLE_CHANGE_ACTION);
                        mBLEChangeIntent.putExtra(BLE_CHANGE_EXTRA, LOG_FOUND);
                        sendBroadcast(mBLEChangeIntent);
                    }
//                    tempDevices = mBluetoothScanner.getDevices();
                    //Log.i(TAG, "Producer continuing");
                    try {
                        Thread.sleep(INTERVAL);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        });
        producerThread.start();
    }

    private void startConsumerThread() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                Log.i(TAG,"Consumer thread starting");
                HashSet<DeviceLog> sentLogs = new HashSet<DeviceLog>();
                while (true) {
                    try {
                        DeviceLog log = mLogs.take();
                        Log.i(TAG,"Consumer thread found log id="+log.getId());
                        /*if (!sentLogs.contains(log)) {
                            try {
                                Log.i(TAG,"Consumer initiating upload log");
                                ServerCommunicator.uploadLog(log);
                                sentLogs.add(log);
                            } catch (ServerCommunicationException e) {
                                Log.e(TAG, "Server Error: " + e.getMessage());
                            }
                        }*/
                    } catch (InterruptedException e) {
                        Log.i(TAG,"Consumer thread interrupted on take");
                    }
                }
            }
        }).start();
        Log.i(TAG, "Consumer thread started");
    }

    @Override
    public Collection<Device> getDevices() {
        return mDevices;
    }

    @Override
    public Collection<DeviceLog> getLogs() {
        return mLogs;
    }

    @Override
    public void registerListener(Observer o) {
        listeners.add(o);
    }

    private void notifyListeners(Notification n, Object o) {
        for(Observer obs : listeners) {
            obs.notifyChange(n,o);
        }
    }
}
