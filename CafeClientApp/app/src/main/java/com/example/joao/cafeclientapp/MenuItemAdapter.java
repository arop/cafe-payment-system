package com.example.joao.cafeclientapp;

/**
 * Created by Joao on 13/10/2016.
 */

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MenuItemAdapter extends RecyclerView.Adapter<MenuItemAdapter.ViewHolder> {

    private ArrayList<Product> dataset;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public View mView;
        public ViewHolder(View v) {
            super(v);
            mView = v;
        }
    }

    public MenuItemAdapter(ArrayList<Product> dataset){
        this.dataset = dataset;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.menu_list_item, parent, false);
        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        TextView name = (TextView) holder.mView.findViewById(R.id.product_name);
        TextView price = (TextView) holder.mView.findViewById(R.id.product_price);

        ImageButton addProductToCart = (ImageButton) holder.mView.findViewById(R.id.product_add);
        ImageButton removeProductFromCart = (ImageButton) holder.mView.findViewById(R.id.product_remove);

        final String productName = dataset.get(position).name;
        final float productPrice = dataset.get(position).price;

        addProductToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Cart.addProductToCart(new Product(productName,productPrice));
                Log.d("cart",productName + "-" + Cart.getCart().get(new Product(productName,productPrice)));
                Cart.saveCart(ShowMenuActivity.getMenuActivity());
                /**
                 * TODO add toast saying product added
                 */
            }
        });

        removeProductFromCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cart.removeProductFromCart(new Product(productName,productPrice));
                Log.d("cart",productName + "-" + Cart.getCart().get(new Product(productName,productPrice)));
                Cart.saveCart(ShowMenuActivity.getMenuActivity());
                /**
                 * TODO add toast saying product added
                 */
            }
        });

        name.setText(productName);
        price.setText(String.format( "%.2f", productPrice )+"€");
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }
}