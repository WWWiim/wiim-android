/*
 * Copyright (C) 2018 José Almeida <jose.afga@gmail.com>
 *
 * https://creativecommons.org/licenses/by-nc/4.0/
 */

package br.com.joseafga.wiim;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicBoolean;

import br.com.joseafga.wiim.models.Record;
import br.com.joseafga.wiim.models.Tag;
import br.com.joseafga.wiim.models.Timeline;

/**
 * This class adapt Tag objects from ArrayList to CardView layout
 */
public class TagAdapter extends RecyclerView.Adapter<TagAdapter.ViewHolder> {

    private Context mContext;
    // store list of tags and records
    private ArrayList<Timeline> mList;
    //private Map<Integer, ViewHolder> mCards = new HashMap<Integer, ViewHolder>();
    private Picasso mPicasso = Picasso.get();

    /**
     * Class Constructor
     * Provide a suitable constructor
     *
     * @param list Tag list
     */
    public TagAdapter(Context context, ArrayList<Timeline> list) {
        mContext = context;
        mList = list;
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView itemImage, itemStatus;
        TextView itemTitle, itemSummary, itemValue, itemUnit, itemDate;

        public ViewHolder(View itemView) {
            super(itemView);

            itemImage = itemView.findViewById(R.id.item_image);
            itemTitle = itemView.findViewById(R.id.item_title);
            itemSummary = itemView.findViewById(R.id.item_summary);
            itemValue = itemView.findViewById(R.id.item_value);
            itemUnit = itemView.findViewById(R.id.item_unit);
            itemStatus = itemView.findViewById(R.id.item_status);
            itemDate = itemView.findViewById(R.id.item_date);
        }
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tag_item, parent, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Timeline tl = mList.get(position);
        Tag tag = tl.getTag();
        Record rec = tl.getRecord();

        // Tag data is required
        int imageStatus;

        // set face according to the status
//        if (tag.getStatus() < 2.5) imageStatus = R.drawable.ic_faces_unhappy_24dp;
//        else if (tag.getStatus() > 4) imageStatus = R.drawable.ic_faces_neutral_24dp;
//        else
        imageStatus = R.drawable.ic_faces_happy_24dp;

        // set image
        // if have no image show default
        if (tag.getIcon() == null || tag.getIcon().equals("")) {
            holder.itemImage.setImageResource(R.drawable.placeholder_tag);
        } else {
            // if have image url ... load it
            mPicasso.load(tag.getIcon_url())
                    .placeholder(R.drawable.placeholder_tag)
                    .into(holder.itemImage); // set image from url
        }

        holder.itemTitle.setText(tag.getAlias());
        holder.itemSummary.setText(tag.getComment());
        // justify(itemSummary); // ugly update when justify on
        holder.itemUnit.setText(tag.getUnit());
        holder.itemStatus.setImageResource(imageStatus);

        // Record data is optional
        if (rec != null) {
            holder.itemValue.setText(String.valueOf(rec.getValue()));
            holder.itemDate.setText(rec.getTimeOpc().substring(11)); // substring to remove d/m/Y
            // TODO: quality is good|?bad
        }
    }

    /**
     *
     * Update Adapter list
     *
     * @param list List with Timeline (Tag, Record)
     * @return greatest record id
     */
    public int updateList(ArrayList<Timeline> list) {
        boolean isUpdate = false; // updates an existing value
        int lastRecId = 0;

        for (Timeline tl : list) {
            // get timeline itens
            Tag tag = tl.getTag();
            Record rec = tl.getRecord();

            ListIterator<Timeline> iterator = mList.listIterator();
            isUpdate = false; // reset value

            // get greatest id
            lastRecId = (rec.getId() > lastRecId) ? rec.getId() : lastRecId;

            while (iterator.hasNext()) {
                Timeline next = iterator.next();
                Tag t = next.getTag();
                Record r = next.getRecord();

                if (tag.getId() == t.getId()) {
                    Log.d("UPDATING", tag.getId().toString());
                    isUpdate = true; // have value to update

                    iterator.set(tl);
                }
            }

            if (!isUpdate)
                // create a new tag item
                mList.add(tl);
        }

//        mList.clear();
//        mList.addAll(list);

        // TODO improve it
        if (list != null && !list.isEmpty())
            notifyDataSetChanged();

        return lastRecId;
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mList.size();
    }

    // used for justify summary text
    // from: <https://github.com/twiceyuan/TextJustification>
    public static void justify(final TextView textView) {

        final AtomicBoolean isJustify = new AtomicBoolean(false);
        final String textString = textView.getText().toString();
        final TextPaint textPaint = textView.getPaint();
        final SpannableStringBuilder builder = new SpannableStringBuilder();

        textView.post(new Runnable() {
            @Override
            public void run() {

                if (!isJustify.get()) {

                    final int lineCount = textView.getLineCount();
                    final int textViewWidth = textView.getWidth();

                    for (int i = 0; i < lineCount; i++) {

                        int lineStart = textView.getLayout().getLineStart(i);
                        int lineEnd = textView.getLayout().getLineEnd(i);

                        String lineString = textString.substring(lineStart, lineEnd);

                        if (i == lineCount - 1) {
                            builder.append(new SpannableString(lineString));
                            break;
                        }

                        String trimSpaceText = lineString.trim();
                        String removeSpaceText = lineString.replaceAll(" ", "");

                        float removeSpaceWidth = textPaint.measureText(removeSpaceText);
                        float spaceCount = trimSpaceText.length() - removeSpaceText.length();

                        float eachSpaceWidth = (textViewWidth - removeSpaceWidth) / spaceCount;

                        SpannableString spannableString = new SpannableString(lineString);
                        for (int j = 0; j < trimSpaceText.length(); j++) {
                            char c = trimSpaceText.charAt(j);
                            if (c == ' ') {
                                Drawable drawable = new ColorDrawable(0x00ffffff);
                                drawable.setBounds(0, 0, (int) eachSpaceWidth, 0);
                                ImageSpan span = new ImageSpan(drawable);
                                spannableString.setSpan(span, j, j + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            }
                        }

                        builder.append(spannableString);
                    }

                    textView.setText(builder);
                    isJustify.set(true);
                }
            }
        });
    }
}
