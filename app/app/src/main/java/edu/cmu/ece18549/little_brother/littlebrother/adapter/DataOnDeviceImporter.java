package edu.cmu.ece18549.little_brother.littlebrother.adapter;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import edu.cmu.ece18549.little_brother.littlebrother.test.IncrementalFakeDeviceFactory;
import edu.cmu.ece18549.little_brother.littlebrother.util.Pair;
import edu.cmu.ece18549.little_brother.littlebrother.data_component.Device;
import edu.cmu.ece18549.little_brother.littlebrother.data_component.Sensor;
import edu.cmu.ece18549.little_brother.littlebrother.data_component.DeviceLog;

import android.util.Log;

/**
 * Created by alexmaeda on 4/24/16.
 */
public class DataOnDeviceImporter implements DataImporter {
    private final String TAG = "DATA_ON_DEVICE_IMPORTER";
    List<Device> mDevices;
    private final int NUM_DEVICES = 5;

    public DataOnDeviceImporter(){
        mDevices = new ArrayList<Device>();
    }

    @Override
    public List<DeviceLog> getAllLogs() {
        return null;
    }

    @Override
    public void importData() {
        IncrementalFakeDeviceFactory ifdf = new IncrementalFakeDeviceFactory();
        for (int i = 0; i < NUM_DEVICES; i++) {
            mDevices.add(ifdf.getNewDevice());
        }
    }

    @Override
    public void exportDataAsDevices(List<Device> devices, HashMap<Device, List<String>> deviceDetails){
        if (devices == null) {
            new RuntimeException("DataOnDeviceImporter: devices cannot be null.");
        }

        devices.clear();

        for (Device d : mDevices) {
            devices.add(d);
        }
    }


    @Override
    public void exportDataAsStrings(List<String> devices, HashMap<String, List<String>> deviceDetails) {
        if (devices == null || deviceDetails == null) {
            new Exception("DataOnDeviceImporter: devices or deviceDetails cannot be null.");
        }

        devices.clear();
        deviceDetails.clear();

        for (Device d : mDevices){
            String deviceName = d.toString();
            devices.add(deviceName);
            List<String> deviceDetail = new ArrayList<String>();
            for (DeviceLog deviceLog : d.getLogs()) {
                String logString = deviceLog.getSensor() + ": \n" +
                                    deviceLog.getDate() + "\n" +
                                    "value: " + deviceLog.getValue();
                deviceDetail.add(logString);
            }
            deviceDetails.put(deviceName, deviceDetail);
        }
    }
}
