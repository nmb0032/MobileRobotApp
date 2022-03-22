package com.example.robovision;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.robovision.bluetooth.BTBaseApplication;

public class RemoteControl extends AppCompatActivity {
    private final static String TAG = "Remote";

    //Motion keys
    private final static String FORWARD = "1";
    private final static String REVERSE = "2";
    private final static String RIGHT   = "3";
    private final static String LEFT    = "4";
    private final static String STOP    = "0";
    private final static String ENTER   = "B";
    private final static String EXIT    = ";";

    //GUI components, contains four direction buttons
    private Button mForward;
    private Button mReverse;
    private Button mRight;
    private Button mLeft;
    private Button mDisconnect;
    //application context
    private BTBaseApplication mApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_control);

        mForward = (Button)findViewById(R.id.Forward);
        mReverse = (Button)findViewById(R.id.Reverse);
        mRight = (Button)findViewById(R.id.Right);
        mLeft = (Button)findViewById(R.id.Left);
        mDisconnect = (Button)findViewById(R.id.disconnect_btn);

        mApplication = (BTBaseApplication)getApplication(); //getting application varaibles

        checkBluetoothConnection(); //checking bluetooth connection
        Enter(); //Entering Remote Control mode

        mForward.setOnTouchListener(new View.OnTouchListener() {
            @Override

            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Forward();
                } else if(event.getAction() == MotionEvent.ACTION_UP){
                    Stop();
                }
                return true;
            }
        });
        mReverse.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Reverse();
                } else if(event.getAction() == MotionEvent.ACTION_UP){
                    Stop();
                }
                return true;
            }
        });
        mRight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Right();
                } else if(event.getAction() == MotionEvent.ACTION_UP){
                    Stop();
                }
                return true;
            }
        });
        mLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Left();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    Stop();
                }
                return true;
            }
        });
        mDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                disconnect();
            }
        });
    }


    @Override
    protected void onStop(){
        super.onStop();
        if(mApplication.bluetoothThread!=null){
            mApplication.bluetoothThread.write(EXIT);
        }
        Log.d(TAG, "Exiting RC mode");
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

    private void disconnect(){
        if(mApplication.bluetoothThread!=null){
            mApplication.bluetoothThread.cancel();
            mApplication.bluetoothThread = null;
            Toast.makeText(getApplicationContext(), "Disconnected from Bluetooth Device", Toast.LENGTH_SHORT).show();
        }
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

    }

    private void checkBluetoothConnection(){
        if(mApplication.bluetoothThread==null)
        {
            Log.e("Bluetooth", "Bluetooth thread not connected!");
            Toast.makeText(getApplicationContext(), "Bluetooth Connection Interrupted",Toast.LENGTH_SHORT).show();
            //Handle this
        }
    }

}
