package com.example.robovision;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;

import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.robovision.bluetooth.BTBaseApplication;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.video.VideoCanvas;

public class firebasePull extends AppCompatActivity {

    // Fill the App ID of your project generated on Agora Console.
    private final String appId = "7d2a1fd3e9d244aa8432403d593dd0f5";
    // Fill the channel name.
    private final String channelName = "robot";
    // Fill the temp token generated on Agora Console.
    private final String token = "007eJxTYGD78nJWZXTP3IDdf94w3TNXnfZbdBJPtcHvrEfz7+kUfV2twGCeYpRomJZinGqZYmRikphoYWJsZGJgnGJqaZySYpBmqrHYK6UhkJFhjosUAyMUgvisDEX5SfklDAwA9jcg2w==";
    // An integer that identifies the local user.
    private final int uid = 0;
    private boolean isJoined = false;

    //SurfaceView to render local video in a Container.
    private SurfaceView localSurfaceView;

    private RtcEngine agoraEngine;
    //SurfaceView to render local video in a Container.
    private DatabaseReference mDatabase;
    private TextView T1;
    private final static String TAG = "Remote";

    //Motion keys
    private final static String FORWARD = "1";
    private final static String REVERSE = "2";
    private final static String RIGHT   = "3";
    private final static String LEFT    = "4";
    private final static String STOP    = "0";
    private final static String ENTER   = "b";
    private final static String EXIT    = ";";
    private BTBaseApplication mApplication;
    private Button mDisconnect;

    private static final int PERMISSION_REQ_ID = 22;
    private static final String[] REQUESTED_PERMISSIONS =
            {
                    android.Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA
            };

    private boolean checkSelfPermission()
    {
        return ContextCompat.checkSelfPermission(this, REQUESTED_PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, REQUESTED_PERMISSIONS[1]) == PackageManager.PERMISSION_GRANTED;
    }

    public void setupVideoSDKEngine() {
        try {
            RtcEngineConfig config = new RtcEngineConfig();
            config.mContext = getBaseContext();
            config.mAppId = appId;
            config.mEventHandler = mRtcEventHandler;
            agoraEngine = RtcEngine.create(config);
            // By default, the video module is disabled, call enableVideo to enable it.
            agoraEngine.enableVideo();
        } catch (Exception e) {
            showMessage(e.toString());
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_firepull);
        T1 = (TextView) findViewById(R.id.title);
        mApplication = (BTBaseApplication) getApplication();
        mDisconnect = (Button) findViewById(R.id.user_btn);
        checkBluetoothConnection(); //checking bluetooth connection
        Enter(); //entering the bluetooth mode
        handler.post(runnable);
        mDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                disconnectPull();
            }
        });

        // If all the permissions are granted, initialize the RtcEngine object and join a channel.
        if (!checkSelfPermission()) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID);
        }
        setupVideoSDKEngine();
    }

    public void fireBasePull() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference messagesRef = mDatabase.child("move");


        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                T1.setText(value);
                if (value.contains("1")) {
                    Forward();
                }
                else if (value.contains("2")) {
                    Reverse();
                }
                else if (value.contains("3")) {
                    Right();
                }
                else if (value.contains("4")) {
                    Left();
                }
                else if (value.contains("0")) {
                    Stop();

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }

        });

    }
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            fireBasePull();
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(runnable);
    }
    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
    }
    private void Forward(){
        Log.d(TAG,"Forward Started");
        if(mApplication.bluetoothThread!=null){
            mApplication.bluetoothThread.write(FORWARD);
        }
    }
    private void Reverse(){
        Log.d(TAG,"Reverse started");
        if(mApplication.bluetoothThread!=null){
            mApplication.bluetoothThread.write(REVERSE);
        }
    }
    private void Right(){
        Log.d(TAG,"Right started");
        if(mApplication.bluetoothThread!=null){
            mApplication.bluetoothThread.write(RIGHT);
        }
    }
    private void Left(){
        Log.d(TAG,"Left started");
        if(mApplication.bluetoothThread!=null){
            mApplication.bluetoothThread.write(LEFT);
        }
    }
    private void Stop(){
        Log.d(TAG, "STOP");
        if(mApplication.bluetoothThread!=null){
            mApplication.bluetoothThread.write(STOP);
        }
    }
    private void Enter(){
        Log.d(TAG, "Entering Remote Mode");
        if(mApplication.bluetoothThread!=null){
            mApplication.bluetoothThread.write(ENTER);
        }
    }

    private void disconnectPull(){
        if(mApplication.bluetoothThread!=null){
            mApplication.bluetoothThread.cancel();
            mApplication.bluetoothThread = null;
            Toast.makeText(getApplicationContext(), "Disconnected from Bluetooth Device", Toast.LENGTH_SHORT).show();
        }
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

    }
    private void checkBluetoothConnection(){
        if(mApplication.bluetoothThread==null) {
            Log.e("Bluetooth", "Bluetooth thread not connected!");
            Toast.makeText(getApplicationContext(), "Bluetooth Connection Interrupted", Toast.LENGTH_SHORT).show();
            //Handle this
        }
    }

    public void joinChannel(View view) {
        if (checkSelfPermission()) {
            ChannelMediaOptions options = new ChannelMediaOptions();

            // For Live Streaming, set the channel profile as LIVE_BROADCASTING.
            options.channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING;

            // Set the client role as BROADCASTER.
            options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;

            // Display LocalSurfaceView.
            setupLocalVideo();
            localSurfaceView.setVisibility(View.VISIBLE);

            // Start local preview.
            agoraEngine.startPreview();

            // Join the channel with a temp token.
            // You need to specify the user ID yourself, and ensure that it is unique in the channel.
            agoraEngine.joinChannel(token, channelName, uid, options);

        } else {
            Toast.makeText(getApplicationContext(), "Permissions was not granted", Toast.LENGTH_SHORT).show();
        }
    }

    public void leaveChannel(View view) {
        if (!isJoined) {
            showMessage("Join a channel first");
        } else {
            agoraEngine.leaveChannel();
            showMessage("You left the channel");
            // Stop local video rendering.
            if (localSurfaceView != null) localSurfaceView.setVisibility(View.GONE);
            isJoined = false;
        }
    }


    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        // Listen for the remote host joining the channel to get the uid of the host.
        public void onUserJoined(int uid, int elapsed) {
            showMessage("Remote user joined " + uid);
        }

        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            isJoined = true;
            showMessage("Joined Channel " + channel);
        }

        @Override
        public void onUserOffline(int uid, int reason) {
            showMessage("Remote user offline " + uid + " " + reason);
        }
    };

    private void setupLocalVideo() {
        FrameLayout container = findViewById(R.id.local_video_container);
        // Create a SurfaceView object and add it as a child to the FrameLayout.
        localSurfaceView = new SurfaceView(getBaseContext());
        container.addView(localSurfaceView);
        // Call setupLocalVideo with a VideoCanvas having uid set to 0.
        agoraEngine.setupLocalVideo(new VideoCanvas(localSurfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0));
    }

    void showMessage(String message) {
        runOnUiThread(() ->
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        agoraEngine.stopPreview();
        agoraEngine.leaveChannel();

        // Destroy the engine in a sub-thread to avoid congestion
        new Thread(() -> {
            RtcEngine.destroy();
            agoraEngine = null;
        }).start();
    }
}

