package com.example.joao.cafeclientapp;

import org.json.JSONException;
import org.json.JSONObject;;import static java.lang.Float.parseFloat;

/**
 * Created by andre on 15/10/2016.
 */

public class Product {
    public String name;
    public float price;

    Product(String n, float p) {
        name = n;
        price = p;
    }

    Product(JSONObject jo) {
        try {
            name = jo.getString("name");
            price = parseFloat(jo.getString("price"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Product product = (Product) o;

        if (Float.compare(product.price, price) != 0) return false;
        return name.equals(product.name);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (price != +0.0f ? Float.floatToIntBits(price) : 0);
        return result;
    }
}
