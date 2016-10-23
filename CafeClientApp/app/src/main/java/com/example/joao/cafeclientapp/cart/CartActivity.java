package com.example.joao.cafeclientapp.cart;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.example.joao.cafeclientapp.NavigationDrawerUtils;
import com.example.joao.cafeclientapp.R;
import com.example.joao.cafeclientapp.menu.ShowMenuActivity;

public class CartActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    Cart currentCart;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Activity currentActivity;
    private Context context;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        this.currentActivity = this;
        this.context = getApplicationContext();

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

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // specify an adapter (see also next example)
        mAdapter = new CartItemAdapter(currentCart,this);
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

        navigationView.setCheckedItem(R.id.nav_cart);
        /////////////////////////////////////////////


    }

    public void refreshCartTotalPrice() {
        double val = currentCart.getTotalPrice();
        MenuItem cart_quant = menu.findItem(R.id.cart_total);
        cart_quant.setTitle(String.format( "%.2f", val )+"€");
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return NavigationDrawerUtils.onNavigationItemSelected(item, this, R.id.nav_cart);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actionbar_cart, menu);
        this.menu = menu;
        refreshCartTotalPrice();
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem actionViewItem = menu.findItem(R.id.actionButton);
        // Retrieve the action-view from menu
        View v = MenuItemCompat.getActionView(actionViewItem);
        // Find the button within action-view
        ImageButton b = (ImageButton) v.findViewById(R.id.checkout_button);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Intent is what you use to start another activity
                Intent intent = new Intent(currentActivity, PinAskActivity.class);
                currentActivity.startActivity (intent);
            }
        });
        // Handle button click here
        return super.onPrepareOptionsMenu(menu);
    }
}


/*


 */