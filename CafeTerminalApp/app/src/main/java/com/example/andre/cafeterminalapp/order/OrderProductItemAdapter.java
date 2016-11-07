package com.example.andre.cafeterminalapp.order;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.andre.cafeterminalapp.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by andre on 31/10/2016.
 */
public class OrderProductItemAdapter extends RecyclerView.Adapter<OrderProductItemAdapter.ViewHolder> {

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

    public OrderProductItemAdapter(JSONArray products, Activity a){
        //convert Hash Map to Array List
        this.dataset = products;
        this.mActivity = a;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.order_product_item, parent, false);
        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        TextView nameView = (TextView) holder.mView.findViewById(R.id.product_name);
        TextView quantityView = (TextView) holder.mView.findViewById(R.id.product_quantity);
        TextView unitPriceView = (TextView) holder.mView.findViewById(R.id.product_unit_price);
        TextView totalPriceView = (TextView) holder.mView.findViewById(R.id.product_total_price);

        JSONObject order_item = null;
        try {
            order_item = dataset.getJSONObject(position);

            nameView.setText(order_item.getString("name"));
            quantityView.setText(""+order_item.getInt("quantity"));
            unitPriceView.setText(String.format( "%.2f", order_item.getDouble("unit_price") )+"€");
            totalPriceView.setText(String.format( "%.2f", order_item.getDouble("unit_price") * order_item.getInt("quantity") )+"€");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return dataset.length();
    }

}
