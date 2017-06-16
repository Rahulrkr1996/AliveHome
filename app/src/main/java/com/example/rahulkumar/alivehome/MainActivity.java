package com.example.rahulkumar.alivehome;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketConnectionHandler;
import de.tavendo.autobahn.WebSocketException;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {//}, TextToSpeech.OnInitListener {

    private static final String TAG = "MainActivity";
    private ImageView home_help, home_settings;
    private ImageView home_bulb_image;
    private ImageView home_fan_image;
    private ImageButton home_fan_speed1, home_fan_speed2, home_fan_speed3, home_fan_speed4, home_fan_speed5;
    private String username_init = null, password_init = null;
    private String[] data_parsed = null;
    public String transfer_session = "";
    private String shared_aes_encryption_key;
    private final WebSocketConnection mConnection = new WebSocketConnection();
    private String BULB_STATE = null;
    private String FAN_STATE = null;
    private ImageButton home_audio;
    private int backPressedCount = 0;
    private ProgressDialog pd;
    private boolean webSocketConnected;
//    //Chatbot
//    private final int REQ_SPEECH_CODE = 100;
//    String IP_ADDR;
//    int PORT;
//
//    // Text to speech
//    private TextToSpeech tts;

    // BLe variables
    private boolean BLEConnected = false;
    private String mDeviceAddress;
    private BluetoothLeService mBluetoothLeService;
    private BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothGattCharacteristic characteristic;
    private int charaProp;
    // Code to manage Service lifecycle.
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };

    private void start() {

        final String wsuri = "ws://10.124.195.9:80";

        try {
            mConnection.connect(wsuri, new WebSocketConnectionHandler() {

                @Override
                public void onOpen() {
                    webSocketConnected = true;

                    shared_aes_encryption_key = shared_key_generator();
                    try {
                        mConnection.sendTextMessage(encryptCrypto("LOGI-" + username_init + "-" + password_init + "-" + shared_aes_encryption_key));
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InvalidKeySpecException e) {
                        e.printStackTrace();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (InvalidKeyException e) {
                        e.printStackTrace();
                    } catch (BadPaddingException e) {
                        e.printStackTrace();
                    } catch (IllegalBlockSizeException e) {
                        e.printStackTrace();
                    } catch (NoSuchPaddingException e) {
                        e.printStackTrace();
                    }
                    try {

                        mConnection.sendTextMessage(encryptCrypto("ENQ-" + username_init + "-" + shared_aes_encryption_key));
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InvalidKeySpecException e) {
                        e.printStackTrace();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (InvalidKeyException e) {
                        e.printStackTrace();
                    } catch (BadPaddingException e) {
                        e.printStackTrace();
                    } catch (IllegalBlockSizeException e) {
                        e.printStackTrace();
                    } catch (NoSuchPaddingException e) {
                        e.printStackTrace();
                    }
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
                                            changeFanSpeed(0, false);
                                            FAN_STATE = "FAN_OFF";
                                        } else if (data_parsed[4].equals("FAN_ON_1")) {
                                            changeFanSpeed(1, false);
                                            FAN_STATE = "FAN_ON_1";
                                        } else if (data_parsed[4].equals("FAN_ON_2")) {
                                            changeFanSpeed(2, false);
                                            FAN_STATE = "FAN_ON_2";
                                        } else if (data_parsed[4].equals("FAN_ON_3")) {
                                            changeFanSpeed(3, false);
                                            FAN_STATE = "FAN_ON_3";
                                        } else if (data_parsed[4].equals("FAN_ON_4")) {
                                            changeFanSpeed(4, false);
                                            FAN_STATE = "FAN_ON_4";
                                        } else if (data_parsed[4].equals("FAN_ON_5")) {
                                            changeFanSpeed(5, false);
                                            FAN_STATE = "FAN_ON_5";
                                        }
                                    }
                                    pd.dismiss();
                                } else if ((size > 2) && data_parsed[2].equals("BLEMAC")) {
                                    mDeviceAddress = data_parsed[3].substring(0, 17);
                                    SharedPreferences ble_mac_add = getSharedPreferences("user_Info", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = ble_mac_add.edit();
                                    editor.putString("ble_add", mDeviceAddress);
                                    editor.commit();
                                    BLEConnected = connectBluetooth(mDeviceAddress);

                                    mConnection.sendTextMessage(encryption("sessionRequest-" + username_init, shared_aes_encryption_key));
                                }

                            } else if (new String("False").equals(data_parsed[1])) {
                                pd.dismiss();
                                UnAuthenticateUser();
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
                    webSocketConnected = false;
                    Toast.makeText(MainActivity.this, "WiFi disconnected, connecting again...", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (WebSocketException e) {
            /** To-Do */
        }
    }

    private void UnAuthenticateUser() {
        finish();
        SharedPreferences sharedPreferences = getSharedPreferences("user_Info", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        Intent i = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bundle user_data = getIntent().getExtras();
        if (user_data == null) {
            UnAuthenticateUser();
            Toast.makeText(MainActivity.this, "Sorry, The login details are incorrect!!! Pls try again...", Toast.LENGTH_SHORT).show();
        } else {
            username_init = user_data.getString("username");
            password_init = user_data.getString("password");
        }
//////////////////////////////////////////////////////////////////////

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

/////////////////////////////////////////////////////////////////////
        pd = new ProgressDialog(MainActivity.this);
        pd.setMessage("Receiving Device States!! Please Wait...");
        pd.show();
        start();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (pd.isShowing()) {
                    pd.dismiss();
                    UnAuthenticateUser();
                    Toast.makeText(MainActivity.this,
                            "Cannot receieve Device state! Please check connectivity of Android Device and Hardware!!",
                            Toast.LENGTH_LONG).show();
                }
            }
        }, 10000);
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

        home_bulb_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (BLEConnected == true) {
                    if (BULB_STATE == "TL_OFF") {
                        toggleBulb(0);//BULB_STATE becomes 'TL_ON'
                        if (FAN_STATE == "FAN_OFF") {
                            sendSerialBLE("g");
                        } else if (FAN_STATE == "FAN_ON_1") {
                            sendSerialBLE("h");
                        } else if (FAN_STATE == "FAN_ON_2") {
                            sendSerialBLE("i");
                        } else if (FAN_STATE == "FAN_ON_3") {
                            sendSerialBLE("j");
                        } else if (FAN_STATE == "FAN_ON_4") {
                            sendSerialBLE("k");
                        } else if (FAN_STATE == "FAN_ON_5") {
                            sendSerialBLE("l");
                        }
                        home_bulb_image.setImageResource(R.drawable.bulb_on);

                    } else {
                        toggleBulb(0);//BULB_STATE becomes 'TL_OFF'
                        if (FAN_STATE == "FAN_OFF") {
                            sendSerialBLE("a");
                        } else if (FAN_STATE == "FAN_ON_1") {
                            sendSerialBLE("b");
                        } else if (FAN_STATE == "FAN_ON_2") {
                            sendSerialBLE("c");
                        } else if (FAN_STATE == "FAN_ON_3") {
                            sendSerialBLE("d");
                        } else if (FAN_STATE == "FAN_ON_4") {
                            sendSerialBLE("e");
                        } else if (FAN_STATE == "FAN_ON_5") {
                            sendSerialBLE("f");
                        }
                        home_bulb_image.setImageResource(R.drawable.bulb_off);
                    }
                } else {
                    boolean connected = connectBluetooth(mDeviceAddress);

                    if (connected) {
                        if (BULB_STATE == "TL_OFF") {
                            toggleBulb(0);//BULB_STATE becomes 'TL_ON'
                            home_bulb_image.setImageResource(R.drawable.bulb_on);

                            if (FAN_STATE == "FAN_OFF") {
                                sendSerialBLE("g");
                            } else if (FAN_STATE == "FAN_ON_1") {
                                sendSerialBLE("h");
                            } else if (FAN_STATE == "FAN_ON_2") {
                                sendSerialBLE("i");
                            } else if (FAN_STATE == "FAN_ON_3") {
                                sendSerialBLE("j");
                            } else if (FAN_STATE == "FAN_ON_4") {
                                sendSerialBLE("k");
                            } else if (FAN_STATE == "FAN_ON_5") {
                                sendSerialBLE("l");
                            }
                        } else {
                            toggleBulb(0);//BULB_STATE becomes 'TL_OFF'
                            home_bulb_image.setImageResource(R.drawable.bulb_off);

                            if (FAN_STATE == "FAN_OFF") {
                                sendSerialBLE("a");
                            } else if (FAN_STATE == "FAN_ON_1") {
                                sendSerialBLE("b");
                            } else if (FAN_STATE == "FAN_ON_2") {
                                sendSerialBLE("c");
                            } else if (FAN_STATE == "FAN_ON_3") {
                                sendSerialBLE("d");
                            } else if (FAN_STATE == "FAN_ON_4") {
                                sendSerialBLE("e");
                            } else if (FAN_STATE == "FAN_ON_5") {
                                sendSerialBLE("f");
                            }
                        }
                    } else {
                        if (webSocketConnected) {
                            toggleBulb(1);
                        } else {
                            try {
                                start();
                            } catch (Exception e) {
                                Log.d(TAG, "WebSocket Closed!!!");
                            }
                        }
                    }
                }
            }
        });

        home_fan_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FAN_STATE == "FAN_OFF") {
                    if (BLEConnected == true) {
                        changeFanSpeed(1, false); //FAN SPEED 1 reflected
                        if (BULB_STATE == "TL_OFF") {
                            sendSerialBLE("b");
                        } else {
                            sendSerialBLE("h");
                        }
                    } else {
                        boolean connected = mBluetoothLeService.connect(mDeviceAddress);
                        if (connected) {
                            changeFanSpeed(1, false); //FAN SPEED 1 reflected
                            if (BULB_STATE == "TL_OFF") {
                                sendSerialBLE("b");
                            } else {
                                sendSerialBLE("h");
                            }
                        } else {
                            if (webSocketConnected) {
                                changeFanSpeed(1, true);
                            } else {
                                try {
                                    start();
                                } catch (Exception e) {
                                    Log.d(TAG, "WebSocket Closed!!!");
                                }
                            }
                        }
                    }
                } else {
                    if (BLEConnected == true) {
                        changeFanSpeed(0, false); //FAN SPEED 0 reflected
                        if (BULB_STATE == "TL_OFF") {
                            sendSerialBLE("a");
                        } else {
                            sendSerialBLE("g");
                        }

                    } else {
                        boolean connected = mBluetoothLeService.connect(mDeviceAddress);
                        if (connected) {
                            changeFanSpeed(0, false); //FAN SPEED 0 reflected
                            if (BULB_STATE == "TL_OFF") {
                                sendSerialBLE("a");
                            } else {
                                sendSerialBLE("g");
                            }
                        } else {
                            if (webSocketConnected) {
                                changeFanSpeed(0, true);
                            } else {
                                try {
                                    start();
                                } catch (Exception e) {
                                    Log.d(TAG, "WebSocket Closed!!!");
                                }
                            }
                        }
                    }
                }
            }
        });
        home_fan_speed1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (BLEConnected == true) {
                    changeFanSpeed(1, false); //FAN SPEED 1 reflected
                    if (BULB_STATE == "TL_OFF") {
                        sendSerialBLE("b");
                    } else {
                        sendSerialBLE("h");
                    }
                } else {
                    boolean connected = mBluetoothLeService.connect(mDeviceAddress);
                    if (connected) {
                        changeFanSpeed(1, false); //FAN SPEED 1 reflected
                        if (BULB_STATE == "TL_OFF") {
                            sendSerialBLE("b");
                        } else {
                            sendSerialBLE("h");
                        }
                    } else {
                        if (webSocketConnected) {
                            changeFanSpeed(1, true);
                        } else {
                            try {
                                start();
                            } catch (Exception e) {
                                Log.d(TAG, "WebSocket Closed!!!");
                            }
                        }
                    }
                }
            }
        });
        home_fan_speed2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (BLEConnected == true) {
                    changeFanSpeed(2, false); //FAN SPEED 2 reflected
                    if (BULB_STATE == "TL_OFF") {
                        sendSerialBLE("c");
                    } else {
                        sendSerialBLE("i");
                    }
                } else {
                    boolean connected = mBluetoothLeService.connect(mDeviceAddress);
                    if (connected) {
                        changeFanSpeed(2, false); //FAN SPEED 2 reflected
                        if (BULB_STATE == "TL_OFF") {
                            sendSerialBLE("c");
                        } else {
                            sendSerialBLE("i");
                        }
                    } else {
                        if (webSocketConnected) {
                            changeFanSpeed(2, true);
                        } else {
                            try {
                                start();
                            } catch (Exception e) {
                                Log.d(TAG, "WebSocket Closed!!!");
                            }
                        }
                    }
                }
            }
        });
        home_fan_speed3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (BLEConnected == true) {
                    changeFanSpeed(3, false); //FAN SPEED 3 reflected
                    if (BULB_STATE == "TL_OFF") {
                        sendSerialBLE("d");
                    } else {
                        sendSerialBLE("j");
                    }
                } else {
                    boolean connected = mBluetoothLeService.connect(mDeviceAddress);
                    if (connected) {
                        changeFanSpeed(3, false); //FAN SPEED 3 reflected
                        if (BULB_STATE == "TL_OFF") {
                            sendSerialBLE("d");
                        } else {
                            sendSerialBLE("j");
                        }
                    } else {
                        if (webSocketConnected) {
                            changeFanSpeed(3, true);
                        } else {
                            try {
                                start();
                            } catch (Exception e) {
                                Log.d(TAG, "WebSocket Closed!!!");
                            }
                        }
                    }
                }
            }
        });
        home_fan_speed4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (BLEConnected == true) {
                    changeFanSpeed(4, false); //FAN SPEED 4 reflected
                    if (BULB_STATE == "TL_OFF") {
                        sendSerialBLE("e");
                    } else {
                        sendSerialBLE("k");
                    }
                } else {
                    boolean connected = mBluetoothLeService.connect(mDeviceAddress);
                    if (connected) {
                        changeFanSpeed(4, false); //FAN SPEED 4 reflected
                        if (BULB_STATE == "TL_OFF") {
                            sendSerialBLE("e");
                        } else {
                            sendSerialBLE("k");
                        }
                    } else {
                        if (webSocketConnected) {
                            changeFanSpeed(4, true);
                        } else {
                            try {
                                start();
                            } catch (Exception e) {
                                Log.d(TAG, "WebSocket Closed!!!");
                            }
                        }
                    }
                }
            }
        });

        home_fan_speed5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (BLEConnected == true) {
                    changeFanSpeed(5, false); //FAN SPEED 5 reflected
                    if (BULB_STATE == "TL_OFF") {
                        sendSerialBLE("f");
                    } else {
                        sendSerialBLE("l");
                    }
                } else {
                    boolean connected = mBluetoothLeService.connect(mDeviceAddress);
                    if (connected) {
                        changeFanSpeed(5, false); //FAN SPEED 5 reflected
                        if (BULB_STATE == "TL_OFF") {
                            sendSerialBLE("f");
                        } else {
                            sendSerialBLE("l");
                        }
                    } else {
                        if (webSocketConnected) {
                            changeFanSpeed(5, true);
                        } else {
                            try {
                                start();
                            } catch (Exception e) {
                                Log.d(TAG, "WebSocket Closed!!!");
                            }
                        }
                    }
                }
            }
        });

