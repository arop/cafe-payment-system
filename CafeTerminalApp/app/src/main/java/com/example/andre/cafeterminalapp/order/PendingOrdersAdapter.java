package com.example.andre.cafeterminalapp.order;

import android.app.Activity;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.andre.cafeterminalapp.R;

import org.json.JSONException;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.DateFormat;
import java.util.ArrayList;

class PendingOrdersAdapter extends RecyclerView.Adapter<PendingOrdersAdapter.ViewHolder> {

    private final Activity currentActivity;
    private ArrayList<Order> dataset;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        View mView;
        ViewHolder(View v) {
            super(v);
            mView = v;
        }
    }

    private class OnItemClickListener implements View.OnClickListener {

        private final int itemPosition;

        OnItemClickListener(int position){
            this.itemPosition = position;
        }

        @Override
        public void onClick(final View view) {
            Snackbar.make(view, "Sending pending order to server", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            Order.sendUnsentOrder(dataset.get(itemPosition), currentActivity, true);
        }
    }

    PendingOrdersAdapter(ArrayList<Order> orders, Activity a){
        //convert Hash Map to Array List
        this.dataset = orders;
        this.currentActivity = a;
    }

    @Override
    public PendingOrdersAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.pending_order_list_item, parent, false);
        // set the view's size, margins, paddings and layout parameters

        PendingOrdersAdapter.ViewHolder vh = new PendingOrdersAdapter.ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(PendingOrdersAdapter.ViewHolder holder, final int position) {

        TextView date = (TextView) holder.mView.findViewById(R.id.order_date);
        TextView num_products = (TextView) holder.mView.findViewById(R.id.num_products);

        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date d = new Date(dataset.get(position).getTimestamp());

        date.setText(df.format(d));
        try {
            num_products.setText(MessageFormat.format("Number of products: {0}", dataset.get(position).getNumProducts()));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        PendingOrdersAdapter.OnItemClickListener clickListener = new PendingOrdersAdapter.OnItemClickListener(position);
        holder.mView.setOnClickListener(clickListener);
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }
}