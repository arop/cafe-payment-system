package com.example.andre.cafeterminalapp.order;

import android.app.Activity;

import com.example.andre.cafeterminalapp.utils.CustomLocalStorage;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

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
}
