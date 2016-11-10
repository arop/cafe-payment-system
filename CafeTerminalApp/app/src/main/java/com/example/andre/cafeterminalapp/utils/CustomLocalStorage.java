package com.example.andre.cafeterminalapp.utils;

import android.app.Activity;
import android.content.SharedPreferences;

import com.example.andre.cafeterminalapp.Blacklist;
import com.example.andre.cafeterminalapp.R;
import com.example.andre.cafeterminalapp.order.Order;

import org.apache.commons.codec.binary.Base64;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by andre on 03/11/2016.
 */

public class CustomLocalStorage {

    private static final String PREFS_NAME = "Globals";

    private static void set(Activity activity, String key, String value){
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

    private static String getString(Activity activity, String key){
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

    /**
     * Get vouchers public key
     * @param a
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws IOException
     */
    public static PublicKey getPublicKey(Activity a) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {

        // reads the public key stored in a file
        InputStream is = a.getResources().openRawResource(R.raw.public_key);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        List<String> lines = new ArrayList<String>();
        String line = null;
        while ((line = br.readLine()) != null)
            lines.add(line);

        // removes the first and last lines of the file (comments)
        if (lines.size() > 1 && lines.get(0).startsWith("-----") && lines.get(lines.size()-1).startsWith("-----")) {
            lines.remove(0);
            lines.remove(lines.size()-1);
        }

        // concats the remaining lines to a single String
        StringBuilder sb = new StringBuilder();
        for (String aLine: lines)
            sb.append(aLine);
        String keyString = sb.toString();

        // converts the String to a PublicKey instance
        byte[] keyBytes = Base64.decodeBase64(keyString.getBytes("utf-8"));
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey key = keyFactory.generatePublic(spec);

        return key;
    }

}
