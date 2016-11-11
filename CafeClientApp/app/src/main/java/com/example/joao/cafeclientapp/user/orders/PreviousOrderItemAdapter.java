package com.example.joao.cafeclientapp.user.orders;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.joao.cafeclientapp.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.DateFormat;
import java.util.ArrayList;

/**
 * Created by norim on 24/10/2016.
 */


public class PreviousOrderItemAdapter extends RecyclerView.Adapter<PreviousOrderItemAdapter.ViewHolder> {

    private final Activity currentActivity;
    private ArrayList<Order> dataset;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public View mView;
        public ViewHolder(View v) {
            super(v);
            mView = v;
        }
    }

    public class OnItemClickListener implements View.OnClickListener {

        private final int itemPosition;

        public OnItemClickListener(int position){
            this.itemPosition = position;
        }

        @Override
        public void onClick(final View view) {
            Intent i = new Intent(currentActivity, ShowOrderActivity.class);
            i.putExtra("order", dataset.get(itemPosition));
            currentActivity.startActivity(i);
        }
    }

    public PreviousOrderItemAdapter(ArrayList<Order> orders, Activity a){
        //convert Hash Map to Array List
        this.dataset = orders;
        this.currentActivity = a;
    }

    @Override
    public PreviousOrderItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.previous_order_list_item, parent, false);
        // set the view's size, margins, paddings and layout parameters

        PreviousOrderItemAdapter.ViewHolder vh = new PreviousOrderItemAdapter.ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(PreviousOrderItemAdapter.ViewHolder holder, final int position) {

        TextView date = (TextView) holder.mView.findViewById(R.id.order_date);
        TextView total_price = (TextView) holder.mView.findViewById(R.id.total_price);

        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Date d = new Date(dataset.get(position).getTimestamp());

        date.setText(df.format(d));
        total_price.setText(String.format( "%.2f", dataset.get(position).getTotalPrice() )+"€");

        PreviousOrderItemAdapter.OnItemClickListener clickListener = new PreviousOrderItemAdapter.OnItemClickListener(position);
        holder.mView.setOnClickListener(clickListener);
    }

    public void addAll(ArrayList<Order> p) {
        this.dataset = new ArrayList<>(p);
        this.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }

    public void clearList(){
        this.dataset.clear();
        this.notifyDataSetChanged();
    }
}