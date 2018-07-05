package com.example.hakeem.demo.utilities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSessionManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.example.hakeem.demo.AudioPlayerActivity;
import com.example.hakeem.demo.GoogleVisionScanningActivity;
import com.example.hakeem.demo.R;
import com.example.hakeem.demo.helper.SessionManager;

import java.io.IOException;

import wseemann.media.FFmpegMediaMetadataRetriever;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static java.lang.String.valueOf;


/**
 * Created by hakeem on 2/13/18.
 */

public class AudioPlayerService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener,
        AudioManager.OnAudioFocusChangeListener {


    /**
     * {{{{{{{User defined VARS}}}}}}}
     *
     * */
/******************************************************/

    /**
     * instance of media player to play files
     */
    private MediaPlayer mediaPlayer;

    /**
     * path to the audio file
     */
    private String mediaFile = Variables.completeAudioFilePath;

    /**
     * Used to pause/resume MediaPlayer
     */
    private int resumePosition;

    /**
     * for dealing with focus state
     */
    private AudioManager audioManager;

    /**
     * Binder given to clients
     */
    private final IBinder iBinder = new LocalBinder();

    /**
     * problem was :-
     * if user pressed paused of media player and once a notification came
     * media player resume by it's own
     * so i used this var "with onAudioFocusChange()" method to control this problem
     */
    private boolean pausedByUser = true;

    /**
     * Handle incoming phone calls
     */
    private boolean ongoingCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;

    /**
     * MediaSession
     */
    private MediaSessionManager mediaSessionManager;
    private MediaSessionCompat mediaSession;
    private MediaControllerCompat.TransportControls transportControls;

    /**
     * AudioPlayer notification ID
     */
    private static final int NOTIFICATION_ID = 101;

    /**
     * for indicate statues of playing audio (is playing now or not to handle new coming audio file
     * if user in shared listening
     */
    private SessionManager session;


    /**
     *          <<<<<<<-------->>>>     VARS for building notification      <<<<<<<-------->>>>
     * */

    /**
     * This notification ID can be used to access our notification after we've displayed it.
     * This can be handy when we need to cancel the notification, or perhaps update it.
     * This number is arbitrary and can be set to whatever you like. 1138 is in no way significant.
     */
    private static final int AUDIO_PLAYER_NOTIFICATION_ID = 101;
    /**
     * This notification channel id is used to link notifications to this channel
     */
    private static final String AUDIO_PLAYER_NOTIFICATION_CHANNEL_ID = "audio-player-notification-channel";


    private Handler mHandler;
    private Runnable mRunnable;

    private int mInterval = 1000;


    /**
     * <<<<<<<<<<<<<<<<<<<<User Defined Methods>>>>>>>>>>>>>>>>>>>>
     */


    /**
     * basic audio player operations
     */
    private void playMedia() {

        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    private void stopMedia() {

        if (mediaPlayer == null)
            return;

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            clearAllNotification();
        }
    }

    private void pauseMedia() {

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            resumePosition = mediaPlayer.getCurrentPosition();

            Log.e("pause", "pause " + resumePosition);
        }
    }

    private void resumeMedia() {

        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();

            Log.e("resume", "resume " + resumePosition);
        }

    }

    private void resetPlayer() {
        if (mediaPlayer != null)
            mediaPlayer.reset();
    }

    public void stopAll() {
        stopSelf();

        stopMedia();

        mediaPlayer.release();
        mediaPlayer = null;
        audioManager.abandonAudioFocus(this);
        mediaSessionManager = null;

        if (mHandler != null) {
            mHandler.removeCallbacks(mRunnable);
        }

    }

    /**
     * Let's initialize our media player
     */
    private void initMediaPlayer() {

        //Log.e("mediaFilexxxx",mediaFile);

        mediaPlayer = new MediaPlayer();

        //Set up MediaPlayer event listeners
        mediaPlayer.setOnCompletionListener(this);

        mediaPlayer.setOnErrorListener(this);

        mediaPlayer.setOnPreparedListener(this);

        mediaPlayer.setOnBufferingUpdateListener(this);

        mediaPlayer.setOnSeekCompleteListener(this);

        mediaPlayer.setOnInfoListener(this);

        //Reset so that the MediaPlayer is not pointing to another data source
        mediaPlayer.reset();

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            mediaPlayer.setDataSource(Variables.completeAudioFilePath);
            Log.e("mediaFilexxxx", Variables.completeAudioFilePath);

        } catch (IOException e) {
            e.printStackTrace();
            stopSelf();


            Log.e(" catch", "11111" + mediaFile);
        }
        mediaPlayer.prepareAsync();
    }


    /**
     * <<<<<<<<-------------->>>>>> {{{{{{ audio focus experience }}}}}} <<<<<-------------->>>>>
     * <p>
     * <p>
     * Invoked when the audio focus of the system is updated
     * To ensure this good user experience the we will have to handle AudioFocus events
     * and these are handled in the coming method.
     * This method is a switch statement with the focus events as its case:s.
     * Keep in mind that this override method is called after a request for
     * AudioFocus has been made from the system or another media app
     * ----------Cases we have-----------
     * AudioManager.AUDIOFOCUS_GAIN --> The service gained audio focus, so it needs to start playing.
     * AudioManager.AUDIOFOCUS_LOSS --> The service lost audio focus, the user probably moved to playing media on another app,
     * so release the media player.
     * AudioManager.AUDIOFOCUS_LOSS_TRANSIENT --> focus lost for a short time, pause the MediaPlayer.
     * AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK --> Lost focus for a short time, probably a notification arrived on the device,
     * lower the playback volume.
     */

    private boolean requestAudioFocus() {

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        assert audioManager != null;
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }


    private boolean removeAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                audioManager.abandonAudioFocus(this);
    }


    /**   <<<<<<<<-------------->>>>>> RECEIVERS FOR DIFFERENT ACTIONS <<<<<-------------->>>>>
     *         broadcasts declarations and registration methods
     **/

    //1- BroadcastReceivers
    /**
     * when the user unplug his headphone
     * it is normal to stop media player
     * ACTION_AUDIO_BECOMING_NOISY which means that the audio is about to become ‘noisy’
     * due to a change in audio outputs.
     */
    private BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //pause audio on ACTION_AUDIO_BECOMING_NOISY
            pauseMedia();
            showAudioPlayerNotification(PlaybackStatus.PAUSED);
        }
    };

    /**
     * this receiver triggered when user press button to resume playing audio file
     */
    private BroadcastReceiver resume = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            pausedByUser = false;
            resumeMedia();
            showAudioPlayerNotification(PlaybackStatus.PLAYING);
        }
    };

    /**
     * if user play track and new one come and user want to play it
     */
    private BroadcastReceiver newComing = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //TODO handle incoming new audio form sharing listening

            stopMedia();
            mediaPlayer.release();
            initMediaPlayer();
            showAudioPlayerNotification(PlaybackStatus.PLAYING);
        }
    };

    /**
     * this receiver triggered when user press button to pause playing audio file
     */
    private BroadcastReceiver pause = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            pausedByUser = true;
            pauseMedia();
            showAudioPlayerNotification(PlaybackStatus.PAUSED);
        }
    };

    /**
     * set seekTo function from seekBar declaration in onCreate in activity in "onProgressChanged"
     */
    private BroadcastReceiver SeekTo = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            boolean b = intent.getBooleanExtra("b_value", false);
            int i = intent.getIntExtra("i_value", 0);
            if (mediaPlayer != null && b) {
                mediaPlayer.seekTo(i);

                Log.e("seeeeeeeeeeek", " i = " + i + " b = " + b);
            }

        }
    };


    //2- registration methods

    /**
     * The BroadcastReceiver instance will pause the MediaPlayer when the system makes
     * an ACTION_AUDIO_BECOMING_NOISY call.
     * To make the BroadcastReceiver available you must register it
     * the coming function handles this and specifies the intent action
     * BECOMING_NOISY which will trigger this BroadcastReceiver
     */
    private void RegisterBecomingNoisyReceiver() {
        //register after getting audio focus
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(becomingNoisyReceiver, intentFilter);
    }

    private void RegisterResume() {

        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(Variables.Broadcast_RESUM);
        registerReceiver(resume, filter);
    }


    private void RegisterPause() {

        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(Variables.Broadcast_PAUSE);
        registerReceiver(pause, filter);
    }

    private void RegisterSeekTo() {

        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(Variables.Broadcast_SET_MEDIA_PLAYER_SEEK_TO);
        registerReceiver(SeekTo, filter);
    }

    private void RegisterNewComingTrack() {

        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(Variables.Broadcast_PLAY_NEW_COMING_AUDIO_FILE_STOP_CURRNET);
        registerReceiver(newComing, filter);
    }


    /**
     *                                      {{{{{Handling Incoming Calls}}}}}
     **/

    /**
     * The callStateListener() function is an implementation of the PhoneStateListener
     * that listens to TelephonyManagers state changes.
     * TelephonyManager provides access to information about the telephony services on the device and listens for
     * changes to the device call state and reacts to these changes.
     */
    //Handle incoming phone calls
    private void callStateListener() {
        // Get the telephony manager
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //Starting listening for PhoneState changes
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {

                switch (state) {

                    //if at least one call exists or the phone is ringing
                    //pause the MediaPlayer
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:

                        if (mediaPlayer != null) {
                            pauseMedia();
                            ongoingCall = true;
                        }
                        break;

                    case TelephonyManager.CALL_STATE_IDLE:
                        // Phone idle. Start playing.
                        if (mediaPlayer != null) {

                            if (ongoingCall) {

                                ongoingCall = false;

                                if (!pausedByUser) {
                                    resumeMedia();
                                }
                            }
                        }
                        break;
                }
            }
        };

        // Register the listener with the telephony manager
        // Listen for changes to the device call state.
        telephonyManager.listen(phoneStateListener,
                PhoneStateListener.LISTEN_CALL_STATE);
    }

