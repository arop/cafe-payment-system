package com.example.joao.cafeclientapp;

/**
 * Created by Joao on 13/10/2016.
 */

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MenuItemAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<JSONObject> mDataSource;

    public MenuItemAdapter(Context context, ArrayList<JSONObject> items) {
        mContext = context;
        mDataSource = items;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    //1
    @Override
    public int getCount() {
        return mDataSource.size();
    }

    //2
    @Override
    public Object getItem(int position) {
        return mDataSource.get(position);
    }

    //3
    @Override
    public long getItemId(int position) {
        return position;
    }

    //4
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get view for row item
        View rowView = mInflater.inflate(R.layout.menu_list_item, parent, false);
        TextView product_name = (TextView) rowView.findViewById(R.id.product_name);
        TextView product_price = (TextView) rowView.findViewById(R.id.product_price);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        try {
            product_name.setText(mDataSource.get(position).get("name").toString());
            product_price.setText(mDataSource.get(position).get("price").toString());
            Log.i("name", position+"#"+mDataSource.get(position).get("name").toString());
            //imageView.setImageResource(R.drawable.no);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return rowView;
    }

}