package com.example.hakeem.demo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.hakeem.demo.NetworkUtilites.MyVolley;
import com.example.hakeem.demo.helper.AppUserInfoDbHelper;
import com.example.hakeem.demo.helper.GroupMembersContract;
import com.example.hakeem.demo.utilities.AudioPlayerService;
import com.example.hakeem.demo.utilities.Variables;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class AudioPlayerActivity extends AppCompatActivity {


    //////////////////////////////////////////////////////////////
    /**
     * an instance of the Service
     */
    private AudioPlayerService player;
    /**
     * contains the status of the Service, bound or not to the activity
     */
    boolean serviceBound = false;
    //////////////////////////////////////////////////////////////

    /**
     * false mean we want to Resume
     * true mean we want to Pause
     */
    private boolean ResumeOrPause = true;


    ArrayList<String> groupMembersUsernames = new ArrayList<String>();
    private AppUserInfoDbHelper db;  //to invoke all user-names in group


    /**
     * UI components
     */
    private ImageButton playPauseButton;
    private SeekBar mSeekBar;
    private TextView mPass;
    private TextView mDuration;
    private TextView statueDescription;
    private ImageView statueImage;
    private ConstraintLayout audioPlayerBackGround;

    /**
     * finish this activity to start it again with new track if user already play one and another on come from listening sharing feature
     */
    public static Activity audioPlayerActivity;

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            AudioPlayerService.LocalBinder binder = (AudioPlayerService.LocalBinder) service;
            player = binder.getService();
            serviceBound = true;

            Toast.makeText(AudioPlayerActivity.this, "Service Bound", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    /**
     * method to bind and start service to begin play audio file after scanning
     */
    private void playAudio() {
        Log.e("playAudio ", "& serviceBound is " + serviceBound);

//        Intent playerIntent = new Intent(this, AudioPlayerService.class);
//        Log.e("playAudio", "before start");
//        startService(playerIntent);
//        Log.e("playAudio", "after start");
//        bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
//        Log.e("playAudio", "after bind");
//
        //Check is service is active
        if (!serviceBound) {
            Intent playerIntent = new Intent(this, AudioPlayerService.class);
            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        } else {
            //Service is active
            //Send media with BroadcastReceiver
        }
    }

    private void Send() {
        if (serviceBound) {
            //Service is active
            if (ResumeOrPause) {
                /** user want to pause*/

                //Send a broadcast to the service -> PAUSE
                Intent broadcastIntent = new Intent(Variables.Broadcast_PAUSE);
                sendBroadcast(broadcastIntent);
                ResumeOrPause = false;
                //   playPauseButton.setText(R.string.resume);
                playPauseButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_play));

            } else if (!ResumeOrPause) {
                /** user want to resume*/

                //Send a broadcast to the service -> RESUME
                Intent broadcastIntent = new Intent(Variables.Broadcast_RESUM);
                sendBroadcast(broadcastIntent);
                ResumeOrPause = true;
                //  playPauseButton.setText(R.string.pause);
                playPauseButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause));
            }

        }

    }


