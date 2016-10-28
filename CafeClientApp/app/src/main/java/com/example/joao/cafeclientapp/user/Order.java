package com.example.joao.cafeclientapp.user;

import android.util.Log;

import com.example.joao.cafeclientapp.menu.Product;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by norim on 24/10/2016.
 */

public class Order {
    private int id;
    private String timestamp;
    private ArrayList<Product> items;
    private float totalPrice = 0;

    public Order(JSONObject jsonObject) {
        try {
            id = Integer.parseInt(jsonObject.get("order_id").toString());
            timestamp = jsonObject.get("timestamp").toString();
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

    public String getTimestamp() {
        return timestamp;
    }

    public ArrayList<Product> getItems() {
        return items;
    }

    public float getTotalPrice() {
        return totalPrice;
    }
}
