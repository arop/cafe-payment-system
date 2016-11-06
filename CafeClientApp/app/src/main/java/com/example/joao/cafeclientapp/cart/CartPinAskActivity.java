package com.example.joao.cafeclientapp.cart;

import android.content.Intent;

import com.example.joao.cafeclientapp.PinAskActivity;

/**
 * Created by Joao on 06/11/2016.
 */

public class CartPinAskActivity extends PinAskActivity{

    public void nextActivity(){
        Intent origin = this.getIntent();
        Intent intent = new Intent(this, QrCodeCheckoutActivity.class);
        intent.putParcelableArrayListExtra("vouchers", origin.getParcelableArrayListExtra("vouchers"));
        this.startActivity (intent);
        this.finish();
    }
}
