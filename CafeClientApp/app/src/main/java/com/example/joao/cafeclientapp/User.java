package com.example.joao.cafeclientapp;

import android.app.Activity;
import android.content.Intent;

import com.example.joao.cafeclientapp.authentication.LoginActivity;

import java.util.ArrayList;

/**
 * Created by Joao on 23/10/2016.
 */

public class User {

    private String uuid, name, email, nif, pin;
    private ArrayList<CreditCard> creditCards;
    private CreditCard primaryCreditCard;
    /**
     * User is singleton
     */
    private static User instance = null;

    public static User getInstance() {
        if(instance == null)
            instance = new User();
        return instance;
    }

    protected User() {
        creditCards = new ArrayList<>();
    }

    public void logout(Activity activity){
        CustomLocalStorage.remove(activity, "uuid");
        CustomLocalStorage.remove(activity, "pin");
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

    public void setCreditCards(ArrayList<CreditCard> creditCards) {
        this.creditCards = creditCards;
    }

    public void addCreditCard(String n, String expDate) {
        addCreditCard(new CreditCard(n,expDate));
    }

    public void addCreditCard(CreditCard c) {
        this.creditCards.add(c);
    }

    public CreditCard getPrimaryCreditCard() {
        return primaryCreditCard;
    }

    public void setPrimaryCreditCard(String n, String e) {
        setPrimaryCreditCard(new CreditCard(n,e));
    }

    public void setPrimaryCreditCard(CreditCard primaryCreditCard) {
        this.primaryCreditCard = primaryCreditCard;
    }

    public class CreditCard {
        protected String number;
        protected String expirationDate;
        public CreditCard(String n, String exp) {
            number = n;
            expirationDate = exp;
        }
    }
}
