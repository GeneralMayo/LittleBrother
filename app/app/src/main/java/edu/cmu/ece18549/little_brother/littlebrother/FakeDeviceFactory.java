package edu.cmu.ece18549.little_brother.littlebrother;
import java.util.Random;

/**
 * Created by Ramsey on 3/22/2016.
 */
public abstract class FakeDeviceFactory {
    private final Random mRandom;
    private final long mSeed;

    public FakeDeviceFactory(long randomSeed) {
        mRandom = new Random(randomSeed);
        mSeed = randomSeed;
    }

    public FakeDeviceFactory() {
        mSeed = 0;
        mRandom = new Random();
    }

    public Device makeDevice() {
        Device device = getNewDevice();
        getNewSensors(device);
        getNewLogs(device);
        return device;
    }

    protected int getRandomInt() {
        return mRandom.nextInt();
    }
    protected int getRandomInt(int range) { return mRandom.nextInt(range); }
    protected long getRandomLong() {
        return mRandom.nextLong();
    }

    protected abstract Device getNewDevice();
    protected abstract void getNewSensors(Device device);
    protected abstract void getNewLogs(Device device);

}
