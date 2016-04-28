package edu.cmu.ece18549.little_brother.littlebrother.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.cmu.ece18549.little_brother.littlebrother.data_component.Device;
import edu.cmu.ece18549.little_brother.littlebrother.data_component.DeviceLog;
import edu.cmu.ece18549.little_brother.littlebrother.test.IncrementalFakeDeviceFactory;
import edu.cmu.ece18549.little_brother.littlebrother.util.Pair;

/**
 * Created by alexmaeda on 4/24/16.
 */
public class DeviceInfoImporter implements DataImporter {

    public List<DeviceLog> getAllLogs(){return null;}

    private final String TAG = "DEVICE_INFO_IMPORTER";
    List<Device> mDevices;
    private final int NUM_DEVICES = 5;

    public DeviceInfoImporter(){
        mDevices = new ArrayList<Device>();
    }

    @Override
    public void importData() {
        IncrementalFakeDeviceFactory ifdf = new IncrementalFakeDeviceFactory();
        for (int i = 0; i < NUM_DEVICES; i++) {
            mDevices.add(ifdf.getNewDevice());
        }
//        List<String> devices
//        HashMap<String, List<String>> deviceDetails
//        String newDevice = "Device 1";
//        ArrayList<String> deviceDetail = new ArrayList<String>();
//        deviceDetail.add("Name: Device 1");
//        deviceDetail.add("Active from: 1/10/16 12:00:00 EST");
//        deviceDetail.add("Battery: 100%");
//        devices.add(newDevice);
//        deviceDetails.put(newDevice, deviceDetail);
//
//        newDevice = "Device 2";
//        deviceDetail = new ArrayList<String>();
//        deviceDetail.add("Name: Device 2");
//        deviceDetail.add("Active from: 1/10/16 12:00:00 EST");
//        deviceDetail.add("Battery: 100%");
//        devices.add(newDevice);
//        deviceDetails.put(newDevice, deviceDetail);
//
//        newDevice = "Device 3";
//        deviceDetail = new ArrayList<String>();
//        deviceDetail.add("Name: Device 3");
//        deviceDetail.add("Active from: 1/10/16 12:00:00 EST");
//        deviceDetail.add("Battery: 100%");
//        devices.add(newDevice);
//        deviceDetails.put(newDevice, deviceDetail);

    }


    @Override
    public void exportData(List<String> devices, HashMap<String, List<String>> deviceDetails) {
        if (devices == null || deviceDetails == null) {
            new Exception("DataOnDeviceImporter: devices or deviceDetails cannot be null.");
        }

        devices.clear();
        deviceDetails.clear();

        for (Device d : mDevices) {
            String deviceName = d.toString();
            devices.add(deviceName);
            List<String> deviceDetail = new ArrayList<String>();
            String position = "";
            String lati = String.format("%.4f", Math.abs(d.getLatitude()));
            String longi = String.format("%.4f", Math.abs(d.getLongitude()));
            position += lati + "° ";
            if (d.getLatitude() > 0) {
                position += "N ";
            } else {
                position += "S ";
            }
            position += longi + "° ";
            if (d.getLongitude() > 0) {
                position += "E";
            } else {
                position += "W";
            }

            deviceDetail.add("Position: " + position);
            deviceDetails.put(deviceName, deviceDetail);
        }
    }
}