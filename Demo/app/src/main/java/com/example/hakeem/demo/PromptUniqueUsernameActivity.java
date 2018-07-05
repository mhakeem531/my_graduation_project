package com.example.hakeem.demo;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.print.PrinterId;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.hakeem.demo.helper.AppUserInfoDbHelper;
import com.example.hakeem.demo.helper.SessionManager;
import com.example.hakeem.demo.utilities.Variables;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;

public class PromptUniqueUsernameActivity extends AppCompatActivity {

    private String userMail;
    private String enterdUsername;
    private AppUserInfoDbHelper db;
    private Button submitButton;
    private EditText usernameEditText;
    private ProgressBar dealing;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prompt_unique_username);

        sessionManager = new SessionManager(this);
        sessionManager.setProbmetUsernameStep(true);

        showAlert();

        userMail = getIntent().getStringExtra("mail");
        if(userMail== null){
            userMail = sessionManager.getUserMail();
        }

        submitButton = findViewById(R.id.submit_username_button);
        usernameEditText = findViewById(R.id.unique_username_filed);
        dealing = findViewById(R.id.progressBar);

        /**
         * {{{SQLite database handler}}}
         * */

        db = new AppUserInfoDbHelper(getApplicationContext());


        /**
         * TODO
         * THIS activity used if our generated username in not unique
         * it will ask user to try enter unique username to
         *
         *
         * 1-user will try until get unique username
         * 2-after get it successfully
         *        a- update it in mysql DB in server
         *        b- set value of updatedAt in DB in server to this date
         *        c- update it in sqlite table in application
         *        d-lunch SemiStartActivity
         * */


        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dealing.setIndeterminate(true);
                dealing.setVisibility(View.VISIBLE);
                enterdUsername = usernameEditText.getText().toString();

                if(enterdUsername.isEmpty()){
                    Toast.makeText(getApplicationContext(), R.string.select_username, Toast.LENGTH_LONG).show();
                }else{
                    enterdUsername=  enterdUsername.trim();
                    insertUniqueUsername(enterdUsername);
                }


            }
        });
    }


    private void insertUniqueUsername(final String username) {

        dealing.setVisibility(View.VISIBLE);
        //   usernameEditText.setText("");
        new Thread(new Runnable() {
            @Override
            public void run() {
                //1- username already exist try another one
                //2- value updated successfully
                //3- value wasn't updated successfully
                //4- there is error in connection

                int result = finallyInsertUniqueUsernameValue(userMail,username);


                if ( result == 1 || result == 3 || result == 4) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), R.string.select_unique_username, Toast.LENGTH_LONG).show();
                            dealing.setVisibility(View.INVISIBLE);
                        }
                    });

                } else if(result == 2) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            sessionManager.setProbmetUsernameStep(false);
                            sessionManager.setLogin(true);

                            dealing.setVisibility(View.INVISIBLE);

                            //todo here the value form user is unique and will be inserted to table

                            String toastMessage = getString(R.string.your_username_is)  +"\" " + username + " \" " + getString(R.string.reason_for_user_name);


                            showAlert(toastMessage);
//                            Toast.makeText(getApplicationContext(),
//                                    toastMessage,
//                                    Toast.LENGTH_LONG)
//                                    .show();


//
//                            Toast.makeText(getApplicationContext(),
//                                    "************"
//                                            + username +
//                                            R.string.reason_for_user_name,
//                                    Toast.LENGTH_LONG)
//                                    .show();


//                            db.updateUsernameValue(userMail, enterdUsername);
//
//                            Intent intent = new Intent(PromptUniqueUsernameActivity.this, StartActivity.class);
//                            startActivity(intent);
//
//                            finish();
                        }
                    });

                }
            }
        }).start();

    }


    private boolean checkUserNameIsUnique(final String currentSelectedUsername) {


        String connectionResult = null;

        try {
            String usernameKey = "username=";

            //String selectUrl = Variables.serverUrl + "guidak_files/select_audio_file_path_with_POST.php";
            String selectUrl = Variables.URL_TEST_SELECTED_USERNAME;


            String connectionParameters = usernameKey
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

    private int finallyInsertUniqueUsernameValue(final String email, final String username) {
        String connectionResult = null;

        try {

            String mailKey = "mail=";
            String usernameKey = "&username=";

            //String selectUrl = Variables.serverUrl + "guidak_files/select_audio_file_path_with_POST.php";
            String selectUrl = Variables.URL_CHECK_AND_UPDATE_USER_NAME_VALUE;


            String connectionParameters = mailKey
                    + URLEncoder.encode(email, "UTF-8")
                    + usernameKey
                    + URLEncoder.encode(username, "UTF-8");

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

            //1- username already exist try another one
            //2- value updated successfully
            //3- value wasn't updated successfully
            //4- there is error in connection
            return 1;
        } else if (connectionResult.equals("2")) {
            return 2;
        } else if (connectionResult.equals("3")) {
            return 3;
        } else {
            return 4;
        }
    }


    public void showAlert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(/*getApplicationContext().getResources().getString(R.string.new_track)*/" ")

                .setMessage(getString(R.string.select_unique_username_alert))

                .setCancelable(false)

                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }



    public void showAlert(String msg){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(/*getApplicationContext().getResources().getString(R.string.new_track)*/" ")

                .setMessage(msg)

                .setCancelable(false)

                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.cancel();

                        db.updateUsernameValue(userMail, enterdUsername);

                        Intent intent = new Intent(PromptUniqueUsernameActivity.this, StartActivity.class);
                        startActivity(intent);

                        finish();

                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

}
