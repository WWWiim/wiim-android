/*
 * Copyright (C) 2018 José Almeida <jose.afga@gmail.com>
 *
 * https://creativecommons.org/licenses/by/4.0/
 */
package br.com.joseafga.wiim;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import br.com.joseafga.wiim.models.Process;
import br.com.joseafga.wiim.models.Tag;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResultActivity extends AppCompatActivity {

    public RecyclerView recyclerView;
    protected Process process;
    protected ArrayList<Tag> tagsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_result);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

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
        recyclerView = findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(layoutManager);

        ResultAdapter adapter = new ResultAdapter(tagsList);

        recyclerView.setAdapter(adapter);
    }

    public void setAppBar(String title, String comment, String zone){
        // set process title
        //ResultActivity.actionBar.setTitle(title);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(title);
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
        String type = intent.getExtras().getString("type"); // process|tag
        String id = intent.getExtras().getString("value"); // id

        final Call<Process> getProcess = WiimApi.getService(serverAddress).getProcess(id);
        getProcess.enqueue(new Callback<Process>() {
            @Override
            public void onResponse(Call<Process> call, Response<Process> response) {
                process = response.body();

                setAppBar(process.getName(), process.getComment(), process.getZone());
                fetchTags(process.getTags());
                //Toast.makeText(ResultActivity.this, "Sucesso!!", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Call<Process> call, Throwable t) {
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
