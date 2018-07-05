package com.example.hakeem.demo.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.hakeem.demo.MainActivity;

public class NewComingAudioFromSharingReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent)
    {


//        Intent intevnt = new Intent(context, MainActivity.class);
//        intevnt.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intevnt.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
//        intevnt.putExtra("value", "playing new one?");

        Log.e("onReceiveBroeadcast", intent.getStringExtra("audioFileUrl"));

        Intent lunchAlert = new Intent(context, MainActivity.class);
        lunchAlert.putExtra("audioFileUrl", intent.getStringExtra("audioFileUrl"));
        lunchAlert.putExtra("imageFileURL", intent.getStringExtra("imageFileURL"));
        lunchAlert.putExtra("statueDescription", intent.getStringExtra("statueDescription"));

        lunchAlert.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        lunchAlert.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

        context.startActivity(lunchAlert);

    }
}