/************************************************************
 //    /**
 //     * When the AudioPlayerService is playing something and the user wants to play a new track,
 //     * you must notify the service that it needs to move to new audio.
 //     * You need a way for the Service to listen to these “play new Audio” calls and act on them
 //     *
 //    private BroadcastReceiver playNewAudio = new BroadcastReceiver() {
 //        @Override
 //        public void onReceive(Context context, Intent intent) {
 //
 //            //Get the new media index form SharedPreferences
 //            stopSelf();
 //            //A PLAY_NEW_AUDIO action received
 //            //reset mediaPlayer to play the new Audio
 //            stopMedia();
 //            resetPlayer();
 //            mediaPlayer = null;
 //
 //            initMediaPlayer();
 //
 //            showAudioPlayerNotification(PlaybackStatus.PLAYING);
 //        }
 //    };
 //
 //    private void RegisterPlayNewAudio() {
 //        //Register playNewMedia receiver
 //        IntentFilter filter = new IntentFilter(Variables.Broadcast_PLAY_NEW_AUDIO);
 //        Log.e("RegisterPlayNewAudio", "form phone");
 //        registerReceiver(playNewAudio, filter);
 //    }
 **************************************************************/


    /**
     * <<<<<<<User Interactions>>>>>>>>>>>>>
     * To have full control over media playback in the AudioPlayerService you need to create an instance of MediaSession.
     * MediaSession allows interaction with media controllers, volume keys, media buttons, and transport controls.
     * An app creates an instance of MediaSession when it wants to publish media playback information or handle media keys
     * Notification.MediaStyle allows you to add media buttons without having to create custom notifications.
     * here we will use the MediaStyles support library, NotificationCompat.MediaStyle to support older Android versions
     **/

    private void initMediaSession() throws RemoteException {

        if (mediaSessionManager != null) return; //mediaSessionManager exists

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        }
        // Create a new MediaSession
        mediaSession = new MediaSessionCompat(getApplicationContext(), "AudioPlayer");


        //Get MediaSessions transport controls
        transportControls = mediaSession.getController().getTransportControls();

        //indicate that the MediaSession handles transport control commands
        // through its MediaSessionCompat.Callback.
        //TODO -->> one more flag and (setMediaButtonReceiver) added
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
                | MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS);
        mediaSession.setMediaButtonReceiver(null);

        // Attach Callback to receive MediaSession updates
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            // Implement callbacks
            @Override
            public void onPlay() {
                super.onPlay();
                resumeMedia();
                showAudioPlayerNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onPause() {
                super.onPause();
                pauseMedia();
                showAudioPlayerNotification(PlaybackStatus.PAUSED);
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
            }

            @Override
            public void onStop() {
                super.onStop();
                clearAllNotification();
                //Stop the service
                stopSelf();
            }

            @Override
            public void onSeekTo(long position) {
                super.onSeekTo(position);
            }
        });


        //set MediaSession -> ready to receive media commands
        mediaSession.setActive(true);

