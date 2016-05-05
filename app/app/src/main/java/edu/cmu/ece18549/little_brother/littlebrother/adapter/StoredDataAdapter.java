package edu.cmu.ece18549.little_brother.littlebrother.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import edu.cmu.ece18549.little_brother.littlebrother.R;
import edu.cmu.ece18549.little_brother.littlebrother.activity.DeviceRegisterActivity;
import edu.cmu.ece18549.little_brother.littlebrother.activity.DevicesAroundActivity;
import edu.cmu.ece18549.little_brother.littlebrother.data_component.Device;
import edu.cmu.ece18549.little_brother.littlebrother.data_component.DeviceLog;

/**
 * Created by alexmaeda on 5/5/16.
 */
public class StoredDataAdapter extends RecyclerView.Adapter<StoredDataAdapter.ViewHolder> {
    private static final int LOG_VIEW = 0;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView logHeaders;
        public TextView logDetails;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);
            logHeaders = (TextView) itemView.findViewById(R.id.logHeader);
            logDetails = (TextView) itemView.findViewById(R.id.logDetails);
        }
    }

    private List<DeviceLog> mLogs;

    public StoredDataAdapter(List<DeviceLog> logs) {
        mLogs = logs;
    }

    @Override
    public StoredDataAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View view = inflater.inflate(R.layout.list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(StoredDataAdapter.ViewHolder viewHolder, int position) {
        DeviceLog log = mLogs.get(position);
        viewHolder.logHeaders.setText(log.getId());
        viewHolder.logDetails.setText(""+log.getValue());

    }

    // Return the total count of items
    @Override
    public int getItemCount() {
        return mLogs.size();
    }

    @Override
    public int getItemViewType(int position) {
        return LOG_VIEW;
    }
}
