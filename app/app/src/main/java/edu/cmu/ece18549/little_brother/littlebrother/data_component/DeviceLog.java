package edu.cmu.ece18549.little_brother.littlebrother.data_component;

import java.util.Date;

/**
 * Created by Ramsey on 3/21/2016.
 */
public class DeviceLog {

    private final int mId;
    private final Date mDate;
    private final double mValue;
    private final Date mTimeRecieved;
    private final Sensor mSensor;

    public DeviceLog(int id, Date date, double value, Date timeRecieved, Sensor sensor) {
        mId = id;
        mDate = date;
        mValue = value;
        mTimeRecieved = timeRecieved;
        mSensor = sensor;
    }

    public int getId() {
        return mId;
    }

    public Date getDate() {
        return mDate;
    }

    public double getValue() {
        return mValue;
    }

    public Date getTimeRecieved() {
        return mTimeRecieved;
    }

    public Sensor getSensor() {
        return mSensor;
    }

    public Device getDevice() {
        return mSensor.getDevice();
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof DeviceLog)) {
            return false;
        }
        DeviceLog o1 = (DeviceLog) object;
        return o1.mId == mId && o1.mSensor.equals(mSensor);
    }

    @Override
    public int hashCode() {
        return mId;
    }

    @Override
    public String toString() {
        return ""+mId;
    }
}