//        //indicate that the MediaSession handles transport control commands
//        // through its MediaSessionCompat.Callback.
//        //TODO -->> one more flag and (setMediaButtonReceiver) added
//        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
//                                 |MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS);
//        mediaSession.setMediaButtonReceiver(null);
//
//        // Attach Callback to receive MediaSession updates
//        mediaSession.setCallback(new MediaSessionCompat.Callback() {
//            // Implement callbacks
//            @Override
//            public void onPlay() {
//                super.onPlay();
//                resumeMedia();
//                showAudioPlayerNotification(PlaybackStatus.PLAYING);
//            }
//
//            @Override
//            public void onPause() {
//                super.onPause();
//                pauseMedia();
//                showAudioPlayerNotification(PlaybackStatus.PAUSED);
//            }
//
//            @Override
//            public void onSkipToNext() {
//                super.onSkipToNext();
//            }
//
//            @Override
//            public void onSkipToPrevious() {
//                super.onSkipToPrevious();
//            }
//
//            @Override
//            public void onStop() {
//                super.onStop();
//                clearAllNotification();
//                //Stop the service
//                stopSelf();
//            }
//
//            @Override
//            public void onSeekTo(long position) {
//                super.onSeekTo(position);
//            }
//        });


    }


    /**
     * <<<<<<<<building notifications>>>>>>>>>
     */

    /*
     * This notification ID can be used to access our notification after we've displayed it. This
     * can be handy when we need to cancel the notification, or perhaps update it. This number is
     * arbitrary and can be set to whatever you like. 1138 is in no way significant.
     */
    private void clearAllNotification() {
        NotificationManager notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationManager != null;
        notificationManager.cancel(NOTIFICATION_ID);
        notificationManager.cancelAll();

        stopSelf();

        //TODO come here to send message to finish MediaPlayerActivity  ---> completed
        Intent broadcastIntent = new Intent(Variables.Broadcast_FINISH_ACTIVITY);
        sendBroadcast(broadcastIntent);
    }

    public void showAudioPlayerNotification(PlaybackStatus playbackStatus) {

        //  Resources res = context1.getResources();
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.image);

        PendingIntent playPauseAction = null;

        int notificationAction = android.R.drawable.ic_media_pause;//needs to be initialized

        /**  displaying pause/resume button **/
        if (playbackStatus == PlaybackStatus.PLAYING) {

            notificationAction = android.R.drawable.ic_media_pause;
            //create the pause action
            playPauseAction = playbackAction(1);
        } else if (playbackStatus == PlaybackStatus.PAUSED) {

            notificationAction = android.R.drawable.ic_media_play;
            //create the play action
            playPauseAction = playbackAction(0);
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            //NotificationChannel mChannel = new NotificationChannel(id, name, importance);
            NotificationChannel mChannel = new NotificationChannel(
                    AUDIO_PLAYER_NOTIFICATION_CHANNEL_ID,
                    getString(R.string.audio_player_notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH);

            assert notificationManager != null;
            notificationManager.createNotificationChannel(mChannel);
        }


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(),
                AUDIO_PLAYER_NOTIFICATION_CHANNEL_ID)

                .setShowWhen(false)

                .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()

