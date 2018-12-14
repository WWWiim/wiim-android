package br.com.joseafga.wiim.helpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import br.com.joseafga.wiim.R;
import br.com.joseafga.wiim.TagActivity;

/**
 * Custom implementation of the MarkerView.
 *
 * @author Philipp Jahoda
 */
@SuppressLint("ViewConstructor")
public class MyMarkerView extends MarkerView {

    private final TextView tvContent;
    private final TextView subContent;
    private final Locale LocaleBR = new Locale("pt", "BR");
    private TagActivity mActivity;

    public MyMarkerView(TagActivity activity, int layoutResource) {
        super(activity, layoutResource);
        mActivity = activity;

        tvContent = findViewById(R.id.tvContent);
        subContent = findViewById(R.id.subContent);
    }

    // runs every time the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        if (e instanceof CandleEntry) {
            CandleEntry ce = (CandleEntry) e;

            tvContent.setText(Utils.formatNumber(ce.getHigh(), 0, true));
        } else {
            String yValue = Utils.formatNumber(e.getY(), 0, true);
            String xValue = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS", LocaleBR)
                    .format(new Date((long) e.getX() + mActivity.getxRef()));

            tvContent.setText(yValue);
            subContent.setText(xValue);
        }

        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2), -getHeight() - 15);
    }
}
