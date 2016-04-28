package edu.cmu.ece18549.little_brother.littlebrother.activity;

import java.util.List;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ExpandableListView;
import android.util.Log;

import edu.cmu.ece18549.little_brother.littlebrother.adapter.DataImporter;
import edu.cmu.ece18549.little_brother.littlebrother.adapter.DataImporterAdapter;
import edu.cmu.ece18549.little_brother.littlebrother.adapter.DataOnDeviceImporter;
import edu.cmu.ece18549.little_brother.littlebrother.R;
import edu.cmu.ece18549.little_brother.littlebrother.data_component.DeviceLog;

public class StoredDataActivity extends AppCompatActivity {
    private static final String TAG = "STORED_DATA_ACTIVITY: ";

    DataImporterAdapter mAdapter;
    DataImporter mData;
    private ExpandableListView mDataListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stored_data);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDataListView = (ExpandableListView) findViewById(R.id.dataList);
        mData = new DataOnDeviceImporter();
        mAdapter = new DataImporterAdapter(this, mData);
        mDataListView.setAdapter(mAdapter);
    }

    public void sendStoredLogs(View v) {
        //for log in logs
        Log.i(TAG, "Pushing all data on device to phone");
        List<DeviceLog> logs = mData.getAllLogs();
    }


}
