package com.example.robovision;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import android.view.MotionEvent;
import android.view.View;

import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.robovision.bluetooth.BTBaseApplication;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class firebasePull extends AppCompatActivity {
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
                disconnect();
            }
        });
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
        if(mApplication.bluetoothThread==null) {
            Log.e("Bluetooth", "Bluetooth thread not connected!");
            Toast.makeText(getApplicationContext(), "Bluetooth Connection Interrupted", Toast.LENGTH_SHORT).show();
            //Handle this
        }
    }
}

