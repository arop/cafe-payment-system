package com.example.joao.cafeclientapp.user.orders;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import com.devmarvel.creditcardentry.library.CreditCardForm;
import com.example.joao.cafeclientapp.NavigationDrawerUtils;
import com.example.joao.cafeclientapp.R;
import com.example.joao.cafeclientapp.user.vouchers.VoucherItemAdapter;

public class ShowOrderActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Order order;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_order);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Order");
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        Intent i = getIntent();
        order = i.getParcelableExtra("order");

        /////////////////////////////////////////////
        ///////////// NAVIGATION DRAWER /////////////

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.setCheckedItem(-1);
        NavigationDrawerUtils.setUser(navigationView, this);
        /////////////////////////////////////////////
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_show_order, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    public Order getOrder() {
        return order;
    }

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

            ShowOrderActivity activity = (ShowOrderActivity) getActivity();
            Order order = activity.getOrder();
            View rootView = null;

            switch(getArguments().getInt(ARG_SECTION_NUMBER)){
                case 1:{
                    rootView = inflater.inflate(R.layout.fragment_show_order_details, container, false);
                    TextView orderIdView = (TextView) rootView.findViewById(R.id.order_id);
                    TextView dateView = (TextView) rootView.findViewById(R.id.date);
                    TextView hourView = (TextView) rootView.findViewById(R.id.hour);
                    CreditCardForm creditCardView = (CreditCardForm) rootView.findViewById(R.id.credit_card_form);
                    TextView priceView = (TextView) rootView.findViewById(R.id.price);

                    creditCardView.setCardNumber(order.getCreditCard(),false);
                    orderIdView.setText(order.getId()+"");
                    dateView.setText(order.getFormatedDate());
                    hourView.setText(order.getFormatedHour());

                    priceView.setText(String.format( "%.2f",order.getTotalPrice()));
                    break;
                }
                case 2:{
                    rootView = inflater.inflate(R.layout.fragment_show_order_products, container, false);
                    RecyclerView mRecyclerView = (RecyclerView) rootView.findViewById(R.id.order_items_recycler_view);

                    // use this setting to improve performance if you know that changes
                    // in content do not change the layout size of the RecyclerView
                    mRecyclerView.setHasFixedSize(true);

                    // use a linear layout manager
                    RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(activity);
                    mRecyclerView.setLayoutManager(mLayoutManager);

                    // specify an adapter (see also next example)
                    OrderProductItemAdapter mRecyclerAdapter = new OrderProductItemAdapter(order.getItems(), activity);
                    mRecyclerView.setAdapter(mRecyclerAdapter);
                    break;
                }

                case 3:{
                    rootView = inflater.inflate(R.layout.fragment_show_order_vouchers, container, false);
                    RecyclerView mRecyclerView = (RecyclerView) rootView.findViewById(R.id.order_vouchers_recycler_view);

                    // use this setting to improve performance if you know that changes
                    // in content do not change the layout size of the RecyclerView
                    mRecyclerView.setHasFixedSize(true);

                    // use a linear layout manager
                    RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(activity);
                    mRecyclerView.setLayoutManager(mLayoutManager);

                    // specify an adapter (see also next example)
                    VoucherItemAdapter mRecyclerAdapter = new VoucherItemAdapter(order.getVouchers(), activity);
                    mRecyclerView.setAdapter(mRecyclerAdapter);
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
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Details";
                case 1:
                    return "Products";
                case 2:
                    return "Vouchers";
            }
            return null;
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return NavigationDrawerUtils.onNavigationItemSelected(item, this, -1);
    }
}
