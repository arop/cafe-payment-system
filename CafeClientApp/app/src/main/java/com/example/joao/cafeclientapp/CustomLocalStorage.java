package com.example.joao.cafeclientapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Joao on 13/10/2016.
 */

public class CustomLocalStorage {

    public static final String PREFS_NAME = "Globals";
    /**
     * Cart {product_name, price, quantity}
     */
    public static ArrayList<JsonObject> currentCart;

    public static void set(Activity activity, String key, String value){
        SharedPreferences sharedPref = activity.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getString(Activity activity, String key){
        SharedPreferences sharedPref = activity.getSharedPreferences(PREFS_NAME, 0);
        return sharedPref.getString(key, null);
    }

    /**
     * Save current cart
     * @param activity
     * @param list
     */
    public static void saveCart(Activity activity, Map<Product,Integer> list)
    {
        Gson gson = new Gson();
        String json = gson.toJson(list);

        set(activity, "cart", json);
    }

    /**
     * Retrieve cart
     * @param activity
     * @return
     */
    public static Map<Product,Integer> getCart(Activity activity) {
        SharedPreferences sharedPref = activity.getSharedPreferences(PREFS_NAME, 0);

        Gson gson = new Gson();
        Map<Product,Integer> list = gson.fromJson(sharedPref.getString("cart", null), (new HashMap<Product,Integer>()).getClass());

        return list;
    }

}
