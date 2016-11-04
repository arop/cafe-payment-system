package com.example.joao.cafeclientapp.cart;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

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

    private int numberOfVouchersSelected;
    private boolean isDiscountVoucherSelected; //only one discount voucher can be selected.

    public VoucherSelectItemAdapter(ArrayList<Voucher> vouchers, Activity a) {
        this.mActivity = a;
        this.dataset = new ArrayList<VoucherHolder>();
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
            vh.setCheckbox((CheckBox) holder.mView.findViewById(R.id.voucher_checkbox));
        }
        vh.checkbox.setOnCheckedChangeListener(null);
        vh.checkbox.setChecked(vh.checked);
        vh.checkbox.setVisibility(vh.visibility);
        vh.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                vh.checked = isChecked;

                if(isChecked){
                    if(vh.voucher.getType() == 'd') {
                        isDiscountVoucherSelected = true;
                        disableUncheckedDiscountCheckboxes();
                    }

                    if(++numberOfVouchersSelected == MAX_SELECTED_VOUCHERS){
                        disableUncheckedCheckBoxes();
                    }
                }
                else{
                    if(vh.voucher.getType() == 'd') {
                        isDiscountVoucherSelected = false;
                        enableDiscountCheckboxes();
                    }

                    if(numberOfVouchersSelected-- == MAX_SELECTED_VOUCHERS){
                        enableAllCheckBoxes();
                    }
                }
            }
        });
    }

    private void enableDiscountCheckboxes() {
        for(VoucherHolder vh : dataset){
            if(!vh.checked && vh.voucher.getType() == 'd'){
                vh.setVisibility(View.VISIBLE);
            }
        }
    }

    private void disableUncheckedDiscountCheckboxes() {
        for(VoucherHolder vh : dataset){
            if(!vh.checked && vh.voucher.getType() == 'd'){
                vh.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void enableAllCheckBoxes() {
        for(VoucherHolder vh : dataset){
            if(vh.voucher.getType() != 'd')
                vh.setVisibility(View.VISIBLE);
            else if(!isDiscountVoucherSelected)
                vh.setVisibility(View.VISIBLE);
        }
    }

    private void disableUncheckedCheckBoxes() {
        for(VoucherHolder vh : dataset){
            if(!vh.checked){
                vh.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }

    private class VoucherHolder {
        public Voucher voucher;
        private int visibility;
        public boolean checked;
        private CheckBox checkbox = null;

        public VoucherHolder(Voucher v) {
            voucher = v;
            visibility = View.VISIBLE;
            checked = false;
        }

        public void setVisibility(int v){
            this.visibility = v;
            if(this.checkbox != null)
                this.checkbox.setVisibility(v);
        }

        public int getVisibility(){
            return this.visibility;
        }

        public void setCheckbox(CheckBox c){
            this.checkbox = c;
            this.checkbox.setVisibility(this.visibility);
        }
    }
}
