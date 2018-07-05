package com.example.hakeem.demo.utilities;

import android.graphics.Bitmap;

import com.example.hakeem.demo.AudioPlayerActivity;

/**
 * Created by hakeem on 1/25/18.
 */

public final class Variables {

    public static final int DATABASE_VERSION = 1;

    /**
     * operation type for AsyncTask to fetch an audio file path
     */
    public static String selectAudioFilePathOperation = "selectCorrectAudioFilePath";

    /**
     * other variables
     * */
    public static String audioFileLanguage = "";
    public static String statueName = "";


    /**
     * variables for connection
     *
     * */
    private static final String AUTHORITY = "192.168.43.149";         //my IP on my network
    private static final String BASE_SCHEME_URI = "http://" + AUTHORITY;/** https://192.168.1.5 */

    public static String serverUrl = BASE_SCHEME_URI + "/";             /** https://192.168.1.5/ */

    /**
     * current scanned statue required info
     * */
    //1-tables names
    public static String audioFilePathTableName = "";
    public static String imageFilePathTable = "";
    //2-required URLs and Strings
    public static String completeAudioFilePath = "";
    public static String completeImageFilePath = "";
    public static String statueDescription = "";


    /***************************************************************************************************************
     *----->{{{{{{{variable for track playing in activity and interaction between activity and service}}}}}}}<-----*
     ***************************************************************************************************************/


    /**
     *  <<<<<<<-------->>>>user interaction with notification control button<<<<<<<-------->>>>
     *
     * The String variables are used to notify which action is triggered from the MediaSession callback listener.
     * The rest of the instances relate to the MediaSession and a notification ID to uniquely identify the MediaStyle notification
     */
    public static final String ACTION_PLAY = "ACTION_PLAY";
    public static final String ACTION_PAUSE = "ACTION_PAUSE";
    public static final String ACTION_STOP = "ACTION_STOP";


    /**
     * these strings for Broadcasts send and receiver
     */
    /** 1-to send broadcast to service to update resume/pause button on the notification to be "pause" */
    public static final String Broadcast_PAUSE = "user_want_to_pause";

    /** 2-to send broadcast to service to update resume/pause button on the notification to be "resume" */
    public static final String Broadcast_RESUM = "user_want_to_resume";

    /** 3-to send broadcast to AudioPlayerActivity class to update text on pause/resume button */
    public static final String Broadcast_UPDATE_BUTTON_TEXT = "update_button_text";

    /** 4-to send broadcast to AudioPlayerActivity to finish it if user press "exit" button on notification or when track playing completed */
    public static final String Broadcast_FINISH_ACTIVITY = "finish_activity";

    /** 5-to send broadcast to AudioPlayerActivity set progress of it's seekBar */
    public static final String Broadcast_SEEKBAR_SET_PROGRESS = "set_seek_bar_progress";

    /** 6-to send broadcast to AudioPlayerActivity to update INFO of two textviews of seekBar(time passed & duration of the track) while playing */
    public static final String Broadcast_UPDATE_SEEKBAR_INFO = "update_seek_bar_info";

    /** 7-to send broadcast to service to update current position of track playing if user change it */
    //used in declaration of seekBar in onCreate
    public static final String Broadcast_SET_MEDIA_PLAYER_SEEK_TO = "update_seek_bar_position";

    /** 7-to send broadcast to service to stop and release media player stop service and then play new track */
    //used in declaration of seekBar in onCreate
    public static final String Broadcast_PLAY_NEW_COMING_AUDIO_FILE_STOP_CURRNET = "play_new_stop_current";





    /** current position played from track to update seek bar */
    public static int CurrentPosition = 0;

    /** whole track duration to be setMax for seek bar */
    public static long trackDuration = 0;

    /** used with text view of seek bar hold total duration of track */
    public static String trackDurationString = "";

    /** used with text view of seek bar hold time passed while playing track */
    public static String trackTimePassed = "";






    /********************************************************************
     *           ----->{{{{{{{php files URL's}}}}}}}<-----              *
     ********************************************************************/


    /**
     * used to be displayed in view of statue image
     * */
    public static Bitmap statueImage = null;

    public static String URL_CHECK_AND_UPDATE_USER_NAME_VALUE = serverUrl + "guidak_files/check_uniqueness_update_username.php";


    public static String URL_TEST_SELECTED_USERNAME = serverUrl + "guidak_files/is_username_exist.php";


    public static String URL_INVOKE_STATUE_INFO = serverUrl + "guidak_files/statueInfo.php";



    //registration urls
    // Server user login url
    public static String URL_LOGIN = serverUrl + "guidak_files/LogAndSign/login.php";

    // Server user register url
    public static String URL_REGISTER = serverUrl + "guidak_files/LogAndSign/register.php";


    // Server user upload profile image url
    public static String URL_UPLOAD_PROFILE_IMAGE = serverUrl + "guidak_files/LogAndSign/include/upload_profile_photo.php";

    // Server user upload profile image url
    public static String URL_INVOKE_PROFILE_IMAGE = serverUrl + "guidak_files/LogAndSign/include/invoke_profile_image.php";

    public static final String URL_SEND_ALL_GROUP_MEMBERS =  serverUrl + "guidak_files/LogAndSign/share_listening/sendToAllGroup.php";

    //to update token if user uninstall app then reinstall it and perform login not sign-up
    public static final String URL_UPDATE_DEVICE_TOKEN_AFTER_LOGIN = serverUrl + "guidak_files/update_device_token.php";

    //to check if mail used in sign-up valid or not*/
    public static final String URL_CHECK_REGISTERD_MAIL_VALIDATION = serverUrl + "guidak_files/LogAndSign/check_fake_mail.php";

    //to check if mail used in sign-up valid or not*/
    public static final String URL_LOG_IN_WITH_GOOGLE_PLUS = serverUrl + "guidak_files/LogAndSign/log_into_with_google_plus.php";

    //to insert new user in server DB with google+ login
    public static final String URL_LOG_IN_WITH_GOOGLE_PLUS_STEP_TWO = serverUrl + "guidak_files/LogAndSign/log_into_with_google_plus_step_two.php";

    // Server user invoke profile image url /opt/lampp/htdocs/guidak_files/LogAndSign/include/invoke_added_member_profile_image.php
    public static String URL_INVOKE_ADDED_MEMBER_PROFILE_IMAGE = serverUrl + "guidak_files/LogAndSign/include/invoke_added_member_profile_image.php";

    public static String INVOKE_FEEDBACK_URL = serverUrl + "guidak_files/feedback_scripts/invoke_feedback.php";

    public static String GET_USER_ID = serverUrl + "guidak_files/LogAndSign/get_user_ID_by_mail.php";

    public static String UPLOAD_FEEDBACK_TEXT_ONLY = serverUrl + "guidak_files/feedback_scripts/upload_feedback_text_only.php";

    public static String UPLOAD_FEEDBACK_TEXT_PHOTO = serverUrl + "guidak_files/feedback_scripts/upload_feedback_photo_text.php";

    public static String UPLOAD_FEEDBACK_PHOTO_ONLY = serverUrl + "guidak_files/feedback_scripts/upload_feedback_photo_only.php";
    /**
     * {{{{{{{{{{{{{{{Variables new version}}}}}}}}}}}}}}}
     **/
    public static String SSID = "hamdy";
    public static String KEY = "cap#@#ahmed45814**tarek";


    public static boolean isTokenRefreshedAfterLogIn = false;




}
