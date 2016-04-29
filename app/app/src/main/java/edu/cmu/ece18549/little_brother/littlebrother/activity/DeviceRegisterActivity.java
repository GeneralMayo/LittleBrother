package edu.cmu.ece18549.little_brother.littlebrother.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.util.ArrayList;

import edu.cmu.ece18549.little_brother.littlebrother.R;
import edu.cmu.ece18549.little_brother.littlebrother.adapter.SensorRegisterAdapter;
import edu.cmu.ece18549.little_brother.littlebrother.data_component.Sensor;

public class DeviceRegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_register);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecyclerView rvSensors = (RecyclerView) findViewById(R.id.sensor_list);

        // Initialize contacts
        ArrayList<String> sensors = new ArrayList<String>();
        sensors.add("");
        sensors.add("");
        sensors.add("");

        // Create adapter passing in the sample user data
        SensorRegisterAdapter adapter = new SensorRegisterAdapter(sensors);
        // Attach the adapter to the recyclerview to populate items
        rvSensors.setAdapter(adapter);
        // Set layout manager to position the items
        rvSensors.setLayoutManager(new LinearLayoutManager(this));

    }

    public void addSensor(View v) {

    }

    public void registerDevice(View v) {

        Intent intent = new Intent(this, DeviceListActivity.class);
        startActivity(intent);
    }

}
