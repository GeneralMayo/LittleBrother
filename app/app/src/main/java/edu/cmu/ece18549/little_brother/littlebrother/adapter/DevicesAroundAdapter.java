package edu.cmu.ece18549.little_brother.littlebrother.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.cmu.ece18549.little_brother.littlebrother.R;
import edu.cmu.ece18549.little_brother.littlebrother.activity.DeviceRegisterActivity;
import edu.cmu.ece18549.little_brother.littlebrother.activity.DevicesAroundActivity;
import edu.cmu.ece18549.little_brother.littlebrother.data_component.Device;
import edu.cmu.ece18549.little_brother.littlebrother.service.DeviceFinderService;

/**
 * Created by alexmaeda on 4/29/16.
 */
public class DevicesAroundAdapter extends RecyclerView.Adapter<DevicesAroundAdapter.ViewHolder>{
    public static final String DEVICE_ID_EXTRA = "device.id.extra";
    private static final int UNREGISTERED = 0;
    private static final int REGISTERED = 1;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public Button registerButton;
        public TextView unregisteredHeaderLabel;
        public TextView unregisteredDetails;

        public TextView registeredHeaderLabel;
        public TextView registeredDetails;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);
            registerButton = (Button) itemView.findViewById(R.id.register_button);
            unregisteredHeaderLabel = (TextView) itemView.findViewById(R.id.unregisteredHeaderLabel);
            unregisteredDetails = (TextView) itemView.findViewById(R.id.unregisteredDetails);
            registeredHeaderLabel = (TextView) itemView.findViewById(R.id.registeredHeaderLabel);
            registeredDetails = (TextView) itemView.findViewById(R.id.registeredDetails);
        }
    }

    private List<Device> mDevices;
    private DevicesAroundActivity mActivity;

    public DevicesAroundAdapter(List<Device> devices, DevicesAroundActivity activity) {
        mDevices = devices;
        mActivity = activity;
    }

    @Override
    public DevicesAroundAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == UNREGISTERED) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);

            // Inflate the custom layout
            View view = inflater.inflate(R.layout.list_group_unregistered, parent, false);

            // Return a new holder instance
            ViewHolder viewHolder = new ViewHolder(view);
            return viewHolder;
        } else {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);

            // Inflate the custom layout
            View view = inflater.inflate(R.layout.list_group, parent, false);

            // Return a new holder instance
            ViewHolder viewHolder = new ViewHolder(view);
            return viewHolder;
        }
    }

    private String getDeviceDetails(Device d) {
        String position = "";
        String latitude = String.format("%.4f", Math.abs(d.getLatitude()));
        String longitude = String.format("%.4f", Math.abs(d.getLongitude()));
        position += latitude + "° ";
        if (d.getLatitude() > 0) {
            position += "N ";
        } else {
            position += "S ";
        }
        position += longitude + "° ";
        if (d.getLongitude() > 0) {
            position += "E";
        } else {
            position += "W";
        }

        return "id: " + d.getId() + " | Position: " + position;
    }

    @Override
    public void onBindViewHolder(DevicesAroundAdapter.ViewHolder viewHolder, int position) {
        Device device = mDevices.get(position);

        if (device.getId() < 0){
            //view must be unregistered view
            viewHolder.registerButton.setOnClickListener(new RegisterButtonListener(device, position, mActivity));
            viewHolder.unregisteredHeaderLabel.setText(device.getName());
            viewHolder.unregisteredDetails.setText(getDeviceDetails(device));

        } else {
            //view must be registered device, since it is non negative
            viewHolder.registeredHeaderLabel.setText(device.getName());
            viewHolder.registeredDetails.setText(getDeviceDetails(device));
        }
    }

    private class RegisterButtonListener implements View.OnClickListener {
        Device device;
        int device_id;
        DevicesAroundActivity activity;

        public RegisterButtonListener(Device d, int id, DevicesAroundActivity activity){
            device = d;
            device_id = id;
            this.activity = activity;
        }

        @Override
        public void onClick(View v) {
            Context context = v.getContext();
            Intent intent = new Intent(context, DeviceRegisterActivity.class);
            intent.putExtra(DEVICE_ID_EXTRA, device_id);
            activity.startActivityForResult(intent, DevicesAroundActivity.REGISTER_REQUEST_CODE);
        }
    }

    // Return the total count of items
    @Override
    public int getItemCount() {
        return mDevices.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (mDevices.get(position).getId() < 0) {
            return UNREGISTERED;
        }
        return REGISTERED;
    }
}