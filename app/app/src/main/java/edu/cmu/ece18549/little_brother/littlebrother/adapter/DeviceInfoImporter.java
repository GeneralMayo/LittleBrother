package edu.cmu.ece18549.little_brother.littlebrother.adapter;

import android.util.Log;

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
        if (Device.devices.size() == 0) {
            IncrementalFakeDeviceFactory ifdf = new IncrementalFakeDeviceFactory();
            for (int i = 0; i < NUM_DEVICES; i++) {
                Device.devices.put(i, ifdf.getNewDevice());
            }
        }
        mDevices = new ArrayList<Device>(Device.devices.values());
        for(int i = 0; i < mDevices.size(); i++){
            Log.i(TAG, mDevices.get(i).toString());
        }
    }

    @Override
    public void exportDataAsDevices(List<Device> devices, HashMap<Device, List<String>> deviceDetails) {
        if (devices == null) {
            new RuntimeException("DataOnDeviceImporter: devices cannot be null.");
        }

        devices.clear();

        for (Device d : mDevices) {
            devices.add(d);
            List<String> deviceDetail = new ArrayList<String>();
            String position = "";
            String latitude = String.format("%.4f", Math.abs(d.getLatitude()));
            String longitude = String.format("%.4f", Math.abs(d.getLongitude()));
            position += latitude + "° ";
            if (d.getLatitude() > 0) {
                position += "N ";
            } else {
                position += "S ";
            }
            position += longitude + "° ";
            if (d.getLongitude() > 0) {
                position += "E";
            } else {
                position += "W";
            }

            deviceDetail.add("Position: " + position);
            deviceDetails.put(d, deviceDetail);
        }

    }

    @Override
    public void exportDataAsStrings(List<String> devices, HashMap<String, List<String>> deviceDetails) {
        if (devices == null || deviceDetails == null) {
            new RuntimeException("DataOnDeviceImporter: devices or deviceDetails cannot be null.");
        }

        devices.clear();
        deviceDetails.clear();

        for (Device d : mDevices) {
            String deviceName = d.toString();
            devices.add(deviceName);
            List<String> deviceDetail = new ArrayList<String>();
            String position = "";
            String latitude = String.format("%.4f", Math.abs(d.getLatitude()));
            String longitude = String.format("%.4f", Math.abs(d.getLongitude()));
            position += latitude + "° ";
            if (d.getLatitude() > 0) {
                position += "N ";
            } else {
                position += "S ";
            }
            position += longitude + "° ";
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