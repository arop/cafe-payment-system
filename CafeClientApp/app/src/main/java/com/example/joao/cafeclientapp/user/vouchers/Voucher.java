package com.example.joao.cafeclientapp.user.vouchers;

import android.app.Activity;
import android.util.Log;

import com.example.joao.cafeclientapp.CustomLocalStorage;
import com.example.joao.cafeclientapp.R;
import com.example.joao.cafeclientapp.menu.Product;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Joao on 02/11/2016.
 */

public class Voucher implements Serializable{

    static private ArrayList<Voucher> vouchers = null;


    static public void setAndSaveInstance(Activity a, ArrayList<Voucher> vouchers){
        Voucher.vouchers = vouchers;
        Voucher.saveVouchers(a);
    }

    private int serialId;
    private byte[] signature;
    private char type;
    private String title;

    private int drawable; // id of vector asset

    public Voucher(int serial_id, byte[] signature, char type){
        this.serialId = serial_id;
        this.signature = signature;
        this.type = type;
        this.setTitle();
    }

    public Voucher(JSONObject jsonObject) {
        try {
            this.serialId = jsonObject.getInt("serial_id");
            this.signature = jsonObject.get("signature").toString().getBytes();
            this.type = jsonObject.get("type").toString().charAt(0);
            this.setTitle();

        } catch (JSONException e) {
            Log.e("Voucher", "Problem in voucher constructor");
            e.printStackTrace();
        }
    }

    private void setTitle(){
        switch (this.type){
            case 'd' : this.title = "5% discount"; this.drawable = R.drawable.ic_percentage_46dp; break;
            case 'p' : this.title = "1 Free Popcorn"; this.drawable = R.drawable.ic_popcorn_64dp; break;
            case 'c' : this.title = "1 Free Coffee"; this.drawable = R.drawable.ic_coffee_64dp; break;
            default: this.title = "Invalid Voucher?";
        }
    }


    public static ArrayList<Voucher> getVouchersInstance(Activity a){
        if(vouchers == null) {
            try {
                vouchers = CustomLocalStorage.getSavedVouchers(a);
            } catch (Exception e) {
                vouchers = new ArrayList<Voucher>();
            }
        }
        return vouchers;
    }

    public static void saveVouchers(Activity a) {
        try {
            CustomLocalStorage.saveVouchers(a,vouchers);
        } catch (IOException e) {
            Log.e("vouchers","couldn't save vouchers");
        }
    }

    public int getSerialId() {
        return serialId;
    }

    public byte[] getSignature() {
        return signature;
    }

    public char getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public int getDrawable() {
        return drawable;
    }
}
