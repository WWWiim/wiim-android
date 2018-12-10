package br.com.joseafga.wiim;

import java.util.ArrayList;

import br.com.joseafga.wiim.models.Record;
import br.com.joseafga.wiim.models.Tag;
import br.com.joseafga.wiim.models.Timeline;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TagActivity extends ResultActivity {

    // cache timeline list to store tag
    private ArrayList<Timeline> cachedList = new ArrayList<>();

    /**
     * Set activity layout
     */
    @Override
    protected void setLayout() {
        setContentView(R.layout.activity_tag);
    }

    /**
     * Get data from API of server address
     */
    @Override
    public void loadData() {
        mService.getTag(qrData).enqueue(new Callback<Tag>() {
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

    /**
     * Get dynamic data update from API of server address
     */
    @Override
    public void loadDynamicData() {
        mService.getTagRecords(qrData, params).enqueue(new Callback<ArrayList<Record>>() {
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