//                        // Attach our MediaSession token
                        .setMediaSession(mediaSession.getSessionToken())
//                        // Show our playback controls in the compact notification view.
                        .setShowActionsInCompactView(0, 1))

                .setColor(getResources().getColor(R.color.colorPrimary))

                .setLargeIcon(largeIcon)

                .setSmallIcon(android.R.drawable.stat_sys_headset)
////
                /**
                 * to force appearing player controls in notification if user enable hide content of notifications
                 * */
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

                //  .setDeleteIntent(playbackAction(4))

                /**
                 * prevent user from swap left or right to delete notification
                 * it will disappear itself if track end or press cancel button on it
                 */
                .setAutoCancel(false)
                .setOngoing(true)
/////
                .setContentTitle("guidake")
                .setContentText(Variables.statueDescription)

                .addAction(notificationAction, "pause", playPauseAction)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "exit", playbackAction(4))
//                .addAction(notificationAction, "play", play_pauseAction)


                .setContentIntent(contentIntent(this));


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        }
        assert notificationManager != null;

        notificationManager.notify(AUDIO_PLAYER_NOTIFICATION_ID, notificationBuilder.build());
        Log.e("11", "1");
    }


    /**
     * <<<<<<<<building notifications>>>>>>>>>
     */
    private PendingIntent playbackAction(int actionNumber) {
        Intent playbackAction = new Intent(this, AudioPlayerService.class);


        switch (actionNumber) {
            case 0:
                /** Play from notification*/
                playbackAction.setAction(Variables.ACTION_PLAY);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);

            case 1:
                /** Pause from notification */
                playbackAction.setAction(Variables.ACTION_PAUSE);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);

            case 4:
                /** exit from notification */
                playbackAction.setAction(Variables.ACTION_STOP);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);

            default:
                break;
        }

        return null;
    }


    private static PendingIntent contentIntent(Context context) {
        //        Log.e("content intent", "hello");
//        // Creates an explicit intent for an Activity in your app
//        Intent resultIntent = new Intent(context, MainActivity.class);
//        /**
//         * The stack builder object will contain an artificial back stack for the
//         * started Activity.
//         * This ensures that navigating backward from the Activity leads out of
//         * your app to the Home screen.
//         */
//        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
//        /**Adds the back stack for the Intent (but not the Intent itself)*/
//        stackBuilder.addParentStack(MainActivity.class);
//        /**Adds the Intent that starts the Activity to the top of the stack*/
//        stackBuilder.addNextIntent(resultIntent);
//        PendingIntent resultPendingIntent =
//                stackBuilder.getPendingIntent(
//                        0,
//                        PendingIntent.FLAG_UPDATE_CURRENT
//                );
//        return resultPendingIntent;


//        Intent intent = new Intent(context, MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
//        return  pendingIntent;


/** https://stackoverflow.com/questions/24051454/start-activity-from-notification-without-createing-activity-again?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa */
//        Intent i = new Intent(context,MainActivity.class);
//        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        PendingIntent pen = PendingIntent.getActivity(context,0,i,0);
//        return pen;


/** https://stackoverflow.com/questions/16898047/how-to-make-notification-resume-and-not-recreate-activity?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa */
        Intent intent = new Intent(context, AudioPlayerActivity.class);
        return PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intent, 0);
    }


    private void handleIncomingActions(Intent playbackAction) {
        if (playbackAction == null || playbackAction.getAction() == null)
            return;

        String actionString = playbackAction.getAction();


        if (actionString.equalsIgnoreCase(Variables.ACTION_PLAY)) {

            transportControls.play();

            /** send Broadcast to mainActivity to update text on button to be pause*/
            Intent broadcastIntent = new Intent(Variables.Broadcast_UPDATE_BUTTON_TEXT);
            sendBroadcast(broadcastIntent);

        } else if (actionString.equalsIgnoreCase(Variables.ACTION_PAUSE)) {

            transportControls.pause();

            /** send Broadcast to mainActivity to update text on button to be resume*/
            Intent broadcastIntent = new Intent(Variables.Broadcast_UPDATE_BUTTON_TEXT);
            sendBroadcast(broadcastIntent);

        } else if (actionString.equalsIgnoreCase(Variables.ACTION_STOP)) {

            transportControls.stop();
            stopAll();

            /** send Broadcast to mainActivity to finish activity if user press exit on notification */
            Intent broadcastIntent = new Intent(Variables.Broadcast_FINISH_ACTIVITY);
            sendBroadcast(broadcastIntent);
        }
    }


