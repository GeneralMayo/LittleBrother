package edu.cmu.ece18549.little_brother.littlebrother.service;

import java.util.Collection;

import edu.cmu.ece18549.little_brother.littlebrother.adapter.BluetoothScanner;
import edu.cmu.ece18549.little_brother.littlebrother.data_component.Device;
import edu.cmu.ece18549.little_brother.littlebrother.data_component.DeviceLog;

/**
 * Created by Ramsey on 3/20/2016.
 */
public interface DeviceFinderServiceInterface {
    Collection<Device> getDevices();
    Collection<DeviceLog> getLogs();
    BluetoothScanner getScanner();
    void registerListener(Observer o);
}
