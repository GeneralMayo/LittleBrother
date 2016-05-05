package edu.cmu.ece18549.little_brother.littlebrother.adapter;

import android.content.Context;
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

import java.util.List;

import edu.cmu.ece18549.little_brother.littlebrother.R;
import edu.cmu.ece18549.little_brother.littlebrother.data_component.Device;
import edu.cmu.ece18549.little_brother.littlebrother.data_component.Sensor;

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
        public Button removeButton;
        public Button addButton;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);
            editTextView = (EditText) itemView.findViewById(R.id.sensor_name_edit);
            removeButton = (Button) itemView.findViewById(R.id.delete_sensor_button);
            addButton = (Button) itemView.findViewById(R.id.plus_one_button);
        }
    }

    private List<Sensor> mSensors;

    public SensorRegisterAdapter(List<Sensor> sensors) {
        mSensors = sensors;
    }

    @Override
    public SensorRegisterAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == SENSOR_VIEW_ID) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);

            // Inflate the custom layout
            View view = inflater.inflate(R.layout.sensor_item, parent, false);

            // Return a new holder instance
            ViewHolder viewHolder = new ViewHolder(view);
            return viewHolder;
        } else {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);

            // Inflate the custom layout
            View view = inflater.inflate(R.layout.plus_one_button, parent, false);

            // Return a new holder instance
            ViewHolder viewHolder = new ViewHolder(view);
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
        if (position == mSensors.size()) {
            Button button = viewHolder.addButton;
            button.setOnClickListener(new SensorAddListener(this, mSensors));
        } else if (position == 0) {
            EditText editText = viewHolder.editTextView;
            editText.addTextChangedListener(new SensorTextWatcher(mSensors.get(position)));
        } else if (position < mSensors.size()) {
            Button button = viewHolder.removeButton;
            EditText editText = viewHolder.editTextView;
            button.setOnClickListener(new SensorDeleteListener(this, mSensors, mSensors.get(position)));
            editText.addTextChangedListener(new SensorTextWatcher(mSensors.get(position)));

        }
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

    private class SensorDeleteListener implements OnClickListener{
        SensorRegisterAdapter adp;
        List<Sensor> sensors;
        Sensor sensor;
        EditText view;

        public SensorDeleteListener(SensorRegisterAdapter adp,
                                    List<Sensor> sensors, Sensor sensor) {
            super();
            this.sensors = sensors;
            this.sensor = sensor;
            this.adp = adp;
        }

        @Override
        public void onClick(View view) {
            for (int i = 0; i < sensors.size(); i++) {
                if(sensor == sensors.get(i)){
                    sensors.remove(i);
                    adp.notifyItemRemoved(i);
                    return;
                }
            }
        }
    }

    private class SensorAddListener implements OnClickListener{
        SensorRegisterAdapter adp;
        List<Sensor> sensors;

        public SensorAddListener(SensorRegisterAdapter adp, List<Sensor> sensors) {
            super();
            this.sensors = sensors;
            this.adp = adp;
        }

        @Override
        public void onClick(View view) {
            sensors.add(new Sensor(0, "", null));
            adp.notifyItemInserted(sensors.size() - 1);
        }
    }

    private class SensorTextWatcher implements TextWatcher {
        Sensor sensor;
        public SensorTextWatcher(Sensor s) {
            this.sensor = s;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            sensor.setName(s.toString());
        }
    }
}
