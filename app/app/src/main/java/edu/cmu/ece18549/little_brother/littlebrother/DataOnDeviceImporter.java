package edu.cmu.ece18549.little_brother.littlebrother;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by alexmaeda on 4/24/16.
 */
public class DataOnDeviceImporter implements DataImporter {

    @Override
    public void importData(List<String> devices,
                           HashMap<String, List<String>> deviceDetails) {

        String newDevice = "Device 1 - 1/10/16 12:00:01 EST";
        ArrayList<String> deviceDetail = new ArrayList<String>();
        deviceDetail.add("Temperature: 10");
        deviceDetail.add("Humidity: 50");
        deviceDetail.add("Air Quality: 30%");
        devices.add(newDevice);
        deviceDetails.put(newDevice, deviceDetail);

        newDevice = "Device 2 - 2/11/16 12:00:01 EST";
        deviceDetail = new ArrayList<String>();
        deviceDetail.add("Temperature: 42");
        deviceDetail.add("Humidity: 12");
        deviceDetail.add("Air Quality: 86%");
        devices.add(newDevice);
        deviceDetails.put(newDevice, deviceDetail);

        newDevice = "Device 3 - 3/3/16 12:00:01 EST";
        deviceDetail = new ArrayList<String>();
        deviceDetail.add("Temperature: 76");
        deviceDetail.add("Humidity: 86");
        deviceDetail.add("Air Quality: 81%");
        devices.add(newDevice);
        deviceDetails.put(newDevice, deviceDetail);

        newDevice = "Device 2 - 2/11/16 12:00:01 EST";
        deviceDetail = new ArrayList<String>();
        deviceDetail.add("Temperature: 42");
        deviceDetail.add("Humidity: 12");
        deviceDetail.add("Air Quality: 86%");
        devices.add(newDevice);
        deviceDetails.put(newDevice, deviceDetail);

        newDevice = "Device 3 - 3/3/16 12:00:01 EST";
        deviceDetail = new ArrayList<String>();
        deviceDetail.add("Temperature: 76");
        deviceDetail.add("Humidity: 86");
        deviceDetail.add("Air Quality: 81%");
        devices.add(newDevice);
        deviceDetails.put(newDevice, deviceDetail);

        newDevice = "Device 2 - 2/11/16 12:00:01 EST";
        deviceDetail = new ArrayList<String>();
        deviceDetail.add("Temperature: 42");
        deviceDetail.add("Humidity: 12");
        deviceDetail.add("Air Quality: 86%");
        devices.add(newDevice);
        deviceDetails.put(newDevice, deviceDetail);

        newDevice = "Device 3 - 3/3/16 12:00:01 EST";
        deviceDetail = new ArrayList<String>();
        deviceDetail.add("Temperature: 76");
        deviceDetail.add("Humidity: 86");
        deviceDetail.add("Air Quality: 81%");
        devices.add(newDevice);
        deviceDetails.put(newDevice, deviceDetail);

        newDevice = "Device 2 - 2/11/16 12:00:01 EST";
        deviceDetail = new ArrayList<String>();
        deviceDetail.add("Temperature: 42");
        deviceDetail.add("Humidity: 12");
        deviceDetail.add("Air Quality: 86%");
        devices.add(newDevice);
        deviceDetails.put(newDevice, deviceDetail);

        newDevice = "Device 3 - 3/3/16 12:00:01 EST";
        deviceDetail = new ArrayList<String>();
        deviceDetail.add("Temperature: 76");
        deviceDetail.add("Humidity: 86");
        deviceDetail.add("Air Quality: 81%");
        devices.add(newDevice);
        deviceDetails.put(newDevice, deviceDetail);

        newDevice = "Device 2 - 2/11/16 12:00:01 EST";
        deviceDetail = new ArrayList<String>();
        deviceDetail.add("Temperature: 42");
        deviceDetail.add("Humidity: 12");
        deviceDetail.add("Air Quality: 86%");
        devices.add(newDevice);
        deviceDetails.put(newDevice, deviceDetail);

        newDevice = "Device 3 - 3/3/16 12:00:01 EST";
        deviceDetail = new ArrayList<String>();
        deviceDetail.add("Temperature: 76");
        deviceDetail.add("Humidity: 86");
        deviceDetail.add("Air Quality: 81%");
        devices.add(newDevice);
        deviceDetails.put(newDevice, deviceDetail);

        newDevice = "Device 2 - 2/11/16 12:00:01 EST";
        deviceDetail = new ArrayList<String>();
        deviceDetail.add("Temperature: 42");
        deviceDetail.add("Humidity: 12");
        deviceDetail.add("Air Quality: 86%");
        devices.add(newDevice);
        deviceDetails.put(newDevice, deviceDetail);

        newDevice = "Device 3 - 3/3/16 12:00:01 EST";
        deviceDetail = new ArrayList<String>();
        deviceDetail.add("Temperature: 76");
        deviceDetail.add("Humidity: 86");
        deviceDetail.add("Air Quality: 81%");
        devices.add(newDevice);
        deviceDetails.put(newDevice, deviceDetail);

        newDevice = "Device 2 - 2/11/16 12:00:01 EST";
        deviceDetail = new ArrayList<String>();
        deviceDetail.add("Temperature: 42");
        deviceDetail.add("Humidity: 12");
        deviceDetail.add("Air Quality: 86%");
        devices.add(newDevice);
        deviceDetails.put(newDevice, deviceDetail);

        newDevice = "Device 3 - 3/3/16 12:00:01 EST";
        deviceDetail = new ArrayList<String>();
        deviceDetail.add("Temperature: 76");
        deviceDetail.add("Humidity: 86");
        deviceDetail.add("Air Quality: 81%");
        devices.add(newDevice);
        deviceDetails.put(newDevice, deviceDetail);
    }

}
