package com.example.rahulkumar.alivehome;

/**
 * Created by Rahul Kumar on 3/1/2017.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.widget.Toast;


public class AlarmReceiver extends BroadcastReceiver {
    private Context cont;
    private String BULB_STATE = null;
    private String FAN_STATE = null;
    MediaPlayer mp;

    @Override
    public void onReceive(Context context, Intent intent) {
        BULB_STATE = intent.getStringExtra("BULB_STATE");
        FAN_STATE = intent.getStringExtra("FAN_STATE");
        cont = context;

        mp = MediaPlayer.create(context, R.raw.alarm_beep);
        mp.start();

        Toast.makeText(context, "Alarm Triggered...", Toast.LENGTH_LONG).show();
    }
}
