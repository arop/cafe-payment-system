package com.example.joao.cafeclientapp.user.vouchers;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.joao.cafeclientapp.R;

import java.util.ArrayList;

/**
 * Created by Joao on 02/11/2016.
 */

public class VoucherItemAdapter extends RecyclerView.Adapter<VoucherItemAdapter.ViewHolder>  {

    private final Activity mActivity;
    private ArrayList<Voucher> dataset;

    public VoucherItemAdapter(ArrayList<Voucher> vouchers, Activity a) {
        this.dataset = vouchers;
        this.mActivity = a;
    }

    public void setDataset(ArrayList<Voucher> dataset) {
        this.dataset = dataset;
        notifyDataSetChanged();
    }

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


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.vouchers_list_item, parent, false);
        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        TextView titleView = (TextView) holder.mView.findViewById(R.id.voucher_title);
        ImageView imageView = (ImageView) holder.mView.findViewById(R.id.voucher_image);

        final String title= dataset.get(position).getTitle();

        titleView.setText(title);
        imageView.setImageResource(dataset.get(position).getDrawable());
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }

}
