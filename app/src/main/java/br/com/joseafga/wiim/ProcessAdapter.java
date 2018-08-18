/*
 * Copyright (C) 2018 José Almeida <jose.afga@gmail.com>
 *
 * https://creativecommons.org/licenses/by-nc/4.0/
 */

package br.com.joseafga.wiim;

import android.content.ClipData;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import br.com.joseafga.wiim.models.Process;

/**
 * This class adapt Process objects from ArrayList to CardView layout
 */
public class ProcessAdapter extends RecyclerView.Adapter<ProcessAdapter.ViewHolder> {

    // store list with tags
    private ArrayList<Process> mList;
    //private Map<Integer, ViewHolder> mCards = new HashMap<Integer, ViewHolder>();

    /**
     * Class Constructor
     * Provide a suitable constructor
     *
     * @param list Process list
     */
    public ProcessAdapter(ArrayList<Process> list) {
        mList = list;
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView ItemTitle, ItemSummary;

        public ViewHolder(View itemView) {
            super(itemView);

            ItemTitle = itemView.findViewById(R.id.item_title);
            ItemSummary = itemView.findViewById(R.id.item_summary);
        }
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.process_item, parent, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Process process = mList.get(position);
        TextView ItemTitle, ItemSummary;

        ItemTitle = holder.ItemTitle;
        ItemSummary = holder.ItemSummary;

        ItemTitle.setText(process.getName());
        ItemSummary.setText(process.getComment());
    }

    /**
     * Update Adapter list
     *
     * @param list
     */
    public void updateList(ArrayList<Process> list) {
        mList.clear();
        mList.addAll(list);
        notifyDataSetChanged();
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mList.size();
    }

}
