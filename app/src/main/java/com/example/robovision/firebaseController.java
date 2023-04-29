package com.example.robovision;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.robovision.bluetooth.BTBaseApplication;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.video.VideoCanvas;

public class firebaseController extends AppCompatActivity {
    private DatabaseReference mDatabase;

    private Button mForward;
    private Button mReverse;
    private Button mRight;
    private Button mLeft;
    private Button mJoinBtn;
    private Button mLeaveBtn;

    private BTBaseApplication mApplication;
    // Fill the App ID of your project generated on Agora Console.
    private final String appId = "7d2a1fd3e9d244aa8432403d593dd0f5";
    // Fill the channel name.
    private final String channelName = "robot";
    // Fill the temp token generated on Agora Console.
    private final String token = "007eJxTYAipMp0xt6w7jPd++8GEmQVCvC4/RGI45h5bHX4g/e40OSUFBvMUo0TDtBTjVMsUIxOTxEQLE2MjEwPjFFNL45QUgzRTLkGflIZARoYs2xsMjFAI4rMyFOUn5ZcwMAAA1hEdVg==";
    // An integer that identifies the local user.
    private final int uid = 0;
    private RtcEngine agoraEngine;
    private boolean isJoined = false;
    //SurfaceView to render Remote video in a Container.
    private SurfaceView remoteSurfaceView;

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

    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        // Listen for the remote host joining the channel to get the uid of the host.
        public void onUserJoined(int uid, int elapsed) {
            showMessage("Remote user joined " + uid);
            // Set the remote video view
            runOnUiThread(() -> setupRemoteVideo(uid));
        }

        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            isJoined = true;
            showMessage("Joined Channel " + channel);
        }

        @Override
        public void onUserOffline(int uid, int reason) {
            showMessage("Remote user offline " + uid + " " + reason);
            runOnUiThread(() -> remoteSurfaceView.setVisibility(View.GONE));
        }
    };

    private void setupRemoteVideo(int uid) {
        FrameLayout container = findViewById(R.id.remote_video_container);
        remoteSurfaceView = new SurfaceView(getBaseContext());
        remoteSurfaceView.setZOrderMediaOverlay(true);
        container.addView(remoteSurfaceView);
        agoraEngine.setupRemoteVideo(new VideoCanvas(remoteSurfaceView, VideoCanvas.RENDER_MODE_FIT, uid));
        // Display RemoteSurfaceView.
        remoteSurfaceView.setVisibility(View.VISIBLE);
    }

    @SuppressLint({"MissingInflatedId", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_controlfire);


        mForward = findViewById(R.id.Forward);
        mReverse = findViewById(R.id.Reverse);
        mRight = findViewById(R.id.Right);
        mLeft = findViewById(R.id.Left);


        mApplication = (BTBaseApplication) getApplication();

        mForward.setOnTouchListener(createOnTouchListener("1"));
        mReverse.setOnTouchListener(createOnTouchListener("2"));
        mRight.setOnTouchListener(createOnTouchListener("3"));
        mLeft.setOnTouchListener(createOnTouchListener("4"));


        if (!checkSelfPermission()) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID);
        }
        setupVideoSDKEngine();
    }

    public void joinChannelUser(View view) {
        if (checkSelfPermission()) {
            ChannelMediaOptions options = new ChannelMediaOptions();

            // For Live Streaming, set the channel profile as LIVE_BROADCASTING.
            options.channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING;

            // Set the client role as AUDIENCE.
            options.clientRoleType = Constants.CLIENT_ROLE_AUDIENCE;

            // Join the channel with a temp token.
            // You need to specify the user ID yourself, and ensure that it is unique in the channel.
            agoraEngine.joinChannel(token, channelName, uid, options);
            showMessage("Joined a channel");

        } else {
            Toast.makeText(getApplicationContext(), "Permissions was not granted", Toast.LENGTH_SHORT).show();
        }
    }

    public void leaveChannelUser(View view) {
        if (!isJoined) {
            showMessage("Join a channel first");
        } else {
            agoraEngine.leaveChannel();
            showMessage("You left the channel");
            // Stop remote video rendering.
            if (remoteSurfaceView != null) remoteSurfaceView.setVisibility(View.GONE);
            isJoined = false;
        }
    }

    private View.OnTouchListener createOnTouchListener(String command) {
        return (v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                fireCommand(command);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                fireCommand("0");
            }
            return true;
        };
    }

    public void fireCommand(String input) {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("move").setValue(input);
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
