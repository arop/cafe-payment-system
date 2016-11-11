package com.example.joao.cafeclientapp.authentication;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
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
import com.example.joao.cafeclientapp.user.User;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

/**
 * A login screen that offers login via email/password.
 */
public class RegisterActivity extends AppCompatActivity {

    private Context context;
    private Activity currentActivity = this;

    private View mProgressView;
    private View mLoginFormView;

    private EditText name_field;
    private EditText email_field;
    private EditText vat_number_field;

    private CreditCardForm credit_card_form;

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

        //go to login
        Button mGoToLoginInButton = (Button) findViewById(R.id.login_button);
        mGoToLoginInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                goToLogin();
            }
        });

        //populate class variables
        context = getApplicationContext();

        name_field = (EditText) findViewById(R.id.name);
        email_field = (EditText) findViewById(R.id.email);
        vat_number_field = (EditText) findViewById(R.id.vat_number);
        credit_card_form = (CreditCardForm) findViewById(R.id.credit_card_form);

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    public void registerAction(View view){
        showProgress(true);

        //get all values
        String name = name_field.getText().toString();
        String email = email_field.getText().toString();
        String vat_number = vat_number_field.getText().toString();

        HashMap<String, String> user_params = new HashMap<>();
        RequestParams user = new RequestParams();

        user_params.put("name", name);
        if(isEmailValid(email)) {
            user_params.put("email", email);
        } else {
            //Alert Credit card invalid
            showProgress(false);
            email_field.requestFocus();
            Toast.makeText(context, "Email not valid!", Toast.LENGTH_LONG).show();
            Log.e("authentication error","Email not valid!");
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
            showProgress(false);
            credit_card_form.focusCreditCard();
            Toast.makeText(context, "Credit card not valid!", Toast.LENGTH_LONG).show();
            Log.e("authentication error","Credit card not valid!");
            return;
        }

        registerCall(user);
    }

    /**
     * HTTP post to server, register user
     * @param user
     */
    private void registerCall(RequestParams user) {
        ServerRestClient.post("register", user, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try{
                    String error = response.get("error").toString();
                    Toast.makeText(context, error, Toast.LENGTH_LONG).show();
                    showProgress(false);
                    return;
                }
                catch(JSONException e){
                    //normal behaviour when there are no errors.
                }

                try{
                    String uuid = response.get("id").toString();
                    String pin = response.get("pin").toString();
                    String name = response.get("name").toString();
                    String email = response.get("email").toString();
                    String nif = response.get("nif").toString();
                    int pcc = response.getInt("primary_credit_card");

                    JSONArray credit_cards = (JSONArray) response.get("creditcards");
                    for(int i = 0; i < credit_cards.length(); i++){
                        JSONObject cc = (JSONObject) credit_cards.get(i);
                        User.getInstance(currentActivity).addCreditCard(cc.getInt("id"),cc.getString("number"),cc.getString("expiration"));
                    }

                    //SUCCESS
                    CustomLocalStorage.set(currentActivity, "uuid", uuid);
                    CustomLocalStorage.set(currentActivity, "pin", pin);
                    Toast.makeText(context, "Registed Successfully!", Toast.LENGTH_LONG).show();

                    // SET SINGLETON USER
                    User.getInstance(currentActivity).setName(name);
                    User.getInstance(currentActivity).setEmail(email);
                    User.getInstance(currentActivity).setPin(pin);
                    User.getInstance(currentActivity).setUuid(uuid);
                    User.getInstance(currentActivity).setNif(nif);
                    User.getInstance(currentActivity).setPrimaryCreditCard(pcc);

                    User.saveInstance(currentActivity);

                    //Start show pin activity
                    Intent intent = new Intent(currentActivity, PinDisplayActivity.class);
                    intent.putExtra("pin", pin);
                    startActivity(intent);
                    currentActivity.finish();
                }
                catch(JSONException e){
                    Log.e("FAILURE:", "error parsing response JSON");
                    Toast.makeText(context, "Server error. Try again later.", Toast.LENGTH_LONG).show();
                    showProgress(false);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String error, Throwable throwable){
                Log.e("FAILURE:", error);
                Toast.makeText(context, "Server not available...", Toast.LENGTH_SHORT).show();
                showProgress(false);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject object){
                Log.e("FAILURE:", "some error I dont know how to handle. timeout?");
                Toast.makeText(context, "Server not available...", Toast.LENGTH_SHORT).show();
                showProgress(false);
            }
        });
    }

    /**
     * Go to login activity
     */
    private void goToLogin() {
        Intent intent = new Intent(currentActivity, LoginActivity.class);
        startActivity(intent);
        currentActivity.finish();
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private boolean isEmailValid(String email) {
        return email != null && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

}

