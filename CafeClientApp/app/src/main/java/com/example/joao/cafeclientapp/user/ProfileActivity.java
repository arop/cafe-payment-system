package com.example.joao.cafeclientapp.user;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.devmarvel.creditcardentry.library.CreditCardForm;
import com.example.joao.cafeclientapp.CustomLocalStorage;
import com.example.joao.cafeclientapp.NavigationDrawerUtils;
import com.example.joao.cafeclientapp.R;
import com.example.joao.cafeclientapp.ServerRestClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

public class ProfileActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private User user;
    private RecyclerView mRecyclerView;
    private CreditCardItemAdapter mRecyclerAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Activity currentActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Profile");

        currentActivity = this;

        user = User.getInstance(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInsertCreditCardAlert();
            }
        });

        /////////////////////////////////////////////////////////////////////
        mRecyclerView = (RecyclerView) findViewById(R.id.credit_cards_recycler_view);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mRecyclerAdapter = new CreditCardItemAdapter(user.getCreditCards(), this);
        mRecyclerView.setAdapter(mRecyclerAdapter);

        ///////////////////////////////////////////////////////////////////

        TextView nameTextView = (TextView) findViewById(R.id.name);
        nameTextView.setText(user.getName());

        TextView emailTextView = (TextView) findViewById(R.id.email);
        emailTextView.setText(user.getEmail());

        TextView nifTextView = (TextView) findViewById(R.id.nif);
        nifTextView.setText(user.getNif());

        CreditCardForm creditCardForm = (CreditCardForm) findViewById(R.id.primary_credit_card_form);
        creditCardForm.setCardNumber(user.getPrimaryCreditCard().number,false);
        creditCardForm.setExpDate(user.getPrimaryCreditCard().expirationDate,false);

        // dont show other credit cards if there are none
        if(user.getCreditCards().size() < 2) {
            TextView otherCCs = (TextView) findViewById(R.id.otherCCsTitle);
            otherCCs.setText("");
        }


        /////////////////////////////////////////////
        ///////////// NAVIGATION DRAWER /////////////


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.setCheckedItem(R.id.nav_profile);
        NavigationDrawerUtils.setUser(navigationView, this);
        /////////////////////////////////////////////
    }

    public void showInsertCreditCardAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New credit card");

        LayoutInflater li = getLayoutInflater();
        View view = li.inflate(R.layout.add_credit_card_layout,null);

        final CreditCardForm creditCardForm = (CreditCardForm) view.findViewById(R.id.add_primary_credit_card_form);
        builder.setView(view);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(creditCardForm.isCreditCardValid()) {
                    // TODO show ask pin activity
                    insertCreditCardRequest(creditCardForm);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void insertCreditCardRequest(CreditCardForm cc) {
        RequestParams params = new RequestParams();
        HashMap<String, String> user_params = new HashMap<>();
        HashMap<String, String> creditCard_params = new HashMap<>();

        creditCard_params.put("number",cc.getCreditCard().getCardNumber());
        creditCard_params.put("cvv",cc.getCreditCard().getSecurityCode());
        creditCard_params.put("exp_date",cc.getCreditCard().getExpDate());

        user_params.put("pin", CustomLocalStorage.getString(this,"pin"));
        user_params.put("id", CustomLocalStorage.getString(this,"uuid"));

        params.put("user",user_params);
        params.put("credit_card",creditCard_params);

        ServerRestClient.post("credit_card", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try{
                    String error = response.get("error").toString();
                    Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
                    //TODO showProgress(false);
                    return;
                }
                catch(JSONException e){
                    //normal behaviour when there are no errors.
                }

                try {
                    JSONObject credit_card = (JSONObject) response.get("credit_card");
                    User.getInstance(currentActivity).addCreditCard(credit_card.getInt("id"),
                            credit_card.getString("number"),credit_card.getString("expiration"));
                    User.saveInstance(currentActivity);
                    mRecyclerAdapter.refreshDataset(currentActivity, User.getInstance(currentActivity).getCreditCards());
                } catch (JSONException e) {
                    Log.e("FAILURE:", e.getMessage());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String error, Throwable throwable){
                Log.e("FAILURE:", error);
                Toast.makeText(getApplicationContext(), "Server not available...", Toast.LENGTH_SHORT).show();
                //TODO showProgress(false);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject object){
                Log.e("FAILURE:", "some error I dont know how to handle. timeout?");
                Toast.makeText(getApplicationContext(), "Server not available...", Toast.LENGTH_SHORT).show();
                //TODO showProgress(false);
            }

        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return NavigationDrawerUtils.onNavigationItemSelected(item, this, R.id.nav_profile);
    }
}
