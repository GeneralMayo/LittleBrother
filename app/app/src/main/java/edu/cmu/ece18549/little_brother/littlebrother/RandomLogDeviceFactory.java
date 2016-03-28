package edu.cmu.ece18549.little_brother.littlebrother;

import android.util.Log;

import java.util.Date;

/**
 * Created by Ramsey on 3/28/2016.
 */
public class RandomLogDeviceFactory extends FakeDeviceFactory {
    private Device device = new Device(66,"Test Device 66",0.0,1.0);
    private Sensor sensor = new Sensor(0, "Test Sensor 0", device);

    public final String TAG = "RANDOM_LOG_FACTORY";
    private static int id = 15;

    public RandomLogDeviceFactory(long seed) {
        super(seed);
        try {
            device.addSensor(sensor);
        } catch (DeviceException e) {
            Log.e(TAG,e.getMessage());
        }
    }

    public RandomLogDeviceFactory() {
        super();
        try {
            device.addSensor(sensor);
        } catch (DeviceException e) {
            Log.e(TAG,e.getMessage());
        }
    }

    @Override
    protected Device getNewDevice() {
        return device;
    }

    @Override
    protected void getNewSensors(Device device) {
        //do nothing since we already added sensors
    }

    @Override
    protected void getNewLogs(Device device) {
        try {
            device.addLog(new DeviceLog(id, new Date(), getRandomLong() % 20, new Date(), sensor));
            id++;
        } catch (DeviceException e) {
            Log.e(TAG,e.getMessage());
        }
    }
}
