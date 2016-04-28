package edu.cmu.ece18549.little_brother.littlebrother.test;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import edu.cmu.ece18549.little_brother.littlebrother.service.DeviceFinderServiceInterface;
import edu.cmu.ece18549.little_brother.littlebrother.service.Observer;
import edu.cmu.ece18549.little_brother.littlebrother.adapter.ServerCommunicationException;
import edu.cmu.ece18549.little_brother.littlebrother.adapter.ServerCommunicator;
import edu.cmu.ece18549.little_brother.littlebrother.data_component.Device;
import edu.cmu.ece18549.little_brother.littlebrother.data_component.DeviceLog;

public class FakeDataService extends Service implements DeviceFinderServiceInterface {
    private final static int WAIT_LOWER = 3000;
    private final static int WAIT_UPPER = 10000;
    private final static int SERVICE_START_MODE = START_STICKY;
    private final static int RANDOM_SEED = 0xdeadbeef;
    private final static String TAG = "FAKE_DATA_SERVICE";

    private final static FakeDeviceFactory mFakeDeviceFactory = new RandomLogDeviceFactory(RANDOM_SEED);
    private final static Random mRandom = new Random(RANDOM_SEED);

    private final LocalBinder mBinder = new LocalBinder();
    private BlockingQueue<Device> mDevices;

    @Override
    public void registerListener(Observer o){
        return;
    }

    @Override
    public void onCreate() {
        mDevices = new LinkedBlockingQueue<Device>();
        startProducerThread();
        startConsumerThread();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return SERVICE_START_MODE;
    }

    @Override
    public IBinder onBind(Intent intent) {
       return mBinder ;
    }

    public class LocalBinder extends Binder {
        public FakeDataService getService() {
            // Return this instance of LocalService so clients can call public methods
            return FakeDataService.this;
        }
    }

    @Override
    public Collection<Device> getDevices() {
        return mDevices;
    }

    private void startProducerThread() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                while (true) {
                    Device device = mFakeDeviceFactory.makeDevice();
                    Log.i(TAG,"Device " + device + " found");
                    if (device != null) {
                        if (!mDevices.contains(device)) {
                            try {
                                mDevices.put(device);
                                Log.i(TAG, "Device " + device + " added");
                            } catch (InterruptedException e) {
                                Log.i(TAG,"Producer thread interrupted on put");
                            }
                        } else {
                            Log.i(TAG, "Device " + device + " already found");
                        }
                    } else {
                        Log.i(TAG,"No device found");
                    }

                    try {
                        long toSleep = WAIT_LOWER + mRandom.nextInt(WAIT_UPPER - WAIT_LOWER);
                        Log.i(TAG,"Sleeping for " + ((float)toSleep) / 1000.0 + " seconds");
                        Thread.sleep(toSleep);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }).start();
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
}
