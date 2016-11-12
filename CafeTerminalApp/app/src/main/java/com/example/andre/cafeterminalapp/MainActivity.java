package com.example.andre.cafeterminalapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.andre.cafeterminalapp.order.Order;
import com.example.andre.cafeterminalapp.order.PendingOrdersActivity;

public class MainActivity extends AppCompatActivity {

    private Blacklist blacklist;
    private Activity currentActivity;
    private final int MY_PERMISSIONS_REQUEST_CAMERA = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentActivity = this;

        blacklist = Blacklist.getInstance(currentActivity);
        Order.getUnsentOrders(currentActivity);

        final Button unsentBtn = (Button) findViewById(R.id.unsentOrdersbtn);
        unsentBtn.setText(getString(R.string.pending_orders, Order.getUnsentOrders(currentActivity).size()));
        unsentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(currentActivity, PendingOrdersActivity.class));
            }
        });

        Button cameraBtn = (Button) findViewById(R.id.camerabtn);
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(currentActivity,CameraActivity.class);
                startActivity(intent);
            }
        });

        FloatingActionButton syncBtn = (FloatingActionButton) findViewById(R.id.syncBtn);
        syncBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //refresh pending orders number
                Button unsentBtn = (Button) findViewById(R.id.unsentOrdersbtn);
                unsentBtn.setText(getString(R.string.pending_orders, Order.getUnsentOrders(currentActivity).size()));

                if(!blacklist.getPendingBlacklist().isEmpty()) {
                    blacklist.sendPendingBlacklist(currentActivity);
                }
                //get blacklist from server
                else blacklist.getBlacklistFromServer(currentActivity);
            }
        });

        requestPermissions(this);
    }

    @Override
    public void onResume(){
        super.onResume();
        //refresh pending orders number
        Button unsentBtn = (Button) findViewById(R.id.unsentOrdersbtn);
        unsentBtn.setText(getString(R.string.pending_orders, Order.getUnsentOrders(currentActivity).size()));
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
