package edu.cmu.ece18549.little_brother.littlebrother;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Ramsey on 3/21/2016.
 */
public class Sensor {

    private final int mId;
    private String mName;
    private final Device mDevice;

    public Sensor(int id, String name, Device device) {
        mId = id;
        mName = name;
        mDevice = device;
    }

    public int getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public Device getDevice() {
        return mDevice;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Sensor)) {
            return false;
        }
        Sensor o1 = (Sensor) object;
        return o1.mId == mId && o1.mDevice.equals(mDevice);
    }

    @Override
    public int hashCode() {
        return mId;
    }

    @Override
    public String toString() {
        return mName + " " + mId;
    }
}
