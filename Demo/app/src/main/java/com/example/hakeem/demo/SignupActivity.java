package com.example.hakeem.demo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.hakeem.demo.NetworkUtilites.MyVolley;
import com.example.hakeem.demo.helper.AppUserInfoDbHelper;
import com.example.hakeem.demo.helper.SessionManager;
import com.example.hakeem.demo.utilities.Variables;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;

public class SignupActivity extends AppCompatActivity {

    private static final String TAG = "SignUpActivity";

    private EditText nameText;
    private EditText emailText;
    private EditText passwordText;
    private EditText reEnterPasswordText;
    private Button signUpButton;
    private TextView loginLink;
    private LinearLayout logInWithGooglePlus;

    private SessionManager session;
    private AppUserInfoDbHelper db;
    public ProgressDialog progressDialog;

    private boolean gotoSemiStartOrInputUsername;

    static int x = 10;
    static boolean isGeneratedUsernameExist;
    String googlePlusUsername = "";

    /**
     * FOR log in with G+
     */
    private static final int RC_SIGN_IN = 9001;

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    private GoogleSignInClient mGoogleSignInClient;


    @Override
    protected void onStart() {
        super.onStart();
        //FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();


        // Session manager
        session = new SessionManager(getApplicationContext());

        // SQLite database handler
        db = new AppUserInfoDbHelper(getApplicationContext());

        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            Intent intent = new Intent(SignupActivity.this, StartActivity.class);
            startActivity(intent);
            finish();
        }


        progressDialog = new ProgressDialog(SignupActivity.this/*, R.style.AppTheme_Dark_Dialog*/);
//        progressDialog.setCancelable(false);

        nameText = findViewById(R.id.input_name);

        emailText = findViewById(R.id.input_email);


        passwordText = findViewById(R.id.input_password);

        reEnterPasswordText = findViewById(R.id.input_reEnterPassword);

        signUpButton = findViewById(R.id.btn_signup);

        loginLink = findViewById(R.id.link_login);

        logInWithGooglePlus = findViewById(R.id.login_in_with_gmail);


        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    signUp();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
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

        logInWithGooglePlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogIntoWithGooglePlus();
                session.setLoggedInWithGooglePLusState(true);
            }
        });

    }

    private void showDialog() {
        progressDialog.setMessage(getResources().getString(R.string.creating_account));
        progressDialog.show();
        // progressDialog = ProgressDialog.show(SignupActivity.this, "Please wait...", "Creating Account......", true);
    }

    private void hideDialog() {
        progressDialog.hide();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        assert manager != null;
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        boolean isAvailable = false;

        if (networkInfo != null && networkInfo.isConnected()) {
            // Network is present and connected
            isAvailable = true;
        }
        return isAvailable;
    }


    public void signUp() throws InterruptedException {
        Log.d(TAG, "Sign up");

        if (!isNetworkAvailable()) {
            Toast.makeText(getApplicationContext(), R.string.check_available_network, Toast.LENGTH_LONG).show();
        } else if (!validate()) {

            onSignUpFailed();
            /// return;

        } else {

            //signupButton.setEnabled(false);


            String name = nameText.getText().toString().trim();
            String email = emailText.getText().toString().trim();
            String password = passwordText.getText().toString().trim();
            String reEnterPassword = reEnterPasswordText.getText().toString().trim();

            StringBuilder usernameBuilder = new StringBuilder();
            int i = 0;
            while (email.charAt(i) != '@') {
                usernameBuilder.append(email.charAt(i++));
            }

            String usernameFromMail = usernameBuilder.toString();

            String token = session.getDeviceToken();


//            final ProgressDialog progressDialog = new ProgressDialog(SignupActivity.this,
//                    R.style.AppTheme_Dark_Dialog);

//            progressDialog.setIndeterminate(true);
//            progressDialog.setMessage("Creating Account...");
            showDialog();


            /**
             * //TODO come here to check if this username is already token or not ---> completed
             *
             * if username wasn't token before
             *     insert it to DB in server
             *     show alert to user tell him by his username and he could user it in share listening feature
             * else if username already token
             *      show dialog to tell him "now please select username for your account!"(as used to enter member in group)
             *      SemiStartActivity will not lunched before he select unique username.
             * */

            //if true --> go to prompt username from user
            //if false --> go to SemiStartActivity
            gotoSemiStartOrInputUsername = connectToCheckGeneratedUsername(usernameFromMail);

            if (gotoSemiStartOrInputUsername) {
                ////////////////////////////goto prombet
            }

            registerUser(email, usernameFromMail, password, name, token);

            // TODO: Implement your own sign up logic here.  ---> completed


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


    private boolean connectToCheckGeneratedUsername(final String currentSelectedUsername) throws InterruptedException {


        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                isGeneratedUsernameExist = checkUserNameIsUnique(currentSelectedUsername);
                x = 20;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("hakeemx[0] = ", " " + x);
                    }
                });

            }
        });

        thread.start();
        thread.join();

        return isGeneratedUsernameExist;

    }

    private boolean checkUserNameIsUnique(final String currentSelectedUsername) {

        String connectionResult = null;

        try {
            String statueNameKey = "username=";


            //String selectUrl = Variables.serverUrl + "guidak_files/select_audio_file_path_with_POST.php";
            String selectUrl = Variables.URL_TEST_SELECTED_USERNAME;


            String connectionParameters = statueNameKey
                    + URLEncoder.encode(currentSelectedUsername, "UTF-8");

            Log.e("HTTP_CONNECTION", "ppp " + connectionParameters);

            byte[] parametersByt = connectionParameters.getBytes("UTF-8");

            URL SelectAudioFilePathUrl = new URL(selectUrl);


            HttpURLConnection filePathConnection = (HttpURLConnection) SelectAudioFilePathUrl.openConnection();

            filePathConnection.setRequestMethod("POST");
            filePathConnection.setDoInput(true);
            filePathConnection.setDoOutput(true);
            Log.e("HTTP_CONNECTION", "1");

            filePathConnection.getOutputStream().write(parametersByt);
            filePathConnection.getOutputStream().flush();
            filePathConnection.getOutputStream().close();

            Log.e("HTTP_CONNECTION", "2");

            InputStreamReader resultStreamReader = new InputStreamReader(filePathConnection.getInputStream());
            Log.e("HTTP_CONNECTION", "3");

            BufferedReader resultReader = new BufferedReader(resultStreamReader);
            Log.e("HTTP_CONNECTION", "4");

            String line;
            final StringBuilder textBulider = new StringBuilder();
            while ((line = resultReader.readLine()) != null) {
                textBulider.append(line);

            }
            //  connectionResult = resultReader.readLine();

            connectionResult = textBulider.toString();


            Log.e("HTTP_CONNECTION", "res -" + connectionParameters);
            resultReader.close();
            resultStreamReader.close();
            filePathConnection.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        Log.e("HTTP_CONNECTION", "file path  fom method is " + connectionResult);
        //1 --> inDB
        //0--> not

        assert connectionResult != null;
        if (connectionResult.equals("1")) {
            return true;
        } else {
            return false;
        }
        //return connectionResult == "1";


    }

    /**
     * Function to store user in MySQL database will post params(tag, name,
     * email, password) to register url
     */
    private void registerUser(final String email, final String userName,
                              final String password, final String displayedName,
                              final String deviceToken) {


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


                        Toast.makeText(getApplicationContext(), R.string.successfully_registered, Toast.LENGTH_LONG).show();


                        // Launch login activity

                        if (isGeneratedUsernameExist) {

                            session.setLoggedInWithGooglePLusState(false);
                            session.setLogin(false);
                            session.setUserMail(email);
                            //TODO intent will go go activity to require user input a username   --> completed
                            Toast.makeText(getApplicationContext(), R.string.sty_to_select_username, Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(SignupActivity.this, PromptUniqueUsernameActivity.class);

                            intent.putExtra("mail", email);
                            startActivity(intent);
                            finish();


                        } else {
                            session.setLoggedInWithGooglePLusState(false);
                            session.setLogin(true);
                            //TODO our generated username is currently unique then go to StartActivity --> completed
                            //TODO CHANGE AFTER DRAWER  --> completed
                            Intent intent = new Intent(SignupActivity.this, StartActivity.class);
                            startActivity(intent);
                            finish();
                        }


                    } else {

                        // Error occurred in registration. Get the error
                        // message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg + "*****", Toast.LENGTH_LONG).show();

                        Intent i = new Intent(SignupActivity.this, LoginActivity.class);
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
        signUpButton.setEnabled(true);
        setResult(RESULT_OK, null);
        finish();
    }

    public void onSignUpFailed() {

        Toast.makeText(getBaseContext(), R.string.sign_up_failed, Toast.LENGTH_LONG).show();

        signUpButton.setEnabled(true);
    }

    public boolean validate() throws InterruptedException {
        boolean valid = true;

        String name = nameText.getText().toString().trim();
        final String email = emailText.getText().toString().trim();
        String password = passwordText.getText().toString().trim();
        String reEnterPassword = reEnterPasswordText.getText().toString().trim();

        if (name.isEmpty() || name.length() < 3) {
            nameText.setError(getResources().getString(R.string.name_check));
            valid = false;
        } else {
            nameText.setError(null);
        }


        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError(getResources().getString(R.string.mail_check));
            valid = false;
        } else {
            emailText.setError(null);
        }

        final boolean[] x = new boolean[1];
        if (!email.isEmpty()) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {

                    x[0] = isMailValid(email);
                }
            });
            thread.start();
            thread.join();
        }


        if (!x[0]) {
            emailText.setError(getString(R.string.mail_valid));
            valid = false;
        } else {
            emailText.setError(null);
        }


        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            passwordText.setError(getResources().getString(R.string.password_check));
            valid = false;
        } else {
            passwordText.setError(null);
        }

        if (reEnterPassword.isEmpty() || reEnterPassword.length() < 4 || reEnterPassword.length() > 10 || !(reEnterPassword.equals(password))) {
            reEnterPasswordText.setError(getResources().getString(R.string.password_match));
            valid = false;
        } else {
            reEnterPasswordText.setError(null);
        }

        return valid;
    }


    private boolean isMailValid(String userSignUpMail) {

        String connectionResult = " ";
        try {
            String mailKey = "mail=";

            //String selectUrl = Variables.serverUrl + "guidak_files/select_audio_file_path_with_POST.php";
            String selectUrl = Variables.URL_CHECK_REGISTERD_MAIL_VALIDATION;

            //  Log.e(LOG_TAG, "ppp " +statueName + "---" + language);

            String connectionParameters = mailKey
                    + URLEncoder.encode(userSignUpMail, "UTF-8");


            byte[] parametersByt = connectionParameters.getBytes("UTF-8");

            URL SelectAudioFilePathUrl = new URL(selectUrl);


            HttpURLConnection filePathConnection = (HttpURLConnection) SelectAudioFilePathUrl.openConnection();

            filePathConnection.setRequestMethod("POST");
            filePathConnection.setDoInput(true);
            filePathConnection.setDoOutput(true);

            filePathConnection.getOutputStream().write(parametersByt);
            filePathConnection.getOutputStream().flush();
            filePathConnection.getOutputStream().close();


            InputStreamReader resultStreamReader = new InputStreamReader(filePathConnection.getInputStream());

            BufferedReader resultReader = new BufferedReader(resultStreamReader);

            String line;
            final StringBuilder textBuilder = new StringBuilder();
            while ((line = resultReader.readLine()) != null) {
                textBuilder.append(line);

            }
            //  connectionResult = resultReader.readLine();
            connectionResult = textBuilder.toString();
            resultReader.close();
            resultStreamReader.close();
            filePathConnection.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return connectionResult.equals("good");
    }


    /*************************************************
     *
     *    {{{{{{{{{{Functions For G+ log in}}}}}}}}}}
     *https://github.com/firebase/quickstart-android/blob/da5e1c8a6174b42fb9cb559a5ce5249ea27d8bf7/auth/app/src/main/java/com/google/firebase/quickstart/auth/GoogleSignInActivity.java#L72-L76
     **************************************************/
    private void LogIntoWithGooglePlus() {

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {

                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);

            } catch (ApiException e) {

                // Google Sign In failed, update UI appropriately
                Log.e(TAG, "Google-sign-in-failed", e);
                // [START_EXCLUDE]
                try {
                    getAndStoreUserInfo(null);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                // [END_EXCLUDE]

            }
        }
    }

    /**
     * START auth_with_google
     */
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        // [START_EXCLUDE silent]
        //   showProgressDialog();
        // [END_EXCLUDE]

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            try {
                                getAndStoreUserInfo(user);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());

                            Toast.makeText(getApplicationContext(), "Authentication Failed", Toast.LENGTH_LONG).show();
                            try {
                                getAndStoreUserInfo(null);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        // [START_EXCLUDE]
                        ////    hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
    }



    /**
     * if user used this gmail before to log in using G+ button
     * so we will fetch it's username from DB from server to store it in DB of app
     * if it used to sign up but within register from so he will be asked to log in vai log in form
     * else we will insert new user with this gmail to DB on server
     */
    private void getAndStoreUserInfo(FirebaseUser user) throws InterruptedException, IOException {
        //  hideProgressDialog();
        if (user != null) {

            String email = user.getEmail();
            String displayedName = user.getDisplayName();
            Uri photoUrl = user.getPhotoUrl();

            int used = checkIfMailUsedBeforeOrNot(email);

            Bitmap GmailProfilePhotoBitmap = null;
            if (photoUrl != null){

                GmailProfilePhotoBitmap = getBitmapFromURL(photoUrl);
            }



            //covert bitmap to byt array to be stored in app DB

            if (Variables.isTokenRefreshedAfterLogIn) {
                updateThisDeviceToken(email, session.getDeviceToken());
            }


            Log.e("ALL-INFO", " " + user.getDisplayName() + " " + user.getEmail());
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            assert GmailProfilePhotoBitmap != null;
            GmailProfilePhotoBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            GmailProfilePhotoBitmap.recycle();

            /**
             * used values
             *            0-->this gmail is used before but with registration form not by "google plus" button
             *            1-->~    ~    ~   ~    ~     and within google plus button from app and we will fetch its username
             *            3-->we will register new user with G+ login
             * */
            if (used == 1) {
                //this gmail was not used by another user in register form
                //but used before to login with G+ button
                /**
                 * so we we need to fetch its username from server
                 *                  store displayed-name, username, profile-photo in app DB
                 * */


                String id = returnUSER_ID(email);
                if (db.addUser(googlePlusUsername, displayedName, email, id, null, byteArray) > 0) {
                    Log.e("is-log-in", "isisisis");
                    session.setLogin(true);
                    Intent intent = new Intent(SignupActivity.this, StartActivity.class);
                    startActivity(intent);
                    finish();
                }


            } else if (used == 0) {
                //this gmail is used by user to register within register form
                Toast.makeText(this, getString(R.string.gmail_used_before) +
                        getString(R.string.use_gmail_in_login_form), Toast.LENGTH_LONG).show();
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();

            } else if (used == 3) {


                Log.e("isGeneratedtcccd223", String.valueOf(isGeneratedUsernameExist));
                int i = 0;
                String usernameFromGmail = "";

                StringBuilder x = new StringBuilder();

                assert email != null;
                while (email.charAt(i) != '@') {

                    x.append(email.charAt(i++));
                }

                usernameFromGmail = x.toString();

                boolean c = connectToCheckGeneratedUsername(usernameFromGmail);

                //we will register new user to DB and server
                if (registerNewGooglePlusUser(email, displayedName, usernameFromGmail, session.getDeviceToken())) {


                    Toast.makeText(this, getString(R.string.successfully_registered), Toast.LENGTH_LONG).show();
                    String id = returnUSER_ID(email);
                    db.addUser(usernameFromGmail, displayedName, email, id, null, byteArray);


                    session.setLogin(true);

                    uploadProfileImage(email, GmailProfilePhotoBitmap);

                    if (isGeneratedUsernameExist) {

                        Log.e("isGeneratedtcccd", String.valueOf(isGeneratedUsernameExist));

                        session.setLogin(false);
                        session.setUserMail(email);
                        //TODO intent will go go activity to require user input a username
                        Toast.makeText(getApplicationContext(), R.string.sty_to_select_username, Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(SignupActivity.this, PromptUniqueUsernameActivity.class);

                        intent.putExtra("mail", email);
                        startActivity(intent);
                        finish();


                    } else {
                        Log.e("isGeneratedtcccd2", String.valueOf(isGeneratedUsernameExist));
                        session.setLogin(true);
                        //TODO our generated username is currently unique then go to StartActivity --> completed
                        //TODO CHANGE AFTER DRAWER
                        Intent intent = new Intent(SignupActivity.this, StartActivity.class);
                        startActivity(intent);
                        finish();
                    }


                } else {
                    Toast.makeText(this, getString(R.string.failed_login_google), Toast.LENGTH_LONG).show();
                }


            }


        } else {
            Toast.makeText(this, R.string.failed_login_google, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * this function to fetch profile image as an array of bytes to be stored in app DB
     */

    @Nullable
    public static Bitmap getBitmapFromURL(Uri uri) {
        try {
            final URL url = new URL(uri.toString());
            final Bitmap[] myBitmap = new Bitmap[1];
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    HttpURLConnection connection = null;
                    try {
                        connection = (HttpURLConnection) url.openConnection();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    assert connection != null;
                    connection.setDoInput(true);
                    try {
                        connection.connect();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    InputStream input = null;
                    try {
                        input = connection.getInputStream();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    myBitmap[0] = BitmapFactory.decodeStream(input);
                }
            });
            thread.start();
            thread.join();

            return myBitmap[0];
        } catch (IOException e) {
            // Log exception
            return null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * this function return true if this gmail was not been used by another user in register form
     * if gmail is used before in register form we will ask user to login with this gmail in login form instead
     */
    private int checkIfMailUsedBeforeOrNot(final String gmail) throws InterruptedException {
        final int[] isUsed = new int[1];
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                isUsed[0] = checkGmailUsedOrNot(gmail);
            }
        });
        thread.start();
        thread.join();


        //true ---> gmail used before with g+ login button
        //false --> used before in register form
        return isUsed[0];

    }


    private int checkGmailUsedOrNot(final String userGmail) {

        String connectionResult = null;

        try {
            String emailKey = "email=";


            //String selectUrl = Variables.serverUrl + "guidak_files/select_audio_file_path_with_POST.php";
            String selectUrl = Variables.URL_LOG_IN_WITH_GOOGLE_PLUS;


            String connectionParameters = emailKey
                    + URLEncoder.encode(userGmail, "UTF-8");

            Log.e("HTTP_CONNECTION", "ppp " + connectionParameters);

            byte[] parametersByt = connectionParameters.getBytes("UTF-8");

            URL SelectAudioFilePathUrl = new URL(selectUrl);


            HttpURLConnection filePathConnection = (HttpURLConnection) SelectAudioFilePathUrl.openConnection();

            filePathConnection.setRequestMethod("POST");
            filePathConnection.setDoInput(true);
            filePathConnection.setDoOutput(true);
            Log.e("HTTP_CONNECTION", "1");

            filePathConnection.getOutputStream().write(parametersByt);
            filePathConnection.getOutputStream().flush();
            filePathConnection.getOutputStream().close();

            Log.e("HTTP_CONNECTION", "2");

            InputStreamReader resultStreamReader = new InputStreamReader(filePathConnection.getInputStream());
            Log.e("HTTP_CONNECTION", "3");

            BufferedReader resultReader = new BufferedReader(resultStreamReader);
            Log.e("HTTP_CONNECTION", "4");

            String line;
            final StringBuilder textBulider = new StringBuilder();
            while ((line = resultReader.readLine()) != null) {
                textBulider.append(line);

            }
            //  connectionResult = resultReader.readLine();

            connectionResult = textBulider.toString();


            Log.e("HTTP_CONNECTION", "res -" + connectionParameters);
            resultReader.close();
            resultStreamReader.close();
            filePathConnection.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        Log.e("HTTP_CONNECTION", "file path  fom method is " + connectionResult);
        //1 --> i
        //0--> this gmail is used before but with registration form not by "google plus" button

        assert connectionResult != null;
        if (connectionResult.equals("0")) {
            //this gmail is used before but with registration form not by "google plus" button
            return 0;

        } else if (connectionResult.equals("3")) {

            //we will register new user with G+ login
            return 3;
        } else {

            googlePlusUsername = connectionResult;
            return 1;
        }
        //return connectionResult == "1";


    }

    public boolean registerNewGooglePlusUser(final String gmail, final String gmailDisplayedName,
                                             final String gmailUsername, final String deviceToken) throws InterruptedException {

        final boolean[] result = {false};

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                result[0] = ConnectToRegisterNewGooglePlusUser(gmail, gmailDisplayedName,
                        gmailUsername, deviceToken);
            }
        });
        thread.start();
        thread.join();


        return result[0];
    }

    public boolean ConnectToRegisterNewGooglePlusUser(final String gmail, final String gmailDisplayedName,
                                                     final String gmailUsername, final String deviceToken) {

        String connectionResult = null;

        try {
            String emailKey = "email=";
            String usernameKey = "&user_name=";
            String displayedNameKey = "&displayed_name=";
            String tokenKey = "&token=";


            String selectUrl = Variables.URL_LOG_IN_WITH_GOOGLE_PLUS_STEP_TWO;


            String connectionParameters = emailKey
                    + URLEncoder.encode(gmail, "UTF-8")
                    + displayedNameKey + URLEncoder.encode(gmailDisplayedName, "UTF-8")
                    + usernameKey + URLEncoder.encode(gmailUsername, "UTF-8")
                    + tokenKey + URLEncoder.encode(deviceToken, "UTF-8");

            Log.e("HTTP_CONNECTION", "ppp " + connectionParameters);

            byte[] parametersByt = connectionParameters.getBytes("UTF-8");

            URL SelectAudioFilePathUrl = new URL(selectUrl);


            HttpURLConnection filePathConnection = (HttpURLConnection) SelectAudioFilePathUrl.openConnection();

            filePathConnection.setRequestMethod("POST");
            filePathConnection.setDoInput(true);
            filePathConnection.setDoOutput(true);
            Log.e("HTTP_CONNECTION", "1");

            filePathConnection.getOutputStream().write(parametersByt);
            filePathConnection.getOutputStream().flush();
            filePathConnection.getOutputStream().close();

            Log.e("HTTP_CONNECTION", "2");

            InputStreamReader resultStreamReader = new InputStreamReader(filePathConnection.getInputStream());
            Log.e("HTTP_CONNECTION", "3");

            BufferedReader resultReader = new BufferedReader(resultStreamReader);
            Log.e("HTTP_CONNECTION", "4");

            String line;
            final StringBuilder textBulider = new StringBuilder();
            while ((line = resultReader.readLine()) != null) {
                textBulider.append(line);

            }
            //  connectionResult = resultReader.readLine();

            connectionResult = textBulider.toString();


            Log.e("HTTP_CONNECTION", "res -" + connectionParameters);
            resultReader.close();
            resultStreamReader.close();
            filePathConnection.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        Log.e("HTTP_CONNECTION", "file path  fom method is " + connectionResult);
        //1 --> i
        //0--> this gmail is used before but with registration form not by "google plus" button

        assert connectionResult != null;
        switch (connectionResult) {
            case "registerd with google plus":
                //this gmail is used before but with registration form not by "google plus" button
                return true;

            case "something went wrong":

                //we will register new user with G+ login
                return false;
            default:
                return false;
        }
    }

    private void updateThisDeviceToken(final String accountMail, final String deviceToken) {

        //TODO make "device toke updating" here  --> completed

        StringRequest strReq = new StringRequest(Request.Method.POST,
                Variables.URL_UPDATE_DEVICE_TOKEN_AFTER_LOGIN, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                if (response.equals("updated")) {

                    Toast.makeText(getApplicationContext(), "device token updated successfully", Toast.LENGTH_LONG).show();
                } else {

                    Toast.makeText(getApplicationContext(), R.string.device_token_didnt_update, Toast.LENGTH_LONG).show();
                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(getApplicationContext(), R.string.device_token_didnt_update, Toast.LENGTH_LONG).show();
            }
        }) {

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



    /**
     * Function to store user in MySQL database will post params
     */
    private void uploadProfileImage(final String email, final Bitmap image) {

        //  final String uploadImage = getStringImage(image);

        StringRequest strReq = new StringRequest(Request.Method.POST,
                Variables.URL_UPLOAD_PROFILE_IMAGE, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Register Response: " + response);
                hideDialog();


                if (response.equals("Image Uploaded Successfully")) {
                    Log.e("google plus profile", "uploaded to server");
                } else {
                    Log.e("google plus profile", "failed to uploaded to server");
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Registration Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(), R.string.profile_image_didnt_updated, Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                String uploadImage = getStringImage(image);
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("mail", email);
                params.put("image", uploadImage);

                return params;
            }

        };

        MyVolley.getInstance(this).addToRequestQueue(strReq);

    }

    //  Now we have the Image which is to bet uploaded in bitmap.
//    We will convert this bitmap to base64 string
//    So we will create a method to convert this bitmap to base64 string

    public String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);

    }


    public String returnUSER_ID(final String email) throws InterruptedException {
        final String[] result = {};

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                result[0] = getThisUserId(email);
            }
        });
        thread.start();
        thread.join();


        return result[0];
    }

    public String getThisUserId(String email){

        String connectionResult = null;

        try {
            String emailKey = "email=";

            String selectUrl = Variables.GET_USER_ID;


            String connectionParameters = emailKey
                    + URLEncoder.encode(email, "UTF-8");

            Log.e("HTTP_CONNECTION", "ppp " + connectionParameters);

            byte[] parametersByt = connectionParameters.getBytes("UTF-8");

            URL SelectAudioFilePathUrl = new URL(selectUrl);


            HttpURLConnection filePathConnection = (HttpURLConnection) SelectAudioFilePathUrl.openConnection();

            filePathConnection.setRequestMethod("POST");
            filePathConnection.setDoInput(true);
            filePathConnection.setDoOutput(true);
            Log.e("HTTP_CONNECTION", "1");

            filePathConnection.getOutputStream().write(parametersByt);
            filePathConnection.getOutputStream().flush();
            filePathConnection.getOutputStream().close();

            Log.e("HTTP_CONNECTION", "2");

            InputStreamReader resultStreamReader = new InputStreamReader(filePathConnection.getInputStream());
            Log.e("HTTP_CONNECTION", "3");

            BufferedReader resultReader = new BufferedReader(resultStreamReader);
            Log.e("HTTP_CONNECTION", "4");

            String line;
            final StringBuilder textBulider = new StringBuilder();
            while ((line = resultReader.readLine()) != null) {
                textBulider.append(line);

            }
            //  connectionResult = resultReader.readLine();

            connectionResult = textBulider.toString();


            Log.e("HTTP_CONNECTION", "res -" + connectionParameters);
            resultReader.close();
            resultStreamReader.close();
            filePathConnection.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return connectionResult;
        }

    }

