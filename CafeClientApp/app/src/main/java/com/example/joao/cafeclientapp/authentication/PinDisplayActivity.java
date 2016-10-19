package com.example.joao.cafeclientapp.authentication;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.joao.cafeclientapp.R;
import com.example.joao.cafeclientapp.menu.ShowMenuActivity;

public class PinDisplayActivity extends AppCompatActivity {

    Activity currentActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.menu.actionbar_pin_display);*/

        setContentView(R.layout.activity_pin_display);

        Intent intent = getIntent();
        String pin = intent.getExtras().getString("pin");

        TextView pin_display = (TextView) findViewById(R.id.view_pin_text);
        pin_display.setText(pin);

        currentActivity = this;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actionbar_pin_display, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem actionViewItem = menu.findItem(R.id.actionButton);
        // Retrieve the action-view from menu
        View v = MenuItemCompat.getActionView(actionViewItem);
        // Find the button within action-view
        Button b = (Button) v.findViewById(R.id.btnCustomAction);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(currentActivity, ShowMenuActivity.class);
                currentActivity.startActivity (intent);
                currentActivity.finish();
            }
        });
        // Handle button click here
        return super.onPrepareOptionsMenu(menu);
    }

}
