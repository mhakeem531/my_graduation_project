package com.example.hakeem.demo;
 
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.example.hakeem.demo.utilities.Variables;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //  setContentView(R.layout.activity_main);

        /***************************************************
         * boolean x = getIntent().getBooleanExtra("user_already_play_one", false);
         ***************************************************/

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getApplicationContext().getResources().getString(R.string.new_track))

                .setMessage(getApplicationContext().getResources().getString(R.string.play_new_track))

                .setCancelable(false)

                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.cancel();

                        if(AudioPlayerActivity.audioPlayerActivity != null)
                        /**https://developer.android.com/guide/components/activities/activity-lifecycle*/
                        AudioPlayerActivity.audioPlayerActivity.finish();

                        Intent broadcastIntent = new Intent(Variables.Broadcast_PLAY_NEW_COMING_AUDIO_FILE_STOP_CURRNET);
                        sendBroadcast(broadcastIntent);


                        Variables.completeAudioFilePath = getIntent().getStringExtra("audioFileUrl");

                        Variables.completeImageFilePath = getIntent().getStringExtra("imageFileURL");

                        Variables.statueDescription = getIntent().getStringExtra("statueDescription");

                        Intent intent = new Intent(getApplicationContext(), AudioPlayerActivity.class);

                        intent.putExtra("user_already_play_one", true);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        startActivity(intent);

                        finish();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();


    }

}