//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//
////        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
////
////            Log.e("onKeyDown2", "onKeyDown2");
////            Log.e("onKeyDown-key", String.valueOf(keyCode));
//////            if ((keyCode == 79)) {
//////                Log.e("onKeyDown", "onKeyDown");
//////                Send();
//////            }
//////
////            if(keyCode == KeyEvent.KEYCODE_HEADSETHOOK){
////                Send();
////            }
////        }
//
//        Log.e("onKeyDown1", "onKeyDown1");
//
//        if(keyCode == KeyEvent.KEYCODE_HEADSETHOOK){
//                Send();
//            }
//        return true;
//       // return super.onKeyDown(keyCode, event);
//    }

    /**
     * --------------------------------------------------------------------------------+
     * {{{{{{whole broadcasts receivers declaration and registration functions }}}}}}  |
     * ---------------------------------------------------------------------------------+
     */

    private BroadcastReceiver updateButton = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (ResumeOrPause) {
                ResumeOrPause = false;
                //  playPauseButton.setText("resume");
                playPauseButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_play));
            } else if (!ResumeOrPause) {
                ResumeOrPause = true;
                // playPauseButton.setText("pause");
                playPauseButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause));
            }
        }
    };

    private BroadcastReceiver SetSeekBarProgress = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            mSeekBar.setProgress(Variables.CurrentPosition);
            mSeekBar.setMax((int) Variables.trackDuration);
            Log.e("ONRECEICVE", Variables.CurrentPosition + " " + Variables.trackDuration);
        }
    };


    private BroadcastReceiver finishAll = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

    /**
     * this receiver triggered to update seekBar info
     */
    private BroadcastReceiver updateSeekBarInfo = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            mDuration.setText(Variables.trackDurationString);
            mPass.setText(Variables.trackTimePassed);
        }
    };


    private void registerUpdateButton() {

        IntentFilter filter = new IntentFilter(Variables.Broadcast_UPDATE_BUTTON_TEXT);
        registerReceiver(updateButton, filter);
    }

    private void registerFinishActivity() {

        IntentFilter filter = new IntentFilter(Variables.Broadcast_FINISH_ACTIVITY);
        registerReceiver(finishAll, filter);
    }

    private void registerUpdateSeekBarInfo() {

        IntentFilter filter = new IntentFilter(Variables.Broadcast_UPDATE_SEEKBAR_INFO);
        registerReceiver(updateSeekBarInfo, filter);
    }

    private void resisterSetSeekBarProgress() {

        IntentFilter filter = new IntentFilter(Variables.Broadcast_SEEKBAR_SET_PROGRESS);
        registerReceiver(SetSeekBarProgress, filter);
    }


    /**
     * {{{{Activity life cycle}}}}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_player);

        audioPlayerActivity = this;

        /**
         * this var to indicate if user already play a track and new one coming from sharing listening feature
         * true --> user want to stop current and play new coming
         * false --> there is no track now been played
         * */
        boolean isTrackPlaying = getIntent().getBooleanExtra("user_already_play_one", false);

