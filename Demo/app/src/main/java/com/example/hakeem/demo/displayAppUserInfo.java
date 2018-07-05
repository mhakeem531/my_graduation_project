package com.example.hakeem.demo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.hakeem.demo.NetworkUtilites.MyVolley;
import com.example.hakeem.demo.helper.CustomGroupMembersAdapter;
import com.example.hakeem.demo.helper.GroupMembersContract;
import com.example.hakeem.demo.helper.GroupMembersDbHelper;
import com.example.hakeem.demo.helper.AppUserInfoDbHelper;
import com.example.hakeem.demo.helper.SessionManager;
import com.example.hakeem.demo.helper.User;
import com.example.hakeem.demo.utilities.Variables;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static android.text.InputType.TYPE_CLASS_TEXT;
import static com.example.hakeem.demo.helper.GroupMembersContract.GroupMemberEntry.TABLE_NAME;


public class displayAppUserInfo extends AppCompatActivity {


    private TextView userDisplayedName;

    private TextView userName;

    private TextView groupStateText;

    private ImageView profilePhoto;

    private Bitmap profilePhotoBitmap;

    private ImageView addNewMemberGroup;

    private ImageView emptyGroup;

    private ImageView wifiControl;

    private ImageView groupStateImage;

    public static final int GET_FROM_GALLERY = 3;

    private WifiManager wifi;
    private SessionManager session;


    private AppUserInfoDbHelper db;  //app user info

    /////////////// private SQLiteDatabase mDb; //members group
    private CustomGroupMembersAdapter mAdapter;

    private User ourUser;


    private String usernameInput = "";
    public static final String UPLOAD_KEY = "image";
    public static final String UPLOAD_EMAIL_KEY = "mail";
    private ProgressDialog pDialog;

    private static final String TAG = displayAppUserInfo.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_display_app_user_info);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        /**

         {{{{{{layout component}}}}}}

         */
        userDisplayedName = findViewById(R.id.user_displayed_name);

        userName = findViewById(R.id.username);

        profilePhoto = findViewById(R.id.user_profile_photo);

        addNewMemberGroup = findViewById(R.id.add_new_member);

        emptyGroup = findViewById(R.id.empty_group);


        wifiControl = findViewById(R.id.wifi_icon);

        groupStateText = findViewById(R.id.group_state);

        groupStateImage = findViewById(R.id.group_state_image);


        /**

         {{{{{{for accessing DB tables}}}}}}

         */
        // SqLite database handler for fetching app user info
        db = new AppUserInfoDbHelper(getApplicationContext());

        Log.e("xx_sidpsfwje", db.getUserID());
////////////////////////////////////////////////////////////////////////////////////
//        // SqLite database handler for group member table
//        GroupMembersDbHelper dbHelper = new GroupMembersDbHelper(this);
//        mDb = dbHelper.getWritableDatabase();


        //fetch user app info
        ourUser = invokeUserInfoFromDB();

        //user is just signed up for first time
        //not logged in after creating account in last time
        //as user has no image photo yet
//        Log.e("displayUserInfo", "image is null");

        /**

         {{{{{{for RecyclerView declaration}}}}}}

         */
        RecyclerView membersRecyclerView;
        membersRecyclerView = this.findViewById(R.id.group_members);
        // Set layout for the RecyclerView, because it's a list we are using the linear layout
        membersRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        Cursor cursor = getAllGroupMembers();
        mAdapter = new CustomGroupMembersAdapter(this, cursor);

        // Link the adapter to the RecyclerView
        membersRecyclerView.setAdapter(mAdapter);

        Log.e("mAdaptergetcount", String.valueOf(mAdapter.getItemCount()));



        /**

         {{{{{{binding data}}}}}}

         */
        userName.setText(ourUser.getUserName());
//        userEmail.setText(ourUser.getEmail());
        userDisplayedName.setText(ourUser.getDisplayedName());

        Log.e(TAG, "ALL INFO : " + ourUser.getUserName() + " " + ourUser.getEmail());
/******************************************************/
        if (ourUser.getProfileImage() != null) {

            Log.e("displayUserInfo", "image is not null");
            byte[] outImage = ourUser.getProfileImage();

            ByteArrayInputStream imageStream = new ByteArrayInputStream(outImage);
            Bitmap theImage = BitmapFactory.decodeStream(imageStream);
            Log.e("INFO0", imageStream.toString());
            profilePhoto.setImageBitmap(theImage);
        } else {

            Log.e("displayUserInfo", "image-is-null");
        }

        /**

         {{{{{{ setting wifi and connection state to deal with}}}}}

         */
        wifi = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // Session manager
        session = new SessionManager(getApplicationContext());
        if (!session.getConnectionState()) {
            session.setConnectionState(false);
        }



