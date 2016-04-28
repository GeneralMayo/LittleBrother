package edu.cmu.ece18549.little_brother.littlebrother.test;

import android.util.Log;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import edu.cmu.ece18549.little_brother.littlebrother.data_component.Device;
import edu.cmu.ece18549.little_brother.littlebrother.data_component.DeviceException;
import edu.cmu.ece18549.little_brother.littlebrother.data_component.DeviceLog;
import edu.cmu.ece18549.little_brother.littlebrother.data_component.Sensor;

/**
 * Created by alexmaeda on 4/27/16.
 */
public class IncrementalFakeDeviceFactory extends FakeDeviceFactory {
    private final double INIT_LAT = 40.4443;
    private final double INIT_LONG = -79.9449;

    private List<Device> mDevices;
    private final static String TAG = "INC_DEVICE_FACTORY";
    private int mDeviceCount;
    private int mSensorCount;
    private int mLogCount;

    public IncrementalFakeDeviceFactory(long seed) {
        super(seed);
        initFactory();
    }

    public IncrementalFakeDeviceFactory() {
        super();
        initFactory();
    }

    private void initFactory() {
        mDevices = new LinkedList<Device>();
        mSensorCount = 0;
        mDeviceCount = 0;
        mLogCount = 0;
    }

    @Override
    public Device getNewDevice() {
        Device device = new Device(-1, "Device " + mDeviceCount,
                                   INIT_LAT + 0.1*mDeviceCount, INIT_LONG + 0.1*mDeviceCount);
        int numSensors = getRandomInt(3);
        for (int i = 0; i < numSensors; i++) {
            try {
                Sensor sensor = new Sensor(mSensorCount, "Sensor " + mSensorCount, device);
                device.addSensor(sensor);
                device.addLog(new DeviceLog(mLogCount, new Date(), mLogCount, new Date(), sensor));
                mLogCount += 1;
                mSensorCount += 1;
            } catch (DeviceException e) {
                Log.e(TAG, "Device exception, add sensor or add log failed");
            }
        }

        mDeviceCount += 1;
        return device;
    }

    @Override
    protected void getNewSensors(Device device) {
        //do nothing in here since sensors were already created
    }

    @Override
    protected void getNewLogs(Device device) {
        //do nothing in here since logs were already created
    }


}
