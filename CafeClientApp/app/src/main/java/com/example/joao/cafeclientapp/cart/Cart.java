package com.example.joao.cafeclientapp.cart;

import android.app.Activity;
import android.util.Log;

import com.example.joao.cafeclientapp.CustomLocalStorage;
import com.example.joao.cafeclientapp.menu.Product;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by andre on 15/10/2016.
 */

public class Cart {
    private Map<String,Double> cart = new HashMap<>();
    static private Cart instance;

    static public Cart getInstance(Activity a) {
        if(instance == null) {
            instance = new Cart();
            instance.getSavedCart(a);
        }
        return instance;
    }

    public Cart(Map<String,Double> m) {
        cart = m;
    }

    private Cart() {}

    public void addProductToCart(Product p) {
        String product_key = generateProductKey(p);

        if(cart.get(product_key) == null)
            cart.put(product_key,1.0);
        else {
            Double qtt = cart.get(product_key);
            cart.remove(product_key);
            cart.put(product_key, ++qtt);
            Log.i("Added to cart", product_key + " - " + cart.get(product_key));
        }
    }

    public void removeProductFromCart(Product p) {
        String product_key = generateProductKey(p);

        if(cart.get(product_key) == null) return;
        Double quantity = cart.get(product_key);
        if(quantity < 2) {
            cart.remove(product_key);
            Log.i("Removed from cart", product_key);
        }
        else if(quantity > 1) {
            cart.remove(product_key);
            cart.put(product_key,--quantity);
            Log.i("Removed from cart", product_key + " - " + cart.get(product_key));
        }
    }

    public String generateProductKey(Product p){
        return p.id+p.name;
    }

    public Map<String,Double> getCart() {
        return cart;
    }

    public void getSavedCart(Activity a) {
        Map<String,Double> temp = CustomLocalStorage.getCart(a);
        if(temp == null)
            cart = new HashMap<>();
        else cart = temp;
        Cart.printCart(cart);
    }

    public void saveCart(Activity a) {
        CustomLocalStorage.saveCart(a,cart);
    }

    public void resetCart() {
        cart.clear();
    }

    public static String printCart(Map<String,Double> c) {
        String ret = "Cart: ";

        for (Map.Entry<String, Double> entry : c.entrySet())
        {
            ret += "\n" + entry.getKey() + "/" + entry.getValue();
        }
        return ret;
    }
}