//        if (wifi.isWifiEnabled()) {
//            wifiControl.setImageResource(R.drawable.wifi_connected_icon);
//        } else if (!wifi.isWifiEnabled()) {
//            wifiControl.setImageResource(R.drawable.wifi_not_connected_icon);
//        }

        /**
         * {{{{{clicking}}}}}
         */
        profilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!session.getLoggedInWithGooglePLusState())
                    updateProfileImage();
            }
        });

        /**
         * add new member to recycle view and DB table to send him audio file after admin scanning
         * */
        addNewMemberGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
/****************************
//                //true ==> user enabled feature
//                //false ==> user din't enable feature yet
//                if (addNewMemberGroup.isEnabled() || addNewMemberGroup.isClickable()) {
//                    addNewMemberPrompt();
//                    emptyGroup.setVisibility(View.VISIBLE);
//                } else {
//                    Toast.makeText(getApplicationContext(), R.string.enable_share_listening_feature, Toast.LENGTH_LONG).show();
//                }
***********************************************************/
                if(session.getShareListeningState()){
                    addNewMemberPrompt();
                }else{
                    Toast.makeText(getApplicationContext(), "please enable feature first", Toast.LENGTH_LONG).show();
                }

                //groupStateText.setText("share listening group members");
            }
        });

        /**
         * empty group from its members
         * */
        emptyGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

               /************************************
                session.setShareListeningState(false);
                if (removeAllMembers()) {


                    mAdapter.swapCursor(getAllGroupMembers());
                    Toast.makeText(getApplicationContext(), R.string.group_empty, Toast.LENGTH_LONG).show();

                    groupStateImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_group_state_sad));

                    groupStateText.setText(R.string.your_group_is_empty);
                    addNewMemberGroup.setEnabled(false);
                    addNewMemberGroup.setClickable(false);
                    session.setShareListeningState(false);
                }
*****************************/

               if(session.getShareListeningState()){
                   if (removeAllMembers()) {


                       mAdapter.swapCursor(getAllGroupMembers());
                       Toast.makeText(getApplicationContext(), R.string.group_empty, Toast.LENGTH_LONG).show();

                       groupStateImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_group_state_sad));

                       groupStateText.setText(R.string.your_group_is_empty);
                       session.setShareListeningState(false);
                   }
               }else{
                   Toast.makeText(getApplicationContext(), "enable feature first,then and members, so you can remove them", Toast.LENGTH_LONG).show();
               }

               // addNewMemberGroup.setColorFilter(R.color.white);
            }
        });

        /**
         * connect device to server network
         * */
        wifiControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                if (wifi.isWifiEnabled()) {
//                    wifiControl.setImageResource(R.drawable.wifi_connected_icon);
//                    // wifi.setWifiEnabled(false);
//                } else if (!wifi.isWifiEnabled()) {
//
//                    wifiControl.setImageResource(R.drawable.wifi_connected_icon);
//                    wifi.setWifiEnabled(true);
//                }

                /**
                 * CONNECT TO OUR NETWORK
                 * */

//                String networkSSID = "hamdy";
//                String networkPass = "cap#@#ahmed45814**tarek";
//                WifiConfiguration conf = new WifiConfiguration();
//                conf.SSID = "\"" + networkSSID + "\"";   // Please note the quotes. String should contain ssid in quotes
//
//                conf.preSharedKey = "\""+ networkPass +"\"";
//                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
//                wifi.addNetwork(conf);
//
//                List<WifiConfiguration> list = wifi.getConfiguredNetworks();
//                for( WifiConfiguration i : list ) {
//                    if(i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
//                        wifi.disconnect();
//                        wifi.enableNetwork(i.networkId, true);
//                        wifi.reconnect();
//                        break;
//                    }
//                }
//
//                session.setConnectionState(true);

            }
        });

        /**

         {{{{{{scrolling}}}}}

         */

