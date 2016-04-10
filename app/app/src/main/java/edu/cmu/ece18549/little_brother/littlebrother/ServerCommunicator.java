package edu.cmu.ece18549.little_brother.littlebrother;
import android.util.Log;

import com.loopj.android.http.*;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Ramsey on 3/25/2016.
 */
public class ServerCommunicator {
    public final static String TAG = "SERVICE_COMMUNICATOR";
    public final static String BASE_URL = "http://ec2-52-90-105-31.compute-1.amazonaws.com/little_brother/";
    public final static String ADD_LOG_URL = "add_log";
    public final static String ADD_DEVICE_URL = "add_device";
    public final static String ADD_SENSOR_URL = "add_sensor";
    public final static String REQUEST_ID_URL = "request_id";

    static SyncHttpClient client = new SyncHttpClient();
    static JsonHttpResponseHandler post_handler = new JsonHttpResponseHandler() {

        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject responseBody) {
            Log.i(TAG,"Request successful with status code " + statusCode);
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, String message, Throwable error){
            Log.e(TAG, "Request failed with status code " + statusCode);
            throw new RuntimeException(new ServerCommunicationException(error.getMessage()));
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject errorResponse){
            Log.e(TAG, "Request failed with status code " + statusCode);
            throw new RuntimeException(new ServerCommunicationException(error.getMessage()));
        }
    };

    private ServerCommunicator() {

    };

    public static void uploadLog(DeviceLog log) throws ServerCommunicationException {
       // String url = BASE_URL + ADD_LOG_URL;
        String url = "http://ec2-52-90-105-31.compute-1.amazonaws.com/little_brother/add_log";
        RequestParams params = new RequestParams();
        params.add("custom_id",log.getId()+"");
        params.add("time",convertDate(log.getDate()));
        params.add("value",log.getValue()+"");
        params.add("time_app",convertDate(log.getTimeRecieved()));
        params.add("device",log.getDevice().getId()+"");
        params.add("sensor_id",log.getSensor().getId()+"");
        try {
            Log.i(TAG,"Initiating log upload with parameters " + params.toString());
            client.post(url, params, post_handler);
        } catch (RuntimeException e) {
            if (e.getCause() instanceof ServerCommunicationException) {
                throw (ServerCommunicationException) e.getCause();
            } else {
                Log.e(TAG,e.getMessage());
            }
        }

    }

    public static int requestUniqueId() throws ServerCommunicationException {
        final ID id = new ID();

        JsonHttpResponseHandler get_handler = new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.i(TAG,"Request successful with statuscode " + statusCode);
                try {
                    id.setId(response.getInt("id"));
                } catch (JSONException e) {
                    throw new RuntimeException(new ServerCommunicationException(e.getMessage()));
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject errorResponse){
                Log.e(TAG, "Request failed with statuscode " + statusCode);
                throw new RuntimeException(new ServerCommunicationException(error.getMessage()));
            }
        };
        String url = BASE_URL + REQUEST_ID_URL;
        try {
            Log.i(TAG,"Requesting id");
            client.get(url, get_handler);
        } catch (RuntimeException e) {
            if (e.getCause() instanceof ServerCommunicationException) {
                throw (ServerCommunicationException) e.getCause();
            } else {
                Log.e(TAG,e.getMessage());
            }
        }
        return id.getId();
    }

    public static void registerDevice(Device device) throws ServerCommunicationException {
        String url = BASE_URL + ADD_DEVICE_URL;
        RequestParams params = new RequestParams();
        params.add("id",device.getId()+"");
        params.add("name",device.getName());
        params.add("latitude",device.getLatitude()+"");
        params.add("longitude",device.getLongitude()+"");
        try {
            Log.i(TAG,"Registering new device with parameters " + params.toString());
            client.post(url, params, post_handler);
        } catch (RuntimeException e) {
            if (e.getCause() instanceof ServerCommunicationException) {
                throw (ServerCommunicationException) e.getCause();
            } else {
                Log.e(TAG,e.getMessage());
            }
        }

    }

    private static String convertDate(Date date) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        return format.format(date);
    }


    private static class ID {
        int id;

        public void setId(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }
}