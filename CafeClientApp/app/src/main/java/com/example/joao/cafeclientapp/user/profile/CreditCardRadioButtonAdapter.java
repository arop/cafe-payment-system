package com.example.joao.cafeclientapp.user.profile;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import com.devmarvel.creditcardentry.library.CreditCardForm;
import com.example.joao.cafeclientapp.R;
import com.example.joao.cafeclientapp.user.User;

import java.util.ArrayList;

/**
 * Created by andre on 02/11/2016.
 */

class CreditCardRadioButtonAdapter extends RecyclerView.Adapter<CreditCardRadioButtonAdapter.ViewHolder> {
    private int mSelectedItem = -1;
    private ArrayList<User.CreditCard> creditCards;
    private Context mContext;

    public CreditCardRadioButtonAdapter(Context context, ArrayList<User.CreditCard> ccs, User.CreditCard primaryCreditCard) {
        this.mContext = context;
        this.creditCards = ccs;
        mSelectedItem = creditCards.indexOf(primaryCreditCard);
    }

    @Override
    public CreditCardRadioButtonAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        final View view = inflater.inflate(R.layout.credit_card_with_button, viewGroup, false);
        return new CreditCardRadioButtonAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CreditCardRadioButtonAdapter.ViewHolder viewHolder, final int i) {
        viewHolder.mRadio.setChecked(i == mSelectedItem);
        viewHolder.mCreditCardForm.setCardNumber(creditCards.get(i).getNumber(),false);
        viewHolder.mCreditCardForm.setExpDate(creditCards.get(i).getExpirationDate(),false);
    }

    @Override
    public int getItemCount() {
        return creditCards.size();
    }

    public User.CreditCard getSelected() {
        return creditCards.get(mSelectedItem);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public RadioButton mRadio;
        public CreditCardForm mCreditCardForm;

        public ViewHolder(final View inflate) {
            super(inflate);
            mCreditCardForm = (CreditCardForm) inflate.findViewById(R.id.credit_card_form);
            mRadio = (RadioButton) inflate.findViewById(R.id.radio);
            View.OnClickListener clickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSelectedItem = getAdapterPosition();
                    notifyItemRangeChanged(0, creditCards.size());
                }
            };
            itemView.setOnClickListener(clickListener);
            mRadio.setOnClickListener(clickListener);
        }
    }
}
