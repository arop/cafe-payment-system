package com.example.joao.cafeclientapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.in;

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
    public static void saveCart(Activity activity, Map<String,Double> list)
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
    public static Map<String,Double> getCart(Activity activity) {

        SharedPreferences sharedPref = activity.getSharedPreferences(PREFS_NAME, 0);
        String cartJson = sharedPref.getString("cart", null);

        Gson gson = new Gson();
        Map<String,Double> list;

        if(cartJson != null){
            list = gson.fromJson(cartJson, (new HashMap<String,Double>()).getClass());
        }
        else list = new HashMap<String, Double>();

        /*Map<Product,Integer> list = new HashMap<>();
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove("cart");*/

        for (Map.Entry<String, Double> entry : list.entrySet())
        {
            Log.i(entry.getKey()+"", ""+entry.getValue());
        }
        return list;
    }

}
