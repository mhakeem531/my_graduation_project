package com.example.hakeem.demo.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.hakeem.demo.utilities.Variables;



/**
 * Created by hakeem on 4/11/18.
 */

public class GroupMembersDbHelper extends SQLiteOpenHelper {
    // The database name
    private static final String DATABASE_NAME = "app.db";

    // If you change the database schema, you must increment the database version
    //private static final int DATABASE_VERSION = 1;

    // Constructor
    public GroupMembersDbHelper(Context context) {
        super(context, DATABASE_NAME, null, Variables.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {


        // Create a table to hold user_db data
        final String SQL_CREATE_GROUP_MEMBERS_TABLE = "CREATE TABLE " +
                GroupMembersContract.GroupMemberEntry.TABLE_NAME +
                " (" +
                GroupMembersContract.GroupMemberEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                GroupMembersContract.GroupMemberEntry.COLUMN_USERNAME + " TEXT NOT NULL" +
                "); ";

        sqLiteDatabase.execSQL(SQL_CREATE_GROUP_MEMBERS_TABLE);
        Log.e("hello fom", "creat members");

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        // For now simply drop the table and create a new one. This means if you change the
        // DATABASE_VERSION the table will be dropped.
        // In a production app, this method might be modified to ALTER the table
        // instead of dropping it, so that existing data is not deleted.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + GroupMembersContract.GroupMemberEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);

    }
}
