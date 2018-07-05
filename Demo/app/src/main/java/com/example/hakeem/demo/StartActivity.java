package com.example.hakeem.demo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hakeem.demo.fragments.AboutTeam;
import com.example.hakeem.demo.fragments.HomeFragment;
import com.example.hakeem.demo.fragments.HowToUse;
import com.example.hakeem.demo.helper.AppUserInfoDbHelper;
import com.example.hakeem.demo.helper.SessionManager;
import com.example.hakeem.demo.helper.User;

import java.io.ByteArrayInputStream;

public class StartActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, HomeFragment.OnFragmentInteractionListener {


    private ImageView goToProfileActivity;

    private TextView headerDisplayedName;
    private TextView headerUsername;

    private AppUserInfoDbHelper db;
    private SessionManager session;

    private Menu navigationMenu;
    private MenuItem fireGroupItem;
    private MenuItem homeItem;
    private MenuItem aboutTeam;
    private MenuItem howToUse;

    private NavigationView navigationView;
    private FloatingActionButton fab;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        db = new AppUserInfoDbHelper(getApplicationContext());


        // session manager
        session = new SessionManager(getApplicationContext());

        //     session.setAudioPlayerState(false);


        Log.e("setsharastate", String.valueOf(session.getShareListeningState()));
        if (!session.isLoggedIn()) {

            Toast.makeText(this, "user will logout", Toast.LENGTH_LONG).show();
            logoutUser();
        }


//        MenuItem item = new MenuItem() {
//
//        }
        fab = findViewById(R.id.fab);
        fab.setVisibility(View.VISIBLE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                Intent intent = new Intent(StartActivity.this, PostFeedback.class);
                startActivity(intent);
            }
        });


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        /*NavigationView*/
        navigationView = findViewById(R.id.nav_view);
        View hView = navigationView.getHeaderView(0);
        navigationView.setNavigationItemSelectedListener(this);
        goToProfileActivity = hView.findViewById(R.id.go_to_profile);
        headerDisplayedName = hView.findViewById(R.id.header_displayed_name);
        headerUsername = hView.findViewById(R.id.header_username);


        navigationMenu = navigationView.getMenu();
        fireGroupItem = navigationMenu.findItem(R.id.nav_fire_group);

        homeItem = navigationMenu.findItem(R.id.nav_home);

        aboutTeam = navigationMenu.findItem(R.id.nav_about_developers);

        howToUse = navigationMenu.findItem(R.id.nav_how_to_use);


        //fireGroupItem.setEnabled(!session.getShareListeningState());


        displaySelectedScreen(homeItem);

        user = db.getUserDetails();

        headerUsername.setText(user.getUserName());
        headerDisplayedName.setText(user.getDisplayedName());

        if (user.getProfileImage() != null) {

            Log.e("StartActivity", "image is not null");

            byte[] outImage = user.getProfileImage();
            ByteArrayInputStream imageStream = new ByteArrayInputStream(outImage);
            Bitmap theImage = BitmapFactory.decodeStream(imageStream);
            Log.e("INFO0", imageStream.toString());
            goToProfileActivity.setImageBitmap(theImage);
        }
        goToProfileActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), displayAppUserInfo.class));
            }
        });


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.start, menu);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        displaySelectedScreen(item);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void displaySelectedScreen(MenuItem item) {

        int itemId = item.getItemId();
        //creating fragment object
        Fragment fragment = null;
        ////////////////////  setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

        switch (itemId) {
            case R.id.nav_home:
                fab.setVisibility(View.VISIBLE);
                aboutTeam.setChecked(false);
                howToUse.setChecked(false);
                //              Toast.makeText(getApplicationContext(), "hello from home", Toast.LENGTH_LONG).show();
                fragment = new HomeFragment();


                break;

            case R.id.nav_scanner:
//                Toast.makeText(getApplicationContext(), "hello from scanner", Toast.LENGTH_LONG).show();
                //  fragment = new ScannerFragment();

                Intent intent = new Intent(StartActivity.this, GoogleVisionScanningActivity.class);
                startActivity(intent);
                // finish();
                return;
            // break;

            case R.id.nav_setting:
                //   Toast.makeText(getApplicationContext(), "hello from setting", Toast.LENGTH_LONG).show();
                startActivity(new Intent(this, SettingsActivity.class));
                break;

            case R.id.nav_fire_group:
                if (session.getShareListeningState()) {
                    Toast.makeText(this, R.string.user_already_fired_group, Toast.LENGTH_LONG).show();
                } else {
                    fireGroupRoutine();
                }

                // finish();
                return;
            // break;

            case R.id.ic_feedback:

                //   session.setShareListeningState(true);
                startActivity(new Intent(this, TimeLine.class));
                // Toast.makeText(getApplicationContext(), "hello from leave-feedback", Toast.LENGTH_LONG).show();
                break;

            case R.id.nav_logout:
                //Toast.makeText(getApplicationContext(), "hello from log-out", Toast.LENGTH_LONG).show();
                logoutUser();
                break;

            case R.id.nav_about_developers:
                homeItem.setChecked(false);
                howToUse.setChecked(false);
                fab.setVisibility(View.GONE);

                // In just this one instance, we turn the sensor off
                // Until a different menu item is selected, which re-enables it
                //http://www.peteonsoftware.com/index.php/2014/03/07/dynamically-preventing-rotation-on-an-android-fragment/
                ///////////////////////////// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

                //   Toast.makeText(getApplicationContext(), "hello from hakeem", Toast.LENGTH_LONG).show();
                fragment = new AboutTeam();
                break;

            case R.id.nav_how_to_use:

                homeItem.setChecked(false);
                aboutTeam.setChecked(false);
                fab.setVisibility(View.GONE);
                fragment = new HowToUse();
                break;
        }

        //replacing the fragment
        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.frame, fragment);
            ft.commit();


            item.setChecked(true);


//            // update selected item and title, then close the drawer
//            mDrawerList.setItemChecked(position, true);
//            mDrawerList.setSelection(position);
//            setTitle(navMenuTitles[position]);
//            mDrawerLayout.closeDrawer(mDrawerList);

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    private void logoutUser() {
        session.setLogin(false);
        session.setShareListeningState(false);
        session.setAudioPlayerState(false);
        session.setProbmetUsernameStep(false);


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
        Intent intent = new Intent(StartActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


    private void fireGroupRoutine() {

        // fireGroupItem.setEnabled(false);
        //this pref to make this option in list dis-enabled until user free all group
        session.setShareListeningState(true);
        // Toast.makeText(getApplicationContext(), "hello from fire-group", Toast.LENGTH_LONG).show();
        Intent intent2 = new Intent(StartActivity.this, displayAppUserInfo.class);
        startActivity(intent2);

    }

    @Override
    protected void onStart() {
//        if(session != null)
//        fireGroupItem.setEnabled(!session.getShareListeningState());
        Log.e("hello-from-star", "onnnnnnnnn");
        if (homeItem != null) {
            homeItem.setChecked(true);
        }
        super.onStart();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        //Toast.makeText(getApplicationContext(), "hello from override method", Toast.LENGTH_LONG).show();
    }
}
