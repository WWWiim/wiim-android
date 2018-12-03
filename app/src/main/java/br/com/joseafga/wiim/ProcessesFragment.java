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
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.util.ArrayList;

import br.com.joseafga.wiim.models.Process;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProcessesFragment extends Fragment {

    public RecyclerView mRecyclerView;
    protected ProcessAdapter mProcessAdapter;
    ProgressBar mProgressBar;
    // preferences
    protected String apiUrl;
    protected Integer updateInterval;

    public ProcessesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_processes, container, false);

        // set widgets
        mProgressBar = view.findViewById(R.id.loading_spinner);// set widgets
        mRecyclerView = view.findViewById(R.id.recycler_view);
        // set recycle view (and layout manager)
        int columns = getResources().getInteger(R.integer.processes_columns);
        GridLayoutManager glm = new GridLayoutManager(getContext(), columns);
        glm.setOrientation(GridLayoutManager.VERTICAL);
        // StaggeredGridLayoutManager glm = new StaggeredGridLayoutManager(columns, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(glm);
        mProcessAdapter = new ProcessAdapter(getContext(), new ArrayList<Process>()); // begin with empty array to avoid error
        mRecyclerView.setAdapter(mProcessAdapter);

        // get processes to view
        getProcessesData();

        return view;
    }

    public void getProcessesData() {
        // Update preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        apiUrl = prefs.getString(SettingsActivity.KEY_PREF_SERVER_ADDRESS, "localhost/");
        updateInterval = prefs.getInt(SettingsActivity.KEY_PREF_UPDATE_INTERVAL, 0) * 100; // multiply x100 to get real milliseconds
        // add API suffix
        if (!apiUrl.endsWith("/")) apiUrl += "/";
        apiUrl += "api/v1/";

        try {
            // API calls
            WiimApi.getService(apiUrl).getProcesses().enqueue(new Callback<ArrayList<Process>>() {
                @Override
                public void onResponse(Call<ArrayList<Process>> call, Response<ArrayList<Process>> response) {
                    // prevent errors on response
                    try {
                        // All done, remove progress bar
                        mProgressBar.setVisibility(View.GONE);

                        // Update TAGs
                        mProcessAdapter.updateList(response.body());
                    } catch (Exception e) {
                        // alert dialog if error occurs
                        onConnectionError(e.getMessage());
                    }
                }

                @Override
                public void onFailure(Call<ArrayList<Process>> call, Throwable t) {
                    onConnectionError(t.getMessage());
                }
            });
        } catch (Exception e) {
            onConnectionError(e.getMessage());
        }
    }

    /**
     * Show connection errors alert dialog with message
     * It have two buttons to exit application or reconfigure
     *
     * @param msg message text
     */
    private void onConnectionError(String msg) {
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.error)
                .setMessage(msg)
                .setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().finish();
                        System.exit(0);
                    }
                })
                .setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(getActivity().getApplicationContext(), SettingsActivity.class);
                        startActivity(intent);
                    }
                })
                .create()
                .show();
    }

}
