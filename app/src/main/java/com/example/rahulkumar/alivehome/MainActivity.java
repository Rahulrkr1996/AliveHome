package com.example.rahulkumar.alivehome;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.chilkatsoft.CkRsa;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketConnectionHandler;
import de.tavendo.autobahn.WebSocketException;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, TextToSpeech.OnInitListener {

    private static final String TAG = "MainActivity";
    private ImageView home_help, home_settings;
    private ImageView home_bulb_image;
    private ImageView home_fan_image;
    private SeekBar home_fan_seekbar;
    private TextView home_fan_speed;
    private String username_init = null, password_init = null;
    private String[] data_parsed = null;
    private int status_request = 0; // Dont know
    private int track = 0; // to check if Wifi is being used.
    public String transfer_session = "";
    public String publicKey = "<RSAPublicKey><Modulus>pManIJm8ZFVpV4w/hGkr+11gHCfou+AvpbBGMFvcYEyLC78Y2geM88v/J1uxXov6vSpZ0DFKgZzlMYgJf8f8/4HuQukZQtnC6mycqdThPxGQu8+USWcNUCkd0ilx7wlO58L/Hy2QqGxaso4HGvarIwGshfIuJDGUQ4OONavFLSk=</Modulus><Exponent>AQAB</Exponent></RSAPublicKey>";
    private String shared_aes_encryption_key;
    private ImageView home_switch_image;
    private Switch home_switch_user;
    private final WebSocketConnection mConnection = new WebSocketConnection();
    private String BULB_STATE = null;
    private boolean bulb_state = false;
    private String FAN_STATE = null;
    private boolean switch_state = false;
    private String SWITCH_STATE = null;
    private ImageButton home_audio;
    private boolean temp = true;

    CkRsa rsaEncryptor = new CkRsa();
    boolean usePrivateKey = false;
    private boolean tempUser = true;

    //Speech to text
    private TextView txtSpeechInput;
    private ImageButton home_speech;
    private final int REQ_CODE_SPEECH_INPUT = 100;

    // Text to speech
    private TextToSpeech tts;

    boolean tempFanSpeedSelector = false;

    private void start() {

        final String wsuri = "ws://10.124.195.9:80";

        try {
            mConnection.connect(wsuri, new WebSocketConnectionHandler() {

                @Override
                public void onOpen() {
                    track = 1;
                    rsaEncryptor.put_EncodingMode("hex");
                    boolean success = rsaEncryptor.ImportPublicKey(publicKey);

/** Duing Login/Signup */
                    shared_aes_encryption_key = shared_key_generator();
                    mConnection.sendTextMessage(rsaEncryptor.encryptStringENC("LOGI-" + username_init + "-" + password_init + "-" + shared_aes_encryption_key, usePrivateKey));
                    mConnection.sendTextMessage(rsaEncryptor.encryptStringENC("ENQ-" + username_init + "-" + shared_aes_encryption_key, usePrivateKey));
                }

                @Override
                public void onTextMessage(String payload) {
                    String decrypted_data = decryption(payload, shared_aes_encryption_key);
                    if (decrypted_data != null) {
                        data_parsed = decrypted_data.split("-");
                        int size = data_parsed.length;

                        if (data_parsed[0].equals(String.valueOf("VERIFY"))) {
                            if (data_parsed[1].equals(String.valueOf("True"))) {
                                if ((size > 2) && data_parsed[2].equals(String.valueOf("STATUS"))) {
                                    if (data_parsed[3].equals(String.valueOf("TL_ON")) || data_parsed[3].equals(String.valueOf("TL_OFF"))) {
                                        if (tempUser == true) {
                                            if (data_parsed[3].equals(String.valueOf("TL_ON"))) {
                                                home_bulb_image.setImageResource(R.drawable.bulb_on);
                                                BULB_STATE = "TL_ON";
                                            } else if (data_parsed[3].equals(String.valueOf("TL_OFF"))) {
                                                home_bulb_image.setImageResource(R.drawable.bulb_off);
                                                BULB_STATE = "TL_OFF";
                                            }
                                        } else {
                                            if (data_parsed[3].equals(String.valueOf("TL_ON"))) {
                                                home_switch_image.setImageResource(R.drawable.ic_switch_on);
                                                SWITCH_STATE = "TL_ON";
                                            } else if (data_parsed[3].equals(String.valueOf("TL_OFF"))) {
                                                home_switch_image.setImageResource(R.drawable.ic_switch_off);
                                                SWITCH_STATE = "TL_OFF";
                                            }
                                        }
                                    }

                                    if (data_parsed[4].equals(String.valueOf("FAN_OFF"))
                                            || data_parsed[4].equals(String.valueOf("FAN_ON_1"))
                                            || data_parsed[4].equals(String.valueOf("FAN_ON_2"))
                                            || data_parsed[4].equals(String.valueOf("FAN_ON_3"))
                                            || data_parsed[4].equals(String.valueOf("FAN_ON_4"))
                                            || data_parsed[4].equals(String.valueOf("FAN_ON_5"))) {

                                        if (data_parsed[4].equals("FAN_OFF")) {
                                            home_fan_image.setImageResource(R.drawable.ic_home_fan);
                                            home_fan_speed.setText("OFF");
                                            home_fan_seekbar.setProgress(0);
                                            FAN_STATE = "FAN_OFF";
                                        } else if (data_parsed[4].equals("FAN_ON_1")) {
                                            home_fan_image.setImageResource(R.drawable.ic_home_fan1);
                                            home_fan_speed.setText("20%");
                                            home_fan_seekbar.setProgress(1);
                                            FAN_STATE = "FAN_ON_1";
                                        } else if (data_parsed[4].equals("FAN_ON_2")) {
                                            home_fan_image.setImageResource(R.drawable.ic_home_fan2);
                                            home_fan_speed.setText("40%");
                                            home_fan_seekbar.setProgress(2);
                                            FAN_STATE = "FAN_ON_2";
                                        } else if (data_parsed[4].equals("FAN_ON_3")) {
                                            home_fan_image.setImageResource(R.drawable.ic_home_fan3);
                                            home_fan_speed.setText("60%");
                                            home_fan_seekbar.setProgress(3);
                                            FAN_STATE = "FAN_ON_3";
                                        } else if (data_parsed[4].equals("FAN_ON_4")) {
                                            home_fan_image.setImageResource(R.drawable.ic_home_fan4);
                                            home_fan_speed.setText("80%");
                                            home_fan_seekbar.setProgress(4);
                                            FAN_STATE = "FAN_ON_4";
                                        } else if (data_parsed[4].equals("FAN_ON_5")) {
                                            home_fan_image.setImageResource(R.drawable.ic_home_fan5);
                                            home_fan_speed.setText("100%");
                                            home_fan_seekbar.setProgress(5);
                                            FAN_STATE = "FAN_ON_5";
                                        }
                                    }
                                } else if ((size > 2) && data_parsed[2].equals("BLEMAC")) {
//                                    SharedPreferences ble_mac_add = getSharedPreferences("BLEMACAdd", Context.MODE_PRIVATE);
//                                    SharedPreferences.Editor editor = ble_mac_add.edit();
//                                    editor.putString("blemacadd", data_parsed[3]);
//                                    editor.commit();
//                                    mBluetoothLeService.disconnect();
//                                    mBluetoothLeService.connect(data_parsed[3]);
//
//                                    Toast.makeText(getApplicationContext(), "You are connected to your room via the Web!!!", Toast.LENGTH_SHORT).show();
//                                    count = 1;
                                    mConnection.sendTextMessage(encryption("sessionRequest-" + username_init, shared_aes_encryption_key));
                                    Toast.makeText(MainActivity.this, "BLEMAC ADD received!!", Toast.LENGTH_SHORT).show();
                                }

                                status_request = 0;
                            } else if (new String("False").equals(data_parsed[1])) {
                                UnAuthenticateUser();
                            } else {
                                /**To-Do */
                            }
                        } else if (data_parsed[0].equals(String.valueOf("NOTIFY"))) {
                            Toast.makeText(getApplicationContext(), data_parsed[1], Toast.LENGTH_SHORT).show();
                        } else if (data_parsed[0].equals("session")) {
                            transfer_session = data_parsed[1];

                            if (tempUser == true) {
                                mConnection.sendTextMessage(encryption("STATUS-" + username_init + "-" + transfer_session, shared_aes_encryption_key));
                            } else {
                                mConnection.sendTextMessage(encryption("STATUS-" + username_init + "-" + transfer_session, shared_aes_encryption_key));
                            }
                            status_request = 1;
                        } else {

                        }
                    }
                }

                @Override
                public void onClose(int code, String reason) {
                    /** To-Do */
                    track = 0;
                    Toast.makeText(MainActivity.this, "WebSocket Closed", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (WebSocketException e) {
            /** To-Do */
        }
    }

    private void UnAuthenticateUser() {
        finish();
        Intent i = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(i);
        Toast.makeText(MainActivity.this, "Sorry, The login details are incorrect!!! Pls try again...", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bundle user_data = getIntent().getExtras();
        if (user_data == null) {
            UnAuthenticateUser();
        } else {
            username_init = user_data.getString("username");
            password_init = user_data.getString("password");
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar);

        CkRsa rsa = new CkRsa();

        boolean success = rsa.UnlockComponent("Anything for 30-day trial");
        if (success != true) {
            Log.i("Chilkat", "RSA component unlock failed");
            return;
        }

        start();
        home_audio = (ImageButton) findViewById(R.id.home_audio);
        home_fan_image = (ImageView) findViewById(R.id.home_fan_image);
        home_bulb_image = (ImageView) findViewById(R.id.home_bulb_image);
        home_fan_image = (ImageView) findViewById(R.id.home_fan_image);
        home_fan_seekbar = (SeekBar) findViewById(R.id.home_fan_seekbar);
        home_fan_speed = (TextView) findViewById(R.id.home_fan_speed);
        home_switch_image = (ImageView) findViewById(R.id.home_switch_image);
        home_settings = (ImageView) findViewById(R.id.home_settings);
        home_help = (ImageView) findViewById(R.id.home_help);


        home_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Feature will be available very soon!!", Toast.LENGTH_SHORT).show();
            }
        });
        home_help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Feature will be available very soon!!", Toast.LENGTH_SHORT).show();
            }
        });

        BULB_STATE = "TL_OFF";
        home_bulb_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (BULB_STATE.equals("TL_ON")) {
                    bulb_state = false;
                    BULB_STATE = "TL_OFF";  // Done to reverse
                } else {
                    bulb_state = true;
                    BULB_STATE = "TL_ON"; // Done to reverse
                }
                toggleBulb(bulb_state, 1);
            }
        });

        home_switch_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SWITCH_STATE.equals("TL_ON")) {
                    switch_state = false;
                    SWITCH_STATE = "TL_OFF";  // Done to reverse
                } else {
                    switch_state = true;
                    SWITCH_STATE = "TL_ON"; // Done to reverse
                }
                toggleSwitch(switch_state, 1);
            }
        });

        home_fan_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                home_fan_speed.setText(String.valueOf(progress * 20) + " %");
                if (progress == 0) {
                    changeFanSpeed("FAN_OFF", 1);
                } else if (progress == 1) {
                    changeFanSpeed("FAN_ON_1", 1);
                } else if (progress == 2) {
                    changeFanSpeed("FAN_ON_2", 1);
                } else if (progress == 3) {
                    changeFanSpeed("FAN_ON_3", 1);
                } else if (progress == 4) {
                    changeFanSpeed("FAN_ON_4", 1);
                } else if (progress == 5) {
                    changeFanSpeed("FAN_ON_5", 1);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
///////////////////////////////////////////
        //For speech rec
        home_audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });
        tts = new TextToSpeech(this, this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public String encryption(String data, String passkey) {
        AESHelper.key = passkey;
        String encryptedData = "";
        try {
            encryptedData = AESHelper.encrypt_string(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encryptedData;
    }

    public String decryption(String data, String passkey) {
        AESHelper.key = passkey;
        String decryptedData = null;
        try {
            decryptedData = AESHelper.decrypt_string(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decryptedData;
    }

    @NonNull
    public static String shared_key_generator() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = 12;
        char tempChar;
        for (int i = 0; i < randomLength; i++) {
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }

    public void toggleBulb(boolean changeTo, int receivedSend) {
        if (changeTo == true) {
            home_bulb_image.setImageResource(R.drawable.bulb_on);
            BULB_STATE = "TL_ON";
        } else if (changeTo == false) {
            home_bulb_image.setImageResource(R.drawable.bulb_off);
            BULB_STATE = "TL_OFF";
        }
        if (receivedSend == 1)
            mConnection.sendTextMessage(encryption("CTRL-" + username_init + "-" + BULB_STATE + "-" + FAN_STATE + "-" + transfer_session, shared_aes_encryption_key));
    }

    public void toggleSwitch(boolean changeTo, int receivedSend) {
        if (changeTo == true) {
            home_switch_image.setImageResource(R.drawable.ic_switch_on);
            SWITCH_STATE = "TL_ON";
        } else if (changeTo == false) {
            home_switch_image.setImageResource(R.drawable.ic_switch_off);
            SWITCH_STATE = "TL_OFF";
        }
        if (receivedSend == 1)
            mConnection.sendTextMessage(encryption("CTRL-" + "ctos" + "-" + SWITCH_STATE + "-" + FAN_STATE + "-" + transfer_session, shared_aes_encryption_key));
    }

    public void changeFanSpeed(String changeTo, int receivedSend) {
        if (changeTo == "FAN_OFF") {
            home_fan_image.setImageResource(R.drawable.ic_home_fan);
            home_fan_speed.setText("OFF");
            home_fan_seekbar.setProgress(0);
            FAN_STATE = "FAN_OFF";
        } else if (changeTo == "FAN_ON_1") {
            home_fan_image.setImageResource(R.drawable.ic_home_fan1);
            home_fan_speed.setText("20%");
            home_fan_seekbar.setProgress(1);
            FAN_STATE = "FAN_ON_1";
        } else if (changeTo == "FAN_ON_2") {
            home_fan_image.setImageResource(R.drawable.ic_home_fan2);
            home_fan_speed.setText("40%");
            home_fan_seekbar.setProgress(2);
            FAN_STATE = "FAN_ON_2";
        } else if (changeTo == "FAN_ON_3") {
            home_fan_image.setImageResource(R.drawable.ic_home_fan3);
            home_fan_speed.setText("60%");
            home_fan_seekbar.setProgress(3);
            FAN_STATE = "FAN_ON_3";
        } else if (changeTo == "FAN_ON_4") {
            home_fan_image.setImageResource(R.drawable.ic_home_fan4);
            home_fan_speed.setText("80%");
            home_fan_seekbar.setProgress(4);
            FAN_STATE = "FAN_ON_4";
        } else if (changeTo == "FAN_ON_5") {
            home_fan_image.setImageResource(R.drawable.ic_home_fan5);
            home_fan_speed.setText("100%");
            home_fan_seekbar.setProgress(5);
            FAN_STATE = "FAN_ON_5";
        }
        if (receivedSend == 1)
            mConnection.sendTextMessage(encryption("CTRL-" + username_init + "-" + BULB_STATE + "-" + FAN_STATE + "-" + transfer_session, shared_aes_encryption_key));

    }

    static {
        System.loadLibrary("chilkat");
    }

    /**
     * Showing google speech input dialog
     */
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Receiving speech input
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String text = result.get(0);

                    if (text.equals("switch on the fan") ||
                            text.equals("switch on the fam") ||
                            text.equals("switch on the phone") ||
                            text.equals("turn on the fan") ||
                            text.equals("turn on the fam") ||
                            text.equals("turn on the phone")) {
                        tempFanSpeedSelector = true;
                        speakOut("What Speed!");
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                promptSpeechInput();
                            }
                        }, 1000);
                    } else if (text.equals("turn off the fan") ||
                            text.equals("turn off the fam") ||
                            text.equals("turn off the phone") ||
                            text.equals("switch off the fan") ||
                            text.equals("switch off the fam") ||
                            text.equals("switch off the phone")) {
                        speakOut("OK");
                        changeFanSpeed("FAN_OFF", 1);
                    } else if (text.equals("lights on")) {
                        speakOut("Lights switched on!!");
                        bulb_state = true;
                        BULB_STATE = "TL_ON";
                        toggleBulb(bulb_state, 1);
                    } else if (text.equals("lights off")) {
                        speakOut("Lights switched off!!");
                        bulb_state = false;
                        BULB_STATE = "TL_OFF";
                        toggleBulb(bulb_state, 1);
                    } else if (tempFanSpeedSelector == true &&
                            (text.equals("1") ||
                                    text.equals("2") ||
                                    text.equals("3") ||
                                    text.equals("4") ||
                                    text.equals("5"))) {
                        speakOut("Fan Speed " + text + "Selected");
                        tempFanSpeedSelector = false;
                        String changeTo = "FAN_ON_" + text;
                        if (changeTo.equals("FAN_ON_1")) {
                            home_fan_image.setImageResource(R.drawable.ic_home_fan1);
                            home_fan_speed.setText("20%");
                            home_fan_seekbar.setProgress(1);
                            FAN_STATE = "FAN_ON_1";
                        } else if (changeTo.equals("FAN_ON_2")) {
                            home_fan_image.setImageResource(R.drawable.ic_home_fan2);
                            home_fan_speed.setText("40%");
                            home_fan_seekbar.setProgress(2);
                            FAN_STATE = "FAN_ON_2";
                        } else if (changeTo.equals("FAN_ON_3")) {
                            home_fan_image.setImageResource(R.drawable.ic_home_fan3);
                            home_fan_speed.setText("60%");
                            home_fan_seekbar.setProgress(3);
                            FAN_STATE = "FAN_ON_3";
                        } else if (changeTo.equals("FAN_ON_4")) {
                            home_fan_image.setImageResource(R.drawable.ic_home_fan4);
                            home_fan_speed.setText("80%");
                            home_fan_seekbar.setProgress(4);
                            FAN_STATE = "FAN_ON_4";
                        } else if (changeTo.equals("FAN_ON_5")) {
                            home_fan_image.setImageResource(R.drawable.ic_home_fan5);
                            home_fan_speed.setText("100%");
                            home_fan_seekbar.setProgress(5);
                            FAN_STATE = "FAN_ON_5";
                        }
                        mConnection.sendTextMessage(encryption("CTRL-" + username_init + "-" + BULB_STATE + "-" + FAN_STATE + "-" + transfer_session, shared_aes_encryption_key));

                    } else if (text.equals("turn on the switch")) {
                        speakOut("OK");
                        switch_state = true;
                        SWITCH_STATE = "TL_ON";
                        toggleSwitch(switch_state, 1);
                    } else if (text.equals("turn off the switch")) {
                        speakOut("OK");
                        switch_state = false;
                        SWITCH_STATE = "TL_OFF";
                        toggleSwitch(switch_state, 1);
                    } else {
                        speakOut("Sorry ! Command not recognised.");
                    }
                }
                break;
            }
        }
    }

    // Text to Speech
    @Override
    public void onDestroy() {
        // Don't forget to shutdown tts!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.US);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "This Language is not supported", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e("TTS", "Initialization Failed!");
        }
    }

    private void speakOut(String text) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

}
