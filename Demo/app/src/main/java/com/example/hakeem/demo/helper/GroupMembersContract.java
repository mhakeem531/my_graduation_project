package com.example.hakeem.demo.helper;

import android.provider.BaseColumns;

/**
 * Created by hakeem on 4/11/18.
 */

public class GroupMembersContract {

    public static final class GroupMemberEntry implements BaseColumns {
        public static final String TABLE_NAME = "members";
        public static final String COLUMN_USERNAME = "memberUsername";
    }
}
