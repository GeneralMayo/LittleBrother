package edu.cmu.ece18549.little_brother.littlebrother.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.cmu.ece18549.little_brother.littlebrother.R;
import edu.cmu.ece18549.little_brother.littlebrother.activity.DeviceRegisterActivity;
import edu.cmu.ece18549.little_brother.littlebrother.activity.DevicesAroundActivity;
import edu.cmu.ece18549.little_brother.littlebrother.data_component.Device;

/**
 * Created by alexmaeda on 4/28/16.
 */
public class DeviceInfoAdapter extends BaseExpandableListAdapter {
    public static final String TAG = "DeviceInfoAdapter";
    public static final String DEVICE_TAG = "Device";

    private Context mContext;
    //List of Device names
    private List<Device> mDevices;
    private HashMap<Device, List<String>> mDeviceDetails;

    public DeviceInfoAdapter(Context context, DataImporter di) {
        this.mContext = context;
        this.mDevices = new ArrayList<>();
        this.mDeviceDetails = new HashMap<>();
        di.importData();
        di.exportDataAsDevices(mDevices, mDeviceDetails);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mDeviceDetails.get(mDevices.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final String childText = (String) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_item, null);
        }

        TextView txtListChild = (TextView) convertView.findViewById(R.id.device_details);

        txtListChild.setText(childText);
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.mDeviceDetails.get(this.mDevices.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.mDevices.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this.mDevices.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        Device d = (Device) getGroup(groupPosition);
        String headerTitle = d.toString();

        if (!d.is_registered()) {
            LayoutInflater infalInflater = (LayoutInflater) this.mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_group_unregistered, null);
            TextView listHeaderLabel = (TextView) convertView
                    .findViewById(R.id.unregisteredHeaderLabel);
            Button registerButton = (Button) convertView
                    .findViewById(R.id.register_button);
            registerButton.setOnClickListener(new RegisterButtonListener(d));

            listHeaderLabel.setTypeface(null, Typeface.BOLD);
            listHeaderLabel.setText(headerTitle);
            return convertView;
        }
        LayoutInflater infalInflater = (LayoutInflater) this.mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = infalInflater.inflate(R.layout.list_group, null);

        TextView listHeaderLabel = (TextView) convertView.findViewById(R.id.listHeaderLabel);
        listHeaderLabel.setTypeface(null, Typeface.BOLD);
        listHeaderLabel.setText(headerTitle);

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    private class RegisterButtonListener implements View.OnClickListener {
        Device device;

        public RegisterButtonListener(Device d){
            device = d;
        }

        @Override
        public void onClick(View v) {


            int deviceId = -1;
            Iterator it = Device.devices.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                if(pair.getValue() == device){
                    deviceId = (int)pair.getKey();
                    break;
                }
            }
//            // if the device does not exist, it is no longer in range of the phone.
//            // Restart the activity with error indicating so.
//            if (!it.hasNext()) {
//                Intent intent = new Intent(context, DevicesAroundActivity.class);
//                context.startActivity(intent);
//            }
            Context context = v.getContext();
            Intent intent = new Intent(context, DeviceRegisterActivity.class);
            intent.putExtra(DEVICE_TAG, deviceId);
            context.startActivity(intent);
        }
    }
}
