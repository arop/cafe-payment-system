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
import com.example.andre.cafeterminalapp.utils.CustomLocalStorage;
import com.example.andre.cafeterminalapp.utils.ServerRestClient;
import com.google.zxing.Result;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
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
                    //Log.d("qr_text", currentScanResult.toString());
                    //JSONObject j = new JSONObject(decompress(currentScanResult.getText()));
                    JSONObject j = new JSONObject(currentScanResult.toString());
                    sendInsertOrderToServer(j);
                } catch (Exception e) {
                    Log.e("error","json error on sending insert order to server");
                    e.printStackTrace();
                    showWarningDialog("error","Unable to get order!",null);
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
            showWarningDialog("error","You are blacklisted!",null);
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
                    showWarningDialog("warning",error,null);
                    return;
                }
                catch(JSONException e){
                    //normal behaviour when there are no errors.
                }

                try{
                    response.get("blacklist");
                    insertOrderRequestProgressDialog.dismiss();
                    showWarningDialog("error","You are blacklisted!",null);
                    Blacklist.getInstance(currentActivity).getBlacklistFromServer(currentActivity);
                    return;
                }
                catch(JSONException e){
                    //normal behaviour when there are no errors.
                }

                Log.e("order",response.toString());
                insertOrderRequestProgressDialog.dismiss();
                try {
                    showWarningDialog("success","Successful order",response.getJSONObject("order").toString());
                } catch (JSONException e) {
                    Log.e("error json",e.getMessage());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String error, Throwable throwable) {
                Log.e("FAILURE:", "~JSON OBJECT - status: "+statusCode);
                Log.e("FAILURE:", error);
                handleOnFailure(statusCode);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject object) {
                Log.e("FAILURE:", "some error I dont know how to handle. timeout?");
                Log.e("FAILURE:", "JSON OBJECT - status: "+statusCode);
                handleOnFailure(statusCode);
            }

            private Integer saveOrder(JSONObject result) {
                Log.d("order","server timeout, saving order");
                try {
                    // CHECK vouchers validity
                    if(result.has("vouchers") && !checkVouchersValidity(result.getJSONArray("vouchers"))) {
                        Blacklist.getInstance(currentActivity).getPendingBlacklist().add(result.getString("user"));
                        Blacklist.saveBlacklist(currentActivity);
                        return 0;
                    } else {
                        Order o = new Order();
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
                        return 1;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return 2;
                }
            }

            private void handleOnFailure(int statusCode) {
                if(statusCode == 0) {
                    int resultOfSave = saveOrder(result);
                    insertOrderRequestProgressDialog.dismiss();
                    switch (resultOfSave) {
                        case 0: //user blacklisted
                            showWarningDialog("error","Vouchers not valid! User was blacklisted!",null);
                            break;
                        case 1: //valid vouchers, order saved
                            showWarningDialog("warning","No internet connection. Order will be performed in the future",null);
                            break;
                        default: //exception!
                            showWarningDialog("error","Something went wrong!",null);
                            break;
                    }
                } else {
                    insertOrderRequestProgressDialog.dismiss();
                    showWarningDialog("error","Server not available...",null);
                }
            }
        });
    }

    public boolean checkVouchersValidity(JSONArray vouchers) throws JSONException, NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException, IOException, SignatureException, InvalidKeyException {

        Log.d("VOUCHER", "gonna check vouchers");
        for(int i = 0; i < vouchers.length(); i++){
            JSONObject voucher = vouchers.getJSONObject(i);

            // GET signature as byte[]
            byte[] sign = new byte[46];
            JSONArray sign_bytes = voucher.getJSONArray("signature");
            for(int j = 0; j < sign_bytes.length(); j++){
                sign[j] = ((Integer) sign_bytes.getInt(j)).byteValue();
            }

            // VERIFY signature
            Signature signature1 = Signature.getInstance("SHA1withRSA");
            signature1.initVerify(CustomLocalStorage.getPublicKey(currentActivity));
            signature1.update(voucher.getString("serial_id").getBytes());
            boolean verify_result = signature1.verify(sign);

            if(verify_result){
                Log.d("VOUCHER", "RSA VOUCHER BOM!!");
            }
            else{
                Log.d("VOUCHER", "RSA VOUCHER MAU!!");
                return false;
            }

        }
        return true;
    }

    /*public String decompress(String str) throws IOException{
        if (str == null || str.length() == 0) {
            return str;
        }
        //ByteArrayInputStream in = new ByteArrayInputStream(Base64.decode(str, Base64.DEFAULT));
        ByteArrayInputStream in = new ByteArrayInputStream(str.getBytes("ISO-8859-1"));
        GZIPInputStream gzip = new GZIPInputStream(in);
        BufferedReader bf = new BufferedReader(new InputStreamReader(gzip, "UTF-8"));
        String outStr = "";
        String line;
        while ((line=bf.readLine())!=null) {
            outStr += line;
        }
        return outStr;
    }*/

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

    private void showWarningDialog(String type, String msg, String response) {
        AlertDialog.Builder builder = new AlertDialog.Builder(currentActivity);
        switch (type) {
            case "warning": //orange warning
                builder.setTitle("Warning");
                builder.setIcon(R.drawable.ic_warning_orange_24dp);
                break;
            case "error": //red warning
                builder.setTitle("Error");
                builder.setIcon(R.drawable.ic_warning_red_24dp);
                break;
            case "success": //success
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
