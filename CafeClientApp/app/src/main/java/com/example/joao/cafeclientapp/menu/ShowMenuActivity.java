package com.example.joao.cafeclientapp.menu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
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

import com.example.joao.cafeclientapp.R;
import com.example.joao.cafeclientapp.ServerRestClient;
import com.example.joao.cafeclientapp.cart.Cart;
import com.example.joao.cafeclientapp.cart.CartActivity;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class ShowMenuActivity extends AppCompatActivity {

    private Context context;
    private static Activity currentActivity;
    final ArrayList<Product> list = new ArrayList<>();

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private Cart currentCart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_menu);

        context = getApplicationContext();
        currentActivity = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRecyclerView = (RecyclerView) findViewById(R.id.menu_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        currentCart = Cart.getInstance(this);

        ServerRestClient.get("menu", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("success", "got menu");
                try{
                    JSONArray menu = (JSONArray) response.get("menu");
                    for (int i = 0; i < menu.length(); ++i) {
                        Product p = new Product(menu.getJSONObject(i));
                        list.add(p);
                    }

                    // specify an adapter (see also next example)
                    mAdapter = new MenuItemAdapter(list, currentActivity);
                    mRecyclerView.setAdapter(mAdapter);

                }
                catch(JSONException e){
                    //normal behaviour when there are no errors.
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String error, Throwable throwable){
                Log.e("FAILURE:", error);
                Toast.makeText(context, "Server not available...", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actionbar_show_menu, menu);
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
                Log.d("cart","Cart size: "+currentCart.getCart().size());
                System.out.println("Cart.printCart(Cart.getCart()) = " + Cart.printCart(currentCart.getCart()));
                // Intent is what you use to start another activity
                Intent myIntent = new Intent(context, CartActivity.class);
                startActivity(myIntent);
            }
        });
        // Handle button click here
        return super.onPrepareOptionsMenu(menu);
    }

    public static Activity getMenuActivity() {
        return currentActivity;
    }

}
