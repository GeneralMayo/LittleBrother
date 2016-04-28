package edu.cmu.ece18549.little_brother.littlebrother.adapter;

import edu.cmu.ece18549.little_brother.littlebrother.data_component.Device;

import java.util.HashMap;
import java.util.List;

import edu.cmu.ece18549.little_brother.littlebrother.data_component.DeviceLog;

/**
 * Created by alexmaeda on 4/24/16.
 */
public interface DataImporter {

    public List<DeviceLog> getAllLogs();

    public void importData();

    public void exportDataAsDevices(List<Device> devices, HashMap<Device, List<String>> deviceDetails);

    public void exportDataAsStrings(List<String> devices, HashMap<String, List<String>> deviceDetails);

}
