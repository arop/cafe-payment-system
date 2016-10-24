package com.example.joao.cafeclientapp.user;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.joao.cafeclientapp.R;
import com.example.joao.cafeclientapp.menu.ShowMenuActivity;

import java.util.ArrayList;

/**
 * Created by norim on 24/10/2016.
 */


public class PreviousOrderItemAdapter extends RecyclerView.Adapter<PreviousOrderItemAdapter.ViewHolder> {

    private final Activity mActivity;
    private ArrayList<Order> dataset;
    private View selectedItem;


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
        }

    }

    public PreviousOrderItemAdapter(ArrayList<Order> orders, Activity a){
        //convert Hash Map to Array List
        this.dataset = orders;
        this.mActivity = a;
    }

    @Override
    public PreviousOrderItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.previous_order_list_item, parent, false);
        // set the view's size, margins, paddings and layout parameters

        PreviousOrderItemAdapter.ViewHolder vh = new PreviousOrderItemAdapter.ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(PreviousOrderItemAdapter.ViewHolder holder, final int position) {
        TextView name = (TextView) holder.mView.findViewById(R.id.product_name);
        TextView price = (TextView) holder.mView.findViewById(R.id.product_price);

        ImageButton addProductToCart = (ImageButton) holder.mView.findViewById(R.id.product_add);
        ImageButton removeProductFromCart = (ImageButton) holder.mView.findViewById(R.id.product_remove);

        addProductToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        removeProductFromCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        PreviousOrderItemAdapter.OnItemClickListener clickListener = new PreviousOrderItemAdapter.OnItemClickListener(position);
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


}