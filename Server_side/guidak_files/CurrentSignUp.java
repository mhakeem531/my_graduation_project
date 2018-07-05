package com.example.hakeem.demo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.hakeem.demo.NetworkUtilites.MyVolley;
import com.example.hakeem.demo.helper.AppUserInfoDbHelper;
import com.example.hakeem.demo.helper.SessionManager;
import com.example.hakeem.demo.utilities.Variables;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;

public class SignupActivity extends AppCompatActivity {

    private static final String TAG = "SignupActivity";

    private EditText nameText;
    private EditText emailText;
    private EditText passwordText;
    private EditText reEnterPasswordText;
    private Button signupButton;
    private TextView loginLink;

    private SessionManager session;
    private AppUserInfoDbHelper db;
    public ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);

        progressDialog = new ProgressDialog(this/*, R.style.AppTheme_Dark_Dialog*/);
        progressDialog.setCancelable(false);

        nameText = findViewById(R.id.input_name);

        emailText = findViewById(R.id.input_email);


        passwordText = findViewById(R.id.input_password);

        reEnterPasswordText = findViewById(R.id.input_reEnterPassword);

        signupButton = findViewById(R.id.btn_signup);

        loginLink = findViewById(R.id.link_login);

        // Session manager
        session = new SessionManager(getApplicationContext());

        // SQLite database handler
        db = new AppUserInfoDbHelper(getApplicationContext());

        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            Intent intent = new Intent(SignupActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }


        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });

    }

    private void showDialog() {
        if (!progressDialog.isShowing())
            progressDialog.show();
    }

    private void hideDialog() {
        if (progressDialog.isShowing())
            progressDialog.hide();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert manager != null;
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            // Network is present and connected
            isAvailable = true;
        }
        return isAvailable;
    }


    public void signup() {
        Log.d(TAG, "Sign up");

        if (!isNetworkAvailable()) {
            Toast.makeText(getApplicationContext(),
                    "Please check connection state!", Toast.LENGTH_LONG)
                    .show();
        } else if (!validate()) {

            onSignupFailed();
           /// return;

        } else {

            signupButton.setEnabled(false);

         final ProgressDialog progressDialog = new ProgressDialog(SignupActivity.this,
                R.style.AppTheme_Dark_Dialog);

            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Creating Account...");
            showDialog();

            String name = nameText.getText().toString().trim();
            String email = emailText.getText().toString().trim();
            String password = passwordText.getText().toString().trim();
            String reEnterPassword = reEnterPasswordText.getText().toString().trim();

            StringBuilder usernameBuilder = new StringBuilder();
            int i = 0;
            while (email.charAt(i) != '@') {
                usernameBuilder.append(email.charAt(i++));
            }

            String username = usernameBuilder.toString();

            String token = session.getDeviceToken();

            /**
             * //TODO come here to check if this username is already token or not
             *
             * if username wasn't token before
             *     insert it to DB in server
             *     show alert to user tell him by his username and he could user it in share listening feature
             * else if username already token
             *      show dialog to tell him "now please select username for your account!"(as used to enter member in group)
             *      SemiStartActivity will not lunched before he select unique username.
             * */






            registerUser(email, username, password, name, token);

            // TODO: Implement your own signup logic here.  ---> completed


            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            // On complete call either onSignupSuccess or onSignupFailed
                            // depending on success
                         //   onSignupSuccess();
                            // onSignupFailed();
                            //  progressDialog.dismiss();
                            hideDialog();
                        }
                    }, 3000);
        }
    }


    /**
     * Function to store user in MySQL database will post params(tag, name,
     * email, password) to register url
     */
    private void registerUser(final String email, final String userName,
                              final String password, final String displayedName,
                              final String deviceToken) {
        // Tag used to cancel the request
        String tag_string_req = "req_register";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                Variables.URL_REGISTER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Register Response: " + response);

                //hideDialog();
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        // User successfully stored in MySQL
                        // Now store the user in sqlite
                        String uid = jObj.getString("uid");

                        JSONObject user = jObj.getJSONObject("user");
                        String userName = user.getString("userName");
                        String displayedName = user.getString("displayedName");
                        String email = user.getString("email");
                        String created_at = user.getString("created_at");

                        // Inserting row in users table
                        db.addUser(userName, displayedName, email, uid, created_at, null);


                        Toast.makeText(getApplicationContext(), "User successfully registered. Try login now!", Toast.LENGTH_LONG).show();

                        session.setLogin(true);
                        // Launch login activity
                        Intent intent = new Intent(SignupActivity.this, SemiStartActivity.class);
                        startActivity(intent);
                        finish();

                    } else {

                        // Error occurred in registration. Get the error
                        // message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();

                        Intent i = new Intent(SignupActivity.this, MainActivity.class);
                        startActivity(i);

                        // close this activity
                        finish();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                hideDialog();
                Log.e(TAG, "Registration Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();

                params.put("email", email);

                params.put("displayed_name", displayedName);

                params.put("password", password);

                params.put("user_name", userName);

                params.put("token", deviceToken);

                return params;
            }

        };

        // Adding request to request queue
        //AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
       /** Volley.newRequestQueue(this).add(strReq);*/

        MyVolley.getInstance(this).addToRequestQueue(strReq);
    }


    public void onSignupSuccess() {
        signupButton.setEnabled(true);
        setResult(RESULT_OK, null);
        finish();
    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        signupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String name = nameText.getText().toString().trim();
        String email = emailText.getText().toString().trim();
        String password = passwordText.getText().toString().trim();
        String reEnterPassword = reEnterPasswordText.getText().toString().trim();

        if (name.isEmpty() || name.length() < 3) {
            nameText.setError("at least 3 characters");
            valid = false;
        } else {
            nameText.setError(null);
        }


        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError("enter a valid email address");
            valid = false;
        } else {
            emailText.setError(null);
        }


        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            passwordText.setError(null);
        }

        if (reEnterPassword.isEmpty() || reEnterPassword.length() < 4 || reEnterPassword.length() > 10 || !(reEnterPassword.equals(password))) {
            reEnterPasswordText.setError("Password Do not match");
            valid = false;
        } else {
            reEnterPasswordText.setError(null);
        }

        return valid;
    }

}
