package com.example.hakeem.demo.sharingListening;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.example.hakeem.demo.AudioPlayerActivity;
import com.example.hakeem.demo.MainActivity;
import com.example.hakeem.demo.R;
import com.example.hakeem.demo.helper.SessionManager;
import com.example.hakeem.demo.utilities.Variables;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by hakeem on 4/15/18.
 * <p>
 * Receiving Messages
 * ------------------
 * To receive the message we need to create a class that will extend FirebaseMessagingService.
 * Again this is also a service, so we need to define it in our AndoirdManifest.xml
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    /**
     * this method will be called when the message is received by the push notification
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        if (remoteMessage.getData().size() > 0) {
            Log.e(TAG, "Data Payload: " + remoteMessage.getData().toString());
            try {
                JSONObject json = new JSONObject(remoteMessage.getData().toString());
                sendPushNotification(json);
            } catch (Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage());
            }
        }

    }

    /**
     * Json object will be as
     * data{
     * audioFileURL : "http://path/to/file.mp3"
     * imageFileURL : "http://path/to/statue/image.jpg"
     * }
     */

    //this method will display the notification
    //We are passing the JSONObject that is received from
    //firebase cloud messaging
    private void sendPushNotification(JSONObject json) {
        //optionally we can display the json into log
        Log.e(TAG, "Notification JSON " + json.toString());
        try {
            //getting the json data
            JSONObject data = json.getJSONObject("data");

            //parsing json data
            String audioFileURL = data.getString("audioFileUrl");
            String imageFileURL = data.getString("imageFileUrl");
            String statueDescription = data.getString("statue_description");

            Log.e("audioFileUrl", audioFileURL);
            Log.e("imageFileURL", imageFileURL);
            Log.e("statueDescription", statueDescription);


            /**
             * TODO HERE a new coming audio file for sharingListening
             * if user is already play another one
             *     he should chose if continue playing or play new coming one
            */

//            new SessionManager(getApplicationContext()).setAudioPlayerState(true);
            boolean isUserAlreadyPlayAudioTrack  = new SessionManager(getApplicationContext()).getAudioPlayerState();

            Log.e("new_coming_audio_file", "is user play now? " + isUserAlreadyPlayAudioTrack);


            if(isUserAlreadyPlayAudioTrack){

                //TODO make user chose to play new on or continue

                /** user currently play a track */
                Intent intent = new Intent("com.Demo.newComingAudioHandlerAction");
                intent.putExtra("audioFileUrl", audioFileURL);
                intent.putExtra("imageFileURL", imageFileURL);
                intent.putExtra("statueDescription", statueDescription);

                sendBroadcast(intent);



            }else{
                /** user currently doesn't play a track*/
                Variables.completeAudioFilePath = audioFileURL;

                Variables.completeImageFilePath = imageFileURL;

                Variables.statueDescription = statueDescription;

                Intent intent = new Intent(getApplicationContext(), AudioPlayerActivity.class);

                intent.putExtra("user_already_play_one", false);

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                startActivity(intent);
            }
/*************************
            Variables.completeAudioFilePath = audioFileURL;

            Variables.completeImageFilePath = imageFileURL;

            Variables.statueDescription = statueDescription;

            Intent intent = new Intent(getApplicationContext(), AudioPlayerActivity.class);

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(intent);
****************************************/

        } catch (JSONException e) {
            Log.e(TAG, "Json Exception: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

}
