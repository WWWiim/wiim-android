/*
 * Copyright (C) 2018 José Almeida <jose.afga@gmail.com>
 *
 * https://creativecommons.org/licenses/by/4.0/
 */
package br.com.joseafga.wiim;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
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

import br.com.joseafga.wiim.models.Process;
import br.com.joseafga.wiim.models.Tag;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResultActivity extends AppCompatActivity {

    public RecyclerView mRecyclerView;
    public Process mProcess;
    protected CollapsingToolbarLayout mCollapsingToolbar;
    protected ProgressBar mProgressBar;
    protected ArrayList<Tag> mTagsList;

    protected ResultActivity resultActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set layout view
        setContentView(R.layout.activity_result);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // get progress bar widget
        mProgressBar = findViewById(R.id.loading_spinner);
        mCollapsingToolbar = findViewById(R.id.collapsing_toolbar);

        getProcessData();
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

    public void fetchTags(ArrayList<Tag> tagsList) {
        // set recycler view layout manager
        mRecyclerView = findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);

        mRecyclerView.setLayoutManager(layoutManager);

        ResultAdapter adapter = new ResultAdapter(tagsList);

        mRecyclerView.setAdapter(adapter);
    }

    public void setAppBar(String title, String comment, String zone){
        // set process title
        mCollapsingToolbar.setTitle(title);

        // get resources layout
        TextView processSummary = findViewById(R.id.process_summary);
        TextView processZone = findViewById(R.id.process_zone);
        // set values
        processSummary.setText(comment);
        processZone.setText(zone);
    }

    public void getProcessData() {
        // Preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String serverAddress = prefs.getString(SettingsActivity.KEY_PREF_SERVER_ADDRESS, "");
        // get update interval preference (multiply x100 to get in milliseconds)
        Integer updateInterval = prefs.getInt(SettingsActivity.KEY_PREF_UPDATE_INTERVAL, 0) * 100;

        // Intent extras
        Intent intent = getIntent();
        String[] scanned = intent.getExtras().getStringArray("scanned"); // process|tag, id

        final Call<Process> getProcess = WiimApi.getService(serverAddress).getProcess(scanned[1]);

        getProcess.enqueue(new Callback<Process>() {
            @Override
            public void onResponse(Call<Process> call, Response<Process> response) {
                mProcess = response.body();

                setAppBar(mProcess.getName(), mProcess.getComment(), mProcess.getZone());
                fetchTags(mProcess.getTags());

                // All done, remove progress bar
                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<Process> call, Throwable t) {

                getSupportActionBar().setTitle("falhou");

                new AlertDialog.Builder(ResultActivity.this)
                        .setTitle(R.string.error)
                        .setMessage(t.getMessage())
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .create()
                        .show();
            }
        });
    }
}
