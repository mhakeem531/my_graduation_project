package com.example.hakeem.demo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;

    private EditText emailText;
    private EditText passwordText;
    private Button loginButton;
    private TextView signupLink;
    public ProgressDialog progressDialog;
    //private LinearLayout logInWithGooglePlus;


    private SessionManager session;
    private AppUserInfoDbHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /**
         *      {{{{{{{{{fetching UI}}}}}}}}}
         * **/
        emailText = findViewById(R.id.input_email);
        passwordText = findViewById(R.id.input_password);
        loginButton = findViewById(R.id.btn_login);
        signupLink = findViewById(R.id.link_signup);
       // logInWithGooglePlus = findViewById(R.id.login_in_with_gmail);

        progressDialog = new ProgressDialog(this/*, R.style.AppTheme_Dark_Dialog*/);
        progressDialog.setCancelable(false);



        /**
         * {{{SQLite database handler}}}
         * */

        db = new AppUserInfoDbHelper(getApplicationContext());

        /**
         * {{{check login state before start app}}}
         * */
        session = new SessionManager(getApplicationContext());
        session.setShareListeningState(false);
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            //TODO CHANGE AFTER DRAWER
            //Intent intent = new Intent(LoginActivity.this, SemiStartActivity.class);
            Intent intent = new Intent(LoginActivity.this, StartActivity.class);
            startActivity(intent);
            finish();
        }


        /**
         * {{{clicking handling}}}
         * */

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });


        signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });


