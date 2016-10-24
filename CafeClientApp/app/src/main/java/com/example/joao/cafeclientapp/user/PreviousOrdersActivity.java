package com.example.joao.cafeclientapp.user;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.example.joao.cafeclientapp.NavigationDrawerUtils;
import com.example.joao.cafeclientapp.R;
import com.example.joao.cafeclientapp.cart.Cart;
import com.example.joao.cafeclientapp.cart.CartItemAdapter;

import java.util.ArrayList;

public class PreviousOrdersActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Activity currentActivity;
    private Context context;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;

    private ArrayList<Order> orders;
    private PreviousOrderItemAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_previous_orders);

        this.currentActivity = this;
        this.context = getApplicationContext();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Orders");

        mRecyclerView = (RecyclerView) findViewById(R.id.previous_orders_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        this.orders = new ArrayList<Order>();

        // specify an adapter (see also next example)
        mAdapter = new PreviousOrderItemAdapter(orders,this);
        mRecyclerView.setAdapter(mAdapter);

        /////////////////////////////////////////////
        ///////////// NAVIGATION DRAWER /////////////

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.setCheckedItem(R.id.nav_orders);
        NavigationDrawerUtils.setUser(navigationView, this);
        /////////////////////////////////////////////
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return NavigationDrawerUtils.onNavigationItemSelected(item, this, R.id.nav_orders);
    }
}
