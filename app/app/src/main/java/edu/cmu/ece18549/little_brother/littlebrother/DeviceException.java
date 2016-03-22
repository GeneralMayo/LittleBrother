package edu.cmu.ece18549.little_brother.littlebrother;

/**
 * Created by Ramsey on 3/21/2016.
 */
public class DeviceException extends Throwable {
    private final String mMessage;
    public DeviceException(String message) {
        mMessage = message;
    }

    public String getMessage() {
        return mMessage;
    }
}
