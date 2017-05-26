package com.example.rahulkumar.alivehome;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.chilkatsoft.CkRsa;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
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
    private ImageButton home_fan_speed1, home_fan_speed2, home_fan_speed3, home_fan_speed4, home_fan_speed5;
    private String username_init = null, password_init = null;
    private String[] data_parsed = null;
    public String transfer_session = "";
    public String publicKey = "<RSAPublicKey><Modulus>pManIJm8ZFVpV4w/hGkr+11gHCfou+AvpbBGMFvcYEyLC78Y2geM88v/J1uxXov6vSpZ0DFKgZzlMYgJf8f8/4HuQukZQtnC6mycqdThPxGQu8+USWcNUCkd0ilx7wlO58L/Hy2QqGxaso4HGvarIwGshfIuJDGUQ4OONavFLSk=</Modulus><Exponent>AQAB</Exponent></RSAPublicKey>";
    private String shared_aes_encryption_key;
    private final WebSocketConnection mConnection = new WebSocketConnection();
    private String BULB_STATE = null;
    private boolean bulb_state = false;
    private String FAN_STATE = null;
    private ImageButton home_audio;
    private boolean temp = true;

    CkRsa rsaEncryptor = new CkRsa();
    boolean usePrivateKey = false;

    //Chatbot
    private final int REQ_SPEECH_CODE = 100;
    String IP_ADDR;
    int PORT;

    // Text to speech
    private TextToSpeech tts;
    private String mAnswerText;
    boolean tempFanSpeedSelector = false;
    private ProgressDialog pd;

    private void start() {

        final String wsuri = "ws://10.124.195.9:80";

        try {
            mConnection.connect(wsuri, new WebSocketConnectionHandler() {

                @Override
                public void onOpen() {
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
                                        if (data_parsed[3].equals(String.valueOf("TL_ON"))) {
                                            home_bulb_image.setImageResource(R.drawable.bulb_on);
                                            BULB_STATE = "TL_ON";
                                        } else if (data_parsed[3].equals(String.valueOf("TL_OFF"))) {
                                            home_bulb_image.setImageResource(R.drawable.bulb_off);
                                            BULB_STATE = "TL_OFF";
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
                                            changeFanSpeed(0, false);
                                            FAN_STATE = "FAN_OFF";
                                        } else if (data_parsed[4].equals("FAN_ON_1")) {
                                            home_fan_image.setImageResource(R.drawable.ic_home_fan1);
                                            changeFanSpeed(1, false);
                                            FAN_STATE = "FAN_ON_1";
                                        } else if (data_parsed[4].equals("FAN_ON_2")) {
                                            home_fan_image.setImageResource(R.drawable.ic_home_fan2);
                                            changeFanSpeed(2, false);
                                            FAN_STATE = "FAN_ON_2";
                                        } else if (data_parsed[4].equals("FAN_ON_3")) {
                                            home_fan_image.setImageResource(R.drawable.ic_home_fan3);
                                            changeFanSpeed(3, false);
                                            FAN_STATE = "FAN_ON_3";
                                        } else if (data_parsed[4].equals("FAN_ON_4")) {
                                            home_fan_image.setImageResource(R.drawable.ic_home_fan4);
                                            changeFanSpeed(4, false);
                                            FAN_STATE = "FAN_ON_4";
                                        } else if (data_parsed[4].equals("FAN_ON_5")) {
                                            home_fan_image.setImageResource(R.drawable.ic_home_fan5);
                                            changeFanSpeed(5, false);
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
                                    pd.dismiss();
                                    mConnection.sendTextMessage(encryption("sessionRequest-" + username_init, shared_aes_encryption_key));
                                    Toast.makeText(MainActivity.this, "BLEMAC ADD received!!", Toast.LENGTH_SHORT).show();
                                }

                            } else if (new String("False").equals(data_parsed[1])) {
                                UnAuthenticateUser();
                            } else {
                                /**To-Do */
                            }
                        } else if (data_parsed[0].equals(String.valueOf("NOTIFY"))) {
                            Toast.makeText(getApplicationContext(), data_parsed[1], Toast.LENGTH_SHORT).show();
                        } else if (data_parsed[0].equals("session")) {
                            transfer_session = data_parsed[1];

                            mConnection.sendTextMessage(encryption("STATUS-" + username_init + "-" + transfer_session, shared_aes_encryption_key));

                        } else {
                            Toast.makeText(getApplicationContext(), decrypted_data, Toast.LENGTH_SHORT).show();
                            pd.dismiss();
                            UnAuthenticateUser();
                        }
                    }
                }

                @Override
                public void onClose(int code, String reason) {
                    /** To-Do */
                    Toast.makeText(MainActivity.this, "WebSocket Closed", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (WebSocketException e) {
            /** To-Do */
        }
    }

    private void UnAuthenticateUser() {
        finish();
        SharedPreferences sharedPreferences = getSharedPreferences("user_Info",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
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

        SharedPreferences sharedPreferences = getSharedPreferences("user_Info", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", username_init.toString());
        editor.putString("password", password_init.toString());
        editor.apply();

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

        pd = new ProgressDialog(MainActivity.this);
        pd.setMessage("Receiving Device States!! Please Wait...");
        pd.show();
        start();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(pd.isShowing()) {
                    pd.dismiss();
                    UnAuthenticateUser();
                    Toast.makeText(MainActivity.this,
                            "Cannot receieve Device state! Please check connectivity of Android Device and Hardware!!",
                            Toast.LENGTH_LONG).show();
                }
            }
        }, 5000);
///////////////////////////////////////

        home_bulb_image = (ImageView) findViewById(R.id.home_bulb_image);

        home_fan_image = (ImageView) findViewById(R.id.home_fan_image);
        home_fan_speed1 = (ImageButton) findViewById(R.id.home_fan_speed1);
        home_fan_speed2 = (ImageButton) findViewById(R.id.home_fan_speed2);
        home_fan_speed3 = (ImageButton) findViewById(R.id.home_fan_speed3);
        home_fan_speed4 = (ImageButton) findViewById(R.id.home_fan_speed4);
        home_fan_speed5 = (ImageButton) findViewById(R.id.home_fan_speed5);

        home_settings = (ImageView) findViewById(R.id.home_settings);
        home_audio = (ImageButton) findViewById(R.id.home_audio);
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

        home_fan_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FAN_STATE == "FAN_OFF") {
                    changeFanSpeed(1, true);
                } else {
                    changeFanSpeed(0, true);
                }
            }
        });
        home_fan_speed1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFanSpeed(1, true);
            }
        });
        home_fan_speed2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFanSpeed(2, true);
            }
        });
        home_fan_speed3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFanSpeed(3, true);
            }
        });
        home_fan_speed4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFanSpeed(4, true);
            }
        });
        home_fan_speed5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFanSpeed(5, true);
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
        IP_ADDR = PreferenceManager.getDefaultSharedPreferences(this).getString("ip_addr", "127.0.0.1");
        PORT = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString("port_addr", "9999"));

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            Toast.makeText(this, "Use Home Button to exit!!", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "To be added", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_help) {
            Toast.makeText(this, "To be added", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_sign_out) {
            SharedPreferences sharedPreferences = getSharedPreferences("user_Info", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.commit();

            mConnection.sendTextMessage(encryption("LOGO-" + username_init + "-" + transfer_session, shared_aes_encryption_key));

            Toast.makeText(this, "Logged Out!!", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(this,LoginActivity.class);
            startActivity(i);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_alarm) {
            Intent i = new Intent(this, AlarmActivity.class);
            i.putExtra("username",username_init);
            i.putExtra("password",password_init);
            startActivity(i);
            // Handle the alarm action
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

    public void changeFanSpeed(int speed, boolean receivedSend) {
        if (speed == 0) {
            home_fan_image.setImageResource(R.drawable.ic_home_fan);

            home_fan_speed1.setBackgroundResource(R.color.grey);
            home_fan_speed2.setBackgroundResource(R.color.grey);
            home_fan_speed3.setBackgroundResource(R.color.grey);
            home_fan_speed4.setBackgroundResource(R.color.grey);
            home_fan_speed5.setBackgroundResource(R.color.grey);

            FAN_STATE = "FAN_OFF";
        } else if (speed == 1) {
            home_fan_image.setImageResource(R.drawable.ic_home_fan1);

            home_fan_speed1.setBackgroundResource(R.color.blue);
            home_fan_speed2.setBackgroundResource(R.color.grey);
            home_fan_speed3.setBackgroundResource(R.color.grey);
            home_fan_speed4.setBackgroundResource(R.color.grey);
            home_fan_speed5.setBackgroundResource(R.color.grey);

            FAN_STATE = "FAN_ON_1";
        } else if (speed == 2) {
            home_fan_image.setImageResource(R.drawable.ic_home_fan2);

            home_fan_speed1.setBackgroundResource(R.color.blue);
            home_fan_speed2.setBackgroundResource(R.color.blue);
            home_fan_speed3.setBackgroundResource(R.color.grey);
            home_fan_speed4.setBackgroundResource(R.color.grey);
            home_fan_speed5.setBackgroundResource(R.color.grey);

            FAN_STATE = "FAN_ON_2";
        } else if (speed == 3) {
            home_fan_image.setImageResource(R.drawable.ic_home_fan3);

            home_fan_speed1.setBackgroundResource(R.color.blue);
            home_fan_speed2.setBackgroundResource(R.color.blue);
            home_fan_speed3.setBackgroundResource(R.color.blue);
            home_fan_speed4.setBackgroundResource(R.color.grey);
            home_fan_speed5.setBackgroundResource(R.color.grey);

            FAN_STATE = "FAN_ON_3";
        } else if (speed == 4) {
            home_fan_image.setImageResource(R.drawable.ic_home_fan4);

            home_fan_speed1.setBackgroundResource(R.color.blue);
            home_fan_speed2.setBackgroundResource(R.color.blue);
            home_fan_speed3.setBackgroundResource(R.color.blue);
            home_fan_speed4.setBackgroundResource(R.color.blue);
            home_fan_speed5.setBackgroundResource(R.color.grey);

            FAN_STATE = "FAN_ON_4";
        } else if (speed == 5) {
            home_fan_image.setImageResource(R.drawable.ic_home_fan5);

            home_fan_speed1.setBackgroundResource(R.color.blue);
            home_fan_speed2.setBackgroundResource(R.color.blue);
            home_fan_speed3.setBackgroundResource(R.color.blue);
            home_fan_speed4.setBackgroundResource(R.color.blue);
            home_fan_speed5.setBackgroundResource(R.color.blue);

            FAN_STATE = "FAN_ON_5";
        }
        if (receivedSend == true)
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
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speech_prompt));

        try {
            startActivityForResult(intent, REQ_SPEECH_CODE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, getString(R.string.speech_not_supported), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQ_SPEECH_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String question = result.get(0);

            ChatbotTask chatbotTask = new ChatbotTask();
            chatbotTask.execute(question);
        }

    }

    private void speakOut() {
        String text = mAnswerText.toString();
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    public void onInit(int status) {
        if(status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.US);

            if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported.");
            } else {
                home_audio.setEnabled(true);
            }
        } else {
            Log.e("TTS", "Initialization Failed");
        }
    }

    private class ChatbotTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            try {
                Socket clientSocket = new Socket(IP_ADDR, PORT);
                DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
                BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String question = strings[0];
                Log.d(TAG, "Question: "+ question);
                outToServer.writeBytes(question + '\n');
                outToServer.flush();

                String answer = inFromServer.readLine();
                Log.d(TAG, "Answer:" + answer);
                clientSocket.close();
                return answer;
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return "Please try again !!";
        }

        @Override
        protected void onPostExecute(String answer) {
            super.onPostExecute(answer);
            mAnswerText = answer;
            speakOut();
        }

    }
}
