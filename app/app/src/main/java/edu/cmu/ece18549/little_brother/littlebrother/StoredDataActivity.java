package edu.cmu.ece18549.little_brother.littlebrother;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class StoredDataActivity extends AppCompatActivity {

    private final String[] FAKE_LOGS = {"Log 1: From Device ... Date ...", "Log 2: From Device ... Date ...",
                                        "Log 3: From Device ... Date ...", "Log 4: From Device ... Date ...",
                                        "Log 5: From Device ... Date ...", "Log 6: From Device ... Date ...",
                                        "Log 7: From Device ... Date ...", "Log 8: From Device ... Date ...",};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stored_data);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        ListView dataList = (ListView) findViewById(R.id.dataList);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_view, FAKE_LOGS);
        dataList.setAdapter(adapter);

    }

}
