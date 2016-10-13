package com.example.joao.cafeclientapp;

import android.app.Activity;
import android.content.SharedPreferences;

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

    public static String getString(Activity activity, String key){
        SharedPreferences sharedPref = activity.getSharedPreferences(PREFS_NAME, 0);
        return sharedPref.getString(key, null);
    }


}
