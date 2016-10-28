package com.example.joao.cafeclientapp.menu;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;;import java.io.Serializable;

import static java.lang.Float.parseFloat;

/**
 * Created by andre on 15/10/2016.
 */

public class Product implements Parcelable, Serializable{
    private Integer id;
    private String name;
    private float price;
    private Integer quantity; // only meaningful if object's copy's in cart.

    public Product(String n, float p, int i) {
        name = n;
        price = p;
        id = i;
        quantity = 0;
    }


    public Product(JSONObject jo) {
        try {
            name = jo.getString("name");
            price = parseFloat(jo.getString("price"));
            id = jo.getInt("id");
            if(jo.has("quantity"))
                quantity = Integer.parseInt(jo.get("quantity").toString());
            else quantity = 0;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected Product(Parcel in) {
        id = in.readInt();
        name = in.readString();
        price = in.readFloat();
        quantity = in.readInt();
    }

    public static final Creator<Product> CREATOR = new Creator<Product>() {
        @Override
        public Product createFromParcel(Parcel in) {
            return new Product(in);
        }

        @Override
        public Product[] newArray(int size) {
            return new Product[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        Product product = (Product) o;
        return id == product.id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeFloat(price);
        dest.writeInt(quantity);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
