package com.example.andre.cafeterminalapp;

import android.app.Activity;
import android.util.Log;

import com.example.andre.cafeterminalapp.utils.CustomLocalStorage;
import com.example.andre.cafeterminalapp.utils.ServerRestClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

/**
 * Created by andre on 03/11/2016.
 */

public class Blacklist implements Serializable {
    /**
     * String is the user id
     */
    private ArrayList<String> blacklist;
    private static Blacklist instance;

    public static Blacklist getInstance(Activity a) {
        if(instance == null)
            instance = new Blacklist();
        instance.getSavedBlacklist(a);
        return instance;
    }

    private Blacklist() {
        this.blacklist = new ArrayList<>();
    }

    public ArrayList<String> getBlacklist() {
        return blacklist;
    }

    private void getSavedBlacklist(Activity a) {
        try {
            instance = CustomLocalStorage.getBlacklist(a);
        } catch (Exception e) {
            instance = new Blacklist();
        }
    }

    public void getBlacklistFromServer() {

        ServerRestClient.get("/blacklist", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try{
                    String error = response.get("error").toString();
                    Log.e("error", error);
                    return;
                }
                catch(JSONException e){
                    //normal behaviour when there are no errors.
                }

                Log.e("blacklist",response.toString());
                try {
                    response.get("blacklist");
                    // TODO transform this into the arraylist
                } catch (JSONException e) {
                    Log.e("error json",e.getMessage());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String error, Throwable throwable){
                Log.e("FAILURE:", error);
                //TODO make toast
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject object) {
                Log.e("FAILURE:", "some error I dont know how to handle. timeout?");
                //TODO make toast
            }
        });
    }
}
