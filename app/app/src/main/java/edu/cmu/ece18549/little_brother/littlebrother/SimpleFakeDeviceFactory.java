package edu.cmu.ece18549.little_brother.littlebrother;

import android.util.Log;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Ramsey on 3/22/2016.
 */
public class SimpleFakeDeviceFactory extends FakeDeviceFactory {

    private List<Device> mDevices;
    private final static String TAG = "SIMPLE_DEVICE_FACTORY";

    public SimpleFakeDeviceFactory(long seed) {
        super(seed);
        initFactory();
    }

    public SimpleFakeDeviceFactory() {
        super();
        initFactory();
    }

    private void initFactory() {
        mDevices = new LinkedList<Device>();
        try {
            //create fake device with 2 sensors and 3 logs for the first and 1 for the second
            Device device0 = new Device(0, "Bathroom", 55.5034070, -5.1275920);
            Sensor sensor00 = new Sensor(0, "Light", device0);
            Sensor sensor01 = new Sensor(1, "Temperature", device0);
            DeviceLog log000 = new DeviceLog(0, new Date(1451721845), 32.0, new Date(), sensor00);
            DeviceLog log001 = new DeviceLog(1, new Date(1454400245), 16.0, new Date(), sensor00);
            DeviceLog log002 = new DeviceLog(2, new Date(1456905845), 8.0, new Date(), sensor00);
            DeviceLog log010 = new DeviceLog(0, new Date(1451721845), 10.0, new Date(), sensor01);
            device0.addSensor(sensor00);
            device0.addSensor(sensor01);
            device0.addLog(log000);
            device0.addLog(log001);
            device0.addLog(log002);
            device0.addLog(log010);

            //create fake device with 1 sensor and 2 logs
            Device device1 = new Device(1, "Deck", 80.103, -50.3333333);
            Sensor sensor10 = new Sensor(0, "Temperature", device1);
            DeviceLog log100 = new DeviceLog(0, new Date(1441710365), -10.0, new Date(), sensor10);
            DeviceLog log101 = new DeviceLog(1, new Date(1407578704), 11.3, new Date(), sensor10);
            device1.addSensor(sensor10);
            device1.addLog(log100);
            device1.addLog(log101);

            //create fake device with no sensors
            Device device2 = new Device(2, "Living Room", 1.89, 1.9422447);

            mDevices.add(device0);
            mDevices.add(device1);
            mDevices.add(device2);

            mDevices.add(null); //add a null object to represent no device found

        } catch (DeviceException e) {
            Log.e(TAG,e.getMessage());
        }
    }

    @Override
    protected Device getNewDevice() {
        return mDevices.get(getRandomInt(mDevices.size()));
    }

    @Override
    protected void getNewSensors(Device device) {
        //do nothing in here since sensors were already created
    }

    @Override
    protected void getNewLogs(Device device) {
        //do nothing in here since sensors were already created
    }
}
