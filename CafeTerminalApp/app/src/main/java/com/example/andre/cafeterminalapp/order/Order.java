package com.example.andre.cafeterminalapp.order;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.andre.cafeterminalapp.Blacklist;
import com.example.andre.cafeterminalapp.utils.CustomLocalStorage;
import com.example.andre.cafeterminalapp.utils.ServerRestClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import cz.msebera.android.httpclient.Header;

/**
 * Created by andre on 07/11/2016.
 * Class used to store orders when no internet connection is available
 */

public class Order implements Serializable {
    private String products;
    private String user_id, user_pin;
    private String vouchers;
    private String timestamp;

    private static ArrayList<Order> unsentOrders;

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getVouchers() {
        return vouchers;
    }

    public void setVouchers(String vouchers) {
        this.vouchers = vouchers;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUser_pin() {
        return user_pin;
    }

    public void setUser_pin(String user_pin) {
        this.user_pin = user_pin;
    }

    public String getProducts() {
        return products;
    }

    public void setProducts(String ps) {
        this.products = ps;
    }

    ////////////////////////////////////////
    ////////// UNSENT ORDERS //////////////
    public static ArrayList<Order> getUnsentOrders(Activity a) {
        Order.getSavedUnsentOrders(a);
        return unsentOrders;
    }

    private static void getSavedUnsentOrders(Activity a) {
        try {
            unsentOrders = CustomLocalStorage.getUnsentOrders(a);
        } catch (Exception e) {
            unsentOrders = new ArrayList<>();
        }
    }

    public static void saveUnsentOrders(Activity a) {
        try {
            CustomLocalStorage.saveUnsentOrders(a, Order.unsentOrders);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addUnsentOrder(Order o) {
        unsentOrders.add(o);
    }

    static void sendUnsentOrders(Activity a) {
        for (Order o: unsentOrders) {
            if(Blacklist.isBlacklisted(o.getUser_id())) {
                unsentOrders.remove(o);
            }
            else sendUnsentOrder(o, a, false);
        }
    }

    static void sendUnsentOrder(final Order o, final Activity a, final boolean isSingle) {
        HashMap<String, String> order_params = new HashMap<>();
        RequestParams order = new RequestParams();

        order_params.put("cart",o.getProducts());
        order_params.put("user",o.getUser_id());
        order_params.put("pin",o.getUser_pin());
        order_params.put("vouchers",o.getVouchers());
        order_params.put("timestamp",o.getTimestamp());
        order.put("order",order_params);

        ServerRestClient.post("transaction", order, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try{
                    response.get("error");
                    return;
                }
                catch(JSONException e){
                    //normal behaviour when there are no errors.
                }
                try {
                    response.get("blacklist");
                    Toast.makeText(a, "Order successfull, user was blacklisted!", Toast.LENGTH_SHORT).show();
                    Blacklist.getInstance(a).getBlacklistFromServer(a);
                }catch (JSONException e) {
                    //normal behaviour when there are no errors.
                    Toast.makeText(a, "Order successfull", Toast.LENGTH_SHORT).show();
                }

                try {
                    unsentOrders.remove(o);
                    Order.saveUnsentOrders(a);
                    ((PendingOrdersActivity) a).mAdapter.notifyDataSetChanged();

                    if(isSingle) {
                        Intent intent = new Intent(a, ShowOrderActivity.class);
                        intent.putExtra("order", response.getJSONObject("order").toString());
                        a.startActivity(intent);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String error, Throwable throwable) {
                Log.e("FAILURE:", "~JSON OBJECT - status: "+statusCode);
                Log.e("FAILURE:", error);

                //error: order stays in unsent orders
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject object) {
                Log.e("FAILURE:", "some error I dont know how to handle. timeout?");
                Log.e("FAILURE:", "JSON OBJECT - status: "+statusCode);

                //error: order stays in unsent orders
            }
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Order order = (Order) o;

        if (products != null ? !products.equals(order.products) : order.products != null)
            return false;
        if (!user_id.equals(order.user_id))
            return false;
        if (!user_pin.equals(order.user_pin))
            return false;
        if (vouchers != null ? !vouchers.equals(order.vouchers) : order.vouchers != null)
            return false;
        return timestamp.equals(order.timestamp);
    }

    public int getNumProducts() throws JSONException {
        JSONObject prods_array = new JSONObject(products);
        int total = 0;
        Iterator<?> keys = prods_array.keys();
        while( keys.hasNext() ) {
            String key = (String) keys.next();
            total += prods_array.getInt(key);
        }
        return total;
    }
}
