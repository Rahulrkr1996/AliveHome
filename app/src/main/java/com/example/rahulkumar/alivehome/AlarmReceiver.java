package com.example.rahulkumar.alivehome;

/**
 * Created by Rahul Kumar on 3/1/2017.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;


public class AlarmReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
//        String phoneNumberReciver="7548082621";
//        String message="Hi I will be there later, See You Soon";
//        SmsManager sms = SmsManager.getDefault();
//        sms.sendTextMessage(phoneNumberReciver, null, message, null, null);

        Toast.makeText(context, "Alarm Triggered!!", Toast.LENGTH_LONG).show();
    }
}
