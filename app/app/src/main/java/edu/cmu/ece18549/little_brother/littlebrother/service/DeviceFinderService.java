package edu.cmu.ece18549.little_brother.littlebrother.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
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

public class DeviceFinderService extends Service implements DeviceFinderServiceInterface, Observer{
    private final static int SERVICE_START_MODE = START_STICKY;
    private final static int INTERVAL = 10000;
    private final static String TAG = "DeviceFinder";
    private final LocalBinder mBinder = new LocalBinder();
    private BlockingQueue<DeviceLog> mLogs;
    private List<Device> mDevices;
    private BluetoothScanner mBluetoothScanner;
    private Handler handler;
    private List<Observer> listeners;
    private Collection<Device> tempDevices;

    @Override
    public void notifyChange(Notification n, Object o) {

        switch(n) {
            case DEVICE_ADDED:
                mDevices.add((Device)o);
                notifyListeners(n,o);
                break;
            case LOG_FOUND:
                try {
                    mLogs.put((DeviceLog) o);
                    notifyListeners(n,o);
                } catch (InterruptedException e) {
                    Log.i(TAG,"NotifyChange interrupted on add");
                }
                break;
        }
    }

    public class LocalBinder extends Binder {
        DeviceFinderService getService() {
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
        return mBinder ;
    }

    @Override
    public void onCreate() {
        mLogs = new LinkedBlockingQueue<DeviceLog>();
        mDevices = Collections.synchronizedList(new LinkedList<Device>());
        mBluetoothScanner = new BluetoothScanner(this.getApplicationContext());
        mBluetoothScanner.registerListener(this);
        handler = new Handler();
        listeners = new LinkedList<Observer>();
        startProducerThread();
        //startConsumerThread();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Service ending");
    }

    private void startProducerThread() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                int iterations = 0;
                while (true) {
                    if (iterations == 1) {
                        stopSelf();
                    } else {
                        Log.i(TAG, "Initiating device scan " +iterations);
                        tempDevices = mBluetoothScanner.getDevices();
                        iterations++;
                        try {
                            Thread.sleep(INTERVAL);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            }
        }).start();
    }

    private void startConsumerThread() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                HashSet<DeviceLog> sentLogs = new HashSet<DeviceLog>();
                while (true) {
                    try {
                        DeviceLog log = mLogs.take();
                        Log.i(TAG,"Consumer thread found log " + log);
                        if (!sentLogs.contains(log)) {
                            try {
                                Log.i(TAG,"Consumer initiating upload log");
                                ServerCommunicator.uploadLog(log);
                                sentLogs.add(log);
                            } catch (ServerCommunicationException e) {
                                Log.e(TAG, "Server Error: " + e.getMessage());
                            }
                        }
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
