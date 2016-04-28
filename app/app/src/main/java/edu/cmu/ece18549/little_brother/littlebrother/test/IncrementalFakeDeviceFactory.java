package edu.cmu.ece18549.little_brother.littlebrother.test;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import edu.cmu.ece18549.little_brother.littlebrother.data_component.Device;
import edu.cmu.ece18549.little_brother.littlebrother.data_component.DeviceLog;
import edu.cmu.ece18549.little_brother.littlebrother.data_component.Sensor;

/**
 * Created by alexmaeda on 4/27/16.
 */
public class IncrementalFakeDeviceFactory extends FakeDeviceFactory {
    private final double INIT_LAT = 40.4443;
    private final double INIT_LONG = -79.9449;

    private List<Device> mDevices;
    private final static String TAG = "SIMPLE_DEVICE_FACTORY";
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
    }

    @Override
    protected Device getNewDevice() {
        Device device = new Device(mDeviceCount, "Device " + mDeviceCount,
                                   INIT_LAT + 0.1*mDeviceCount, INIT_LONG + 0.1*mDeviceCount);
        int numSensors = getRandomInt(3);
        Sensor[] sensors = new Sensor[numSensors];
        for (int i = 0; i < numSensors; i++) {
            sensors[i] = new Sensor(mSensorCount, "Sensor " + mSensorCount, device);
            new DeviceLog(0, new Date(), mLogCount, new Date(), sensors[i]);
            mSensorCount += 1;
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
