package edu.cmu.ece18549.little_brother.littlebrother.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.cmu.ece18549.little_brother.littlebrother.data_component.DeviceLog;
import edu.cmu.ece18549.little_brother.littlebrother.util.Pair;

/**
 * Created by alexmaeda on 4/24/16.
 */
public class DeviceInfoImporter implements DataImporter {

    public List<DeviceLog> getAllLogs(){return null;}


    @Override
    public void importData() {
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
    public List<Pair<String, List<String>>> exportData() {
        return null;
    }

}
