package edu.cmu.ece18549.little_brother.littlebrother.adapter;
import android.util.Log;
import android.widget.TextView;

import com.loopj.android.http.*;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import cz.msebera.android.httpclient.Header;
import edu.cmu.ece18549.little_brother.littlebrother.data_component.Device;
import edu.cmu.ece18549.little_brother.littlebrother.data_component.DeviceLog;
import edu.cmu.ece18549.little_brother.littlebrother.data_component.Sensor;

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
    public final static String DEFAULT_USER = "1";

    static SyncHttpClient client = new SyncHttpClient();
    static JsonHttpResponseHandler post_handler = new JsonHttpResponseHandler() {

        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject responseBody) {
            Log.i(TAG,"Request successful with status code " + statusCode);
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, String message, Throwable error){
            Log.e(TAG, "Request failed with status code " + statusCode);
            Log.e(TAG, "ERROR: " + message);
            throw new RuntimeException(new ServerCommunicationException(error.getMessage()));
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject errorResponse){
            Log.e(TAG, "Request failed with status code " + statusCode);
            Log.e(TAG, "ERROR: " + errorResponse.toString());
            throw new RuntimeException(new ServerCommunicationException(error.getMessage()));
        }
    };

    private ServerCommunicator() {};

    public static void uploadLog(DeviceLog log) throws ServerCommunicationException {
       // String url = BASE_URL + ADD_LOG_URL;
        String url = "http://ec2-52-90-105-31.compute-1.amazonaws.com/little_brother/add_log";
        RequestParams params = new RequestParams();
        params.add("custom_id",log.getId() + "");
        params.add("time",convertDate(log.getDate()));
        params.add("value",log.getValue() + "");
        params.add("time_app",convertDate(log.getTimeRecieved()));
        params.add("device",log.getDevice().getId() + "");
        params.add("sensor_id",log.getSensor().getId() + "");
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

    public static void registerSensor(Sensor sensor) throws ServerCommunicationException {
        String url = BASE_URL + ADD_SENSOR_URL;
        RequestParams params = new RequestParams();
        params.add("custom_id", sensor.getId() + "");
        params.add("name", sensor.getName());
        params.add("device", sensor.getDevice().getId() + "");
        try {
            Log.i(TAG,"Registering new sensor with parameters " + params.toString());
            Log.i(TAG, "Registering to url: " + url + "?" + params.toString());
            client.post(url, params, post_handler);
        } catch (RuntimeException e) {
            if (e.getCause() instanceof ServerCommunicationException) {
                throw (ServerCommunicationException) e.getCause();
            } else {
                Log.e(TAG,e.getMessage());
            }
        }
    }

    public static void registerDevice(Device device, String newDeviceName) throws ServerCommunicationException {
        String url = BASE_URL + ADD_DEVICE_URL;
        RequestParams params = new RequestParams();
        params.add("name",newDeviceName);
        params.add("latitude",device.getLatitude()+"");
        params.add("longitude",device.getLongitude()+"");
        params.add("admin", DEFAULT_USER);
        try {
            Log.i(TAG,"Registering new device with parameters " + params.toString());
            ID responseId = new ID();
            client.post(url, params, new JsonResponseRetrieverHandler(responseId));
            device.registerDevice(responseId.getId(), newDeviceName);
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

    private static class JsonResponseRetrieverHandler extends JsonHttpResponseHandler {
        private ID responseId;

        public JsonResponseRetrieverHandler(ID responseId) {
            this.responseId = responseId;
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject responseBody) {
            Log.i(TAG,"Request successful with status code " + statusCode);
            try {
                responseId.setId(responseBody.getInt("id"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, String message, Throwable error){
            Log.e(TAG, "Request failed with status code " + statusCode);
            Log.e(TAG, "ERROR: " + message);
            throw new RuntimeException(new ServerCommunicationException(error.getMessage()));
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject errorResponse){
            Log.e(TAG, "Request failed with status code " + statusCode);
            Log.e(TAG, "ERROR: " + errorResponse.toString());
            throw new RuntimeException(new ServerCommunicationException(error.getMessage()));
        }
    };


}
