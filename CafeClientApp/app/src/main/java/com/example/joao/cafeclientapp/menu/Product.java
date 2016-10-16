package com.example.joao.cafeclientapp.menu;

import org.json.JSONException;
import org.json.JSONObject;;import static java.lang.Float.parseFloat;

/**
 * Created by andre on 15/10/2016.
 */

public class Product {
    public String name;
    public float price;
    public Integer id;

    Product(String n, float p, int i) {
        name = n;
        price = p;
        id = i;
    }

    Product(JSONObject jo) {
        try {
            name = jo.getString("name");
            price = parseFloat(jo.getString("price"));
            id = jo.getInt("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean equals(Object o) {
        Product product = (Product) o;
        return id == product.id;
    }

}
