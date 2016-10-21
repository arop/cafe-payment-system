package com.example.joao.cafeclientapp.cart;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.example.joao.cafeclientapp.R;
import com.example.joao.cafeclientapp.menu.ShowMenuActivity;

public class CartActivity extends AppCompatActivity {
    Cart currentCart;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Activity currentActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        this.currentActivity = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Cart");

        mRecyclerView = (RecyclerView) findViewById(R.id.cart_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        currentCart = Cart.getInstance(this);

        /**
         * TODO use to checkout
         */
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Snackbar.make(view, "This will be the checkout button", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                Intent intent = new Intent(currentActivity, QrCodeCheckoutActivity.class);
                currentActivity.startActivity (intent);
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // specify an adapter (see also next example)
        mAdapter = new CartItemAdapter(currentCart,this);
        mRecyclerView.setAdapter(mAdapter);
    }
}
