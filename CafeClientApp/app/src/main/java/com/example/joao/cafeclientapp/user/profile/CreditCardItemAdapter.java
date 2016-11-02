package com.example.joao.cafeclientapp.user.profile;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.devmarvel.creditcardentry.library.CreditCardForm;
import com.example.joao.cafeclientapp.R;
import com.example.joao.cafeclientapp.user.User;

import java.util.ArrayList;

/**
 * Created by andre on 28/10/2016.
 */

class CreditCardItemAdapter extends RecyclerView.Adapter<CreditCardItemAdapter.ViewHolder> {

    private ArrayList<User.CreditCard> creditCards;
    private final Activity mActivity;
    private boolean showPrimary;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        View mView;
        ViewHolder(View v) {
            super(v);
            mView = v;
        }
    }

    CreditCardItemAdapter(ArrayList<User.CreditCard> cc, Activity a){
        this.mActivity = a;
        this.creditCards = (ArrayList<User.CreditCard>) cc.clone();

        //remove primary credit card
        creditCards.remove(User.getInstance(a).getPrimaryCreditCard());
    }

    @Override
    public CreditCardItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        // create a new view
        v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.credit_card_item, parent, false);
        // set the view's size, margins, paddings and layout parameters

        return new CreditCardItemAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(CreditCardItemAdapter.ViewHolder holder, int position) {
        CreditCardForm credit_card_form = (CreditCardForm) holder.mView.findViewById(R.id.credit_card_form);

        User.CreditCard cc = creditCards.get(position);
        String ccNumber = cc.getNumber();
        String ccExpDate = cc.getExpirationDate();

        credit_card_form.setCardNumber(ccNumber,false);
        credit_card_form.setExpDate(ccExpDate,false);
    }

    void refreshDataset(Activity a, ArrayList<User.CreditCard> ccs) {
        this.creditCards = (ArrayList<User.CreditCard>) ccs.clone();

        //remove primary credit card
        creditCards.remove(User.getInstance(a).getPrimaryCreditCard());
        this.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return creditCards.size();
    }
}
