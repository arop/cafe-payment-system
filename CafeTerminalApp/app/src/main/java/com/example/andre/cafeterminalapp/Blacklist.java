package com.example.andre.cafeterminalapp;

import android.app.Activity;
import android.util.Log;

import com.example.andre.cafeterminalapp.utils.CustomLocalStorage;
import com.example.andre.cafeterminalapp.utils.ServerRestClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
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
    private ArrayList<String> pendingBlacklist;
    private static Blacklist instance;

    public static Blacklist getInstance(Activity a) {
        if(instance == null) {
            instance = new Blacklist();
            instance.getSavedBlacklist(a);
        }
        return instance;
    }

    private Blacklist() {
        this.blacklist = new ArrayList<>();
        this.pendingBlacklist = new ArrayList<>();
    }

    public ArrayList<String> getBlacklist() {
        return blacklist;
    }

    public ArrayList<String> getPendingBlacklist() {
        return pendingBlacklist;
    }

    private void getSavedBlacklist(Activity a) {
        try {
            instance = CustomLocalStorage.getBlacklist(a);
        } catch (Exception e) {
            instance = new Blacklist();
        }
    }

    public static void saveBlacklist(Activity a) {
        try {
            CustomLocalStorage.saveBlacklist(a,Blacklist.getInstance(a));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isBlacklisted(String user_id) {
        return (instance.blacklist.contains(user_id) || instance.pendingBlacklist.contains(user_id));
    }

    public void getBlacklistFromServer(final Activity currentActivity) {

        ServerRestClient.get("blacklist", null, new JsonHttpResponseHandler() {
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

                try {
                    JSONArray bs = response.getJSONArray("blacklist");
                    blacklist.clear();
                    for(int i = 0; i < bs.length(); i++) {
                        blacklist.add(bs.getJSONObject(i).getString("user_id"));
                    }
                    Log.e("blacklist","before save: " + blacklist.size());
                    Blacklist.saveBlacklist(currentActivity);
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

    public void sendPendingBlacklist(final Activity a) {
        RequestParams b = new RequestParams();
        b.put("blacklist",pendingBlacklist);

        ServerRestClient.post("blacklist", b, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try{
                    response.get("error").toString();
                    return;
                }
                catch(JSONException e){
                    //normal behaviour when there are no errors.
                }

                Log.e("inserted",response.toString());
                try {
                    //if successful remove from unsent
                    JSONArray toRemove = response.getJSONArray("inserted");
                    for(int i = 0; i < toRemove.length(); i++) {
                        pendingBlacklist.remove(toRemove.get(i));
                    }

                    getBlacklistFromServer(a);
                } catch (JSONException e) {
                    Log.e("error json",e.getMessage());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String error, Throwable throwable) {
                Log.e("FAILURE:", "~JSON OBJECT - status: "+statusCode);
                Log.e("FAILURE:", error);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject object) {
                Log.e("FAILURE:", "some error I dont know how to handle. timeout?");
                Log.e("FAILURE:", "JSON OBJECT - status: "+statusCode);
            }
        });
    }

}
