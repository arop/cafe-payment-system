package com.example.joao.cafeclientapp;

import android.app.Activity;

import java.security.Key;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by andre on 15/10/2016.
 */

public class Cart {
    static private Map<Product,Integer> cart = new HashMap<>();

    public Cart(Map<Product,Integer> m) {
        cart = m;
    }

    static public void addProductToCart(Product p) {
        if(cart.get(p) == null)
            cart.put(p,1);
        else {
            int qtt = cart.get(p);
            cart.remove(p);
            cart.put(p, ++qtt);
        }
    }

    static public void removeProductFromCart(Product p) {
        if(cart.get(p) == null) return;
        int quantity = cart.get(p);
        if(quantity < 2) {
            cart.remove(p);
        }
        else if(quantity > 1) {
            cart.remove(p);
            cart.put(p,--quantity);
        }
    }

    static public Map<Product,Integer> getCart() {
        return cart;
    }

    static public void getSavedCart(Activity a) {
        Map<Product,Integer> temp = CustomLocalStorage.getCart(a);
        if(temp == null)
            cart = new HashMap<>();
        else cart = temp;
    }

    static public void saveCart(Activity a) {
        CustomLocalStorage.saveCart(a,cart);
    }

    static public void resetCart() {
        cart.clear();
    }

    public static String printCart(Map<Product,Integer> c) {
        String ret = "Cart: ";

        for (Map.Entry<Product, Integer> entry : c.entrySet())
        {
            ret += "\n" + entry.getKey() + "/" + entry.getValue();
        }
        return ret;
    }
}
