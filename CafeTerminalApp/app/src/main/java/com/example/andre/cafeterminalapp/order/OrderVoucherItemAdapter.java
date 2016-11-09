package com.example.andre.cafeterminalapp.order;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.andre.cafeterminalapp.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by andre on 31/10/2016.
 */
public class OrderVoucherItemAdapter extends RecyclerView.Adapter<OrderVoucherItemAdapter.ViewHolder> {

    private final Activity mActivity;
    private JSONArray dataset;

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

    public OrderVoucherItemAdapter(JSONArray vouchers, Activity a){
        //convert Hash Map to Array List
        this.dataset = vouchers;
        this.mActivity = a;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.order_voucher_item, parent, false);
        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        //type (p,c,d)
        TextView titleView = (TextView) holder.mView.findViewById(R.id.voucher_title);
        ImageView imageView = (ImageView) holder.mView.findViewById(R.id.voucher_image);

        try {
            JSONObject voucher = dataset.getJSONObject(position);

            String type = voucher.getString("type");
            switch (type) {
                case "d" :
                    titleView.setText("5% discount");
                    imageView.setImageResource(R.drawable.ic_percentage_46dp);
                    break;
                case "p" :
                    titleView.setText("1 Free Popcorn");
                    imageView.setImageResource(R.drawable.ic_popcorn_64dp);
                    break;
                case "c" :
                    titleView.setText("1 Free Coffee");
                    imageView.setImageResource(R.drawable.ic_coffee_64dp);
                    break;
                default:
                    titleView.setText("Invalid Voucher?");
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return dataset.length();
    }

}
