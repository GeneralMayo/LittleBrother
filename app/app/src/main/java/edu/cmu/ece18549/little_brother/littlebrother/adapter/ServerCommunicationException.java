package edu.cmu.ece18549.little_brother.littlebrother.adapter;

/**
 * Created by Ramsey on 3/25/2016.
 */
public class ServerCommunicationException extends Throwable {
    private final String mMessage;
    public ServerCommunicationException(String message) {
        mMessage = message;
    }

    public String getMessage() {
        return mMessage;
    }
}
