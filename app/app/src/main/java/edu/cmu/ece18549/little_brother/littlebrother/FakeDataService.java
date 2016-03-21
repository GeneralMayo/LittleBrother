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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class FakeDataService extends Service implements DeviceFinderService {
   private final LocalBinder mBinder = new LocalBinder();
   private List<Device> mDevices;
    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    private final static int THREAD_WAIT_TIME = 10000;
    private final static int SERVICE_START_MODE = START_STICKY;

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            //add one device to the list of devices every 10 seconds until 10 devices are present
            try {
                int counter = 0;
                while(true) {
                    if (counter < 10) {
                        mDevices.add(new Device(counter));
                        counter++;
                    }
                        Thread.sleep(THREAD_WAIT_TIME);
                }
            } catch (InterruptedException e) {
                // Restore interrupt status.
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void onCreate() {
        mDevices = Collections.synchronizedList(new LinkedList<Device>());
        HandlerThread thread = new HandlerThread("SimpleWorker", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
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
    public List<Device> getDevices() {
        return mDevices;
    }
}
