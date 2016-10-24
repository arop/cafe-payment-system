package com.example.joao.cafeclientapp.menu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.joao.cafeclientapp.CustomLocalStorage;
import com.example.joao.cafeclientapp.NavigationDrawerUtils;
import com.example.joao.cafeclientapp.R;
import com.example.joao.cafeclientapp.SerializeToString;
import com.example.joao.cafeclientapp.ServerRestClient;
import com.example.joao.cafeclientapp.cart.Cart;
import com.example.joao.cafeclientapp.cart.CartActivity;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

public class ShowMenuActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private Context context;
    private static Activity currentActivity;
    private ProductsMenu list;

    private RecyclerView mRecyclerView;
    private MenuItemAdapter mRecyclerAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private SwipeRefreshLayout swipeContainer;
    private Menu menu;

    private Cart currentCart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_menu);

        context = getApplicationContext();
        currentActivity = this;

        try {
            list = (ProductsMenu) SerializeToString.fromString(CustomLocalStorage.getString(currentActivity, "menu"));
        } catch (Exception e){
            list = new ProductsMenu();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRecyclerView = (RecyclerView) findViewById(R.id.menu_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mRecyclerAdapter = new MenuItemAdapter(list, currentActivity);
        mRecyclerView.setAdapter(mRecyclerAdapter);

        currentCart = Cart.getInstance(this);

        ///////////////////////////////////////////
        ////////// SETUP SWIPE REFRESH ////////////
        // Lookup the swipe container view
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshMenuContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                fetchProductsAsync();
            }
        });

        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        ///////////////////////////////////////////


        /////////////////////////////////////////////
        ///////////// NAVIGATION DRAWER /////////////

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.setCheckedItem(R.id.nav_menu);
        NavigationDrawerUtils.setUser(navigationView, this);
        /////////////////////////////////////////////

        fetchProductsAsync();
        swipeContainer.setRefreshing(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actionbar_show_menu, menu);
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
        ImageButton b = (ImageButton) v.findViewById(R.id.cart_button);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Intent is what you use to start another activity
                Intent myIntent = new Intent(context, CartActivity.class);
                startActivity(myIntent);
            }
        });
        // Handle button click here
        return super.onPrepareOptionsMenu(menu);
    }

    // Clean all elements of the recycler
    public void clearList() {
        mRecyclerAdapter.clearList();
        //notifyDataSetChanged();
    }

    // Add a list of items
    public void refreshAdapterData() {
        mRecyclerAdapter.addAll(list);
        //notifyDataSetChanged();
    }

    public void fetchProductsAsync(){

        String menu_version = CustomLocalStorage.getString(currentActivity,"menu_version");
        if(menu_version == null) menu_version = "0";
        RequestParams version = new RequestParams();
        version.put("version",menu_version);

        ServerRestClient.get("menu", version, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if(statusCode == 204) { //Menu up to date
                    Log.d("success", "menu up-to-date!");
                    swipeContainer.setRefreshing(false);
                } else {
                    Log.d("success", "got menu");
                    try {
                        JSONArray menu = (JSONArray) response.get("menu");
                        HashMap<Integer, Product> new_menu = new HashMap<>();

                        for (int i = 0; i < menu.length(); ++i) {
                            Product p = new Product(menu.getJSONObject(i));
                            new_menu.put(p.getId(), p);
                        }

                        list.setProducts(new_menu);
                        refreshAdapterData();

                        //Save menu to memory
                        CustomLocalStorage.set(currentActivity, "menu", SerializeToString.toString(list));
                        String menu_version = response.getString("version");
                        CustomLocalStorage.set(currentActivity, "menu_version", menu_version);
                    } catch (JSONException e) {
                        //problem with server. probably.
                        Toast.makeText(currentActivity.getApplicationContext(), "Server error...", Toast.LENGTH_SHORT).show();
                    } catch (IOException io) {
                        //error serializing menu object
                        Log.e("Serialize", "Couldn't save Menu to memory");
                    } finally {
                        swipeContainer.setRefreshing(false);
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String error, Throwable throwable){
                Log.e("FAILURE:", error);
                Toast.makeText(context, "Server not available...", Toast.LENGTH_SHORT).show();
                swipeContainer.setRefreshing(false);
            }
        });
    }

    public void refreshCartTotalPrice() {
        double val = currentCart.getTotalPrice();
        MenuItem cart_quant = menu.findItem(R.id.cart_quantity_show_menu);
        if (val == 0){
            cart_quant.setTitle("");
        }
        else{
            cart_quant.setTitle(String.format( "%.2f", val )+"€");
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return NavigationDrawerUtils.onNavigationItemSelected(item, this, R.id.nav_menu);
    }
}