//    /**
//     * next method to control media player(resume & pause)
//     * it's work affects on notification and vice versa
//     **/
//    public void playPause() {
//
//        if (mediaPlayer.isPlaying()) {
//            pauseByUser = true;
//            pauseMedia();
//            showAudioPlayerNotification(PlaybackStatus.PAUSED);
//            AudioPlayerActivity.playPause.setText(getString(R.string.play));
//        } else if (!mediaPlayer.isPlaying()) {
//            playMedia();
//            showAudioPlayerNotification(PlaybackStatus.PLAYING);
//            AudioPlayerActivity.playPause.setText(getString(R.string.pause));
//        }
//
//    }


    /**
     * Invoked when the audio focus of the system is updated
     * To ensure this good user experience the we will have to handle AudioFocus events
     * and these are handled in the coming method.
     * This method is a switch statement with the focus events as its case:s.
     * Keep in mind that this override method is called after a request for AudioFocus has been made from the system or another media app
     * ----------Cases we have-----------
     * AudioManager.AUDIOFOCUS_GAIN --> The service gained audio focus, so it needs to start playing.
     * AudioManager.AUDIOFOCUS_LOSS --> The service lost audio focus, the user probably moved to playing media on another app,
     * so release the media player.
     * AudioManager.AUDIOFOCUS_LOSS_TRANSIENT --> focus lost for a short time, pause the MediaPlayer.
     * AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK --> Lost focus for a short time, probably a notification arrived on the device,
     * lower the playback volume.
     */

    @Override
    public void onAudioFocusChange(int focusState) {

        //Invoked when the audio focus of the system is updated.
        switch (focusState) {
            case AudioManager.AUDIOFOCUS_GAIN:

                if (!pausedByUser) {

                    // resume playback
                    if (mediaPlayer == null) {
                        initMediaPlayer();
                    } else if (!mediaPlayer.isPlaying()) {
                        mediaPlayer.start();
                    }
                    mediaPlayer.setVolume(1.0f, 1.0f);
                }
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
                mediaPlayer = null;
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:

                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (mediaPlayer.isPlaying()) {
                    //TODO here check if user himself pause or something else  ---> completed
                    mediaPlayer.pause();
                }
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.setVolume(0.1f, 0.1f);
                }
                break;
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {

    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

        //        if (mediaPlayer != null) {
//            stopMedia();
//            mediaPlayer.release();
//        }
        removeAudioFocus();

        clearAllNotification();

//        //TODO here send message to finish Activity  ---> completed
//        Intent broadcastIntent = new Intent(MainActivity.Broadcast_FINISH_ACTIVITY);
//        sendBroadcast(broadcastIntent);


        //stop the service
        stopSelf();

        Log.e("completion", "completion");

    }

    /**
     * Handle errors
     * Invoked when there has been an error during an asynchronous operation.
     */
    @Override
    public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.e("MediaPlayer Error", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.e("MediaPlayer Error", "MEDIA ERROR SERVER DIED " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.e("MediaPlayer Error", "MEDIA ERROR UNKNOWN " + extra);
                break;
        }
        return false;
    }


    /**
     * Invoked to communicate some info
     */
    @Override
    public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        Log.e("onPrepared", "player");
        playMedia();
        //  showAudioPlayerNotification(PlaybackStatus.PLAYING);
    }

    /**
     * Invoked indicating the completion of a seek operation
     */
    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {

    }


