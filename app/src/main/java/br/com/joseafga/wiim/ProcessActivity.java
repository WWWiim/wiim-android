/*
 * Copyright (C) 2018 José Almeida <jose.afga@gmail.com>
 *
 * License AGPLv3/Commercial, see LICENSE file for more details
 */

package br.com.joseafga.wiim;

import java.util.ArrayList;

import br.com.joseafga.wiim.models.Process;
import br.com.joseafga.wiim.models.Timeline;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProcessActivity extends ResultActivity {

    /**
     * Set activity layout
     */
    @Override
    protected void addOnCreate() {
        setContentView(R.layout.activity_process);
    }

    /**
     * Get data from API of server address
     */
    @Override
    public void loadData() {
        // API calls
        mService.getProcess(qrData).enqueue(new Callback<Process>() {
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
    }

    /**
     * Get dynamic data update from API of server address
     */
    @Override
    public void loadDynamicData() {
        mService.getProcessTimeline(qrData, params).enqueue(new Callback<ArrayList<Timeline>>() {
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
    }
}