//        if(isTrackPlaying){
//            //TODO come hear and sent a broadcast to tell service we want to play another audio file
//
//            Intent broadcastIntent = new Intent(Variables.Broadcast_PLAY_NEW_COMING_AUDIO_FILE_STOP_CURRNET);
//            sendBroadcast(broadcastIntent);
//
//            Log.e("HeLoLo", "HeLoLo");
//        }

        /** declare UI components */
        statueImage = findViewById(R.id.statue_image);

        playPauseButton = findViewById(R.id.play_pause_btn);
        mSeekBar = findViewById(R.id.seekBar);
        mPass = findViewById(R.id.tv_pass);
        mDuration = findViewById(R.id.tv_due);
        statueDescription = findViewById(R.id.statue_description);
        statueDescription.setText(Variables.statueDescription);
        audioPlayerBackGround = findViewById(R.id.audio_player_background);


        /**

         {{{{{{for accessing DB tables}}}}}}

         */
        // SqLite database handler for fetching app user info
        db = new AppUserInfoDbHelper(getApplicationContext());
        if (getAllGroupMembers()) {
            //TODO here send file_path and image_path to all  ---> completed

            sendMultiplePush();
        } else {
            //TODO user has not group yet
        }


        statueImage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //   Toast.makeText(AudioPlayerActivity.this, "clicked", Toast.LENGTH_LONG).show();

                new PhotoFullPopupWindow(AudioPlayerActivity.this, R.layout.popup_photo_full, v, Variables.completeImageFilePath, null);
            }
        });


        /** UI interaction */
        Log.e("onCreate from : ", "player activity");

        /** user want to pause or resume*/
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Send();
            }
        });

        /** seek bar operations */
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
//                if(MediaPlayerService.mediaPlayer!=null && b){
//                    MediaPlayerService.mediaPlayer.seekTo(i*1000);
//                }

                Intent broadcastIntent = new Intent(Variables.Broadcast_SET_MEDIA_PLAYER_SEEK_TO);
                broadcastIntent.putExtra("i_value", i * 1000);
                broadcastIntent.putExtra("b_value", b);
                sendBroadcast(broadcastIntent);
                Log.e("onProgressChanged", " i = " + i + " b = " + b);
                //  MediaPlayerService.mediaPlayer;

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        /** let's play audio file after scanning */
        playAudio();


        /** let's register our broadcasts receivers*/
        registerUpdateButton();
        registerFinishActivity();
        registerUpdateSeekBarInfo();
        resisterSetSeekBarProgress();


        /** let's download image of statue to display in audio played activity */
        new DownloadImageTask(statueImage, audioPlayerBackGround).execute(Variables.completeImageFilePath);
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    private boolean userRotateScreen = false;

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("ServiceState", serviceBound);
        userRotateScreen = true;
        savedInstanceState.putBoolean("ResumeOrPause", ResumeOrPause);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean("ServiceState");

        //now media player is paused and screen is rotated
        if (!savedInstanceState.getBoolean("ResumeOrPause")) {
            Log.e("now", "is_is_paused");
            playPauseButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_play));
        } else {
            Log.e("now", "is_is_resumed");
            playPauseButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause));

        }
        mDuration.setText(Variables.trackDurationString);
        mPass.setText(Variables.trackTimePassed);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!userRotateScreen) {
            if (serviceBound) {
                unbindService(serviceConnection);
                //service is active
                player.stopSelf();

                /** let's unregister all broadcasts receivers */
                unregisterReceiver(updateButton);
                unregisterReceiver(finishAll);
                unregisterReceiver(updateSeekBarInfo);
                unregisterReceiver(SetSeekBarProgress);
            }
        }

        userRotateScreen = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * Query the mDb and get all members from the members table
     *
     * @return Cursor containing the list of members
     */
    private boolean getAllGroupMembers() {
        Cursor members = db.queryAllMembers();

        if (members.moveToFirst()) {
            int i = 0;
            do {

                String username = members.getString(members.getColumnIndex(GroupMembersContract.GroupMemberEntry.COLUMN_USERNAME));

                groupMembersUsernames.add(username);
            } while (members.moveToNext());
            return true;
        } else {
            return false;
        }
    }


    @SuppressLint("StaticFieldLeak")
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
        ConstraintLayout backGround;

        DownloadImageTask(ImageView bmImage, ConstraintLayout backGround) {
            this.bmImage = bmImage;
            this.backGround = backGround;
        }

        protected Bitmap doInBackground(String... urls) {
            String urlDisplay = urls[0];
            Bitmap mIcon11 = null;
            try {

                InputStream in = new java.net.URL(urlDisplay).openStream();

                mIcon11 = BitmapFactory.decodeStream(in);

            } catch (Exception e) {

                Log.e("Error", e.getMessage());

                e.printStackTrace();
            }
            return mIcon11;
        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);

            Drawable d = new BitmapDrawable(getResources(), result);
            backGround.setBackground(d);

            Variables.statueImage = result;
        }
        /**
         * source = https://stackoverflow.com/questions/5776851/load-image-from-url
         * */
    }


    private void sendMultiplePush() {
        final String audioFilePath = Variables.completeAudioFilePath;
        final String imageFilePath = Variables.completeImageFilePath;
        final String statueDescription = Variables.statueDescription;
        final int count = this.groupMembersUsernames.size();


        StringRequest stringRequest = new StringRequest(Request.Method.POST, Variables.URL_SEND_ALL_GROUP_MEMBERS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Toast.makeText(AudioPlayerActivity.this, response, Toast.LENGTH_LONG).show();
                        Log.e("ERRORRR", response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("audio_file_url", audioFilePath);
                params.put("image_file_url", imageFilePath);
                params.put("statue_description", statueDescription);


                Log.e("COUNT = ", String.valueOf(count));

                for (int i = 0; i < count; i++) {

                    params.put("members_usernames[]", groupMembersUsernames.get(i));
                }


                return params;
            }
        };

        MyVolley.getInstance(this).addToRequestQueue(stringRequest);
        /** Volley.newRequestQueue(this).add(stringRequest);*/
    }


}
