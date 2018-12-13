/*
 * Copyright (C) 2018 José Almeida <jose.afga@gmail.com>
 *
 * https://creativecommons.org/licenses/by-nc/4.0/
 */

package br.com.joseafga.wiim;

import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendForm;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.LimitLine.LimitLabelPosition;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.Utils;

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
    // chart
    private Thread mThread;
    private LineChart mChart;
    private YAxis yAxis;
    private XAxis xAxis;
    private boolean plotData = true;
    // settings
    private int chartRange = 100;

    /**
     * Set activity layout
     */
    @Override
    protected void setLayout() {
        setContentView(R.layout.activity_tag);

        setGraph();
    }

    /**
     *
     */
    protected void setGraph() {
        mChart = findViewById(R.id.chart);

        mChart.getDescription().setEnabled(false);

        // touchscreen
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        // chart.setScaleXEnabled(true);
        // chart.setScaleYEnabled(true);
        mChart.setPinchZoom(true);

        // drawables
        mChart.setBackgroundColor(Color.WHITE);

        // listeners
//        mChart.setOnChartValueSelectedListener(this);
        mChart.setDrawGridBackground(true);

        // create marker to display box when values are selected
//        MyMarkerView mv = new MyMarkerView(this, R.layout.custom_marker_view);

        // Set the marker to the chart
//        mv.setChartView(chart);
//        chart.setMarker(mv);

        // Axis Style
        xAxis = mChart.getXAxis();
        yAxis = mChart.getAxisLeft();

        // disable dual axis (only use LEFT axis)
        mChart.getAxisRight().setEnabled(false);

        // vertical grid lines
        xAxis.enableGridDashedLine(10f, 10f, 0f);
        // horizontal grid lines
        yAxis.enableGridDashedLine(10f, 10f, 0f);

        // axis range
//        yAxis.setAxisMaximum(2000f);
//        yAxis.setAxisMinimum(0f);

//        setGraphLimiters();
        setGraphData();

        // draw points over time
        mChart.animateX(1000);

        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();
        // draw legend entries as lines
        l.setForm(LegendForm.LINE);
    }

    public void setGraphLimiters(){
        // Create Limit Lines //
        LimitLine llXAxis = new LimitLine(9f, "Index 10");
        llXAxis.setLineWidth(4f);
        llXAxis.enableDashedLine(10f, 10f, 0f);
        llXAxis.setLabelPosition(LimitLabelPosition.RIGHT_BOTTOM);
        llXAxis.setTextSize(10f);
        llXAxis.setTypeface(Typeface.DEFAULT);

        LimitLine ll1 = new LimitLine(150f, "Upper Limit");
        ll1.setLineWidth(4f);
        ll1.enableDashedLine(10f, 10f, 0f);
        ll1.setLabelPosition(LimitLabelPosition.RIGHT_TOP);
        ll1.setTextSize(10f);
        ll1.setTypeface(Typeface.DEFAULT);

        LimitLine ll2 = new LimitLine(-30f, "Lower Limit");
        ll2.setLineWidth(4f);
        ll2.enableDashedLine(10f, 10f, 0f);
        ll2.setLabelPosition(LimitLabelPosition.RIGHT_BOTTOM);
        ll2.setTextSize(10f);
        ll2.setTypeface(Typeface.DEFAULT);

        // draw limit lines behind data instead of on top
        yAxis.setDrawLimitLinesBehindData(true);
        xAxis.setDrawLimitLinesBehindData(true);

        // add limit lines
        yAxis.addLimitLine(ll1);
        yAxis.addLimitLine(ll2);
        //xAxis.addLimitLine(llXAxis);
    }

    /**
     * set initial data to graph
     */
    public void setGraphData() {
        // add empty data
        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);
        mChart.setData(data);
    }


    /**
     * Set graph legend
     *
     * @param set Data set of the line
     */
    public void setGraphLegend(LineDataSet set) {
        setGraphLegend(set, false);
    }

    /**
     * Set graph legend
     *
     * @param set  Data set of the line
     * @param dashed The line is dashed?
     */
    public void setGraphLegend(LineDataSet set, boolean dashed) {
        // customize legend entry
        if (dashed)
            set.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));

        set.setFormLineWidth(1f);
        set.setFormSize(15.f);
    }

    /**
     * set initial data to graph
     */
    public void addGraphData(ArrayList<Record> recs) {
//        LineData data = mChart.getData();
//
//        if (data != null) {
//            ILineDataSet set = data.getDataSetByIndex(0);
//
//            if (set != null) {
//                set = createSet();
//                data.addDataSet(set);
//            }
//
//            float val = Float.valueOf(rec.getValue().toString());
//            Log.d("GRAPH", String.valueOf(val));
//
//
//            data.addEntry(new Entry(set.getEntryCount(), val), 0);
//            data.notifyDataChanged();
//
//            mChart.setVisibleXRangeMaximum(100);
//            mChart.moveViewToX(data.getEntryCount());
//        }


        ArrayList<Entry> values = new ArrayList<>();

        for (Record rec : recs) {
            float val = rec.getValue().floatValue();
            Log.d("GRAPH", String.valueOf(val));

            values.add(new Entry(values.size(), val, 0));
        }

        LineDataSet set1;

        if (mChart.getData() != null && mChart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) mChart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            set1.notifyDataSetChanged();
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            set1 = new LineDataSet(values, "DataSet 1");

            set1.setDrawIcons(false);

//            set1.enableDashedLine(10f, 5f, 0f); // draw dashed line
            // black lines and points
            set1.setColor(Color.parseColor("#aa29c1"));
            set1.setCircleColor(Color.parseColor("#884b216b"));

            // line thickness and point size
            set1.setLineWidth(2f);
            set1.setCircleRadius(4f);

            // draw points as solid circles
            set1.setDrawCircleHole(false);

            setGraphLegend(set1);

            // text size of values
            set1.setValueTextSize(9f);

            // draw selection line as dashed
            set1.enableDashedHighlightLine(10f, 5f, 0f);

            // set the filled area
            set1.setDrawFilled(true);
            set1.setFillFormatter(new IFillFormatter() {
                @Override
                public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
                    return mChart.getAxisLeft().getAxisMinimum();
                }
            });

            // set color of filled area
            if (Utils.getSDKInt() >= 18) {
                // drawables only supported on api level 18 and above
                Drawable drawable = ContextCompat.getDrawable(this, R.drawable.fade_fucsia);
                set1.setFillDrawable(drawable);
            } else {
                set1.setFillColor(Color.BLACK);
            }

            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1); // add the data sets

            // create a data object with the data sets
            LineData data = new LineData(dataSets);

            // set data
            mChart.setData(data);
        }

    }

    private void feedMultiple() {

        if (mThread != null){
            mThread.interrupt();
        }

        mThread = new Thread(new Runnable() {

            @Override
            public void run() {
                while (true){
                    plotData = true;
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });

        mThread.start();
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
                    cachedList.clear();
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

                    if (!recs.isEmpty()) {  // checks if have content
                        for (Record rec : recs) {
                            // TODO use opc time
                            if (lastRec == null || rec.getId() > lastRec.getId())
                                lastRec = rec;  // get last record
                        }

                        // TODO update graph
                        addGraphData(recs);

                        // set record
                        Timeline tl = cachedList.get(0);
                        tl.setRecord(lastRec);
                        cachedList.add(tl);

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

    @Override
    protected void onDestroy() {
//        mThread.interrupt();
        super.onDestroy();
    }
}
