package com.example.hakeem.demo.sharingListening;

import android.util.Log;

import com.example.hakeem.demo.helper.SessionManager;
import com.example.hakeem.demo.utilities.Variables;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by hakeem on 4/15/18.
 * Every device generates a unique token to receive notifications.
 * And for this we have to create a class that will extend the class FirebaseInstanceIdService.
 *
 * we need to define this class inside AndroidManifest.xml
 *
 *                  {{{{{{INTRO}}}}}}
 *  Ways of Receiving Push Notification :-
 *          there is two ways to send and push messages using FCM(firebase cloud messaging)
 *    1-Using FCM Token: We use this method when we want to send a notification to a specific device.
 *      Or some dynamic group of devices. Upon initial startup,
 *      the firebase SDK generates a registration token for the application.
 *      We use this token to identify the device.
 *
 *    2-Using Topic: We can create topics and let our users subscribe to those topics.
 *      Then we can send the message to the topic.
 *      And the message will be sent to all the users of that particular topic.
 *      In this method, we don’t need to store any token.
 *
 *
 *
 *  we now will apply "Firebase Cloud Messaging using FCM Access Token"
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseIIDService";

    /**
     * The token is not every time generated. We get the token only when firebase refreshes the token.
     * So it might happen that once you run the application and by mistake cleared the log.
     * Then if you again run your application, you will not find the token.
     * So, in this case, you have to uninstall the app, and then you need to again run it.
     *
     *
     * Google Play Service version should be higher than the Firebase version that you are using in your project.
     * It is always better to update the Google Play Services to the latest version.
     * */
    @Override
    public void onTokenRefresh() {

        //Getting registration token
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        //Displaying token on logcat
        Log.e(TAG, "Refreshed token: " + refreshedToken);

        //calling the method store token and passing token
        storeToken(refreshedToken);
    }

    private void storeToken(String token) {

        Variables.isTokenRefreshedAfterLogIn = true;

        //saving the token on shared preferences
        SessionManager sessionManager = new SessionManager(getApplicationContext());

        sessionManager.saveDeviceToken(token);

    }


    /**
     * NOW we have device token stored in sharedPreference
     * */

}
