package com.example.joao.cafeclientapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Skip register if already logged.
        if (CustomLocalStorage.getString(this, "uuid") != null)
        {
            Intent intent = new Intent(this, ShowMenuActivity.class);
            this.startActivity (intent);
            this.finish();
            return;
        }

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
                    currentActivity.finish();

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

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

}

