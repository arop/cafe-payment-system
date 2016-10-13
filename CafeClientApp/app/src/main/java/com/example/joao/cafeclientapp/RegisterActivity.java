package com.example.joao.cafeclientapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.*;
import com.loopj.android.http.*;

import cz.msebera.android.httpclient.Header;

import static android.Manifest.permission.READ_CONTACTS;
import static android.provider.AlarmClock.EXTRA_MESSAGE;

/**
 * A login screen that offers login via email/password.
 */
public class RegisterActivity extends AppCompatActivity {



    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "João Norim Bandeira", "norim_17@hotmail.com", "999999999", "4563214512345632", "235", "12/16"
    };


    private Context context;
    private Activity currentActivity = this;

    private EditText name_field;
    private EditText email_field;
    private EditText vat_number_field;
    private EditText credit_card_number_field;
    private EditText credit_card_cvv_field;
    private EditText credit_card_expiration_field;


    /*private DatePickerDialog createDialogWithoutDateField() {
        DatePickerDialog dpd = new DatePickerDialog(this, null, 2014, 1, 24);
        try {
            java.lang.reflect.Field[] datePickerDialogFields = dpd.getClass().getDeclaredFields();
            for (java.lang.reflect.Field datePickerDialogField : datePickerDialogFields) {
                if (datePickerDialogField.getName().equals("mDatePicker")) {
                    datePickerDialogField.setAccessible(true);
                    DatePicker datePicker = (DatePicker) datePickerDialogField.get(dpd);
                    java.lang.reflect.Field[] datePickerFields = datePickerDialogField.getType().getDeclaredFields();
                    for (java.lang.reflect.Field datePickerField : datePickerFields) {
                        Log.i("datames", datePickerField.getName());
                        if ("mDaySpinner".equals(datePickerField.getName())) {
                            datePickerField.setAccessible(true);
                            Object dayPicker = datePickerField.get(datePicker);
                            ((View) dayPicker).setVisibility(View.GONE);
                        }
                    }
                }
            }
        }
        catch (Exception ex) {
        }
        return dpd;
    }*/



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //set action to perform on "Register" click
        Button mEmailSignInButton = (Button) findViewById(R.id.sign_up_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                registerAction(view);
            }
        });

        //populate class variables
        context = getApplicationContext();

        name_field = (EditText) findViewById(R.id.name);
        email_field = (EditText) findViewById(R.id.email);
        vat_number_field = (EditText) findViewById(R.id.vat_number);
        credit_card_number_field = (EditText) findViewById(R.id.credit_card_number);
        credit_card_cvv_field = (EditText) findViewById(R.id.credit_card_cvv);
        credit_card_expiration_field = (EditText) findViewById(R.id.credit_card_expiration);

        //fill form with dummy info for testing
        name_field.setText(DUMMY_CREDENTIALS[0]);
        email_field.setText(DUMMY_CREDENTIALS[1]);
        vat_number_field.setText(DUMMY_CREDENTIALS[2]);
        credit_card_number_field.setText(DUMMY_CREDENTIALS[3]);
        credit_card_cvv_field.setText(DUMMY_CREDENTIALS[4]);
        credit_card_expiration_field.setText(DUMMY_CREDENTIALS[5]);
    }


    public void registerAction(View view){

        disableAllFields();

        //get all values
        String name = name_field.getText().toString();
        String email = email_field.getText().toString();
        String vat_number = vat_number_field.getText().toString();
        String credit_card_number = credit_card_number_field.getText().toString();
        String credit_card_cvv = credit_card_cvv_field.getText().toString();
        String credit_card_expiration = credit_card_expiration_field.getText().toString();

        HashMap<String, String> user_params = new HashMap<String, String>();
        RequestParams user = new RequestParams();

        user_params.put("name", name);
        user_params.put("email", email);
        user_params.put("nif", vat_number);
        user_params.put("credit_card_number", credit_card_number);
        user_params.put("credit_card_cvv", credit_card_cvv);
        user_params.put("credit_card_expiration", credit_card_expiration);

        user.put("user", user_params);

        ServerRestClient.post("register", user, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try{
                    String error = response.get("error").toString();
                    Toast.makeText(context, error, Toast.LENGTH_LONG).show();
                    enableAllFields();
                    return;
                }
                catch(JSONException e){
                    //normal behaviour when there are no errors.
                }

                try{
                    String uuid = response.get("id").toString();
                    String pin = response.get("pin").toString();
                    //SUCCESS
                    CustomLocalStorage.set(currentActivity, "uuid", uuid);
                    Toast.makeText(context, "Registed Successfully!", Toast.LENGTH_LONG).show();

                    //Start show pin activity
                    Intent intent = new Intent(currentActivity, PinDisplayActivity.class);
                    intent.putExtra("pin", pin);
                    startActivity(intent);

                }
                catch(JSONException e){
                    Log.e("FAILURE:", "error parsing response JSON");
                    Toast.makeText(context, "Server error. Try again later.", Toast.LENGTH_LONG).show();
                    enableAllFields();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String error, Throwable throwable){
                Log.e("FAILURE:", error);
                Toast.makeText(context, "Server not available...", Toast.LENGTH_SHORT).show();
                enableAllFields();
            }

        });


    }

    private void disableAllFields(){
        name_field.setEnabled(false);
        email_field.setEnabled(false);
        vat_number_field.setEnabled(false);
        credit_card_number_field.setEnabled(false);
        credit_card_cvv_field.setEnabled(false);
        credit_card_expiration_field.setEnabled(false);
    }

    private void enableAllFields(){
        name_field.setEnabled(true);
        email_field.setEnabled(true);
        vat_number_field.setEnabled(true);
        credit_card_number_field.setEnabled(true);
        credit_card_cvv_field.setEnabled(true);
        credit_card_expiration_field.setEnabled(true);
    }

    /*public void expirationDateOnClick(View view){
        createDialogWithoutDateField().show();
    }*/

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

}

