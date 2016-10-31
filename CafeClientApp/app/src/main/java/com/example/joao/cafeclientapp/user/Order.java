package com.example.joao.cafeclientapp.user;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.example.joao.cafeclientapp.menu.Product;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by norim on 24/10/2016.
 */

public class Order implements Parcelable{
    private int id;
    private long timestamp;
    private ArrayList<Product> items;
    private float totalPrice = 0;

    public Order(JSONObject jsonObject) {
        try {
            id = Integer.parseInt(jsonObject.get("order_id").toString());
            timestamp = Long.parseLong(jsonObject.get("timestamp").toString());
            JSONArray order_items = (JSONArray) jsonObject.get("products");
            items = new ArrayList<Product>();

            for (int i = 0; i < order_items.length(); ++i) {
                Product p = new Product(order_items.getJSONObject(i));
                items.add(p);
                totalPrice += p.getPrice()*p.getQuantity();
            }
        } catch (JSONException e) {
            Log.e("ORDER", "Problem in order constructor");
            e.printStackTrace();
        }
    }


    public int getId() {
        return id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public ArrayList<Product> getItems() {
        return items;
    }

    public float getTotalPrice() {
        return totalPrice;
    }

    public String getFormatedDate(){
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        Date d = new Date(timestamp);
        return df.format(d);
    }

    public String getFormatedHour(){
        DateFormat df = new SimpleDateFormat("HH:mm");
        Date d = new Date(timestamp);
        return df.format(d);
    }


    ////////////////////////////////////
    //////////// PARCELABLE ////////////

    public Order (Parcel in){
        id = in.readInt();
        timestamp = in.readLong();
        items = new ArrayList<Product>();
        in.readList(items, Order.class.getClassLoader());
        totalPrice = in.readFloat();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeLong(timestamp);
        dest.writeList(items);
        dest.writeFloat(totalPrice);
    }

    public static final Creator<Order> CREATOR = new Creator<Order>() {
        @Override
        public Order createFromParcel(Parcel in) {
            return new Order(in);
        }

        @Override
        public Order[] newArray(int size) {
            return new Order[size];
        }
    };
}
