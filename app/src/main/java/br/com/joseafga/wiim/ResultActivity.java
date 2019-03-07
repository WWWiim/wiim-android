/*
 * Copyright (C) 2018 José Almeida <jose.afga@gmail.com>
 *
 * License AGPLv3/Commercial, see LICENSE file for more details
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

import br.com.joseafga.wiim.models.Timeline;

public abstract class ResultActivity extends AppCompatActivity {

    // components
    protected RecyclerView mRecyclerView;
    protected TagAdapter mTagAdapter;
    protected CollapsingToolbarLayout mCollapsingToolbar;
    protected ProgressBar mProgressBar;
    // preferences
    public String apiUrl;
    public Integer updateInterval;
    public Integer faultTolerance = 0;
    public Integer faultCount = 0;
    public Integer requestCount = 0;
    // QRCode result (tag or process id)
    protected String qrData;
    // API
    protected WiimApi.Service mService;
    protected Map<String, String> params = new HashMap<>();
    // prevent unnecessary update
    private boolean isRunning = false;
    private boolean isWaiting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set layout view
        addOnCreate();
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
        qrData = getIntent().getExtras().getString("QRData");
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

        // run data loads
        isRunning = true;
        loadData();
    }

    @Override
    public void onPause() {
        super.onPause();

        // stop isRunning and all requests
        isRunning = false;
        WiimApi.cancelRequests();
    }

    /**
     * Set activity layout
     */
    protected abstract void addOnCreate();

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

        // return if already exists runnable
        if (isWaiting) return;
        isWaiting = true;

        // All done, remove progress bar
        mProgressBar.setVisibility(View.GONE);


        handler.postDelayed(new Runnable() {
            public void run() {
                isWaiting = false;  // no more waiting
                if (!isRunning) return;  // execute only if is running

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
    public abstract void loadData();

    /**
     * Get dynamic data update from API of server address
     */
    public abstract void loadDynamicData();

    /**
     * Show connection errors alert dialog with message
     * It have two buttons to exit application or reconfigure
     *
     * @param msg message text
     */
    public void onConnectionError(String msg) {
        // checks how many fails
        if (faultCount >= faultTolerance) {
            // reset counters
            faultCount = 0;
            requestCount = 0;

            // stop isRunning and all requests
            isRunning = false;
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
                            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
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
