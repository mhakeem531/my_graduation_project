package com.example.hakeem.demo;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.example.hakeem.demo.NetworkUtilites.ConnectToInvokeObjectInfo;
import com.example.hakeem.demo.helper.AppUserInfoDbHelper;
import com.example.hakeem.demo.helper.SessionManager;
import com.example.hakeem.demo.utilities.Variables;
import com.google.android.gms.vision.barcode.Barcode;

public class SemiStartActivity extends AppCompatActivity {

    public static final int REQUEST_CODE = 100;
    public static final int PERMISSION_REQUEST = 200;

    private String statueName;
    private String fileLanguage;

    private AppUserInfoDbHelper db;
    private SessionManager session;

    /**
     * this context will be used to make alert message to inform that
     * the scanned QR is unknown one
     * */
    public static Context context;
    public static final String LOG_TAG = MainActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.try_change_theme);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        context = this;

        transparentToolbar();

        String key = getResources().getString(R.string.pref_langs_key);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        Variables.audioFileLanguage = prefs.getString(key, "");

        this.fileLanguage = prefs.getString(key, "");


        Button goToScannerBtn = findViewById(R.id.go_to_scanner);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST);
        }



        goToScannerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.e("LANG IS ", "+++++" + fileLanguage);
                if(fileLanguage != null){

                    Intent intent = new Intent(SemiStartActivity.this, GoogleVisionScanningActivity.class);
                    startActivityForResult(intent, REQUEST_CODE);

                }else{
                    Toast.makeText(getApplicationContext(), R.string.select_lang_first, Toast.LENGTH_LONG).show();
                }
            }
        });


        db = new AppUserInfoDbHelper(getApplicationContext());

        // session manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }
    }



    /**
     * This is where we inflate and set up the menu for this Activity.
     *
     * @param menu The options menu in which you place your items.
     *
     * @return You must return true for the menu to be displayed;
     *         if you return false it will not be shown.
     *
     * @see #onPrepareOptionsMenu
     * @see #onOptionsItemSelected
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.main, menu);
        /* Return true so that the menu is displayed in the Toolbar */
        return true;
    }

    /**
     * Callback invoked when a menu item was selected from this Activity's menu.
     *
     * @param item The menu item that was selected by the user
     *
     * @return true if you handle the menu click here, false otherwise
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id){
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.action_logout:
                logoutUser();
                break;
            case R.id.go_to_user_activity:
                startActivity(new Intent(this, displayAppUserInfo.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Logging out the user. Will set isLoggedIn flag to false in shared
     * preferences Clears the user data from sqlite users table
     * */
    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();
        db.removeAllGroupMembers();

        //delete members table also
//        // SqLite database handler for group member table
//        SQLiteDatabase mDb; //members group
//        GroupMembersDbHelper dbHelper = new GroupMembersDbHelper(this);
//        mDb = dbHelper.getWritableDatabase();
//        // Delete All Rows
//
//        //int x = mDb.delete(TABLE_NAME, null, null);



        Log.d("delete all ", "Deleted all user info from sqlite");



        // Launching the login activity
        Intent intent = new Intent(SemiStartActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


    @Override
    protected void onResume() {

        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("result is " , "1");
        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK){
            Log.e("result is " , "2");
            if(data != null){
                final Barcode barcode = data.getParcelableExtra("barcode");
                Log.e("result isssssss " , barcode.displayValue);
                Variables.statueName = barcode.displayValue;
                this.statueName = barcode.displayValue;
                FetchStatueAudioFilePath();
            }
        }
    }




    private void transparentToolbar() {
        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true);
        }
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }

    private void setWindowFlag(Activity activity, final int bits, boolean on) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    public void FetchStatueAudioFilePath(){

        ConnectToInvokeObjectInfo invokeAudioFilePath = new ConnectToInvokeObjectInfo(this);
        invokeAudioFilePath.execute(Variables.selectAudioFilePathOperation, this.statueName, this.fileLanguage);

    }

}
