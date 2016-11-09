package com.example.joao.cafeclientapp.user.vouchers;

import android.app.Activity;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.example.joao.cafeclientapp.CustomLocalStorage;
import com.example.joao.cafeclientapp.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Joao on 02/11/2016.
 */

public class Voucher implements Serializable, Parcelable{

    static private ArrayList<Voucher> vouchers = null;


    static public void setAndSaveInstance(Activity a, ArrayList<Voucher> vouchers){
        Voucher.vouchers = vouchers;
        Voucher.saveVouchers(a);
    }

    private Integer serial_id;
    private byte[] signature;
    private char type;
    private String title;

    private int drawable; // id of vector asset

    public Voucher(int serial_id, byte[] signature, char type){
        this.serial_id = serial_id;
        this.signature = signature;
        this.type = type;
        this.setTitle();
    }

    public Voucher(JSONObject jsonObject) {
        try {
            this.serial_id = jsonObject.getInt("serial_id");

            byte[] sign = new byte[46];
            JSONArray sign_bytes = jsonObject.getJSONObject("signature").getJSONArray("data");
            Log.d("array size", sign_bytes.length()+"");
            for(int i = 0; i < sign_bytes.length(); i++){
                sign[i] = ((Integer) (sign_bytes.getInt(i) + 256)).byteValue();
            }
            this.signature = sign;
            this.type = jsonObject.get("type").toString().charAt(0);
            this.setTitle();

            Log.d("sign 0", this.signature[0]+"");

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
        return serial_id;
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

    ////////////////////////////////////
    //////////// PARCELABLE ////////////

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(serial_id);
        dest.writeInt(signature.length);
        dest.writeByteArray(signature);
        dest.writeString(""+type);
        dest.writeString(title);
        dest.writeInt(drawable);
    }

    public Voucher (Parcel in){
        serial_id = in.readInt();
        signature = new byte[in.readInt()];
        in.readByteArray(signature);
        type = in.readString().charAt(0);
        title = in.readString();
        drawable = in.readInt();
    }

    public static final Creator<Voucher> CREATOR = new Creator<Voucher>() {
        @Override
        public Voucher createFromParcel(Parcel in) {
            return new Voucher(in);
        }

        @Override
        public Voucher[] newArray(int size) {
            return new Voucher[size];
        }
    };
}
