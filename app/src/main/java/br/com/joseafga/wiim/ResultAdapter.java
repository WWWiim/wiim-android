/*
 * Copyright (C) 2018 José Almeida <jose.afga@gmail.com>
 *
 * https://creativecommons.org/licenses/by/4.0/
 */
package br.com.joseafga.wiim;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import br.com.joseafga.wiim.models.Tag;

/**
 * This class is ...
 */
public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ViewHolder> {

    // store list with tags
    private ArrayList<Tag> mList;

    /**
     * Class Constructor
     * Provide a suitable constructor
     *
     * @param context Activity context
     * @param list    Tag list
     */
    ResultAdapter(ArrayList<Tag> list) {
        mList = list;
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView tagImage;
        TextView tagTitle, tagSummary;

        public ViewHolder(View itemView) {
            super(itemView);

            tagImage = itemView.findViewById(R.id.tag_image);
            tagTitle = itemView.findViewById(R.id.tag_title);
            tagSummary = itemView.findViewById(R.id.tag_summary);
        }
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.result_tag, parent, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Tag tag = mList.get(position);
        ImageView tagImage;
        TextView tagTitle, tagSummary;

        int imageRes;
        tagImage = holder.tagImage;
        tagTitle = holder.tagTitle;
        tagSummary = holder.tagSummary;

        if (tag.getStatus() < 2.5) imageRes = R.drawable.ic_tag_faces_unhappy_24dp;
        else if (tag.getStatus() > 4) imageRes = R.drawable.ic_tag_faces_neutral_24dp;
        else imageRes = R.drawable.ic_tag_faces_happy_24dp;

        tagImage.setImageResource(imageRes);
        tagTitle.setText(tag.getDescription());
        tagSummary.setText(tag.getComment());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mList.size();
    }

}
