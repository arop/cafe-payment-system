package com.example.joao.cafeclientapp.user.orders;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.joao.cafeclientapp.NavigationDrawerUtils;
import com.example.joao.cafeclientapp.R;
import com.example.joao.cafeclientapp.ServerRestClient;
import com.example.joao.cafeclientapp.user.User;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

import cz.msebera.android.httpclient.Header;

public class PreviousOrdersActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Activity currentActivity;
    private Context context;

    private ArrayList<Order> orders;
    private PreviousOrderItemAdapter mAdapter;
    private SwipeRefreshLayout swipeContainer;

    private int previousTotal = 0;
    private boolean loading = true;
    private int visibleThreshold = 5;
    int firstVisibleItem, visibleItemCount, totalItemCount;
    int offset = 0;
    private boolean stopRequest = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_previous_orders);

        this.currentActivity = this;
        this.context = getApplicationContext();

        this.orders = new ArrayList<>();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Orders");

        setRecyclerView();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ///////////////////////////////////////////
        ////////// SETUP SWIPE REFRESH ////////////
        // Lookup the swipe container view
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshOrdersContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                offset = 0;
                stopRequest = false;
                loading = true;
                previousTotal = 0;
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                fetchOrdersAsync(offset);
            }
        });

        /////////////////////////////////////////////
        ///////////// NAVIGATION DRAWER /////////////

        setNavigationDrawer(toolbar);
        /////////////////////////////////////////////

        swipeContainer.setRefreshing(true);
        fetchOrdersAsync(offset);
    }

    private void setRecyclerView() {
        final RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.previous_orders_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new PreviousOrderItemAdapter(orders,this);
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                visibleItemCount = mRecyclerView.getChildCount();
                totalItemCount = mLayoutManager.getItemCount();
                firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();

                if (loading) {
                    if (totalItemCount > previousTotal) {
                        loading = false;
                        previousTotal = totalItemCount;
                    }
                }
                if (!stopRequest && !loading && (totalItemCount - visibleItemCount)
                        <= (firstVisibleItem + visibleThreshold)) {
                    // End has been reached
                    // Do something
                    offset++;
                    fetchOrdersAsync(offset);
                    loading = true;
                }
            }
        });

    }

    private void setNavigationDrawer(Toolbar toolbar) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.setCheckedItem(R.id.nav_orders);
        NavigationDrawerUtils.setUser(navigationView, this);
    }

    public void fetchOrdersAsync(final int offset){
        RequestParams params = new RequestParams();

        HashMap<String, String> user_params = new HashMap<>();
        user_params.put("id", User.getInstance(this).getUuid());
        user_params.put("pin", User.getInstance(this).getPin());
        params.put("user",user_params);
        Log.d("user", user_params.toString());

        params.put("offset", offset);

        ServerRestClient.post("pasttransactions", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                Log.d("success", "got orders");
                Log.d("response", response.toString());
                try {
                    JSONObject orders_response = (JSONObject) response.get("orders");

                    if(offset == 0)
                        orders = new ArrayList<>();

                    Iterator<?> keys = orders_response.keys();
                    if(!keys.hasNext())
                        stopRequest = true;
                    while( keys.hasNext() ) {
                        String key = (String)keys.next();
                        Order o = new Order((JSONObject) orders_response.get(key));
                        orders.add(o);
                    }

                    // Sorting
                    Collections.sort(orders, new Comparator<Order>() {
                        @Override
                        public int compare(Order order2, Order order1)
                        {
                            return Long.compare(order1.getTimestamp(),order2.getTimestamp());
                        }
                    });

                    refreshAdapterData();

                } catch (Exception e) {
                    //problem with server. probably.
                    e.printStackTrace();
                    Toast.makeText(currentActivity.getApplicationContext(), "Server error...", Toast.LENGTH_SHORT).show();
                } finally {
                    swipeContainer.setRefreshing(false);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String error, Throwable throwable){
                Log.e("FAILURE:", error);
                Toast.makeText(context, "Server not available...", Toast.LENGTH_SHORT).show();
                swipeContainer.setRefreshing(false);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject json){
                Log.e("server error" , json.toString());
                swipeContainer.setRefreshing(false);
            }
        });
    }

    private void refreshAdapterData() {
        mAdapter.addAll(orders);
        //notifyDataSetChanged();
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return NavigationDrawerUtils.onNavigationItemSelected(item, this, R.id.nav_orders);
    }
}
