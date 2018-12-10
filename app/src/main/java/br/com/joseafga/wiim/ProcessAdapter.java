/*
 * Copyright (C) 2018 José Almeida <jose.afga@gmail.com>
 *
 * https://creativecommons.org/licenses/by-nc/4.0/
 */

package br.com.joseafga.wiim;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
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

    private Context mContext ;
    // store list of processes
    private ArrayList<Process> mList;
    //private Map<Integer, ViewHolder> mCards = new HashMap<Integer, ViewHolder>();

    /**
     * Class Constructor
     * Provide a suitable constructor
     *
     * @param list Process list
     */
    public ProcessAdapter(Context context, ArrayList<Process> list) {
        mContext = context;
        mList = list;
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder {

        CardView ItemCard;
        TextView ItemTitle, ItemSummary, ItemZone;

        public ViewHolder(View itemView) {
            super(itemView);

            ItemCard = itemView.findViewById(R.id.item_card);
            ItemTitle = itemView.findViewById(R.id.item_title);
            ItemSummary = itemView.findViewById(R.id.item_summary);
            ItemZone = itemView.findViewById(R.id.item_zone);
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
        final Process process = mList.get(position);

        // set texts
        holder.ItemTitle.setText(process.getName());
        holder.ItemSummary.setText(process.getComment());
        holder.ItemZone.setText(process.getZone().getName());
        // set events
        holder.ItemCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start new activity passing process data
                Intent intent = new Intent(mContext, ProcessActivity.class);
                intent.putExtra("QRData", String.valueOf(process.getId()));

                mContext.startActivity(intent);
            }
        });
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
