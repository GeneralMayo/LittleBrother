package edu.cmu.ece18549.little_brother.littlebrother.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import edu.cmu.ece18549.little_brother.littlebrother.R;
import edu.cmu.ece18549.little_brother.littlebrother.data_component.Sensor;

/**
 * Created by alexmaeda on 4/29/16.
 */
public class SensorRegisterAdapter extends RecyclerView.Adapter<SensorRegisterAdapter.ViewHolder>{

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView nameTextView;
        public Button messageButton;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            nameTextView = (TextView) itemView.findViewById(R.id.contact_name);
            messageButton = (Button) itemView.findViewById(R.id.message_name);
        }
    }

    private List<Sensor> mSensors;

    // Pass in the contact array into the constructor
    public SensorRegisterAdapter(List<Sensor> contacts) {
        mSensors = contacts;
    }

    @Override
    public SensorRegisterAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.sensor_item, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(SensorRegisterAdapter.ViewHolder viewHolder, int position) {
        // Get the data model based on position
        Sensor sensor = mSensors.get(position);

        // Set item views based on the data model
        TextView textView = viewHolder.nameTextView;
        textView.setText(sensor.getName());

        Button button = viewHolder.messageButton;
    }

    // Return the total count of items
    @Override
    public int getItemCount() {
        return mSensors.size();
    }
}
