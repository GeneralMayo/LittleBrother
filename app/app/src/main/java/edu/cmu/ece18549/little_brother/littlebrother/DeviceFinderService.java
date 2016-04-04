package edu.cmu.ece18549.little_brother.littlebrother;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class DeviceFinderService extends Service implements DeviceFinderServiceInterface{
    private final static int SERVICE_START_MODE = START_STICKY;
    private final static int INTERVAL = 15000;
    private final static String TAG = "DeviceFinder";
    private final LocalBinder mBinder = new LocalBinder();
    private BlockingQueue<Device> mDevices;
    private BluetoothScanner mBluetoothScanner;
    private Handler handler;

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
        mDevices = new LinkedBlockingQueue<Device>();
        mBluetoothScanner = new BluetoothScanner(this.getApplicationContext());
        handler = new Handler();
        startProducerThread();
        //startConsumerThread();
    }

    private void startProducerThread() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG,"Initiating scan for devices");
                mBluetoothScanner.getDevices();
            }
        }, INTERVAL);
        Log.i("FAKE_DATA_SERVICE", "Producer thread started");
    }

    private void startConsumerThread() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                HashSet<DeviceLog> sentLogs = new HashSet<DeviceLog>();
                while (true) {
                    try {
                        Device d = mDevices.take();
                        Log.i(TAG,"Consumer thread found device " + d);
                        ArrayList<DeviceLog> logs = d.getLogs();
                        for (DeviceLog log : logs) {
                            if (!sentLogs.contains(log)) {
                                try {
                                    Log.i(TAG,"Consumer initiating upload log");
                                    ServerCommunicator.uploadLog(log);
                                    sentLogs.add(log);
                                } catch (ServerCommunicationException e) {
                                    Log.e(TAG, "Server Error: " + e.getMessage());
                                }
                            }
                        }
                    } catch (InterruptedException e) {
                        Log.i(TAG,"Consumer thread interrupted on take");
                    }
                }
            }
        }).start();
        Log.i("FAKE_DATA_SERVICE", "Consumer thread started");
    }

    @Override
    public Collection<Device> getDevices() {
        return mDevices;
    }
}
