package com.example.joao.cafeclientapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.joao.cafeclientapp.cart.Cart;
import com.example.joao.cafeclientapp.menu.ProductsMenu;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
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
    public static void saveCart(Activity activity, Cart list) throws IOException {
        set(activity, "cart", SerializeToString.toString(list));
    }

    /**
     * Retrieve cart
     * @param activity
     * @return
     */
    public static Cart getCart(Activity activity) throws IOException, ClassNotFoundException {
        return (Cart) SerializeToString.fromString(CustomLocalStorage.getString(activity, "cart"));
    }

}
