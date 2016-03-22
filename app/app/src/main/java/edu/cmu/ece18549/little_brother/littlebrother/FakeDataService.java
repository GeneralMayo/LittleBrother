package edu.cmu.ece18549.little_brother.littlebrother;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class FakeDataService extends Service implements DeviceFinderServiceInterface {
    private final static int WAIT_LOWER = 3000;
    private final static int WAIT_UPPER = 10000;
    private final static int SERVICE_START_MODE = START_STICKY;
    private final static int RANDOM_SEED = 0xdeadbeef;
    private final static int MAX_NUM_DEVICES = 3;
    private final static int P_DELETION = 3;
    private final static String TAG = "FAKE_DATA_SERVICE";

    private final static FakeDeviceFactory mFakeDeviceFactory = new SimpleFakeDeviceFactory(RANDOM_SEED);
    private final static Random mRandom = new Random(RANDOM_SEED);

   private final LocalBinder mBinder = new LocalBinder();
   private Collection<Device> mDevices;


    @Override
    public void onCreate() {
        mDevices = Collections.synchronizedSet(new HashSet<Device>());
        new Thread(new Runnable() {

            @Override
            public void run() {
                while (true) {
                    Device device = mFakeDeviceFactory.makeDevice();
                    Log.i(TAG,"Device " + device + " found");
                    if (device != null) {
                        if (mDevices.contains(device)) {
                            if (mRandom.nextInt(P_DELETION) == 0) {
                                mDevices.remove(device);
                                Log.i(TAG, "Device " + device + " removed");
                            } else {
                                Log.i(TAG, "Device " + device + " remains");
                            }
                        } else {
                            mDevices.add(device);
                            Log.i(TAG, "Device " + device + " added");
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
        Log.i("FAKE_DATA_SERVICE","Thread started");
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
        FakeDataService getService() {
            // Return this instance of LocalService so clients can call public methods
            return FakeDataService.this;
        }
    }

    @Override
    public Collection<Device> getDevices() {
        return mDevices;
    }
}
