package com.example.hakeem.demo.helper;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.hakeem.demo.utilities.Variables;

import static com.example.hakeem.demo.helper.AppUserBasicInfoContract.AppUserInfoEntry.*;
import static com.example.hakeem.demo.helper.GroupMembersContract.GroupMemberEntry.TABLE_NAME;

/**
 * Created by hakeem on 3/3/18.
 */

public class AppUserInfoDbHelper extends SQLiteOpenHelper {

    private static final String TAG = AppUserInfoDbHelper.class.getSimpleName();

    // All Static variables
    // Database Version
    //private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "app.db";

//    // Login table name
//    private static final String TABLE_USER = "user";
//
//    // Login Table Columns names
//    private static final String KEY_ID = "id";
//    private static final String KEY_UID = "uid";
//    private static final String KEY_NAME = "user_name";
//    private static final String KEY_EMAIL = "email";
//    private static final String KEY_DISPLAYED_NAME = "displayed_name";
//    private static final String KEY_IMAGE = "image";
//
//    private static final String KEY_CREATED_AT = "created_at";

    public AppUserInfoDbHelper(Context context) {
        super(context, DATABASE_NAME, null, Variables.DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.e("SQL", "onCreate");
        String CREATE_USER_BASIC_INFO_TABLE = "CREATE TABLE "
                + AppUserBasicInfoContract.AppUserInfoEntry.TABLE_NAME + "("
                + AppUserBasicInfoContract.AppUserInfoEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + AppUserBasicInfoContract.AppUserInfoEntry.KEY_NAME + " TEXT,"
                + AppUserBasicInfoContract.AppUserInfoEntry.KEY_DISPLAYED_NAME + " TEXT,"
                + AppUserBasicInfoContract.AppUserInfoEntry.KEY_EMAIL + " TEXT UNIQUE,"
                + AppUserBasicInfoContract.AppUserInfoEntry.KEY_UID + " TEXT,"
                + AppUserBasicInfoContract.AppUserInfoEntry.KEY_CREATED_AT + " TEXT, "
                + AppUserBasicInfoContract.AppUserInfoEntry.KEY_IMAGE + " BLOB" + ")";
        sqLiteDatabase.execSQL(CREATE_USER_BASIC_INFO_TABLE);


        // Create a table to hold user_db data
        final String SQL_CREATE_GROUP_MEMBERS_TABLE = "CREATE TABLE " +
                GroupMembersContract.GroupMemberEntry.TABLE_NAME +
                " (" +
                GroupMembersContract.GroupMemberEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                GroupMembersContract.GroupMemberEntry.COLUMN_USERNAME + " TEXT NOT NULL" +
                "); ";

        sqLiteDatabase.execSQL(SQL_CREATE_GROUP_MEMBERS_TABLE);

        Log.d(TAG, "Database tables createcccccd");
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // Drop older table if existed
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + AppUserBasicInfoContract.AppUserInfoEntry.TABLE_NAME);
        // Create tables again
        onCreate(sqLiteDatabase);

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + GroupMembersContract.GroupMemberEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    /**
     * Storing user details in database
     */
    public long addUser(String Username, String DisplayedName, String email, String uid, String created_at, byte[] imageInByte) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, Username); // user Name
        values.put(KEY_DISPLAYED_NAME, DisplayedName); // Displayed Name
        values.put(KEY_EMAIL, email); // Email
        values.put(KEY_UID, uid); // Email
        values.put(KEY_CREATED_AT, created_at); // Created At
        values.put(KEY_IMAGE, imageInByte);    //profile image

        // Inserting Row
        long id = db.insert(AppUserBasicInfoContract.AppUserInfoEntry.TABLE_NAME, null, values);
        db.close(); // Closing database connection
        Log.e(TAG, "New user inserted into sqlite: " + id);
        return id;

    }

    /**
     * Re create database Delete all tables and create them again
     */
    public void deleteUsers() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(AppUserBasicInfoContract.AppUserInfoEntry.TABLE_NAME, null, null);
        db.close();

        Log.d(TAG, "Deleted all user info from sqlite");
    }


    /**
     * Getting user data from database
     */
    public User getUserDetails() {
        User appUserInfo = new User();
        String selectQuery = "SELECT  * FROM " + AppUserBasicInfoContract.AppUserInfoEntry.TABLE_NAME;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            appUserInfo.setUserName(cursor.getString(1));
            appUserInfo.setDisplayedName(cursor.getString(2));
            appUserInfo.setEmail(cursor.getString(3));
            appUserInfo.setId(cursor.getString(4));
            appUserInfo.setCreatedAt(cursor.getString(5));
            appUserInfo.setProfileImage(cursor.getBlob(6));

        } else {
            Log.e(TAG, "ERROR IN SELECT STATEMENT");
        }
        cursor.close();
        db.close();
        // return user
        Log.d(TAG, "Fetching user from Sqlite: " + appUserInfo.toString());

        return appUserInfo;
    }

    public String getUserID(){

        User appUserInfo = new User();
        String selectQuery = "SELECT "+   KEY_UID  + " FROM " + AppUserBasicInfoContract.AppUserInfoEntry.TABLE_NAME;

        SQLiteDatabase db = this.getReadableDatabase();

        @SuppressLint("Recycle") Cursor cursor = db.rawQuery(selectQuery, null);

        // Move to first row
        cursor.moveToFirst();
        String x = cursor.getString(0);
        return  x;

    }
    public int updateUserProfileImage(String email, byte[] imageInByte) {

        Log.e("from--update--profile", imageInByte.toString() + "mail " + email);
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_IMAGE, imageInByte);

        int x = db.update(AppUserBasicInfoContract.AppUserInfoEntry.TABLE_NAME, values, " email= ?", new String[]{email});
        Log.e("from--update--profile", String.valueOf(x));
        return x;
    }


    public int updateUsernameValue(String email, String uniqueUsername) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, uniqueUsername);

        int x = db.update(AppUserBasicInfoContract.AppUserInfoEntry.TABLE_NAME, values, " email= ?", new String[]{email});
        return x;
    }


    /**
     * empty user group he listens with
     */
    public boolean removeAllGroupMembers() {

        SQLiteDatabase db = this.getWritableDatabase();

        int x = db.delete(TABLE_NAME, null, null);

        db.close();

        Log.d("delete all ", "Deleted all members info from sqlite");

        return x > 0;
    }

    /**
     * remove specific member from the group
     **/
    public boolean removeSpecificMember(long id) {

        SQLiteDatabase db = this.getWritableDatabase();

        // Delete All Rows
        long result = db.delete(GroupMembersContract.GroupMemberEntry.TABLE_NAME, GroupMembersContract.GroupMemberEntry._ID + "=" + id, null);
        db.close();

        Log.d(TAG, "Deleted all members from table");

        return result > 0;
    }


    /**
     * get all user names of group members
     */
    public Cursor queryAllMembers() {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor all = db.query(
                GroupMembersContract.GroupMemberEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
        );

        //db.close();

        return all;
    }

    /**
     * insert new member to group
     */
    public long insertNewMember(String username) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(GroupMembersContract.GroupMemberEntry.COLUMN_USERNAME, username);
        long x = db.insert(TABLE_NAME, null, cv);

        db.close();

        return x;

    }

}

