package com.example.joao.cafeclientapp;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;

import com.example.joao.cafeclientapp.cart.CartActivity;
import com.example.joao.cafeclientapp.menu.ShowMenuActivity;

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
                    break;

                case R.id.nav_share:
                    break;

                case R.id.nav_logout:
                    User.logout(activity);
                    break;
            }
        }

        DrawerLayout drawer = (DrawerLayout) activity.findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
