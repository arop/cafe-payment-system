package com.example.joao.cafeclientapp.cart;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.joao.cafeclientapp.R;
import com.example.joao.cafeclientapp.user.vouchers.Voucher;

import java.util.ArrayList;

/**
 * Created by Joao on 04/11/2016.
 */

public class VoucherSelectItemAdapter extends RecyclerView.Adapter<VoucherSelectItemAdapter.ViewHolder>  {

    private static final int MAX_SELECTED_VOUCHERS = 3;
    private final Activity mActivity;

    private ArrayList<VoucherHolder> dataset;
    public ArrayList<Voucher> selectedVouchers;

    private int numberOfVouchersSelected;
    private boolean isDiscountVoucherSelected; //only one discount voucher can be selected.

    public VoucherSelectItemAdapter(ArrayList<Voucher> vouchers, Activity a) {
        this.mActivity = a;
        this.dataset = new ArrayList<VoucherHolder>();
        this.selectedVouchers = new ArrayList<Voucher>();
        for(Voucher v : vouchers){
            VoucherHolder vh = new VoucherHolder(v);
            dataset.add(vh);
        }
        this.numberOfVouchersSelected = 0;
    }

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

    public ArrayList<Voucher> getSelectedVouchers() {
        return this.selectedVouchers;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.vouchers_select_list_item, parent, false);
        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        TextView titleView = (TextView) holder.mView.findViewById(R.id.voucher_title);
        ImageView imageView = (ImageView) holder.mView.findViewById(R.id.voucher_image);

        final VoucherHolder vh = dataset.get(position);

        titleView.setText(vh.voucher.getTitle());
        imageView.setImageResource(vh.voucher.getDrawable());

        if(vh.checkbox == null) {
            vh.checkbox = (CheckBox) holder.mView.findViewById(R.id.voucher_checkbox);
        }
        vh.checkbox.setOnCheckedChangeListener(null);
        vh.checkbox.setChecked(vh.checked);
        vh.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                Log.d("checked changed", vh.voucher.getSerialId()+"");
                if(isChecked){
                    if(numberOfVouchersSelected < MAX_SELECTED_VOUCHERS){
                        if(vh.voucher.getType() == 'd'){
                            if(isDiscountVoucherSelected){
                                Toast.makeText(mActivity.getApplicationContext(), "Only one discount voucher allowed.", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                isDiscountVoucherSelected = true;
                                numberOfVouchersSelected++;
                                selectedVouchers.add(vh.voucher);
                            }
                        }
                        else{
                            vh.checked = isChecked;
                            numberOfVouchersSelected++;
                            selectedVouchers.add(vh.voucher);
                        }
                    }
                    else{
                        Toast.makeText(mActivity.getApplicationContext(), "Only three total vouchers allowed.", Toast.LENGTH_SHORT).show();
                        vh.checkbox.setChecked(false);
                    }

                }
                else{
                    vh.checked = false;
                    numberOfVouchersSelected--;
                    selectedVouchers.remove(vh.voucher);
                    if(vh.voucher.getType() == 'd'){
                        isDiscountVoucherSelected = false;
                    }
                }
            }
        });
    }



    @Override
    public int getItemCount() {
        return dataset.size();
    }

    private class VoucherHolder {
        public Voucher voucher;
        public boolean checked;
        private CheckBox checkbox = null;

        public VoucherHolder(Voucher v) {
            voucher = v;
            checked = false;
        }
    }
}
