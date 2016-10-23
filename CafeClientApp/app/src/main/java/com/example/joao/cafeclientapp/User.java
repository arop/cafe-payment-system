package com.example.joao.cafeclientapp;

import android.app.Activity;
import android.content.Intent;

import com.example.joao.cafeclientapp.authentication.LoginActivity;
/**
 * Created by Joao on 23/10/2016.
 */

public class User {

    public static void logout(Activity activity){
        CustomLocalStorage.remove(activity, "uuid");
        CustomLocalStorage.remove(activity, "pin");
        Intent myIntent = new Intent(activity.getApplicationContext(), LoginActivity.class);
        activity.startActivity(myIntent);
        activity.finish();
    }
}
