package com.example.joao.cafeclientapp.user;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.devmarvel.creditcardentry.library.CreditCardForm;
import com.example.joao.cafeclientapp.R;

import java.util.ArrayList;

/**
 * Created by andre on 28/10/2016.
 */

public class CreditCardItemAdapter extends RecyclerView.Adapter<CreditCardItemAdapter.ViewHolder> {

    private ArrayList<User.CreditCard> creditCards;
    private final Activity mActivity;

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

    public CreditCardItemAdapter(ArrayList<User.CreditCard> cc, Activity a){
        this.creditCards = (ArrayList<User.CreditCard>) cc.clone();
        this.mActivity = a;

        //remove primary credit card
        creditCards.remove(User.getInstance(a).getPrimaryCreditCard());
    }

    @Override
    public CreditCardItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.credit_card_item, parent, false);
        // set the view's size, margins, paddings and layout parameters

        return new CreditCardItemAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(CreditCardItemAdapter.ViewHolder holder, int position) {
        CreditCardForm credit_card_form = (CreditCardForm) holder.mView.findViewById(R.id.credit_card_form);

        User.CreditCard cc = creditCards.get(position);
        String ccNumber = cc.number;
        String ccExpDate = cc.expirationDate;

        credit_card_form.setCardNumber(ccNumber,true);
        credit_card_form.setExpDate(ccExpDate,false);

        if(cc.id == User.getInstance(mActivity).getPrimaryCreditCard().id)
            credit_card_form.setBackgroundColor(mActivity.getResources().getColor(R.color.colorPrimaryDark));

        //TODO set not editable

        //Log.d("credit card", "" + position);
    }

    @Override
    public int getItemCount() {
        return creditCards.size();
    }
}
