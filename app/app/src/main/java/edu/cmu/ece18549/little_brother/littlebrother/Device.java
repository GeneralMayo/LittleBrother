package edu.cmu.ece18549.little_brother.littlebrother;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Ramsey on 3/20/2016.
 */
public class Device {
    private int mId;
    private String mName;
    private double mLatitude;
    private double mLongitude;
    private Map<Sensor,List<DeviceLog>> mComponents;

    public Device(int id, String name, double latitude, double longitude) {

        mId = id;
        mName = name;
        mLatitude = latitude;
        mLongitude = longitude;
        mComponents = new HashMap<Sensor,List<DeviceLog>>();
    }

    public Device() {

    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(float longitude) {
        mLongitude = longitude;
    }

    public void setLatitude(float latitude) {
        mLatitude = latitude;
    }

    public synchronized void addLog(DeviceLog log) throws DeviceException {
        Sensor sensor = log.getSensor();
        if (!mComponents.containsKey(sensor)) {
            throw new DeviceException("Sensor " + sensor +" not found");
        } else {
            List<DeviceLog> logs = mComponents.get(sensor);
            logs.add(log);
        }
    }

    public synchronized void removeLog(DeviceLog log, Sensor sensor) throws DeviceException {
        if (!mComponents.containsKey(sensor)) {
            throw new DeviceException("Sensor " + sensor +" not found");
        } else {
            List<DeviceLog> logs = mComponents.get(sensor);
            if (!logs.contains(log)) {
                throw new DeviceException("Log " + log + " not found");
            } else {
                logs.remove(log);
            }
        }
    }

    public synchronized ArrayList<DeviceLog> getLogs(Sensor sensor) throws DeviceException {
        if (!mComponents.containsKey(sensor)) {
            throw new DeviceException("Sensor " + sensor +" not found");
        } else {
            return new ArrayList<DeviceLog>(mComponents.get(sensor));
        }
    }

    public synchronized ArrayList<DeviceLog> getLogs() {
        ArrayList<DeviceLog> newLogs = new ArrayList<DeviceLog>();
        Collection<List<DeviceLog>> allLogs = mComponents.values();
        for(List<DeviceLog> list : allLogs) {
            for(DeviceLog log : list) {
                newLogs.add(log);
            }
        }

        return newLogs;
    }

    public synchronized ArrayList<Sensor> getSensors() {
        return new ArrayList<Sensor>(mComponents.keySet());
    }

    public synchronized void addSensor(Sensor sensor) throws DeviceException {
        if (mComponents.containsKey(sensor)) {
            throw new DeviceException("Sensor " + sensor + " already on device");
        }

        List<DeviceLog> newLogs = new LinkedList<DeviceLog>();
        mComponents.put(sensor,newLogs);
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Device)) {
            return false;
        }
        Device o1 = (Device) object;
        return o1.mId == mId;
    }

    @Override
    public int hashCode() {
        return mId;
    }

    @Override
    public String toString() {
        return "" + mId;
    }

}
