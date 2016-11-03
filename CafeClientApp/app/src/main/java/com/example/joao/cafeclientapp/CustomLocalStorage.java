package com.example.joao.cafeclientapp;

import android.app.Activity;
import android.content.SharedPreferences;

import com.example.joao.cafeclientapp.cart.Cart;
import com.example.joao.cafeclientapp.user.User;
import com.example.joao.cafeclientapp.user.vouchers.Voucher;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Joao on 13/10/2016.
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

    public static User getUser(Activity activity) throws IOException, ClassNotFoundException {
        return (User) SerializeToString.fromString(CustomLocalStorage.getString(activity, "user"));
    }

    public static void saveUser(Activity activity, User u) throws IOException {
        set(activity, "user", SerializeToString.toString(u));
    }

    public static ArrayList<Voucher> getSavedVouchers(Activity activity) throws IOException, ClassNotFoundException {
        return (ArrayList<Voucher>) SerializeToString.fromString(CustomLocalStorage.getString(activity, "vouchers"));
    }

    public static void saveVouchers(Activity a, ArrayList<Voucher> vouchers) throws IOException {
        set(a, "vouchers", SerializeToString.toString(vouchers));
    }
}
