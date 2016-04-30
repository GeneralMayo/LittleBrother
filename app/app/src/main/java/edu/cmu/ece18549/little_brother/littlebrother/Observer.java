package edu.cmu.ece18549.little_brother.littlebrother;

/**
 * Created by Ramsey on 4/10/2016.
 */
public interface Observer {
    void notifyChange(Notification n, Object o);
}
