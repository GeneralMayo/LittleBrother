package edu.cmu.ece18549.little_brother.littlebrother.activity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import edu.cmu.ece18549.little_brother.littlebrother.R;
import edu.cmu.ece18549.little_brother.littlebrother.adapter.StoredDataAdapter;
import edu.cmu.ece18549.little_brother.littlebrother.data_component.DeviceLog;
import edu.cmu.ece18549.little_brother.littlebrother.service.DeviceFinderService;

public class StoredDataActivity extends AppCompatActivity {
    private static final String TAG = "STORED_DATA_ACTIVITY: ";
    StoredDataAdapter mAdapter;
    private RecyclerView mDataListView;
    private List<DeviceLog> mLogs;
    private RecyclerView mLogListView;
    private BroadcastReceiver receiver;
    private boolean mBound;
    private DeviceFinderService mService;

    private class DevicesAroundBroadcastReceiver extends BroadcastReceiver {
        DevicesAroundActivity activity;

        public DevicesAroundBroadcastReceiver(){
            this.activity = activity;
        }

        @Override
        public void onReceive(final Context context, final Intent intent) {
            Log.i(TAG, "BroadcastReceiver received intent");
            Serializable s = intent.getSerializableExtra(DeviceFinderService.BLE_CHANGE_EXTRA);

            if (s == null) {
                Log.i(TAG, "intent was not from the DeviceFinderService. Ignore.");
            }
            else {
                int id = (int) s;
                Log.i(TAG, "Received ID:" +id);
                updateLogs();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stored_data);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mLogs = new ArrayList<>();
        mAdapter = new StoredDataAdapter(mLogs);
        mDataListView = (RecyclerView) findViewById(R.id.storedDataList);
        mDataListView.setAdapter(mAdapter);
        mDataListView.setLayoutManager(new LinearLayoutManager(this));
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i(TAG, "BroadcastReceiver received intent");
                Serializable s = intent.getSerializableExtra(DeviceFinderService.BLE_CHANGE_EXTRA);

                if (s == null) {
                    Log.i(TAG, "intent was not from the DeviceFinderService. Ignore.");
                }
                else {
                    int id = (int) s;
                    Log.i(TAG, "Received ID:" +id);
                    updateLogs();
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Bind to LocalService
        Intent intent = new Intent(this, DeviceFinderService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            Log.i(TAG, "device finder service connected");

            DeviceFinderService.LocalBinder binder = (DeviceFinderService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            IntentFilter filter = new IntentFilter();
            filter.addAction(DeviceFinderService.BLE_CHANGE_ACTION);
            registerReceiver(receiver, filter);
            updateLogs();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    protected void onPause() {
        unregisterReceiver(receiver);
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        super.onPause();
    }

    public void updateLogs() {
        mLogs.clear();
        mLogs.addAll(mService.getLogs());
        mAdapter.notifyDataSetChanged();
    }
}
