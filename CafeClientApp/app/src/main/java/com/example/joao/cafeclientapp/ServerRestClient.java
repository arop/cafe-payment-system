package com.example.joao.cafeclientapp;

/**
 * Created by Joao on 13/10/2016.
 */

import com.loopj.android.http.*;

public class ServerRestClient {

    private static final String BASE_URL = "http://cmov-nodejs-server.herokuapp.com/";
    //private static final String BASE_URL = "http://192.168.2.117:5000/";
    //private static final String BASE_URL = "http://172.30.11.233:5000/";

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }

}