//        logInWithGooglePlus.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                LogIntoWithGooglePlus();
//                session.setLoggedInWithGooglePLusState(true);
//            }
//        });

    }

    private void showDialog() {
        if (!progressDialog.isShowing())
            progressDialog.show();
    }

    private void hideDialog() {
        if (progressDialog.isShowing())
            progressDialog.hide();
    }

    public void login() {
        Log.d(TAG, "Login");

        if (!isNetworkAvailable()) {

            Toast.makeText(getApplicationContext(),
                    R.string.check_available_network, Toast.LENGTH_LONG)
                    .show();
        } else if (!validate()) {

            onLoginFailed();

            return;

        } else {

            loginButton.setEnabled(false);

            // final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this, R.style.AppTheme_Dark_Dialog);

            progressDialog.setIndeterminate(true);

            progressDialog.setMessage(getApplicationContext().getResources().getString(R.string.authenticating));

            showDialog();

            String email = emailText.getText().toString().trim();
            String password = passwordText.getText().toString().trim();

            if (!email.isEmpty() && !password.isEmpty()) {

                checkLogin(email, password);


                //TODO IF DEVICE IS NOT ONLINE DON'T MAKE LOGIN  ---> completed

                //TODO here check if device token refreshed you should update it's value in DB in server

            } else {
                // Prompt user to enter credentials
                Toast.makeText(getApplicationContext(),
                        R.string.enter_form_parameter, Toast.LENGTH_LONG)
                        .show();
            }


            // TODO: Implement your own authentication logic here.  ---> completed

            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            // On complete call either onLoginSuccess or onLoginFailed
                            // onLoginSuccess();
                            // onLoginFailed();
                            hideDialog();
                        }
                    }, 3000);
        }
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

    /**
     * function to verify login details in mysql db
     */
    private void checkLogin(final String email, final String password) {

        progressDialog.setMessage(getApplicationContext().getResources().getString(R.string.logging));
//        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                Variables.URL_LOGIN, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Login Response: " + response);
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {

                        // user successfully logged in
                        // Create login session
                        session.setLogin(true);

                        // Now store the user in SQLite
                        String uid = jObj.getString("uid");
Log.e("ID-FOR-FEED", uid);
                        JSONObject user = jObj.getJSONObject("user");
                        String userName = user.getString("userName");
                        String displayedName = user.getString("displayedName");
                        String email = user.getString("email");
                        String created_at = user.getString("created_at");

                        session.setUserMail(email);
                        // Inserting row in users table
                        db.addUser(userName, displayedName, email, uid, created_at, null);

                        //setProfileImageIntoUserTable(email);

                        downloadProfile(email, userName);

                        if (Variables.isTokenRefreshedAfterLogIn) {
                            Variables.isTokenRefreshedAfterLogIn = false;

                            //TODO here update token value of this device --> complete

                            updateThisDeviceToken(email, session.getDeviceToken());
                        }
                        // Launch main activity
                        //TODO CHANGE AFTER DRAWER
                        Intent intent = new Intent(LoginActivity.this,
                                StartActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                        loginButton.setEnabled(true);
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();

            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);
                params.put("password", password);

                return params;
            }

        };

        // Adding request to request queue
        //AppController.getInstance().addToRequestQueue(strReq, tag_string_req);

        /**Volley.newRequestQueue(this).add(strReq);*/

        MyVolley.getInstance(this).addToRequestQueue(strReq);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {

                Log.e("onActivityResult", "onActivityResult");
                // TODO: Implement successful signup logic here  ---> completed
                // By default we just finish the Activity and log them in automatically
                this.finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess() {
        loginButton.setEnabled(true);
        Log.e("hhhxxhhh", "hhhhh");
        finish();
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), R.string.login_failed, Toast.LENGTH_LONG).show();
        loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = emailText.getText().toString().trim();
        String password = passwordText.getText().toString().trim();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError(getApplicationContext().getResources().getString(R.string.enter_valid_mail));
            valid = false;
        } else {
            emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            passwordText.setError(getApplicationContext().getResources().getString(R.string.password_check));
            valid = false;
        } else {
            passwordText.setError(null);
        }

        return valid;
    }

    /**
     * invoke profile image from server and update user table and insert it
     */
    private void setProfileImageIntoUserTable(final String userEmail) {

        progressDialog.setMessage(getApplicationContext().getResources().getString(R.string.image_fetch));
        showDialog();

        //  final String uploadImage = getStringImage(image);

        StringRequest strReq = new StringRequest(Request.Method.POST,
                Variables.URL_INVOKE_PROFILE_IMAGE, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "i---nvoked: " + response);
                hideDialog();


                if(!response.equals("\nfff")){
                    byte[] encodeByte = Base64.decode(response, Base64.DEFAULT);
                    Log.e("from00--server", encodeByte.toString() + "mail " + userEmail);

                    db.updateUserProfileImage(userEmail, encodeByte);



                    ByteArrayInputStream imageStream = new ByteArrayInputStream(encodeByte);
                    Bitmap theImage = BitmapFactory.decodeStream(imageStream);
                    //Variables.statueImage = theImage;

                }else{
                    Toast.makeText(getApplicationContext(), "can't fetch you'r profile",Toast.LENGTH_LONG).show();
                }




            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "invockation  Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {

                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("mail", userEmail);

                return params;
            }

        };

        // Adding request to request queue
        // AppController.getInstance().addToRequestQueue(strReq, tag_string_req);


        // Volley.newRequestQueue(this).add(strReq);
        MyVolley.getInstance(this).addToRequestQueue(strReq);
    }

    /**
     * invoke profile image  of member added by admin to be displayed in list item
     * @param email : username of added member
     */
    private void downloadProfile(final String email, final String username) {

        //  final String uploadImage = getStringImage(image);

        StringRequest strReq = new StringRequest(Request.Method.POST,
                Variables.URL_INVOKE_ADDED_MEMBER_PROFILE_IMAGE, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                Log.e("user-mebxxder-", response);
                if(!response.equals("\nfff")){

                    byte[] encodeByte = Base64.decode(response, Base64.DEFAULT);


                    Log.e("user-meber-phtot", encodeByte.toString());
                    ByteArrayInputStream imageStream = new ByteArrayInputStream(encodeByte);
                    Bitmap theImage = BitmapFactory.decodeStream(imageStream);



                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    theImage.compress(Bitmap.CompressFormat.PNG, 0, outputStream);

                    byte[] imag = outputStream.toByteArray();
                    db.updateUserProfileImage(email,imag);

                    Log.e("INFO0", imageStream.toString());
                }else {
                    Log.e("user-mebxer-", response);

                }



            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {

            @Override
            protected Map<String, String> getParams() {

                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", username);

                return params;
            }

        };

        MyVolley.getInstance(this).addToRequestQueue(strReq);
    }



    private void updateThisDeviceToken(final String accountMail, final String deviceToken) {

        //TODO make "device toke updating" here

        StringRequest strReq = new StringRequest(Request.Method.POST,
                Variables.URL_UPDATE_DEVICE_TOKEN_AFTER_LOGIN, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                if(response.equals("updated")){

                    Toast.makeText(getApplicationContext(), "device token updated successfully", Toast.LENGTH_LONG).show();
                }else{

                    Toast.makeText(getApplicationContext(), R.string.device_token_didnt_update, Toast.LENGTH_LONG).show();
                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(getApplicationContext(), R.string.device_token_didnt_update, Toast.LENGTH_LONG).show();
            }
        }){

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("mail", accountMail);
                params.put("device_token", deviceToken);

                Log.e("updated-mail-is", accountMail + "  " + deviceToken);

                return params;
            }
        };

        MyVolley.getInstance(this).addToRequestQueue(strReq);
    }



//    private void LogIntoWithGooglePlus(){
//
//    }

}

