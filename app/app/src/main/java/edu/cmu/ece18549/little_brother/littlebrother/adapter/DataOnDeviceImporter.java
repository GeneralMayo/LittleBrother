package edu.cmu.ece18549.little_brother.littlebrother.adapter;

import java.util.HashMap;
import java.util.List;

import edu.cmu.ece18549.little_brother.littlebrother.util.Pair;
import edu.cmu.ece18549.little_brother.littlebrother.data_component.Device;
import edu.cmu.ece18549.little_brother.littlebrother.data_component.DeviceLog;
import edu.cmu.ece18549.little_brother.littlebrother.test.SimpleFakeDeviceFactory;


/**
 * Created by alexmaeda on 4/24/16.
 */
public class DataOnDeviceImporter implements DataImporter {

    List<Device> mDevices;


    @Override
    public List<DeviceLog> getAllLogs() {
        return null;
    }

    @Override
    public void importData() {
//        SimpleFakeDeviceFactory sfd = new SimpleFakeDeviceFactory(0xdeadbeef);
//        Device device1 = sfd.getNewDevice();



    }

    @Override
    public List<Pair<String, List<String>>> exportData() {
        return null;
    }

}
