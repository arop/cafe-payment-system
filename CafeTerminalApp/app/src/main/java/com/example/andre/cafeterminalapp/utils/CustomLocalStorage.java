package com.example.andre.cafeterminalapp.utils;

import android.app.Activity;
import android.content.SharedPreferences;

import com.example.andre.cafeterminalapp.Blacklist;
import com.example.andre.cafeterminalapp.order.Order;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by andre on 03/11/2016.
 */

public class CustomLocalStorage {

    public static final String PREFS_NAME = "Globals";

    public static void set(Activity activity, String key, String value){
        SharedPreferences sharedPref = activity.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static void remove(Activity activity, String key){
        SharedPreferences sharedPref = activity.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(key);
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
    public static void saveBlacklist(Activity activity, Blacklist list) throws IOException {
        set(activity, "blacklist", SerializeToString.toString(list));
    }

    /**
     * Retrieve blacklist
     * @param a
     * @return
     */

    public static Blacklist getBlacklist(Activity a) throws IOException, ClassNotFoundException {
        return (Blacklist) SerializeToString.fromString(CustomLocalStorage.getString(a, "blacklist"));
    }

    /**
     * Save unsent orders
     * @param activity
     * @param list
     */
    public static void saveUnsentOrders(Activity activity, ArrayList<Order> list) throws IOException {
        set(activity, "unsentOrders", SerializeToString.toString(list));
    }

    /**
     * Retrieve unsent orders
     * @param a
     * @return
     */
    public static ArrayList<Order> getUnsentOrders(Activity a) throws IOException, ClassNotFoundException {
        return (ArrayList<Order>) SerializeToString.fromString(CustomLocalStorage.getString(a, "unsentOrders"));
    }
}
