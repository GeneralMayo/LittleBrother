package edu.cmu.ece18549.little_brother.littlebrother.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.util.List;

import edu.cmu.ece18549.little_brother.littlebrother.R;

/**
 * Created by alexmaeda on 4/29/16.
 */
public class SensorRegisterAdapter extends RecyclerView.Adapter<SensorRegisterAdapter.ViewHolder>{
    private static final int SENSOR_VIEW_ID = 0;
    private static final int PLUS_ONE_VIEW_ID = 1;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public EditText editTextView;
        public Button messageButton;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            editTextView = (EditText) itemView.findViewById(R.id.contact_name);
            messageButton = (Button) itemView.findViewById(R.id.message_name);
        }
    }

    private List<String> mSensors;

    // Pass in the contact array into the constructor
    public SensorRegisterAdapter(List<String> contacts) {
        mSensors = contacts;
    }

    @Override
    public SensorRegisterAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == SENSOR_VIEW_ID) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);

            // Inflate the custom layout
            View contactView = inflater.inflate(R.layout.sensor_item, parent, false);

            // Return a new holder instance
            ViewHolder viewHolder = new ViewHolder(contactView);
            return viewHolder;
        } else {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);

            // Inflate the custom layout
            View contactView = inflater.inflate(R.layout.plus_one_button, parent, false);

            // Return a new holder instance
            ViewHolder viewHolder = new ViewHolder(contactView);
            return viewHolder;
        }
    }

    @Override
    public void onBindViewHolder(SensorRegisterAdapter.ViewHolder viewHolder, int position) {
        // Get the data model based on position

        //Sensor sensor = mSensors.get(position);

        // Set item views based on the data model
        //EditText editView = viewHolder.editTextView;
        //editView.setText(sensor.getName());

        //Button button = viewHolder.messageButton;
    }

    // Return the total count of items
    @Override
    public int getItemCount() {
        return mSensors.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position < mSensors.size()){
            return SENSOR_VIEW_ID;
        }
        return PLUS_ONE_VIEW_ID;
    }
}
