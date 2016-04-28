package edu.cmu.ece18549.little_brother.littlebrother.adapter;

import edu.cmu.ece18549.little_brother.littlebrother.data_component.Device;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cmu.ece18549.little_brother.littlebrother.util.Pair;
import edu.cmu.ece18549.little_brother.littlebrother.data_component.DeviceLog;

/**
 * Created by alexmaeda on 4/24/16.
 */
public interface DataImporter {

    public List<DeviceLog> getAllLogs();

    public void importData();

    public void exportData(List<String> devices, HashMap<String, List<String>> deviceDetails);

}
