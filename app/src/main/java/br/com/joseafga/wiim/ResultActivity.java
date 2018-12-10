/*
 * Copyright (C) 2018 José Almeida <jose.afga@gmail.com>
 *
 * https://creativecommons.org/licenses/by-nc/4.0/
 */

package br.com.joseafga.wiim;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import br.com.joseafga.wiim.models.Process;
import br.com.joseafga.wiim.models.Record;
import br.com.joseafga.wiim.models.Tag;
import br.com.joseafga.wiim.models.Timeline;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResultActivity extends AppCompatActivity {

    public RecyclerView mRecyclerView;
    protected TagAdapter mTagAdapter;
    protected CollapsingToolbarLayout mCollapsingToolbar;
    protected ProgressBar mProgressBar;
    // preferences
    private String apiUrl;
    private Integer updateInterval;
    private Integer faultTolerance = 0;
    private Integer faultCount = 0;
    private Integer requestCount = 0;
    // QRCode result (process|tag, id)
    private String[] qrData;
    // API
    private WiimApi.Service mService;
    private Map<String, String> params = new HashMap<>();
    private ArrayList<Timeline> cachedList = new ArrayList<>();
    // prevent unnecessary update
    private boolean running = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set layout view
        setContentView(R.layout.activity_result);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // add back arrow to toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // get saved preferences
        getPreferences();

        // set widgets
        mProgressBar = findViewById(R.id.loading_spinner);
        mCollapsingToolbar = findViewById(R.id.collapsing_toolbar);
        mRecyclerView = findViewById(R.id.recycler_view);
        // set recycle view (and layout manager)
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(llm);
        mTagAdapter = new TagAdapter(this, new ArrayList<Timeline>()); // begin with empty array to avoid error
        mRecyclerView.setAdapter(mTagAdapter);

        // set a default title
        mCollapsingToolbar.setTitle(getString(R.string.loading));
        // set API connection
        mService = WiimApi.getService(apiUrl);

        // get intent extras from main activity
        //qrData = getIntent().getExtras().getStringArray("QRData");
        qrData = new String[]{"tag", "64"}; // force tag log for debug

        // set it is running
        running = true;
        loadData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.ac_settings:
                Intent settings = new Intent(this, SettingsActivity.class);
                startActivity(settings);

                break;
            // ?more options
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();

        // update preferences and service
        getPreferences();
        mService = WiimApi.getService(apiUrl);

        // continue
        running = true;
        loadData();
    }

    @Override
    public void onPause() {
        super.onPause();

        // stop running and all requests
        running = false;
        WiimApi.cancelRequests();
    }

    /**
     * Get and/or update preferentes
     */
    public void getPreferences(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        apiUrl = prefs.getString(SettingsActivity.KEY_PREF_SERVER_ADDRESS, "localhost/");
        updateInterval = prefs.getInt(SettingsActivity.KEY_PREF_UPDATE_INTERVAL, 10) * 100; // multiply x100 to get real milliseconds
        faultTolerance = prefs.getInt(SettingsActivity.KEY_PREF_FAULT_TOLERANCE, 10);

        // add API suffix
        if (!apiUrl.endsWith("/"))  apiUrl += "/";
        apiUrl += "api/v1/";
    }

    /**
     * Set Collapsing toolbar texts
     *
     * @param title   toolbar text title
     * @param comment toolbar text summary
     * @param zone    toolbar bottom text
     */
    public void setToolbarTexts(String title, String comment, String zone) {
        // set process title
        mCollapsingToolbar.setTitle(title);

        // get resources layout
        TextView processSummary = findViewById(R.id.process_summary);
        TextView processZone = findViewById(R.id.process_zone);
        // set values
        processSummary.setText(comment);
        processZone.setText(zone);
    }

    /**
     * Call get data after wait setting time
     */
    public void updateDelayed() {
        final Handler handler = new Handler();

        // All done, remove progress bar
        mProgressBar.setVisibility(View.GONE);

        handler.postDelayed(new Runnable() {
            public void run() {
                // execute only if is running
                if (!running)
                    return;

                try {
                    // load data to update process
                    loadDynamicData();

                    // check if requests reached 100
                    if (requestCount >= 100) {
                        // if true reset counters
                        requestCount = 0;
                        faultCount = 0;
                    } else
                        requestCount++; // +1 requests
                } catch (Exception e) {
                    // alert dialog if error occurs
                    onConnectionError(e.getMessage());
                }
            }
        }, updateInterval);
    }

    /**
     * Get data from API of server address
     */
    public void loadData() {
        // API calls
        if (qrData[0].equals("process")) {
            mService.getProcess(qrData[1]).enqueue(new Callback<Process>() {
                @Override
                public void onResponse(Call<Process> call, Response<Process> response) {
                    // prevent errors on response
                    try {
                        Process process = response.body();

                        // sets and updates
                        setToolbarTexts(process.getName(), process.getComment(), process.getZone().getName());
                    } catch (Exception e) {
                        // alert dialog if error occurs
                        onConnectionError(e.getMessage());
                    }
                }

                @Override
                public void onFailure(Call<Process> call, Throwable t) {
                    onConnectionError(t.getMessage());
                }
            });

            // load timeline list async
            loadDynamicData();
        } else {
            // TODO single tag
            mService.getTag(qrData[1]).enqueue(new Callback<Tag>() {
                @Override
                public void onResponse(Call<Tag> call, Response<Tag> response) {
                    try {
                        Tag tag = response.body();

                        // set and updates
                        setToolbarTexts(tag.getAlias(), tag.getComment(), tag.getName());

                        // wrap timeline in a array to adapter can read it
                        Timeline tl = new Timeline();
                        tl.setTag(tag);
                        cachedList.add(0, tl);

                        // display tag item
                        mTagAdapter.updateList(cachedList);

                        loadDynamicData();
                    } catch (Exception e) {
                        // alert dialog if error occurs
                        onConnectionError(e.getMessage());
                    }
                }

                @Override
                public void onFailure(Call<Tag> call, Throwable t) {
                    onConnectionError(t.getMessage());
                }
            });
        }
    }

    /**
     * Get dynamic data update from API of server address
     */
    public void loadDynamicData() {
        if (qrData[0].equals("process")) {
            mService.getProcessTimeline(qrData[1], params).enqueue(new Callback<ArrayList<Timeline>>() {
                @Override
                public void onResponse(Call<ArrayList<Timeline>> call, Response<ArrayList<Timeline>> response) {
                    try {
                        // use response to update items
                        int lastRecId = mTagAdapter.updateList(response.body());
                        // if last record id greater than zero update params
                        if (lastRecId > 0)
                            params.put("since", String.valueOf(lastRecId));

                        // delayed function to update
                        updateDelayed();
                    } catch (Exception e) {
                        // alert dialog if error occurs
                        onConnectionError(e.getMessage());
                    }
                }

                @Override
                public void onFailure(Call<ArrayList<Timeline>> call, Throwable t) {
                    onConnectionError(t.getMessage());
                }
            });
        } else {
            mService.getTagRecords(qrData[1], params).enqueue(new Callback<ArrayList<Record>>() {
                @Override
                public void onResponse(Call<ArrayList<Record>> call, Response<ArrayList<Record>> response) {
                    try {
                        ArrayList<Record> recs = response.body();
                        Record lastRec = null;

                        assert recs != null;
                        if (!recs.isEmpty()) {  // checks if have content
                            for (Record rec : recs) {
                                // TODO use opc time
                                if (lastRec == null || rec.getId() > lastRec.getId())
                                    lastRec = rec;  // get last record
                            }

                            // TODO update graph

                            Timeline tl = cachedList.get(0);
                            tl.setRecord(lastRec);

                            // use response to update items
                            int lastRecId = mTagAdapter.updateList(cachedList);
                            // if last record id greater than zero update params
                            if (lastRecId > 0)
                                params.put("since", String.valueOf(lastRecId));
                        }

                        // delayed function to update
                        updateDelayed();
                    } catch (Exception e) {
                        // alert dialog if error occurs
                        onConnectionError(e.getMessage());
                    }
                }

                @Override
                public void onFailure(Call<ArrayList<Record>> call, Throwable t) {
                    onConnectionError(t.getMessage());
                }
            });
        }
    }

    /**
     * Show connection errors alert dialog with message
     * It have two buttons to exit application or reconfigure
     *
     * @param msg message text
     */
    private void onConnectionError(String msg) {
        // checks how many fails
        if (faultCount >= faultTolerance) {
            // reset counters
            faultCount = 0;
            requestCount = 0;

            // stop running and all requests
            running = false;
            WiimApi.cancelRequests();

            // show message alert
            new AlertDialog.Builder(this)
                    .setTitle(R.string.error)
                    .setMessage(msg)
                    .setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                            System.exit(0);
                        }
                    })
                    .setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(ResultActivity.this, SettingsActivity.class);
                            startActivity(intent);
                        }
                    })
                    .create()
                    .show();
        } else {
            faultCount++;
            Log.e("FAIL", "Connection error total errors: " + String.valueOf(faultCount));
            // recall
            updateDelayed();
        }
    }

}
