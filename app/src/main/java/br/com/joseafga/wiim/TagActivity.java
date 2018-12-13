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

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import br.com.joseafga.wiim.helpers.ValueFormatter;
import br.com.joseafga.wiim.models.Record;
import br.com.joseafga.wiim.models.Tag;
import br.com.joseafga.wiim.models.Timeline;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TagActivity extends ResultActivity {

    // cache timeline list to store tag
    private ArrayList<Timeline> cachedList = new ArrayList<>();
    // tag
    private Tag mTag = null;
    // chart
    private Thread mThread = null;
    private LineChart mChart = null;
    private YAxis yAxis;
    private XAxis xAxis;
    private long xRef = 0;
    // hard settings
    private final boolean DASHED = false;
    private final Locale LocaleBR = new Locale("pt", "BR");

    /**
     * Set activity layout
     */
    @Override
    protected void addOnCreate() {
        setContentView(R.layout.activity_tag);

        mChart = findViewById(R.id.chart);
        // configure chart
        setupChart();
        setupChartAxes();
        //setupChartLimiters();
        setupChartData();
        setChartLegend();

        params.put("order", "asc");  // API SQL order by
    }

    /**
     * Configure chart
     */
    private void setupChart() {
        mChart.getDescription().setEnabled(false);
        mChart.setTouchEnabled(true); // touchscreen

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        // mChart.setScaleXEnabled(true);
        // mChart.setScaleYEnabled(true);
        mChart.setPinchZoom(true);

        // drawables
        mChart.setBackgroundColor(Color.WHITE);
        mChart.setDrawGridBackground(false);
        //mChart.setGridBackgroundColor(Color.WHITE);

        // create marker to display box when values are selected
//        MyMarkerView mv = new MyMarkerView(this, R.layout.custom_marker_view);

        // Set the marker to the chart
//        mv.setChartView(chart);
//        chart.setMarker(mv);

        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);
        // add empty data
        mChart.setData(data);
    }

    private void setupChartAxes() {
        // Axis Style
        xAxis = mChart.getXAxis();
        yAxis = mChart.getAxisLeft();

        // X Axis
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawAxisLine(true);
        xAxis.setAxisLineWidth(1f);
        xAxis.setAxisLineColor(Color.GRAY);
        // vertical grid lines
        //xAxis.setLabelCount(5, true);  // number of grids
        xAxis.enableGridDashedLine(10f, 10f, 0f);
        xAxis.setGranularity(1000f); // 100 milliseconds
        xAxis.setCenterAxisLabels(false);
        xAxis.setValueFormatter(new ValueFormatter() {

            private final SimpleDateFormat mFormat =
                    new SimpleDateFormat("HH:mm:ss", LocaleBR);

            @Override
            public String getFormattedValue(float value) {

                long millis = TimeUnit.MILLISECONDS.toMillis((long) value + xRef);
                return mFormat.format(new Date(millis));
            }
        });

        // Y Axis
        yAxis.setTextColor(Color.BLACK);// color
        yAxis.setDrawAxisLine(true);
        yAxis.setAxisLineWidth(1f);
        yAxis.setAxisLineColor(Color.GRAY);
        // horizontal grid lines
        yAxis.enableGridDashedLine(10f, 10f, 0f);
        yAxis.setValueFormatter(new ValueFormatter() {
            private DecimalFormat mFormat =
                    new DecimalFormat("#.");

            @Override
            public String getFormattedValue(float value) {
                //return mFormat.format(value); // no format
                return String.valueOf(value);
            }
        });

        // disable dual axis (only use LEFT axis)
        mChart.getAxisRight().setEnabled(false);

        // axis range
        //yAxis.setAxisMaximum(2000f);
        //yAxis.setAxisMinimum(0f);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);
    }

    private void setupChartLimiters() {
        // Create Limit Lines //
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
    }

    private void setupChartData() {
        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);

        // add empty data
        mChart.setData(data);
    }

    private void setChartLegend() {
        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();
        l.setEnabled(false);

        if (l.isEnabled()) {
            // draw legend entries as lines
            l.setForm(LegendForm.LINE);

            // customize legend entry
            if (DASHED)
                l.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));

            l.setFormLineWidth(1f);
            l.setFormSize(15.f);
        }
    }

    /**
     * Create a line chart style
     *
     * @return Line data set
     */
    private LineDataSet createChartLine() {// create a dataset and give it a type
        LineDataSet set = new LineDataSet(null, mTag.getAlias());

        set.setDrawIcons(false);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);

        // lines and points
        if (DASHED)
            set.enableDashedLine(10f, 5f, 0f); // draw dashed line
        //set.setMode(LineDataSet.Mode.CUBIC_BEZIER); // curve
        set.setColor(Color.parseColor("#aa29c1"));
        set.setCircleColor(Color.parseColor("#4b216b"));

        // line thickness and point size
        set.setLineWidth(2f);
        set.setCircleRadius(4f);

        // draw points as solid circles
        set.setDrawCircleHole(true);

        // text size of values
        set.setValueTextSize(9f);

        // draw selection line as dashed
        //set1.enableDashedHighlightLine(10f, 5f, 0f);
        set.setHighlightEnabled(false);

        // set the filled area
        set.setDrawFilled(true);
        set.setFillFormatter(new IFillFormatter() {
            @Override
            public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
                return mChart.getAxisLeft().getAxisMinimum();
            }
        });

        // set color of filled area
        if (Utils.getSDKInt() >= 18) {
            // drawables only supported on api level 18 and above
            Drawable drawable = ContextCompat.getDrawable(this, R.drawable.fade_fucsia);
            set.setFillDrawable(drawable);
            //set.setFillColor(Color.parseColor("#4b216b"));
        } else {
            set.setFillColor(Color.BLACK);
        }

        // To show values of each point
        set.setDrawValues(false);

        return set;
    }

    private void addChartEntries(ArrayList<Record> recs) {
        LineData data = mChart.getData();

        if (data == null) return; // do nothing

        ILineDataSet set = data.getDataSetByIndex(0);

        if (set == null) {
            set = createChartLine();
            data.addDataSet(set);
        }

        for (Record rec : recs) {
            float val = rec.getValue().floatValue();
            long time = rec.getTimeOpc().getTime();

            // update last value
            if (xRef == 0) xRef = time;

            data.addEntry(new Entry(time - xRef, val), 0);

            Log.d("RECORD",
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", LocaleBR)
                            .format(rec.getTimeOpc()));

            Log.d("RECORD", String.valueOf(time - xRef));
        }

        // let the chart know it's data has changed
        data.notifyDataChanged();
        mChart.notifyDataSetChanged();

        // limit the number of visible milliseconds
        mChart.setVisibleXRangeMaximum(60 * 1000);

        // move to the latest entry
        mChart.moveViewToX(data.getXMax());

        // draw points over time
        //mChart.animateX(1000);
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
                    mTag = response.body();

                    // set and updates
                    setToolbarTexts(mTag.getAlias(), mTag.getComment(), mTag.getName());

                    // wrap timeline in a array to adapter can read it
                    Timeline tl = new Timeline();
                    tl.setTag(mTag);
                    cachedList.clear();
                    cachedList.add(0, tl);

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

                        // update graph
                        addChartEntries(recs);

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
        super.onDestroy();

        if (mThread != null) {
            mThread.interrupt();
        }
    }
}