// Create an item touch helper to handle swiping items off the list
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                //do nothing, we only care about swiping
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {


                ensureRemoveMember((long) viewHolder.itemView.getTag());

//                mAdapter.swapCursor(getAllGroupMembers());
//
//                if(ensureRemoveMember()){
//
//                    Log.e("remove", "?");
//                    //get the id of the item being swiped
//                    long id = (long) viewHolder.itemView.getTag();
//                    //remove from DB
//                    removeMember(id);
//                    //update the list
//                    mAdapter.swapCursor(getAllGroupMembers());
//                }else{
//                    Log.e("not remove", "?");
//                }

            }

        }).attachToRecyclerView(membersRecyclerView);


        if (session.getShareListeningState()) {
            //ture --> user made a group

            emptyGroup.setEnabled(true);
            addNewMemberGroup.setEnabled(true);
        } else {
            emptyGroup.setEnabled(false);
            addNewMemberGroup.setEnabled(false);
        }


        if (mAdapter.getItemCount() <= 0) {
            groupStateImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_group_state_sad));

            groupStateText.setText(R.string.your_group_is_empty);

        } else {
            groupStateImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_group_state_good));
            groupStateText.setText(R.string.not_alone_in_listening);
        }

        ////////////////////////////////////
        //downloadProfile(ourUser.getUserName(), profilePhoto);


    }

