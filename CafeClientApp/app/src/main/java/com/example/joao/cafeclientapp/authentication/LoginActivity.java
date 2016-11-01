package com.example.joao.cafeclientapp.authentication;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

import com.example.joao.cafeclientapp.CustomLocalStorage;
import com.example.joao.cafeclientapp.R;
import com.example.joao.cafeclientapp.ServerRestClient;
import com.example.joao.cafeclientapp.user.User;
import com.example.joao.cafeclientapp.menu.ShowMenuActivity;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {
    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    private Activity currentActivity;

    // DO NOT TOUCH THIS, even if you think this is wrong, it's not
    private String pin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Skip authentication if already logged.
        if (CustomLocalStorage.getString(this, "uuid") != null)
        {
            Intent intent = new Intent(this, ShowMenuActivity.class);
            this.startActivity (intent);
            this.finish();
            return;
        }

        setContentView(R.layout.activity_login);

        currentActivity = this;

        // Set up the login form.
        mEmailView = (EditText) findViewById(R.id.email);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        Button mGoToRegisterButton = (Button) findViewById(R.id.register_button);
        mGoToRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                goToRegister();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    /**
     * Go to authentication activity
     */
    private void goToRegister() {
        Intent intent = new Intent(currentActivity, RegisterActivity.class);
        startActivity(intent);
        currentActivity.finish();
    }

    /**
     * Attempts to sign in the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            attemptLoginInServer(email,password);
        }
    }

    private void attemptLoginInServer(String email, String password) {
        HashMap<String, String> user_params = new HashMap<>();
        RequestParams user = new RequestParams();

        user_params.put("email", email);
        user_params.put("pin", password);

        user.put("user",user_params);

        this.pin = password;

        ServerRestClient.post("login", user, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try{
                    String error = response.get("error").toString();
                    Toast.makeText(currentActivity.getApplicationContext(), error, Toast.LENGTH_LONG).show();
                    //FAIL LOGIN
                    onPostExecute(false);
                    return;
                }
                catch(JSONException e){
                    //normal behaviour when there are no errors.
                }

                try{
                    String uuid = response.get("id").toString();
                    String name = response.get("name").toString();
                    String email = response.get("email").toString();
                    String nif = response.get("nif").toString();
                    int pcc = response.getInt("primary_credit_card");

                    JSONArray credit_cards = (JSONArray) response.get("creditcards");
                    for(int i = 0; i < credit_cards.length(); i++){
                        JSONObject cc = (JSONObject) credit_cards.get(i);
                        User.getInstance(currentActivity).addCreditCard(cc.getInt("id"),cc.getString("number"),cc.getString("expiration"));
                    }

                    CustomLocalStorage.set(currentActivity, "uuid", uuid);
                    CustomLocalStorage.set(currentActivity, "pin", pin);

                    // SET SINGLETON USER
                    User.getInstance(currentActivity).setName(name);
                    User.getInstance(currentActivity).setEmail(email);
                    User.getInstance(currentActivity).setPin(pin);
                    User.getInstance(currentActivity).setUuid(uuid);
                    User.getInstance(currentActivity).setNif(nif);
                    User.getInstance(currentActivity).setPrimaryCreditCard(pcc);

                    User.saveInstance(currentActivity);
                    onPostExecute(true);

                    //SUCCESS LOGIN
                    Toast.makeText(currentActivity.getApplicationContext(), "Successful login!", Toast.LENGTH_LONG).show();
                    Log.e("user",response.toString());
                }
                catch(JSONException e){
                    Log.e("FAILURE:", "error parsing response JSON");
                    e.printStackTrace();
                    showProgress(false);
                    Toast.makeText(currentActivity.getApplicationContext(), "Server error. Try again later.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String error, Throwable throwable){
                Log.e("FAILURE:", error);
                showProgress(false);
                Toast.makeText(currentActivity.getApplicationContext(), "Server not available...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject object) {
                Log.e("FAILURE:", "some error I dont know how to handle. timeout?");
                showProgress(false);
                Toast.makeText(currentActivity.getApplicationContext(), "Server not available...", Toast.LENGTH_SHORT).show();
            }

        });
    }

    private boolean isEmailValid(String email) {
        if (email == null)
            return false;

        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Pin has 4 digits
     * @param password
     * @return
     */
    private boolean isPasswordValid(String password) {
        return password.length() == 4;
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

    private void onPostExecute(final Boolean success) {
        showProgress(false);

        if (success) {
            //Start show pin activity
            Intent intent = new Intent(currentActivity, ShowMenuActivity.class);
            startActivity(intent);
            currentActivity.finish();
        } else {
            mPasswordView.setError(getString(R.string.error_incorrect_password));
            mPasswordView.requestFocus();
        }
    }
}

