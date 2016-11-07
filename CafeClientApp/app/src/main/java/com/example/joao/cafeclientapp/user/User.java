package com.example.joao.cafeclientapp.user;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.example.joao.cafeclientapp.CustomLocalStorage;
import com.example.joao.cafeclientapp.authentication.LoginActivity;
import com.example.joao.cafeclientapp.cart.Cart;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Joao on 23/10/2016.
 */

public class User implements Serializable{

    private String uuid, name, email, nif, pin;
    private ArrayList<CreditCard> creditCards;
    private CreditCard primary_credit_card;
    /**
     * User is singleton
     */
    private static User instance = null;

    public static User getInstance(Activity a) {
        if(instance == null)
            getSavedUser(a);
        return instance;
    }

    protected User() {
        creditCards = new ArrayList<>();
    }

    public void logout(Activity activity){
        CustomLocalStorage.remove(activity, "uuid");
        CustomLocalStorage.remove(activity, "pin");
        CustomLocalStorage.remove(activity, "user");
        CustomLocalStorage.remove(activity, "cart");
        Cart.getInstance(activity).resetCart();
        User.instance = null;

        Intent myIntent = new Intent(activity.getApplicationContext(), LoginActivity.class);
        activity.startActivity(myIntent);
        activity.finish();
    }


    //////////////////////////////////////////
    ////////////GETTERS AND SETTERS///////////
    //////////////////////////////////////////
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNif() {
        return nif;
    }

    public void setNif(String nif) {
        this.nif = nif;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public ArrayList<CreditCard> getCreditCards() {
        return creditCards;
    }

    public void addCreditCard(int i, String n, String expDate) {
        addCreditCard(new CreditCard(i, n,expDate));
    }

    public void addCreditCard(CreditCard c) {
        this.creditCards.add(c);
    }

    public void setPrimaryCreditCard(int id) {
        for (CreditCard cc: this.creditCards) {
            if(cc.id == id) {
                this.primary_credit_card = cc;
                break;
            }
        }
    }

    public CreditCard getPrimaryCreditCard() {
        return primary_credit_card;
    }

    public class CreditCard implements Serializable{
        protected int id;
        protected String number;
        protected String expirationDate;

        CreditCard(int i, String n, String exp) {
            id = i;
            number = n;
            expirationDate = exp;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CreditCard that = (CreditCard) o;

            if (id != that.id) return false;
            if (!number.equals(that.number)) return false;
            return expirationDate.equals(that.expirationDate);

        }

        @Override
        public int hashCode() {
            int result = id;
            result = 31 * result + number.hashCode();
            result = 31 * result + expirationDate.hashCode();
            return result;
        }


        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getNumber() {
            return number;
        }

        public void setNumber(String number) {
            this.number = number;
        }

        public String getExpirationDate() {
            return expirationDate;
        }

        public void setExpirationDate(String expirationDate) {
            this.expirationDate = expirationDate;
        }
    }

    public static void getSavedUser(Activity a){
        try {
            instance = CustomLocalStorage.getUser(a);
        } catch (Exception e) {
            Log.d("user", "couldnt get user from storage");
            instance = new User();
        }
    }

    public static void saveInstance(Activity a) {
        try {
            CustomLocalStorage.saveUser(a,instance);
        } catch (IOException e) {
            Log.e("user","couldn't save user");
        }
    }
}