//    private void ShowHideGroupManagementOptions(boolean isGroupFired){
//        if(isGroupFired){
//            this.addNewMemberGroup.setVisibility(View.VISIBLE);
//            this.emptyGroup.setVisibility(View.VISIBLE);
//        }else{
//            this.addNewMemberGroup.setVisibility(View.INVISIBLE);
//            this.emptyGroup.setVisibility(View.INVISIBLE);
//        }
//    }

    private void updateProfileImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(intent, GET_FROM_GALLERY);

    }

    private User invokeUserInfoFromDB() {

        return db.getUserDetails();
    }


    @Override
    protected void onResume() {
//        if (wifi.isWifiEnabled()) {
//            wifiControl.setImageResource(R.drawable.wifi_connected_icon);
//        } else if (!wifi.isWifiEnabled()) {
//            wifiControl.setImageResource(R.drawable.wifi_not_connected_icon);
//        }
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Detects request codes
        if (requestCode == GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();

            try {
                profilePhotoBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);

//
//                //convert bitmap to byt
//                ByteArrayOutputStream stream = new ByteArrayOutputStream();
//
//                profilePhotoBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
//
//                byte imageInByte[] = stream.toByteArray();

                Log.e("mail is ", ourUser.getEmail());


                uploadProfileImage(ourUser.getEmail(), profilePhotoBitmap);


//                if (db.updateUserProfileImage(ourUser.getEmail(), imageInByte) > 0) {
//
//                    Toast.makeText(getApplicationContext(), R.string.profile_image_updated, Toast.LENGTH_LONG).show();
//                    // TODO upload image to server  ---> completed
//                    //  uploadImage();
//
//                    profilePhoto.setImageBitmap(profilePhotoBitmap);
//
//
//
//                } else {
//                    Toast.makeText(getApplicationContext(), R.string.profile_image_didnt_updated, Toast.LENGTH_LONG).show();
//                }
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block  ---> completed
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block  ---> completed
                e.printStackTrace();
            }
        }

    }

    /**
     * Function to store user in MySQL database will post params
     */
    private void uploadProfileImage(final String email, final Bitmap image) {

        pDialog.setMessage(getApplicationContext().getResources().getString(R.string.upload_image));
        showDialog();

        //  final String uploadImage = getStringImage(image);

        StringRequest strReq = new StringRequest(Request.Method.POST,
                Variables.URL_UPLOAD_PROFILE_IMAGE, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Register Response: " + response);
                hideDialog();


                if (response.equals("Image Uploaded Successfully")) {

                    ByteArrayOutputStream stream = new ByteArrayOutputStream();

                    profilePhotoBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);

                    byte imageInByte[] = stream.toByteArray();

                    if (db.updateUserProfileImage(email, imageInByte) > 0) {


                        profilePhoto.setImageBitmap(profilePhotoBitmap);

                        Toast.makeText(getApplicationContext(),
                                R.string.profile_image_updated, Toast.LENGTH_LONG).show();

                    }
                } else {
                    Toast.makeText(getApplicationContext(),
                            R.string.profile_image_didnt_updated + response, Toast.LENGTH_LONG).show();
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

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }


    /**

     {{{{{{{{{{for accessing group member table and make different transactions}}}}}}}}}}

     */

    /**
     * Removes all members from table
     *
     * @return True: if removed successfully, False: if failed
     */
    public boolean removeAllMembers() {


        return db.removeAllGroupMembers();
//
//        // Delete All Rows
//        int x = mDb.delete(TABLE_NAME, null, null);
//
//        Log.d("delete all ", "Deleted all members info from sqlite");
//        return x > 0;
    }

    /**
     * Removes the record with the specified id
     *
     * @param id the DB id to be removed
     * @return True: if removed successfully, False: if failed
     */
    private boolean removeMember(long id) {
        return db.removeSpecificMember(id);
//        return mDb.delete(GroupMembersContract.GroupMemberEntry.TABLE_NAME,
//                GroupMembersContract.GroupMemberEntry._ID + "=" + id, null) > 0;

    }

    /**
     * Query the mDb and get all members from the members table
     *
     * @return Cursor containing the list of members
     */
    private Cursor getAllGroupMembers() {
        return db.queryAllMembers();
//        Cursor all = mDb.query(
//                GroupMembersContract.GroupMemberEntry.TABLE_NAME,
//                null,
//                null,
//                null,
//                null,
//                null,
//                null
//        );
//        return all;


    }

    /**
     * Adds a new member to the mDb including the party count and the current timestamp
     *
     * @param username Member's name
     * @return id of new record added
     */
    private long addNewGroupMember(String username) {
        return db.insertNewMember(username);

//        ContentValues cv = new ContentValues();
//        cv.put(GroupMembersContract.GroupMemberEntry.COLUMN_USERNAME, username);
//
//        return mDb.insert(TABLE_NAME, null, cv);


    }

    public void addMemberToGroup(String memberUsername) {
        if (memberUsername.length() == 0 || memberUsername.isEmpty()) {
            return;
        }

        // Add member info to mDb
        addNewGroupMember(memberUsername);

        // Update the cursor in the adapter to trigger UI to display the new list
        mAdapter.swapCursor(getAllGroupMembers());

        if (mAdapter.getItemCount() > 0) {
            groupStateText.setText(R.string.not_alone_in_listening);
            groupStateImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_group_state_good));
        }


    }

    public void addNewMemberPrompt() {
        //input types --> https://developer.android.com/reference/android/widget/TextView.html#attr_android:inputType
        //https://stackoverflow.com/questions/10903754/input-text-dialog-android?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyDialogTheme);
        builder.setTitle(getApplicationContext().getResources().getString(R.string.add_new_member));

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text

        input.setInputType(TYPE_CLASS_TEXT);
        input.setHint(getApplicationContext().getResources().getString(R.string.member_username));
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton(getApplicationContext().getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        usernameInput = input.getText().toString();
                        if (!usernameInput.isEmpty() && !usernameInput.trim().equals(ourUser.getUserName())){
                            addMemberToGroup(usernameInput.trim());
                            Log.e("ourUser.getUserName()", ourUser.getUserName());
                        }

                    }
                });
        builder.setNegativeButton(getApplicationContext().getResources().getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        //  builder.show();
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }


    private void ensureRemoveMember(final long id) {


        //input types --> https://developer.android.com/reference/android/widget/TextView.html#attr_android:inputType
        //https://stackoverflow.com/questions/10903754/input-text-dialog-android?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyDialogTheme);
        builder.setTitle(getApplicationContext().getResources().getString(R.string.remove_member));

        // Set up the buttons
        builder.setPositiveButton(getApplicationContext().getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!session.getConnectionState()) {
                    Log.e("test connection", "connect first");

                    mAdapter.swapCursor(getAllGroupMembers());
                    removeMember(id);
                    mAdapter.swapCursor(getAllGroupMembers());

                } else {
                    Log.e("remove", "?");
                    mAdapter.swapCursor(getAllGroupMembers());
                    removeMember(id);
                    mAdapter.swapCursor(getAllGroupMembers());


                    Log.e("test connection", "connected");
                }

                if (mAdapter.getItemCount() <= 0) {
                    session.setShareListeningState(false);
                    groupStateText.setText(R.string.your_group_is_empty);
                    groupStateImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_group_state_sad));
                }
            }


        });
        builder.setNegativeButton(getApplicationContext().getResources().getString(R.string.cancel)
                , new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        mAdapter.swapCursor(getAllGroupMembers());
                        dialog.cancel();
                    }
                });


        // builder.show();

        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }
}
