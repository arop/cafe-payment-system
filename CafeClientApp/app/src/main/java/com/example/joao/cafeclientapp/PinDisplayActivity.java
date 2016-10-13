package com.example.joao.cafeclientapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class PinDisplayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_display);

        Intent intent = getIntent();
        String pin = intent.getExtras().getString("pin");

        TextView pin_display = (TextView) findViewById(R.id.view_pin_text);
        pin_display.setText(pin);
    }
}
