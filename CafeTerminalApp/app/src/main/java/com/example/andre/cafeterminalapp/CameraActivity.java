package com.example.andre.cafeterminalapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.example.andre.cafeterminalapp.order.Order;
import com.example.andre.cafeterminalapp.order.ShowOrderActivity;
import com.example.andre.cafeterminalapp.utils.ServerRestClient;
import com.google.zxing.Result;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class CameraActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private ZXingScannerView mScannerView;
    private Activity currentActivity;
    private Result currentScanResult;

    private ProgressDialog insertOrderRequestProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        currentActivity = this;

        insertOrderRequestProgressDialog = new ProgressDialog(this);
        insertOrderRequestProgressDialog.setTitle("Loading");
        insertOrderRequestProgressDialog.setMessage("Wait while loading...");
        insertOrderRequestProgressDialog.setCancelable(false); // disable dismiss by tapping outside of the dialog

        View view = findViewById(R.id.scannerView);
        QrScanner(view);
    }

    @Override
    public void handleResult(Result rawResult) {
        currentScanResult = rawResult;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Scan Result");
        //builder.setMessage(rawResult.getText());
        builder.setMessage("Successful scan");
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                onResume();
            }
        });
        builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    JSONObject j = new JSONObject(currentScanResult.toString());
                    sendInsertOrderToServer(j);
                } catch (JSONException e) {
                    Log.e("error","json error on sending insert order to server");
                    e.printStackTrace();
                    showWarningDialog(1,"Unable to get order!",null);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void sendInsertOrderToServer(final JSONObject result) throws JSONException {

        if(Blacklist.getInstance(this).getBlacklist().contains(result.getString("user")) ||
                Blacklist.getInstance(this).getPendingBlacklist().contains(result.getString("user"))) {
            showWarningDialog(1,"You are blacklisted!",null);
            return;
        }
        insertOrderRequestProgressDialog.show();
        HashMap<String, String> order_params = new HashMap<>();
        RequestParams order = new RequestParams();

        order_params.put("cart",result.getString("cart"));
        order_params.put("user",result.getString("user"));
        order_params.put("pin",result.getString("pin"));
        order_params.put("vouchers",result.getString("vouchers"));
        order.put("order",order_params);

        ServerRestClient.post("transaction", order, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try{
                    String error = response.get("error").toString();
                    insertOrderRequestProgressDialog.dismiss();
                    showWarningDialog(0,error,null);
                    return;
                }
                catch(JSONException e){
                    //normal behaviour when there are no errors.
                }

                try{
                    response.get("blacklist");
                    insertOrderRequestProgressDialog.dismiss();
                    showWarningDialog(1,"You are blacklisted!",null);
                    Blacklist.getInstance(currentActivity).getBlacklistFromServer(currentActivity);
                    return;
                }
                catch(JSONException e){
                    //normal behaviour when there are no errors.
                }

                Log.e("order",response.toString());
                insertOrderRequestProgressDialog.dismiss();
                try {
                    showWarningDialog(2,"Successful order",response.getJSONObject("order").toString());
                } catch (JSONException e) {
                    Log.e("error json",e.getMessage());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String error, Throwable throwable) {
                Log.e("FAILURE:", "~JSON OBJECT - status: "+statusCode);
                Log.e("FAILURE:", error);

                if(statusCode == 0) {
                    saveOrder(result);
                    insertOrderRequestProgressDialog.dismiss();
                    showWarningDialog(0,"No internet connection. Order will be performed in the future",null);
                } else {
                    insertOrderRequestProgressDialog.dismiss();
                    showWarningDialog(1,"Server not available...",null);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject object) {
                Log.e("FAILURE:", "some error I dont know how to handle. timeout?");
                Log.e("FAILURE:", "JSON OBJECT - status: "+statusCode);
                if(statusCode == 0) {
                    saveOrder(result);
                    insertOrderRequestProgressDialog.dismiss();
                    showWarningDialog(0,"No internet connection. Order will be performed in the future",null);
                } else {
                    insertOrderRequestProgressDialog.dismiss();
                    showWarningDialog(1,"Server not available...",null);
                }
            }

            private void saveOrder(JSONObject result) {
                Log.d("order","server timeout, saving order");
                Order o = new Order();
                try {
                    o.setProducts(result.getString("cart"));
                    o.setUser_id(result.getString("user"));
                    o.setUser_pin(result.getString("pin"));
                    o.setVouchers(result.getString("vouchers"));

                    Date date = new Date();
                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
                    String formattedDate = sdf.format(date);
                    o.setTimestamp(formattedDate);

                    Order.addUnsentOrder(o);
                    Order.saveUnsentOrders(currentActivity);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void QrScanner(View view) {
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();         // Start camera
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();   // Stop camera on pause
    }

    private void showWarningDialog(int type, String msg, String response) {
        AlertDialog.Builder builder = new AlertDialog.Builder(currentActivity);
        switch (type) {
            case 0: //orange warning
                builder.setTitle("Warning");
                builder.setIcon(R.drawable.ic_warning_orange_24dp);
                break;
            case 1: //red warning
                builder.setTitle("Error");
                builder.setIcon(R.drawable.ic_warning_red_24dp);
                break;
            case 2: //success
                builder.setTitle("Success");
                builder.setIcon(R.drawable.ic_check_circle_green_24dp);

                Intent intent = new Intent(this,ShowOrderActivity.class);
                intent.putExtra("order",response);
                startActivity(intent);
                break;
            default:
                break;
        }

        builder.setMessage(msg);
        builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }
}