///////////////////////////////////////////
        //For speech rec
        home_audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "To be added!!", Toast.LENGTH_SHORT).show();
                // promptSpeechInput();
            }
        });

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (backPressedCount % 5 == 0) {
                Toast.makeText(this, "Use Home Button to exit!!", Toast.LENGTH_SHORT).show();
            }
            backPressedCount++;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (BLEConnected == false) {
            BLEConnected = connectBluetooth(mDeviceAddress);
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
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (BLEConnected == true) {
            unbindService(mServiceConnection);
            mBluetoothLeService = null;
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_alarm) {
            Intent i = new Intent(this, AlarmActivity.class);
            i.putExtra("username", username_init);
            i.putExtra("password", password_init);
            startActivity(i);
            // Handle the alarm action
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public String encryptCrypto(String message) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException {
        InputStream is = getResources().openRawResource(R.raw.public_key);
        byte[] keyBytes = new byte[is.available()];
        is.read(keyBytes);
        is.close();
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PublicKey key = kf.generatePublic(spec);
        String s = message;
        Cipher c = Cipher.getInstance("RSA/ECB/OAEPWithSHA-512AndMGF1Padding");
        c.init(Cipher.ENCRYPT_MODE, key);
        byte[] bytes = c.doFinal(s.getBytes());
        String encrypted = Base64.encodeToString(bytes, Base64.DEFAULT);
        return encrypted;
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

    public void toggleBulb(int receivedSend) {
        String BULB_STATE_TEMP = "";
        if (BULB_STATE == "TL_OFF") {
            BULB_STATE_TEMP = "TL_ON";
            BULB_STATE = "TL_ON";
        } else {
            BULB_STATE_TEMP = "TL_OFF";
            BULB_STATE = "TL_OFF";
        }
        if (receivedSend == 1)
            mConnection.sendTextMessage(encryption("CTRL-" + username_init + "-" + BULB_STATE_TEMP + "-" + FAN_STATE + "-" + transfer_session, shared_aes_encryption_key));
    }

    public void changeFanSpeed(int speed, boolean receivedSend) {
        String FAN_STATE_TEMP = "";
        if (speed == 0) {
            FAN_STATE_TEMP = "FAN_OFF";
        } else if (speed == 1) {
            FAN_STATE_TEMP = "FAN_ON_1";
        } else if (speed == 2) {
            FAN_STATE_TEMP = "FAN_ON_2";
        } else if (speed == 3) {
            FAN_STATE_TEMP = "FAN_ON_3";
        } else if (speed == 4) {
            FAN_STATE_TEMP = "FAN_ON_4";
        } else if (speed == 5) {
            FAN_STATE_TEMP = "FAN_ON_5";
        }

        if (receivedSend == true) {
            mConnection.sendTextMessage(encryption("CTRL-" + username_init + "-" + BULB_STATE + "-" + FAN_STATE_TEMP + "-" + transfer_session, shared_aes_encryption_key));
        } else if (speed == 0) {
            home_fan_image.setImageResource(R.drawable.ic_home_fan);

            home_fan_speed1.setBackgroundResource(R.color.fan_low);
            home_fan_speed2.setBackgroundResource(R.color.fan_low);
            home_fan_speed3.setBackgroundResource(R.color.fan_low);
            home_fan_speed4.setBackgroundResource(R.color.fan_low);
            home_fan_speed5.setBackgroundResource(R.color.fan_low);

            FAN_STATE = "FAN_OFF";
        } else if (speed == 1) {
            home_fan_image.setImageResource(R.drawable.ic_home_fan1);

            home_fan_speed1.setBackgroundResource(R.color.fan_high1);
            home_fan_speed2.setBackgroundResource(R.color.fan_low);
            home_fan_speed3.setBackgroundResource(R.color.fan_low);
            home_fan_speed4.setBackgroundResource(R.color.fan_low);
            home_fan_speed5.setBackgroundResource(R.color.fan_low);

            FAN_STATE = "FAN_ON_1";
        } else if (speed == 2) {
            home_fan_image.setImageResource(R.drawable.ic_home_fan2);

            home_fan_speed1.setBackgroundResource(R.color.fan_high1);
            home_fan_speed2.setBackgroundResource(R.color.fan_high2);
            home_fan_speed3.setBackgroundResource(R.color.fan_low);
            home_fan_speed4.setBackgroundResource(R.color.fan_low);
            home_fan_speed5.setBackgroundResource(R.color.fan_low);

            FAN_STATE = "FAN_ON_2";
        } else if (speed == 3) {
            home_fan_image.setImageResource(R.drawable.ic_home_fan3);

            home_fan_speed1.setBackgroundResource(R.color.fan_high1);
            home_fan_speed2.setBackgroundResource(R.color.fan_high2);
            home_fan_speed3.setBackgroundResource(R.color.fan_high3);
            home_fan_speed4.setBackgroundResource(R.color.fan_low);
            home_fan_speed5.setBackgroundResource(R.color.fan_low);

            FAN_STATE = "FAN_ON_3";
        } else if (speed == 4) {
            home_fan_image.setImageResource(R.drawable.ic_home_fan4);

            home_fan_speed1.setBackgroundResource(R.color.fan_high1);
            home_fan_speed2.setBackgroundResource(R.color.fan_high2);
            home_fan_speed3.setBackgroundResource(R.color.fan_high3);
            home_fan_speed4.setBackgroundResource(R.color.fan_high4);
            home_fan_speed5.setBackgroundResource(R.color.fan_low);

            FAN_STATE = "FAN_ON_4";
        } else if (speed == 5) {
            home_fan_image.setImageResource(R.drawable.ic_home_fan5);

            home_fan_speed1.setBackgroundResource(R.color.fan_high1);
            home_fan_speed2.setBackgroundResource(R.color.fan_high2);
            home_fan_speed3.setBackgroundResource(R.color.fan_high3);
            home_fan_speed4.setBackgroundResource(R.color.fan_high4);
            home_fan_speed5.setBackgroundResource(R.color.fan_high5);

            FAN_STATE = "FAN_ON_5";
        }

    }

    public boolean connectBluetooth(final String DeviceAddress) {
        mServiceConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName componentName, IBinder service) {
                mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
                if (!mBluetoothLeService.initialize()) {
                    Log.e(TAG, "Unable to initialize Bluetooth");
                    finish();
                }
                // Automatically connects to the device upon successful start-up initialization.
                mBluetoothLeService.connect(DeviceAddress);
                BLEConnected = true;
                Toast.makeText(MainActivity.this, "Bluetooth connected!!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                mBluetoothLeService = null;
                BLEConnected = false;
            }
        };

        mGattUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                    BLEConnected = true;
                } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                    BLEConnected = false;
                } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                    // Show all the supported services and characteristics on the user interface.
                    //displayGattServices(mBluetoothLeService.getSupportedGattServices());
                    characteristic = mBluetoothLeService.getSupportedGattServices().get(2).getCharacteristics().get(0);
                    charaProp = mBluetoothLeService.getSupportedGattServices().get(2).getCharacteristics().get(0).getProperties();
                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                        mBluetoothLeService.readCharacteristic(characteristic);
                    }
                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                        mBluetoothLeService.setCharacteristicNotification(characteristic, true);
                    }
                } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                    final byte[] rxBytes = characteristic.getValue();
                    String btData = new String(rxBytes);
                    parseBTData(btData);
                }
            }
        };
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "Bluetooth not supported. Alive sensing won't work!!", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "BLe not supported. Alive sensing won't work!!", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            if (BLEConnected == false)
                return false;
        }
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(DeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
            return result;
        }
        return false;
    }

    private void parseBTData(String load) {
        char payload = load.charAt(0);
        if (payload=='N') {
            BULB_STATE = "TL_OFF";
            home_bulb_image.setImageResource(R.drawable.bulb_off);
            changeFanSpeed(0, false);
        } else if (payload== 'O') {
            BULB_STATE = "TL_OFF";
            home_bulb_image.setImageResource(R.drawable.bulb_off);
            changeFanSpeed(1, false);
        } else if (payload== 'P')  {
            BULB_STATE = "TL_OFF";
            home_bulb_image.setImageResource(R.drawable.bulb_off);
            changeFanSpeed(2, false);
        } else if  (payload== 'Q')  {
            BULB_STATE = "TL_OFF";
            home_bulb_image.setImageResource(R.drawable.bulb_off);
            changeFanSpeed(3, false);
        } else if  (payload== 'R')  {
            BULB_STATE = "TL_OFF";
            home_bulb_image.setImageResource(R.drawable.bulb_off);
            changeFanSpeed(4, false);
        } else if  (payload== 'S')  {
            BULB_STATE = "TL_OFF";
            home_bulb_image.setImageResource(R.drawable.bulb_off);
            changeFanSpeed(5, false);
        } else if  (payload== 'T')  {
            BULB_STATE = "TL_ON";
            home_bulb_image.setImageResource(R.drawable.bulb_on);
            FAN_STATE = "FAN_OFF";
            changeFanSpeed(0, false);
        } else if  (payload== 'U')  {
            BULB_STATE = "TL_ON";
            home_bulb_image.setImageResource(R.drawable.bulb_on);
            changeFanSpeed(1, false);
        } else if  (payload== 'V')  {
            BULB_STATE = "TL_ON";
            home_bulb_image.setImageResource(R.drawable.bulb_on);
            changeFanSpeed(2, false);
        } else if (payload=='W') {
            BULB_STATE = "TL_ON";
            home_bulb_image.setImageResource(R.drawable.bulb_on);
            changeFanSpeed(3, false);
        } else if (payload == 'X') {
            BULB_STATE = "TL_ON";
            home_bulb_image.setImageResource(R.drawable.bulb_on);
            changeFanSpeed(4, false);
        } else if (payload=='Y') {
            BULB_STATE = "TL_ON";
            home_bulb_image.setImageResource(R.drawable.bulb_on);
            changeFanSpeed(5, false);
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
// User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(this, "Bluetooth turned off. Alive sensing won't be avaliable...", Toast.LENGTH_SHORT).show();
            BLEConnected = false;
            return;
        }
    }

    private void sendSerialBLE(String message) {
        Log.d(TAG, "Sending: " + message);
        final byte[] tx = message.getBytes();
        if (BLEConnected == true) {
            characteristic.setValue(tx);
            mBluetoothLeService.writeCharacteristic(characteristic);
        } // if
    }

//        else if (requestCode == REQ_SPEECH_CODE && resultCode == RESULT_OK && data != null) {
//            ArrayList<String> result = data
//                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
//
//            speakOut(result.get(0));
//        }


    /**
     * Showing google speech input dialog
     */
//    private void promptSpeechInput() {
//        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
//        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
//        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
//        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speech_prompt));
//
//        try {
//            startActivityForResult(intent, REQ_SPEECH_CODE);
//        } catch (ActivityNotFoundException e) {
//            Toast.makeText(this, getString(R.string.speech_not_supported), Toast.LENGTH_SHORT).show();
//        }
//    }
//    private void speakOut(String data) {
//        tts.speak(data, TextToSpeech.QUEUE_FLUSH, null);
//    }
//
//    @Override
//    public void onInit(int status) {
//        if (status == TextToSpeech.SUCCESS) {
//            int result = tts.setLanguage(Locale.US);
//
//            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
//                Log.e("TTS", "This Language is not supported.");
//            } else {
//                home_audio.setEnabled(true);
//            }
//        } else {
//            Log.e("TTS", "Initialization Failed");
//        }
//    }
}

