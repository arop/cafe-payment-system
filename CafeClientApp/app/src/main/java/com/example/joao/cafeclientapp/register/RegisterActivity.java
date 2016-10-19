package com.example.joao.cafeclientapp.register;

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

import com.devmarvel.creditcardentry.library.CreditCard;
import com.devmarvel.creditcardentry.library.CreditCardForm;
import com.example.joao.cafeclientapp.CustomLocalStorage;
import com.example.joao.cafeclientapp.R;
import com.example.joao.cafeclientapp.ServerRestClient;
import com.example.joao.cafeclientapp.menu.ShowMenuActivity;
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
            "João Norim Bandeira", "norim_17@hotmail.com", "999999999", "5444640177212251", "235", "12/16"
    };

    private Context context;
    private Activity currentActivity = this;

    private EditText name_field;
    private EditText email_field;
    private EditText vat_number_field;

    private CreditCardForm credit_card_form;


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

        //fill form with dummy info for testing
        name_field.setText(DUMMY_CREDENTIALS[0]);
        email_field.setText(DUMMY_CREDENTIALS[1]);
        vat_number_field.setText(DUMMY_CREDENTIALS[2]);

        credit_card_form = (CreditCardForm) findViewById(R.id.credit_card_form);
        credit_card_form.setCardNumber(DUMMY_CREDENTIALS[3],false);
        credit_card_form.setSecurityCode(DUMMY_CREDENTIALS[4],false);
        credit_card_form.setExpDate(DUMMY_CREDENTIALS[5],false);
    }


    public void registerAction(View view){

        disableAllFields();

        //get all values
        String name = name_field.getText().toString();
        String email = email_field.getText().toString();
        String vat_number = vat_number_field.getText().toString();

        HashMap<String, String> user_params = new HashMap<String, String>();
        RequestParams user = new RequestParams();

        user_params.put("name", name);
        if(isEmailValid(email)) {
            user_params.put("email", email);
        } else {
            //Alert Credit card invalid
            enableAllFields();
            email_field.requestFocus();
            Toast.makeText(context, "Email not valid!", Toast.LENGTH_LONG).show();
            Log.e("register error","Email not valid!");
            return;
        }

        user_params.put("nif", vat_number);

        user.put("user", user_params);


        if(credit_card_form.isCreditCardValid())
        {
            CreditCard card = credit_card_form.getCreditCard();
            //Pass credit card to service
            user_params.put("credit_card_number", card.getCardNumber());
            user_params.put("credit_card_cvv", card.getSecurityCode());
            user_params.put("credit_card_expiration", card.getExpDate());
        }
        else
        {
            //Alert Credit card invalid
            credit_card_form.clearForm();
            enableAllFields();
            credit_card_form.focusCreditCard();
            Toast.makeText(context, "Credit card not valid!", Toast.LENGTH_LONG).show();
            Log.e("register error","Credit card not valid!");
            return;
        }

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

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject object){
                Log.e("FAILURE:", "some error I dont know how to handle. timeout?");
                Toast.makeText(context, "Server not available...", Toast.LENGTH_SHORT).show();
                enableAllFields();
            }
        });
    }

    private void disableAllFields(){
        name_field.setEnabled(false);
        email_field.setEnabled(false);
        vat_number_field.setEnabled(false);
        credit_card_form.setEnabled(false);
    }

    private void enableAllFields(){
        name_field.setEnabled(true);
        email_field.setEnabled(true);
        vat_number_field.setEnabled(true);
        credit_card_form.setEnabled(true);
    }

    private boolean isEmailValid(String email) {
        if (email == null)
            return false;

        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

}

