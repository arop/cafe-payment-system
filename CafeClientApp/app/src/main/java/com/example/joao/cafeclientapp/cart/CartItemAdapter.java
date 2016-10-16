package com.example.joao.cafeclientapp.cart;

/**
 * Created by Joao on 13/10/2016.
 */

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.joao.cafeclientapp.R;
import com.example.joao.cafeclientapp.menu.Product;

import java.util.ArrayList;

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.ViewHolder> {

    private final Activity mActivity;
    private CartItemAdapter mRecyclerView;
    private static View selectedItem;

    private Cart currentCart;
    ArrayList<String> productNames;

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

    public CartItemAdapter(Activity a){
        this.mActivity = a;
        this.currentCart = Cart.getInstance(a);
        this.productNames = new ArrayList<>(currentCart.getCart().keySet());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mRecyclerView = this;

        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cart_list_item, parent, false);
        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        TextView name = (TextView) holder.mView.findViewById(R.id.cart_product_name);
        TextView price = (TextView) holder.mView.findViewById(R.id.cart_product_price);
        TextView quantity = (TextView) holder.mView.findViewById(R.id.cart_product_quantity);

        ImageButton addProductToCart = (ImageButton) holder.mView.findViewById(R.id.cart_product_add);
        ImageButton removeProductFromCart = (ImageButton) holder.mView.findViewById(R.id.cart_product_remove);


        final String productName = productNames.get(position);
        /*final double productPrice = currentCart.getCart().get(productName).price;
                dataset.get(position).price;*/
        final double productQuantity = currentCart.getCart().get(productName);

        addProductToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //currentCart.addProductToCart(dataset.get(position));
                //currentCart.saveCart(mActivity);
                /**
                 * TODO add toast saying product added
                 */
            }
        });

        removeProductFromCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //currentCart.removeProductFromCart(dataset.get(position));
                //currentCart.saveCart(mActivity);
                /**
                 * TODO add toast saying product added
                 */
            }
        });

        name.setText(productName.substring(2));
        //price.setText(String.format( "%.2f", productPrice )+"€");
        price.setText("€");
        quantity.setText(""+(int)productQuantity);
    }

    @Override
    public int getItemCount() {
        return currentCart.getCart().size();
    }
}