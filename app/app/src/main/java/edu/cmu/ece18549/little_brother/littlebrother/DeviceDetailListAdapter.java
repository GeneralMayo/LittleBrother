package edu.cmu.ece18549.little_brother.littlebrother;

/**
 * Created by alexmaeda on 4/23/16.
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class DeviceDetailListAdapter extends BaseExpandableListAdapter {

    private Context mContext;
    //List of Device names
    private List<String> mDevices;
    private HashMap<String, List<String>> mDeviceDetails;

    public DeviceDetailListAdapter(Context context) {
        this.mContext = context;
        importData();

    }

    //TODO: Add import data to be attached to blue tooth and devices
    private void importData() {
        mDevices = new ArrayList<String>();
        mDeviceDetails = new HashMap<String, List<String>>();

        String newDevice = "Device 1";
        ArrayList<String> deviceDetails = new ArrayList<String>();
        deviceDetails.add("Name: Device 1");
        deviceDetails.add("Active from: 1/10/16 12:00:00 EST");
        deviceDetails.add("Battery: 100%");
        mDevices.add(newDevice);
        mDeviceDetails.put(newDevice, deviceDetails);

        newDevice = "Device 2";
        deviceDetails = new ArrayList<String>();
        deviceDetails.add("Name: Device 2");
        deviceDetails.add("Active from: 1/10/16 12:00:00 EST");
        deviceDetails.add("Battery: 100%");
        mDevices.add(newDevice);
        mDeviceDetails.put(newDevice, deviceDetails);

        newDevice = "Device 3";
        deviceDetails = new ArrayList<String>();
        deviceDetails.add("Name: Device 3");
        deviceDetails.add("Active from: 1/10/16 12:00:00 EST");
        deviceDetails.add("Battery: 100%");
        mDevices.add(newDevice);
        mDeviceDetails.put(newDevice, deviceDetails);
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

        TextView txtListChild = (TextView) convertView.findViewById(R.id.lblListItem);

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
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_group, null);
        }

        TextView lblListHeader = (TextView) convertView
                .findViewById(R.id.listHeaderLabel);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);

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
}