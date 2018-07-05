package com.example.hakeem.demo.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by hakeem on 3/3/18.
 */

public class SessionManager {
    private static String TAG = SessionManager.class.getSimpleName();

    // Shared Preferences
    private SharedPreferences pref;

    private SharedPreferences.Editor editor;

    private Context _context;

    // Shared pref mode
    private int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String PREF_NAME = "Login";

    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";



    private static final String KEY_IS_WIFI_CONNECTED = "is_connected";


    private static final String KEY_IS_AUDIO_TRACK_PLAYED = "is_audio_track_played_now";

    private static final String KEY_DID_USER_PUBLISH_GROUP = "user_published_group";

    private static final String KEY_USER_ON_PROMBET_USERNAME_STEP = "enter_user_name";

    private static final String KEY_USER_MAIL = "user_mail";

    private static final String KEY_LOGGED_IN_WITH_GOOGLE_PLUSE = "google_plus";


    @SuppressLint("CommitPrefEdits")
    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setLogin(boolean isLoggedIn) {

        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);

        // commit changes
        editor.commit();

        Log.d(TAG, "User login session modified!");
    }

    public boolean isLoggedIn(){

        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }


    public void setConnectionState(boolean isConnected) {

        editor.putBoolean(KEY_IS_WIFI_CONNECTED, isConnected);

        // commit changes
        editor.commit();

        Log.d(TAG, "User login session modified!");
    }

    public boolean getConnectionState(){
        return pref.getBoolean(KEY_IS_WIFI_CONNECTED, false);
    }




    /**
     * the coming part to store device token in shared preference
     * */


    private static final String SHARED_PREF_NAME = "FCMSharedPref";
    private static final String TAG_TOKEN = "tagtoken";
    private static SessionManager mInstance;


    //this method will save the device token to shared preferences
    public boolean saveDeviceToken(String token){

        if(!token.equals(this.getDeviceToken())){
            //TODO--> come here to update device token in db
            /**
             * this happens if user uninstall app from device
             * then reinstall it and make log-in not sign-up
             * in this case new token is generated so it's value in DB should be updated
             * */
        }

        editor.putString(TAG_TOKEN, token);

        editor.apply();

        editor.commit();

        return true;
    }

    //this method will fetch the device token from shared preferences
    public String getDeviceToken(){

        return  pref.getString(TAG_TOKEN, null);
    }




    /**
     * the coming part to store statues of playing audio now
     * if user play audio now(means AudioPlayerActivity's onCreate() has been called)
     *    then this pref will be true:
     *        so if this user in group for sharing listening
     *        we will warning him for a new coming audio file
     *        if he accept it we will play it and stop current one
     *        else incoming audio will be ignored
     *
     * else we will lunch the audioPlayerActivity immediately
     *
     *
     * we will track statue of the playing audio now through statues of the service
     *    onStartCommand() --->> audio is paying now --->> player is running
     *    onFinish()       --->>   ~    ~    ~    ~           ~    ~ not running
     *
     *
     * */


    public void setAudioPlayerState(boolean isAudioTrackPlayed) {

        editor.putBoolean(KEY_IS_AUDIO_TRACK_PLAYED, isAudioTrackPlayed);

        // commit changes
        editor.commit();

        Log.d(TAG, "User login session modified!");
    }

    public boolean getAudioPlayerState(){

        return pref.getBoolean(KEY_IS_AUDIO_TRACK_PLAYED, false);
    }


    /**
     * the coming part to check if user already created a share listening group or not
     * if so --> (publish group button) in navigation menu will dis enabled
     * */


    public void setShareListeningState(boolean userPublishedGroup) {

        editor.putBoolean(KEY_DID_USER_PUBLISH_GROUP, userPublishedGroup);

        // commit changes
        editor.commit();

        Log.d(TAG, "User login session modified!");
    }

    public boolean getShareListeningState(){

        return pref.getBoolean(KEY_DID_USER_PUBLISH_GROUP, false);
    }




    //true --> means that user signed up but didn't entered unique username

    public void setProbmetUsernameStep(boolean inSelectStep) {

        editor.putBoolean(KEY_USER_ON_PROMBET_USERNAME_STEP, inSelectStep);

        // commit changes
        editor.commit();

        Log.d(TAG, "User login session modified!");
    }

    public boolean getProbmetUsernameStep(){

        return pref.getBoolean(KEY_USER_ON_PROMBET_USERNAME_STEP, false);
    }

    public void setUserMail(String mail) {

        editor.putString(KEY_USER_ON_PROMBET_USERNAME_STEP, mail);

        // commit changes
        editor.commit();

        Log.d(TAG, "User login session modified!");
    }

    public String getUserMail(){

        return pref.getString(KEY_USER_MAIL, "h");
    }




    public void setLoggedInWithGooglePLusState(boolean loggedInWithGooglePlus) {

        editor.putBoolean(KEY_LOGGED_IN_WITH_GOOGLE_PLUSE, loggedInWithGooglePlus);

        // commit changes
        editor.commit();

        Log.d(TAG, "User login session modified!");
    }

    public boolean getLoggedInWithGooglePLusState(){

        return pref.getBoolean(KEY_LOGGED_IN_WITH_GOOGLE_PLUSE, false);
    }



    /**
    public static synchronized SessionManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SessionManager(context);
        }
        return mInstance;
    }

     using :-
            SessionManager.getInstance(getApplicationContext()).saveDeviceToken(token);
     */

}
