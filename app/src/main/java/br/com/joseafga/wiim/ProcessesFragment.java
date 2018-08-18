package br.com.joseafga.wiim;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ProgressBar;

import java.util.ArrayList;

import br.com.joseafga.wiim.models.Process;
import br.com.joseafga.wiim.models.Tag;
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
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(llm);
        mProcessAdapter = new ProcessAdapter(new ArrayList<Process>()); // begin with empty array to avoid error
        mRecyclerView.setAdapter(mProcessAdapter);

        // get processes to view
        getProcessesData();

        return view;
    }

    public void getProcessesData() {
        // Update preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        apiUrl = prefs.getString(SettingsActivity.KEY_PREF_SERVER_ADDRESS, "");
        updateInterval = prefs.getInt(SettingsActivity.KEY_PREF_UPDATE_INTERVAL, 0) * 100; // multiply x100 to get real milliseconds

        try {
            // API calls
            WiimApi.getService(apiUrl).getProcesses().enqueue(new Callback<ArrayList<Process>>() {
                @Override
                public void onResponse(Call<ArrayList<Process>> call, Response<ArrayList<Process>> response) {
                    // All done, remove progress bar
                    mProgressBar.setVisibility(View.GONE);

                    // Update TAGs
                    mProcessAdapter.updateList(response.body());
                }

                @Override
                public void onFailure(Call<ArrayList<Process>> call, Throwable t) {
                    onErrorAlert(t.getMessage());
                }
            });
        } catch (Exception e) {
            onErrorAlert(e.getMessage());
        }
    }

    /**
     * Show errors alert dialog with message
     *
     * @param msg message text
     */
    private void onErrorAlert(String msg) {
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.error)
                .setMessage(msg)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().finish();
                    }
                })
                .create()
                .show();
    }

}
