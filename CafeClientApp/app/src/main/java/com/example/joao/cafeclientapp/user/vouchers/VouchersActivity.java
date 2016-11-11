package com.example.joao.cafeclientapp.user.vouchers;

import android.app.Activity;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.joao.cafeclientapp.NavigationDrawerUtils;
import com.example.joao.cafeclientapp.R;
import com.example.joao.cafeclientapp.ServerRestClient;
import com.example.joao.cafeclientapp.user.User;
import com.example.joao.cafeclientapp.user.orders.Order;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import cz.msebera.android.httpclient.Header;

public class VouchersActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener  {

    private ArrayList<Voucher> vouchers;

    private RecyclerView mRecyclerView;
    private VoucherItemAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Activity currentActivity;
    private Menu menu;
    private SwipeRefreshLayout swipeContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vouchers);

        this.currentActivity = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("My Vouchers");

        mRecyclerView = (RecyclerView) findViewById(R.id.vouchers_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        vouchers = Voucher.getVouchersInstance(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // specify an adapter (see also next example)
        mAdapter = new VoucherItemAdapter(vouchers,this);
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

        navigationView.setCheckedItem(R.id.nav_vouchers);
        NavigationDrawerUtils.setUser(navigationView, this);
        /////////////////////////////////////////////

        ///////////////////////////////////////////
        ////////// SETUP SWIPE REFRESH ////////////
        // Lookup the swipe container view
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_vouchers_container);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                fetchVouchersAsync();
            }
        });

        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        ///////////////////////////////////////////

        swipeContainer.setRefreshing(true);
        fetchVouchersAsync();
    }

    private void fetchVouchersAsync() {

        RequestParams params = new RequestParams();

        HashMap<String, String> user_params = new HashMap<>();
        user_params.put("id", User.getInstance(this).getUuid());
        user_params.put("pin", User.getInstance(this).getPin());
        params.put("user",user_params);
        Log.d("user", user_params.toString());

        ServerRestClient.post("validvouchers", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                Log.d("success", "got vouchers");
                Log.d("response", response.toString());
                try {
                    JSONArray vouchersResponse = (JSONArray) response.get("vouchers");
                    vouchers = new ArrayList<Voucher>();

                    for (int i = 0; i < vouchersResponse.length(); ++i) {
                        Voucher v = new Voucher((JSONObject) vouchersResponse.get(i));
                        vouchers.add(v);
                    }

                    Voucher.setAndSaveInstance(currentActivity, vouchers);
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
                Toast.makeText(currentActivity.getApplicationContext(), "Server not available...", Toast.LENGTH_SHORT).show();
                swipeContainer.setRefreshing(false);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject json){
                Log.e("server error" , json.toString());
                swipeContainer.setRefreshing(false);
            }

        });
    }

    public void refreshAdapterData() {
        mAdapter.setDataset(vouchers);
        //notifyDataSetChanged();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return NavigationDrawerUtils.onNavigationItemSelected(item, this, R.id.nav_vouchers);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        // TODO change to custom toolbar
        inflater.inflate(R.menu.actionbar_vouchers, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        // Handle button click here
        return super.onPrepareOptionsMenu(menu);
    }


}
