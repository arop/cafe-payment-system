package com.example.andre.cafeterminalapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.zxing.Result;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

import cz.msebera.android.httpclient.Header;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class MainActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private ZXingScannerView mScannerView;
    private Activity currentActivity;

    private final int MY_PERMISSIONS_REQUEST_CAMERA = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        currentActivity = this;

        requestPermissions(this);

        View view = findViewById(R.id.scannerView);
        QrScanner(view);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void handleResult(Result rawResult) {
        // Do something with the result here
        Log.e("handler", rawResult.getText()); // Prints scan results
        Log.e("handler", rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode)
        // show the scanner result into dialog box.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Scan Result");
        builder.setMessage(rawResult.getText());
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                onResume();
            }
        });
        AlertDialog alert1 = builder.create();
        alert1.show();
        // If you would like to resume scanning, call this method below:
        // mScannerView.resumeCameraPreview(this);

        //TODO parse rawResult
        try {
            JSONObject j = new JSONObject(rawResult.toString());
            sendInsertOrderToServer(j);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendInsertOrderToServer(JSONObject result) throws JSONException {
        HashMap<String, String> order_params = new HashMap<>();
        RequestParams order = new RequestParams();

        //TODO transform quantity from string to int (or try to parse it in the server)
        order_params.put("cart",result.getString("cart"));
        order_params.put("user",result.getString("user"));
        order.put("order",order_params);

        ServerRestClient.post("transaction", order, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try{
                    String error = response.get("error").toString();
                    Toast.makeText(currentActivity.getApplicationContext(), error, Toast.LENGTH_LONG).show();
                    return;
                }
                catch(JSONException e){
                    //normal behaviour when there are no errors.
                }

                //Toast.makeText(currentActivity.getApplicationContext(), "Successful login!", Toast.LENGTH_LONG).show();
                Log.e("order",response.toString());

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String error, Throwable throwable){
                Log.e("FAILURE:", error);
                //showProgress(false); TODO show spinner
                Toast.makeText(currentActivity.getApplicationContext(), "Server not available...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject object) {
                Log.e("FAILURE:", "some error I dont know how to handle. timeout?");
                //showProgress(false); TODO show spinner
                Toast.makeText(currentActivity.getApplicationContext(), "Server not available...", Toast.LENGTH_SHORT).show();
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

    ////////// PERMISSIONS //////////////
    /**
     * Request permissions
     * @param thisActivity
     */
    private void requestPermissions(Activity thisActivity) {
        if (ContextCompat.checkSelfPermission(thisActivity,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(thisActivity,
                    new String[]{Manifest.permission.CAMERA},
                    MY_PERMISSIONS_REQUEST_CAMERA);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                } else {

                    // permission denied, re-ask
                    showAlertDialogOnPermissions();
                }
                return;
            }
        }
    }

    /**
     * Alert to re-ask for permissions
     */
    private void showAlertDialogOnPermissions() {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(currentActivity);
        builder.setCancelable(false);
        builder.setTitle("Error");
        builder.setMessage("This app requires camera permission to work!");
        builder.setPositiveButton("OK!!!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                requestPermissions(currentActivity);
            }
        });
        // Create the AlertDialog object and return it
        builder.create().show();
    }
}
