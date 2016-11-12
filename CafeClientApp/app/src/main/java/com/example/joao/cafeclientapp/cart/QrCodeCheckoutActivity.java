package com.example.joao.cafeclientapp.cart;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.joao.cafeclientapp.CustomLocalStorage;
import com.example.joao.cafeclientapp.R;
import com.example.joao.cafeclientapp.menu.Product;
import com.example.joao.cafeclientapp.menu.ShowMenuActivity;
import com.example.joao.cafeclientapp.user.vouchers.Voucher;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;

public class QrCodeCheckoutActivity extends AppCompatActivity {

    private Activity currentActivity;
    private ImageView qrcodeView;
    public final static int WIDTH = 400;
    public final static int HEIGHT = 400;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code_checkout);

        this.currentActivity = this;

        Intent origin = this.getIntent();
        ArrayList<Voucher> vouchers = origin.getParcelableArrayListExtra("vouchers");

        /////// GENERATE JSON to be sent to terminal via QR CODE //////
        Gson gson = new Gson();
        Cart current_cart = Cart.getInstance(this);
        Map<Integer, Product> products = current_cart.getProducts();
        Map<Integer, Integer> products_quantity = new HashMap<>();
        for(Product p : products.values()){
            products_quantity.put(p.getId(), p.getQuantity());
        }

        Map<String, Object> future_json = new HashMap<>();
        future_json.put("user", CustomLocalStorage.getString(this, "uuid"));
        future_json.put("cart", products_quantity);
        future_json.put("vouchers", vouchers);
        future_json.put("pin", CustomLocalStorage.getString(this, "pin"));
        String json_str = gson.toJsonTree(future_json).toString();
        Log.d("json cart", json_str);
        //////////////////// END of JSON generation //////////////////


        qrcodeView = (ImageView) findViewById(R.id.qrcode);
        try {
            //String compressedJson = compress(json_str);
            /*String toEncodeFull = json_str;
            String toEncodeProcessed = decompress(compress(json_str));
            Log.d("encode", "full size: " + toEncodeFull.length());
            Log.d("encode", "comp size: " + compressedJson.length());
            Log.d("encode", "proc size: " + toEncodeProcessed.length());
            Log.d("encode", "full: " + toEncodeFull);
            Log.d("encode", "proc: " + toEncodeProcessed);*/
            Bitmap bitmap = encodeAsBitmap(json_str);
            qrcodeView.setImageBitmap(bitmap);
            qrcodeView.invalidate();
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }


    /*public String compress(String str) throws IOException {
        if (str == null || str.length() == 0) {
            return str;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(out);
        gzip.write(str.getBytes());
        gzip.close();
        //return new String(Base64.encode(out.toByteArray(), Base64.DEFAULT));
        return new String(out.toString("ISO-8859-1"));
    }

    // TO USE IN TERMINAL
    public String decompress(String str) throws IOException{
        if (str == null || str.length() == 0) {
            return str;
        }
        //ByteArrayInputStream in = new ByteArrayInputStream(Base64.decode(str, Base64.DEFAULT));
        ByteArrayInputStream in = new ByteArrayInputStream(str.getBytes("ISO-8859-1"));
        GZIPInputStream gzip = new GZIPInputStream(in);
        BufferedReader bf = new BufferedReader(new InputStreamReader(gzip, "UTF-8"));
        String outStr = "";
        String line;
        while ((line=bf.readLine())!=null) {
            outStr += line;
        }
        return outStr;
    }*/


    private Bitmap encodeAsBitmap(String str) throws WriterException {
        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(str,
                    BarcodeFormat.QR_CODE, WIDTH, WIDTH, null);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int w = result.getWidth();
        int h = result.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            int offset = y * w;
            for (int x = 0; x < w; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
        return bitmap;
    }


    /////////////// ACTION BAR ////////////////
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actionbar_qrcode_checkout, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem actionViewItem = menu.findItem(R.id.qrcode_checkout_done_container);
        // Retrieve the action-view from menu
        View v = MenuItemCompat.getActionView(actionViewItem);
        // Find the button within action-view
        Button b = (Button) v.findViewById(R.id.qrcode_checkout_done_btn);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Cart.getInstance(currentActivity).resetCart();
                Intent intent = new Intent(currentActivity, ShowMenuActivity.class);
                currentActivity.startActivity (intent);
                currentActivity.finish();
            }
        });
        // Handle button click here
        return super.onPrepareOptionsMenu(menu);
    }
}
