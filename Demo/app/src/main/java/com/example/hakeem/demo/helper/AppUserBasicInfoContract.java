package com.example.hakeem.demo.helper;

import android.provider.BaseColumns;

/**
 * Created by hakeem on 4/13/18.
 */

public class AppUserBasicInfoContract {
    public static final class AppUserInfoEntry implements BaseColumns {
        // Login table name
        public static final String TABLE_NAME = "user";

        // Login Table Columns names
        public static final String KEY_UID = "uid";
        public static final String KEY_NAME = "user_name";
        public static final String KEY_EMAIL = "email";
        public static final String KEY_DISPLAYED_NAME = "displayed_name";
        public static final String KEY_IMAGE = "image";

        public static final String KEY_CREATED_AT = "created_at";
    }
}
