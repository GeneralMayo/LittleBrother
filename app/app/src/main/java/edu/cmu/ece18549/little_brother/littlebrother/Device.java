package edu.cmu.ece18549.little_brother.littlebrother;

/**
 * Created by Ramsey on 3/20/2016.
 */
public class Device {
    private final int mId;
    public Device(int id) {
        mId = id;
    }

    public String getName() {
        return mId+"";
    }

    public String getId() {
        return mId+"";
    }
}
