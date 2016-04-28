package edu.cmu.ece18549.little_brother.littlebrother.adapter;

import java.util.HashMap;
import java.util.List;

import edu.cmu.ece18549.little_brother.littlebrother.util.Pair;
import edu.cmu.ece18549.little_brother.littlebrother.data_component.DeviceLog;

/**
 * Created by alexmaeda on 4/24/16.
 */
public interface DataImporter {

    public List<DeviceLog> getAllLogs();

    public void importData();

    public List<Pair<String, List<String>>> exportData();

}
