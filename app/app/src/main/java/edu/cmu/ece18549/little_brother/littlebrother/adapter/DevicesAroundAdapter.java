package edu.cmu.ece18549.little_brother.littlebrother.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.Adapter.ExpandableRecyclerAdapter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.cmu.ece18549.little_brother.littlebrother.R;
import edu.cmu.ece18549.little_brother.littlebrother.activity.DeviceRegisterActivity;
import edu.cmu.ece18549.little_brother.littlebrother.activity.DevicesAroundActivity;
import edu.cmu.ece18549.little_brother.littlebrother.data_component.Device;

import com.bignerdranch.expandablerecyclerview.Model.ParentObject;
import com.bignerdranch.expandablerecyclerview.ViewHolder.ChildViewHolder;
import com.bignerdranch.expandablerecyclerview.ViewHolder.ParentViewHolder;

/**
 * Created by alexmaeda on 5/4/16.
 */
public class DevicesAroundAdapter
        extends ExpandableRecyclerAdapter<DevicesAroundAdapter.PVH,
                                          DevicesAroundAdapter.CVH> {

    public static class PVH extends ParentViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public Button registerButton;
        public TextView unregisteredHeaderLabel;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public PVH(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);
            registerButton = (Button) itemView.findViewById(R.id.register_button);
            unregisteredHeaderLabel = (TextView) itemView.findViewById(R.id.unregisteredHeaderLabel);
        }
    }

    public static class CVH extends ChildViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView deviceDetails;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public CVH(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);
            deviceDetails = (TextView) itemView.findViewById(R.id.device_details);
        }
    }

    public static class DeviceObject implements ParentObject {
        Device mDevice;
        List<Object> mDetails;

        public DeviceObject(Device d) {
            mDevice = d;
        }

        public Device getDevice() {
            return mDevice;
        }

        @Override
        public List<Object> getChildObjectList() {
            return mDetails;
        }

        @Override
        public void setChildObjectList(List<Object> list) {
            mDetails = list;
        }
    }

    public static List<ParentObject> createParentList(List<Device> devices) {
        List<ParentObject> po = new ArrayList<ParentObject>();
        for (Device d : devices) {
            po.add(new DevicesAroundAdapter.DeviceObject(d));
        }
        return po;
    }

    private List<Device> mDevices;
    private List<ParentObject> mDeviceObjectList;
    private LayoutInflater mInflater;
    public static final String DEVICE_TAG = "Device";

    public DevicesAroundAdapter(Context context, List<ParentObject> parentItemList) {
        super(context, parentItemList);
        mDeviceObjectList = parentItemList;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public PVH onCreateParentViewHolder(ViewGroup viewGroup) {
        View view = mInflater.inflate(R.layout.list_group_unregistered, viewGroup, false);
        return new PVH(view);
    }

    @Override
    public CVH onCreateChildViewHolder(ViewGroup viewGroup) {
        View view = mInflater.inflate(R.layout.list_item, viewGroup, false);
        return new CVH(view);
    }

    @Override
    public void onBindParentViewHolder(PVH pvh, int i, Object o) {
        DeviceObject deviceObject = (DeviceObject) o;
        pvh.unregisteredHeaderLabel.setText(deviceObject.getDevice().getName());
        pvh.registerButton.setOnClickListener(new RegisterButtonListener(deviceObject.getDevice()));
    }

    @Override
    public void onBindChildViewHolder(CVH cvh, int i, Object o) {

    }

    @Override
    public int getItemCount() {
        return mParentItemList.size();
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

            Context context = v.getContext();
            Intent intent = new Intent(context, DeviceRegisterActivity.class);
            intent.putExtra(DEVICE_TAG, deviceId);
            context.startActivity(intent);
        }
    }
}


