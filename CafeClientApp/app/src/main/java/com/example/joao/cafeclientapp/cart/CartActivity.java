package com.example.joao.cafeclientapp.cart;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.joao.cafeclientapp.NavigationDrawerUtils;
import com.example.joao.cafeclientapp.R;
import com.example.joao.cafeclientapp.user.vouchers.Voucher;

import java.util.ArrayList;

public class CartActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Cart currentCart;
    private ArrayList<Voucher> currentVouchers;

    private RecyclerView.LayoutManager mLayoutManager;

    private RecyclerView cartRecyclerView;
    private RecyclerView.Adapter cartAdapter;

    private RecyclerView vouchersRecyclerView;
    private RecyclerView.Adapter vouchersAdapter;

    private Activity currentActivity;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        this.currentActivity = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Cart");

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        currentCart = Cart.getInstance(this);
        currentVouchers = Voucher.getVouchersInstance(this);

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
        NavigationDrawerUtils.setUser(navigationView, this);
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
                if(!currentCart.getProducts().isEmpty()) {
                    // Intent is what you use to start another activity
                    Intent intent = new Intent(currentActivity, CartPinAskActivity.class);
                    intent.putParcelableArrayListExtra("vouchers", ((VoucherSelectItemAdapter) vouchersAdapter).getSelectedVouchers());
                    currentActivity.startActivity(intent);
                } else {
                    Toast.makeText(currentActivity.getApplicationContext(),"Please choose products first!",Toast.LENGTH_SHORT).show();
                }
            }
        });

        MenuItem clearCartItem = menu.findItem(R.id.cart_clear);
        clearCartItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Cart.getInstance(currentActivity).resetCart();
                ((CartItemAdapter)cartAdapter).refreshDataSet();
                refreshCartTotalPrice();
                currentCart.saveCart(currentActivity);
                return true;
            }
        });

        // Handle button click here
        return super.onPrepareOptionsMenu(menu);
    }


    //////////////////////////////////////////////
    /////////////// TAB VIEW STUFF ////////////////

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            CartActivity activity = (CartActivity) getActivity();
            View rootView = null;

            switch(getArguments().getInt(ARG_SECTION_NUMBER)){
                case 1:{
                    rootView = inflater.inflate(R.layout.fragment_cart_cart, container, false);

                    activity.cartRecyclerView = (RecyclerView) rootView.findViewById(R.id.cart_recycler_view);

                    // use this setting to improve performance if you know that changes
                    // in content do not change the layout size of the RecyclerView
                    activity.cartRecyclerView.setHasFixedSize(true);

                    // use a linear layout manager
                    activity.mLayoutManager = new LinearLayoutManager(activity);
                    activity.cartRecyclerView.setLayoutManager(activity.mLayoutManager);
                    // specify an adapter (see also next example)
                    activity.cartAdapter = new CartItemAdapter(activity.currentCart,activity);
                    activity.cartRecyclerView.setAdapter(activity.cartAdapter);

                    break;
                }
                case 2:{
                    rootView = inflater.inflate(R.layout.fragment_cart_vouchers, container, false);

                    activity.vouchersRecyclerView = (RecyclerView) rootView.findViewById(R.id.vouchers_select_recycler_view);

                    // use this setting to improve performance if you know that changes
                    // in content do not change the layout size of the RecyclerView
                    activity.vouchersRecyclerView.setHasFixedSize(true);

                    // use a linear layout manager
                    activity.mLayoutManager = new LinearLayoutManager(activity);
                    activity.vouchersRecyclerView.setLayoutManager(activity.mLayoutManager);
                    // specify an adapter (see also next example)
                    activity.vouchersAdapter = new VoucherSelectItemAdapter(activity.currentVouchers,activity);
                    activity.vouchersRecyclerView.setAdapter(activity.vouchersAdapter);

                    Log.e("VOUCHERS", activity.currentVouchers.size()+"");

                    break;
                }
            }

            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Cart";
                case 1:
                    return "Vouchers";
            }
            return null;
        }
    }

}
