package com.example.rahulkumar.alivehome;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TimePicker;

import java.util.GregorianCalendar;

public class AlarmActivity extends AppCompatActivity {

    private Button alarm_set_alarm;
    private TimePicker alarm_selector;
    private int hour, min;
    private RelativeLayout alarm_appliance_prompt;
    private ImageView alarm_prompt_cross;
    private String FAN_STATE = "FAN_OFF", BULB_STATE = "TL_ON";
    private ImageView alarm_bulb_image;
    private ImageView alarm_fan_image;
    private ImageButton alarm_fan_speed1, alarm_fan_speed2, alarm_fan_speed3, alarm_fan_speed4, alarm_fan_speed5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);


        alarm_set_alarm = (Button) findViewById(R.id.alarm_set_alarm);
        alarm_selector = (TimePicker) findViewById(R.id.alarm_selector);
        alarm_appliance_prompt = (RelativeLayout) findViewById(R.id.alarm_appliance_prompt);
        alarm_prompt_cross = (ImageView) findViewById(R.id.alarm_prompt_cross);
        alarm_bulb_image = (ImageView) findViewById(R.id.alarm_bulb_image);
        alarm_fan_image = (ImageView) findViewById(R.id.alarm_fan_image);
        alarm_fan_speed1 = (ImageButton) findViewById(R.id.alarm_fan_speed1);
        alarm_fan_speed2 = (ImageButton) findViewById(R.id.alarm_fan_speed2);
        alarm_fan_speed3 = (ImageButton) findViewById(R.id.alarm_fan_speed3);
        alarm_fan_speed4 = (ImageButton) findViewById(R.id.alarm_fan_speed4);
        alarm_fan_speed5 = (ImageButton) findViewById(R.id.alarm_fan_speed5);

        alarm_fan_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FAN_STATE == "FAN_OFF") {
                    changeFanSpeed(1);
                } else {
                    changeFanSpeed(0);
                }
            }
        });
        alarm_fan_speed1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFanSpeed(1);
            }
        });
        alarm_fan_speed2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFanSpeed(2);
            }
        });
        alarm_fan_speed3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFanSpeed(3);
            }
        });
        alarm_fan_speed4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFanSpeed(4);
            }
        });
        alarm_fan_speed5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFanSpeed(5);
            }
        });


        alarm_bulb_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (BULB_STATE.equals("BULB_ON")) {
                    alarm_bulb_image.setImageResource(R.drawable.bulb_off);
                    BULB_STATE = "BULB_OFF";
                } else {
                    alarm_bulb_image.setImageResource(R.drawable.bulb_on);
                    BULB_STATE = "BULB_ON";
                }
            }
        });

        alarm_prompt_cross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alarm_appliance_prompt.setVisibility(View.GONE);
            }
        });

        alarm_selector.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                hour = hourOfDay;
                min = minute;
            }
        });

        alarm_set_alarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                alarm_appliance_prompt.setVisibility(View.VISIBLE);

                Long currentTime = new GregorianCalendar().getTimeInMillis();
                Long alarmTime = currentTime + 60000;
                Intent intentAlarm = new Intent(AlarmActivity.this, AlarmReceiver.class);
                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, PendingIntent.getBroadcast(AlarmActivity.this, 1, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
            }
        });
    }

    public void changeFanSpeed(int speed) {
        if (speed == 0) {
            alarm_fan_image.setImageResource(R.drawable.ic_home_fan);

            alarm_fan_speed1.setBackgroundResource(R.color.grey);
            alarm_fan_speed2.setBackgroundResource(R.color.grey);
            alarm_fan_speed3.setBackgroundResource(R.color.grey);
            alarm_fan_speed4.setBackgroundResource(R.color.grey);
            alarm_fan_speed5.setBackgroundResource(R.color.grey);

            FAN_STATE = "FAN_OFF";
        } else if (speed == 1) {
            alarm_fan_image.setImageResource(R.drawable.ic_home_fan1);


            alarm_fan_speed1.setBackgroundResource(R.color.blue);
            alarm_fan_speed2.setBackgroundResource(R.color.grey);
            alarm_fan_speed3.setBackgroundResource(R.color.grey);
            alarm_fan_speed4.setBackgroundResource(R.color.grey);
            alarm_fan_speed5.setBackgroundResource(R.color.grey);

            FAN_STATE = "FAN_ON_1";
        } else if (speed == 2) {
            alarm_fan_image.setImageResource(R.drawable.ic_home_fan2);

            alarm_fan_speed1.setBackgroundResource(R.color.blue);
            alarm_fan_speed2.setBackgroundResource(R.color.blue);
            alarm_fan_speed3.setBackgroundResource(R.color.grey);
            alarm_fan_speed4.setBackgroundResource(R.color.grey);
            alarm_fan_speed5.setBackgroundResource(R.color.grey);

            FAN_STATE = "FAN_ON_2";
        } else if (speed == 3) {
            alarm_fan_image.setImageResource(R.drawable.ic_home_fan3);

            alarm_fan_speed1.setBackgroundResource(R.color.blue);
            alarm_fan_speed2.setBackgroundResource(R.color.blue);
            alarm_fan_speed3.setBackgroundResource(R.color.blue);
            alarm_fan_speed4.setBackgroundResource(R.color.grey);
            alarm_fan_speed5.setBackgroundResource(R.color.grey);

            FAN_STATE = "FAN_ON_3";
        } else if (speed == 4) {
            alarm_fan_image.setImageResource(R.drawable.ic_home_fan4);

            alarm_fan_speed1.setBackgroundResource(R.color.blue);
            alarm_fan_speed2.setBackgroundResource(R.color.blue);
            alarm_fan_speed3.setBackgroundResource(R.color.blue);
            alarm_fan_speed4.setBackgroundResource(R.color.blue);
            alarm_fan_speed5.setBackgroundResource(R.color.grey);

            FAN_STATE = "FAN_ON_4";
        } else if (speed == 5) {
            alarm_fan_image.setImageResource(R.drawable.ic_home_fan5);

            alarm_fan_speed1.setBackgroundResource(R.color.blue);
            alarm_fan_speed2.setBackgroundResource(R.color.blue);
            alarm_fan_speed3.setBackgroundResource(R.color.blue);
            alarm_fan_speed4.setBackgroundResource(R.color.blue);
            alarm_fan_speed5.setBackgroundResource(R.color.blue);

            FAN_STATE = "FAN_ON_5";
        }
    }

}
