package com.example.joao.cafeclientapp;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.joao.cafeclientapp.cart.CartActivity;
import com.example.joao.cafeclientapp.menu.ShowMenuActivity;
import com.example.joao.cafeclientapp.user.orders.PreviousOrdersActivity;
import com.example.joao.cafeclientapp.user.profile.ProfileActivity;
import com.example.joao.cafeclientapp.user.User;
import com.example.joao.cafeclientapp.user.vouchers.VouchersActivity;

/**
 * Created by Joao on 23/10/2016.
 */

public class NavigationDrawerUtils {

    public static boolean onNavigationItemSelected(@NonNull MenuItem item, Activity activity, int current_item_id){
        int id = item.getItemId();

        if(id != current_item_id){
            Intent intent;
            switch(id){
                case R.id.nav_menu:
                    intent = new Intent(activity.getApplicationContext(), ShowMenuActivity.class);
                    activity.startActivity(intent);
                    break;

                case R.id.nav_cart:
                    intent = new Intent(activity.getApplicationContext(), CartActivity.class);
                    activity.startActivity(intent);
                    break;

                case R.id.nav_vouchers:
                    intent = new Intent(activity.getApplicationContext(), VouchersActivity.class);
                    activity.startActivity(intent);
                    break;

                case R.id.nav_orders:
                    intent = new Intent(activity.getApplicationContext(), PreviousOrdersActivity.class);
                    activity.startActivity(intent);
                    break;

                case R.id.nav_profile:
                    intent = new Intent(activity.getApplicationContext(), ProfileActivity.class);
                    activity.startActivity(intent);
                    break;

                case R.id.nav_logout:
                    User.getInstance(activity).logout(activity);
                    break;

            }
        }

        DrawerLayout drawer = (DrawerLayout) activity.findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public static void setUser(NavigationView nav_view, Activity a) {
        View header = nav_view.getHeaderView(0);
        TextView name = (TextView) header.findViewById(R.id.navigation_drawer_header_name);
        TextView email = (TextView) header.findViewById(R.id.navigation_drawer_header_email);

        name.setText(User.getInstance(a).getName());
        email.setText(User.getInstance(a).getEmail());
    }

}
