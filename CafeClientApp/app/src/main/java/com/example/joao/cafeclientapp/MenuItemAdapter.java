package com.example.joao.cafeclientapp;

/**
 * Created by Joao on 13/10/2016.
 */

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static java.lang.Float.parseFloat;

public class MenuItemAdapter extends RecyclerView.Adapter<MenuItemAdapter.ViewHolder> {

    private ArrayList<JSONObject> dataset;

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


    public MenuItemAdapter(ArrayList<JSONObject> dataset){
        this.dataset = dataset;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.menu_list_item, parent, false);
        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(v);
        return vh;

    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TextView name = (TextView) holder.mView.findViewById(R.id.product_name);
        TextView price = (TextView) holder.mView.findViewById(R.id.product_price);

        try {
            name.setText(dataset.get(position).getString("name"));
            price.setText(String.format( "%.2f", parseFloat(dataset.get(position).getString("price")) )+"€");
        } catch (JSONException e) {
            Log.e("MENU RECYCLER VIEW BIND", "Error binding position: "+position);
        }

    }


    @Override
    public int getItemCount() {
        return dataset.size();
    }
}