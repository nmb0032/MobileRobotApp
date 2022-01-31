package com.example.robovision;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import android.view.View.OnClickListener;

public class RemoteControl extends AppCompatActivity {
    private final int HOLD_INTERVAL = 10;

    //GUI components, contains four direction buttons
    private Button mForward;
    private Button mReverse;
    private Button mRight;
    private Button mLeft;

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

        mApplication = (BTBaseApplication)getApplication(); //getting application varaibles

        checkBluetoothConnection(); //checking bluetooth connection

        mForward.setOnTouchListener(new RepeatListener(HOLD_INTERVAL, HOLD_INTERVAL, new OnClickListener() {
            @Override
            public void onClick(View v) {
                Forward();
            }
        }));
        mReverse.setOnTouchListener(new RepeatListener(HOLD_INTERVAL, HOLD_INTERVAL, new OnClickListener() {
            @Override
            public void onClick(View v) {
                Reverse();
            }
        }));
        mRight.setOnTouchListener(new RepeatListener(HOLD_INTERVAL, HOLD_INTERVAL, new OnClickListener() {
            @Override
            public void onClick(View v) {
                Right();
            }
        }));
        mLeft.setOnTouchListener(new RepeatListener(HOLD_INTERVAL, HOLD_INTERVAL, new OnClickListener() {
            @Override
            public void onClick(View v) {
                Left();
            }
        }));
    }

    private void Forward(){
        Log.d("Remote","Forward pressed");
        if(mApplication.bluetoothThread!=null){
            mApplication.bluetoothThread.write("1");
        }
    }
    private void Reverse(){
        Log.d("Remote","Reverse pressed");
        if(mApplication.bluetoothThread!=null){
            mApplication.bluetoothThread.write("2");
        }
    }
    private void Right(){
        Log.d("Remote","Right pressed");
        if(mApplication.bluetoothThread!=null){
            mApplication.bluetoothThread.write("4");
        }
    }
    private void Left(){
        Log.d("Remote","Left pressed");
        if(mApplication.bluetoothThread!=null){
            mApplication.bluetoothThread.write("3");
        }
    }

    private void checkBluetoothConnection(){
        if(mApplication.bluetoothThread==null)
        {
            Toast.makeText(getApplicationContext(), "Bluetooth Connection Interrupted",Toast.LENGTH_SHORT).show();
            //Handle this
        }
    }
}
