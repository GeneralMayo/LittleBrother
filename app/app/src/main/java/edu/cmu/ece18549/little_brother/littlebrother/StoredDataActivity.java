package edu.cmu.ece18549.little_brother.littlebrother;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class StoredDataActivity extends AppCompatActivity {

    DataImporterAdapter mAdapter;
    private ExpandableListView mDataListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stored_data);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDataListView = (ExpandableListView) findViewById(R.id.dataList);
        DataOnDeviceImporter data = new DataOnDeviceImporter();
        mAdapter = new DataImporterAdapter(this, data);
        mDataListView.setAdapter(mAdapter);

    }

}
