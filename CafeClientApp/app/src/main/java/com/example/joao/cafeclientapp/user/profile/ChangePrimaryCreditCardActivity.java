package com.example.joao.cafeclientapp.user.profile;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.joao.cafeclientapp.CustomLocalStorage;
import com.example.joao.cafeclientapp.R;
import com.example.joao.cafeclientapp.ServerRestClient;
import com.example.joao.cafeclientapp.user.User;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

public class ChangePrimaryCreditCardActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private CreditCardRadioButtonAdapter mRecyclerAdapter;
    private Activity currentActivity;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_primary_credit_card);

        setTitle("Primary Credit Card");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        currentActivity = this;

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendChangePrimaryCreditCardRequest();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /////////////////////////////////////////////////////////////////////
        mRecyclerAdapter = new CreditCardRadioButtonAdapter(this, User.getInstance(this).getCreditCards(),
                User.getInstance(this).getPrimaryCreditCard());
        mRecyclerView = (RecyclerView) findViewById(R.id.credit_cards_recycler_view);

        mRecyclerView.setAdapter(mRecyclerAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void sendChangePrimaryCreditCardRequest() {
        User.CreditCard cc = mRecyclerAdapter.getSelected();

        RequestParams params = new RequestParams();
        HashMap<String, String> user_params = new HashMap<>();
        HashMap<String, Integer> creditCard_params = new HashMap<>();

        creditCard_params.put("id",cc.getId());

        user_params.put("pin", CustomLocalStorage.getString(this,"pin"));
        user_params.put("id", CustomLocalStorage.getString(this,"uuid"));

        params.put("user",user_params);
        params.put("credit_card",creditCard_params);

        progressDialog = ProgressDialog.show(ChangePrimaryCreditCardActivity.this,
                "Please wait ...", "Requesting to server ...", true);

        ServerRestClient.post("primary_credit_card", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try{
                    String error = response.get("error").toString();
                    Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                    return;
                }
                catch(JSONException e){
                    //normal behaviour when there are no errors.
                }

                try {
                    JSONObject u = response.getJSONObject("user");
                    int pcc = u.getInt("primary_credit_card");

                    // SET SINGLETON USER
                    User.getInstance(currentActivity).setPrimaryCreditCard(pcc);
                    User.saveInstance(currentActivity);
                    Intent intent = new Intent(currentActivity,ProfileActivity.class);
                    progressDialog.dismiss();
                    startActivity(intent);
                    currentActivity.finish();
                } catch (JSONException e) {
                    Log.e("FAILURE:", e.getMessage());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String error, Throwable throwable){
                Log.e("FAILURE:", error);
                Toast.makeText(getApplicationContext(), "Server not available...", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject object){
                Log.e("FAILURE:", "some error I dont know how to handle. timeout?");
                Toast.makeText(getApplicationContext(), "Server not available...", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }

        });
    }
}
