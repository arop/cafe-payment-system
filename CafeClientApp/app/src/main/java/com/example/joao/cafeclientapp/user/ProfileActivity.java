package com.example.joao.cafeclientapp.user;

import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.devmarvel.creditcardentry.library.CreditCardForm;
import com.example.joao.cafeclientapp.NavigationDrawerUtils;
import com.example.joao.cafeclientapp.R;

public class ProfileActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private User user;
    private RecyclerView mRecyclerView;
    private CreditCardItemAdapter mRecyclerAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Profile");

        user = User.getInstance(this);

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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return NavigationDrawerUtils.onNavigationItemSelected(item, this, R.id.nav_profile);
    }
}
