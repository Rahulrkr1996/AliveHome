package com.example.rahulkumar.alivehome;

/**
 * Created by Rahul Kumar on 3/1/2017.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chilkatsoft.CkRsa;

import java.util.Random;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketConnectionHandler;
import de.tavendo.autobahn.WebSocketException;


public class AlarmReceiver extends BroadcastReceiver {
    private Context cont;


    private static final String TAG = "AlarmReceiver";
    private String username_init = null, password_init = null;
    private String[] data_parsed = null;
    public String transfer_session = "";
    public String publicKey = "<RSAPublicKey><Modulus>pManIJm8ZFVpV4w/hGkr+11gHCfou+AvpbBGMFvcYEyLC78Y2geM88v/J1uxXov6vSpZ0DFKgZzlMYgJf8f8/4HuQukZQtnC6mycqdThPxGQu8+USWcNUCkd0ilx7wlO58L/Hy2QqGxaso4HGvarIwGshfIuJDGUQ4OONavFLSk=</Modulus><Exponent>AQAB</Exponent></RSAPublicKey>";
    private String shared_aes_encryption_key;
    private final WebSocketConnection mConnection = new WebSocketConnection();
    private String BULB_STATE = null;
    private String FAN_STATE = null;

    CkRsa rsaEncryptor = new CkRsa();
    boolean usePrivateKey = false;

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
                                            BULB_STATE = "TL_ON";
                                        } else if (data_parsed[3].equals(String.valueOf("TL_OFF"))) {
                                            BULB_STATE = "TL_OFF";
                                        }
                                    }

                                    if (data_parsed[4].equals(String.valueOf("FAN_OFF"))
                                            || data_parsed[4].equals(String.valueOf("FAN_ON_1"))
                                            || data_parsed[4].equals(String.valueOf("FAN_ON_2"))
                                            || data_parsed[4].equals(String.valueOf("FAN_ON_3"))
                                            || data_parsed[4].equals(String.valueOf("FAN_ON_4"))
                                            || data_parsed[4].equals(String.valueOf("FAN_ON_5"))) {

                                        if (data_parsed[4].equals("FAN_OFF") ||
                                                data_parsed[4].equals("FAN_ON_1") ||
                                                data_parsed[4].equals("FAN_ON_2") ||
                                                data_parsed[4].equals("FAN_ON_3") ||
                                                data_parsed[4].equals("FAN_ON_4") ||
                                                data_parsed[4].equals("FAN_ON_5")) {
                                            FAN_STATE = data_parsed[4];
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
                                   // Toast.makeText(cont, "BLEMAC ADD received!!", Toast.LENGTH_SHORT).show();
                                }

                            } else if (new String("False").equals(data_parsed[1])) {
                                Toast.makeText(cont, "Could not connect to server!!", Toast.LENGTH_SHORT).show();
                            } else {
                                /**To-Do */
                            }
                        } else if (data_parsed[0].equals(String.valueOf("NOTIFY"))) {
                            Toast.makeText(cont, data_parsed[1], Toast.LENGTH_SHORT).show();
                        } else if (data_parsed[0].equals("session")) {
                            transfer_session = data_parsed[1];

                            mConnection.sendTextMessage(encryption("STATUS-" + username_init + "-" + transfer_session, shared_aes_encryption_key));
                        } else {

                        }
                    }
                }

                @Override
                public void onClose(int code, String reason) {
                    /** To-Do */
                    Toast.makeText(cont, "WebSocket Closed", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (WebSocketException e) {
            /** To-Do */
        }
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

    public void changeApplianceState() {
        mConnection.sendTextMessage(encryption("CTRL-" + username_init + "-" + BULB_STATE + "-" + FAN_STATE + "-" + transfer_session, shared_aes_encryption_key));
    }

    static {
        System.loadLibrary("chilkat");
    }

    MediaPlayer mp;

    @Override
    public void onReceive(Context context, Intent intent) {
        BULB_STATE = intent.getStringExtra("BULB_STATE");
        FAN_STATE = intent.getStringExtra("FAN_STATE");
        cont = context;

        mp = MediaPlayer.create(context, R.raw.alarm_beep);
        mp.start();

        changeApplianceState();
        Toast.makeText(context, "Alarm Triggered...", Toast.LENGTH_LONG).show();
    }
}