//    /**
//     * used to handel and control seek bar movement
//     */
//    private Runnable mRunnable;
//    private Handler mHandler = new Handler();
//    private int duration;// = audioFilePlayer.mediaPlayer.getDuration() / 1000; // In milliseconds
//
//    //@SuppressLint("ResourceAsColor")
//    public void initializeSeekBar() {
//        //  MainActivity.seekBar.setMax   (audioFilePlayer.mediaPlayer.getDuration() / 1000);
//        AudioPlayerActivity.seekBar.setBackgroundColor(getResources().getColor(R.color.fifth));
//        Variables.boolInitializeSeekBar = true;
//
//        mRunnable = new Runnable() {
//            @Override
//            public void run() {
//                if (mediaPlayer != null) {
//                    int mCurrentPosition = mediaPlayer.getCurrentPosition() / 1000; // In milliseconds
//                    Variables.CurrentPosition = mCurrentPosition;
//                    AudioPlayerActivity.seekBar.setProgress(mCurrentPosition);
//
//                    getAudioStats();
//                    Log.e("in initializeSeekBar", valueOf(mCurrentPosition));
//                    Log.e("thread", "started");
//                }
//                mHandler.postDelayed(mRunnable, 1000);
//            }
//        };
//        mHandler.postDelayed(mRunnable, 1000);
//    }


    /**********************************************/
    /**   {{{{{{{{ SERVICE LIFE CYCLE }}}}}}}}    */
    /**********************************************/

    @Override
    public boolean onUnbind(Intent intent) {

        return super.onUnbind(intent);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        //Request audio focus
        if (!requestAudioFocus()) {
            //Could not gain focus
            stopSelf();
        }

        mHandler = new Handler();

        if (mediaSessionManager == null) {
            try {

                initMediaSession();
                initMediaPlayer();
                showAudioPlayerNotification(PlaybackStatus.PLAYING);

                //   MediaButtonReceiver.handleIntent(mediaSession, intent);


                Log.e("onStartCommand", "after initMediaPlayer");
            } catch (RemoteException e) {

                Log.e("onStart ", "catch ");
                e.printStackTrace();
                stopSelf();

            }
        }


        getTrackDuration();


        handleIncomingActions(intent);

        initializeSeekBar();
        /**
         * here we now that user now playing an audio file
         * so pref value for this will be true
         *
         * if  a new audio coming we will make user chose (continue playing or play the new coming)
         * */


        session.setAudioPlayerState(true);

        Log.e("onStartCommand statues", " " + session.getAudioPlayerState());

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * In this method the MediaPlayer resources must be released,
     * as this service is about to be destroyed and there is no need for the app to control the media resources
     * When the Service is destroyed it must stop listening to incoming calls
     * and release the TelephonyManager resources.
     * Another final thing the Service handles before it’s destroyed is
     * clearing the data stored in the SharedPreferences
     * <p>
     * method also releases audio focus, this is more of a personal choice.
     * If you release the focus in this method the MediaPlayerService will have audio focus until destroyed
     * if there are no interruptions from other media apps for audio focus.
     **/
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            stopMedia();
            mediaSession.setActive(false);
            mediaPlayer.release();
            mediaPlayer = null;
            mediaSessionManager = null;


        }

        removeAudioFocus();

        //Disable the PhoneStateListener
        if (phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

        clearAllNotification();

        unregisterReceiver(resume);
        unregisterReceiver(pause);
        unregisterReceiver(becomingNoisyReceiver);
        unregisterReceiver(SeekTo);

        unregisterReceiver(newComing);


        /**
         * here we now that user stopped playing current audio track (track finished or user forced it to be finished)
         * so pref value for this will be false
         *
         * if a new audio coming it will be played immediately
         * */

        session.setAudioPlayerState(false);


        Log.e("onDestroy", "service");

        Log.e("onDestroy statues is ", " " + session.getAudioPlayerState());
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //onCreate coming before onStartCommand
        Log.e("onCreate", "service");


        session = new SessionManager(getApplicationContext());

        // Perform one-time setup procedures

        // Manage incoming phone calls during playback.
        // Pause MediaPlayer on incoming call,
        // Resume on hangup.
        callStateListener();


        //ACTION_AUDIO_BECOMING_NOISY -- change in audio outputs -- BroadcastReceiver
        RegisterBecomingNoisyReceiver();

        //RESUME
        RegisterResume();

        //PAUSE
        RegisterPause();

        RegisterSeekTo();

        RegisterNewComingTrack();
    }


    /************************************
     * {{{{{{{dealing with seekBar}}}}}}}
     ************************************/

    protected void getTrackDuration() {

        FFmpegMediaMetadataRetriever mmr = new FFmpegMediaMetadataRetriever();
        Log.e("getTrackDuration", Variables.completeAudioFilePath);
        try {
            mmr.setDataSource(Variables.completeAudioFilePath);
        } catch (Exception e) {
            e.printStackTrace();
            stopSelf();

            Log.e(" catch", "getTrackDuration");
        }

        mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ALBUM);
        mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ARTIST);

        String f = mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION);
