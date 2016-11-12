package com.example.andre.cafeterminalapp.order;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.example.andre.cafeterminalapp.R;

public class PendingOrdersActivity extends AppCompatActivity {
    Activity currentActivity;
    PendingOrdersAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_orders);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Pending orders");

        currentActivity = this;

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Sending pending orders to server", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                Order.sendUnsentOrders(currentActivity);
                //mAdapter.addAll(Order.getUnsentOrders(currentActivity));
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setRecyclerView();
    }

    private void setRecyclerView() {
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.pending_orders_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new PendingOrdersAdapter(Order.getUnsentOrders(currentActivity),this);
        mRecyclerView.setAdapter(mAdapter);
    }
}
