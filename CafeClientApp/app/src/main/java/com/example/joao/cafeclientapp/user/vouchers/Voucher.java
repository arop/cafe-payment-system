package com.example.joao.cafeclientapp.user.vouchers;

import android.app.Activity;
import android.util.Log;

import com.example.joao.cafeclientapp.CustomLocalStorage;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Joao on 02/11/2016.
 */

public class Voucher implements Serializable{

    static private ArrayList<Voucher> vouchers = null;

    private final int serialId;
    private final Byte[] signature;
    private final char type;

    public Voucher(int serial_id, Byte[] signature, char type){
        this.serialId = serial_id;
        this.signature = signature;
        this.type = type;
    }

    public ArrayList<Voucher> getVouchersInstance(Activity a){
        if(vouchers == null) {
            try {
                vouchers = CustomLocalStorage.getSavedVouchers(a);
            } catch (Exception e) {
                vouchers = new ArrayList<Voucher>();
            }
        }
        return vouchers;
    }

    public void saveVouchers(Activity a) {
        try {
            CustomLocalStorage.saveVouchers(a,vouchers);
        } catch (IOException e) {
            Log.e("vouchers","couldn't save vouchers");
        }
    }

    public int getSerialId() {
        return serialId;
    }

    public Byte[] getSignature() {
        return signature;
    }

    public char getType() {
        return type;
    }
}
