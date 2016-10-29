package com.example.joao.cafeclientapp.menu;

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
import com.example.joao.cafeclientapp.cart.Cart;

import java.util.ArrayList;

public class MenuItemAdapter extends RecyclerView.Adapter<MenuItemAdapter.ViewHolder> {

    private final Activity mActivity;
    private ArrayList<Product> dataset;
    private View selectedItem;

    private Cart currentCart;

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

    public class OnItemClickListener implements View.OnClickListener {

        private final int itemPosition;

        public OnItemClickListener(int position){
            this.itemPosition = position;
        }

        @Override
        public void onClick(final View view) {
            Log.i("click",itemPosition+"");
            View add_btn = view.findViewById(R.id.product_add);
            View rem_btn = view.findViewById(R.id.product_remove);

            if(add_btn.getVisibility() == View.VISIBLE){ //if already visible
                if(view.equals(selectedItem)) //currently selected is this object
                    selectedItem = null; //no object will be selected. click works as "deselect"
                makeInvisible(add_btn, rem_btn);
            }
            else{
                View oldSelected = selectedItem;
                selectedItem = view; //update currently selected item
                if(oldSelected != null){ //if there was a previously selected item
                    oldSelected.callOnClick(); //call its onclick, to make its buttons invisble
                }
                makeVisible(add_btn, rem_btn);
            }
        }

        private void makeVisible(View add_btn, View rem_btn) {
            add_btn.setVisibility(View.VISIBLE);
            rem_btn.setVisibility(View.VISIBLE);
            /*add_btn.animate().alpha(1.0f).setDuration(500);
            rem_btn.animate().alpha(1.0f).setDuration(500);*/
        }

        private void makeInvisible(View add_btn, View rem_btn) {
            add_btn.setVisibility(View.INVISIBLE);
            rem_btn.setVisibility(View.INVISIBLE);
            /*add_btn.animate().alpha(0.0f).setDuration(500);
            rem_btn.animate().alpha(0.0f).setDuration(500);*/
        }

    }

    public MenuItemAdapter(ProductsMenu menu, Activity a){
        //convert Hash Map to Array List
        this.dataset = new ArrayList<>(menu.getProducts().values());

        this.mActivity = a;
        this.currentCart = Cart.getInstance(a);
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

        final String productName = dataset.get(position).getName();
        final float productPrice = dataset.get(position).getPrice();

        addProductToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentCart.addProductToCart(dataset.get(position));
                currentCart.saveCart(mActivity);
                ((ShowMenuActivity) mActivity).refreshCartTotalPrice();
                /**
                 * TODO add toast saying product added
                 */
            }
        });

        removeProductFromCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentCart.removeProductFromCart(dataset.get(position));
                currentCart.saveCart(mActivity);
                ((ShowMenuActivity) mActivity).refreshCartTotalPrice();
                /**
                 * TODO add toast saying product added
                 */
            }
        });

        name.setText(productName);
        price.setText(String.format( "%.2f", productPrice )+"€");

        OnItemClickListener clickListener = new OnItemClickListener(position);
        holder.mView.setOnClickListener(clickListener);
    }



    @Override
    public int getItemCount() {
        return dataset.size();
    }

    public void clearList(){
        this.dataset.clear();
        this.notifyDataSetChanged();
    }

    public void addAll(ProductsMenu pm) {
        this.dataset = new ArrayList<>(pm.getProducts().values());
        this.notifyDataSetChanged();
    }

}