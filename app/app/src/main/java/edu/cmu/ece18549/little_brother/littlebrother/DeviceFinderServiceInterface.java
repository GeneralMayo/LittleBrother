package edu.cmu.ece18549.little_brother.littlebrother;

import java.util.Collection;

/**
 * Created by Ramsey on 3/20/2016.
 */
public interface DeviceFinderServiceInterface {
    Collection<Device> getDevices();
    void registerListener(Observer o);
}