//       // if(f != null)
//            assert f != null;
//        long duration = Long.parseLong(mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION));
//
//        mmr.release();
//
//        duration = duration / 1000;
//
//        Variables.trackDuration = duration;

        if (f != null) {
            long duration = Long.parseLong(f);

            mmr.release();

            duration = duration / 1000;

            Variables.trackDuration = duration;
        } else {

            Intent intent = new Intent(getApplicationContext(), GoogleVisionScanningActivity.class);
            intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            clearAllNotification();
            Toast.makeText(getApplicationContext(), "cant get track duration,please scan again", Toast.LENGTH_LONG).show();
            //TODO here come and tell user we couldn't get duration of the track go to scanner activity and scan again -->>too important
        }
    }

    protected void getAudioStats() {
        long minute = Variables.trackDuration / (60);
        long second = Variables.trackDuration - (minute * 60);


        String strMin = placeZeroIfNeed(minute);
        String strSec = placeZeroIfNeed(second);

        String trackDuration = String.format("%s:%s", strMin, strSec);
        Variables.trackDurationString = trackDuration;

        //TODO here lunch the registered   ---> completed
        //  mDuration.setText(trackDuration);


        long timePassedInSeconds = (mediaPlayer.getCurrentPosition() / 1000);

        long minutePassed = timePassedInSeconds / 60;
        long secondPassed = timePassedInSeconds % 60;

        strMin = placeZeroIfNeed(minutePassed);
        strSec = placeZeroIfNeed(secondPassed);


        //TODO : equal trackDuration to Constant.trackTimePassed; then fire a receiver to set string to textView  ---> completed
        String stringTimePassed = String.format("%s:%s", strMin, strSec);
        Variables.trackTimePassed = stringTimePassed;

        //TODO here lunch the registered receiver  ---> completed
        // mPass.setText(stringTimePassed);

        Intent broadcastIntent = new Intent(Variables.Broadcast_UPDATE_SEEKBAR_INFO);
        sendBroadcast(broadcastIntent);


    }


    private String placeZeroIfNeed(long number) {
        return (number >= 10) ? Long.toString(number) : String.format("0%s", Long.toString(number));
    }


    public void initializeSeekBar() {
        //  https://android--code.blogspot.com.eg/2017/08/android-media-player-pause-resume.html
        //https://androidexperinz.wordpress.com/?s=Communication+between+service+and+activity
        //https://medium.com/@ankit_aggarwal/ways-to-communicate-between-activity-and-service-6a8f07275297
        //https://www.sitepoint.com/a-step-by-step-guide-to-building-an-android-audio-player-app/

        mRunnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    if (mediaPlayer.isPlaying()) {
                        int mCurrentPosition = mediaPlayer.getCurrentPosition() / mInterval; // In milliseconds
                        Variables.CurrentPosition = mCurrentPosition;

                        //TODO : lunch receiver to setProgress of seekBar  ---> completed
                        //  AudioPlayerActivity.seekBar.setProgress(mCurrentPosition);

                        Intent broadcastIntent = new Intent(Variables.Broadcast_SEEKBAR_SET_PROGRESS);
                        sendBroadcast(broadcastIntent);


                        getAudioStats();
                        Log.e("in initializeSeekBar", valueOf(mCurrentPosition));
                        Log.e("thread", "started");
                    }
                }
                mHandler.postDelayed(mRunnable, mInterval);
            }
        };
        mHandler.postDelayed(mRunnable, mInterval);
    }


    /**
     * {{{{{{{defined CLASSES}}}}}}}
     */

    //class used to bind service with activity
    public class LocalBinder extends Binder {
        public AudioPlayerService getService() {
            return AudioPlayerService.this;
        }
    }
}