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
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.robovision.bluetooth.BTBaseApplication;
import com.example.robovision.bluetooth.ConnectedThread;


public class firebaseController extends AppCompatActivity{
    private DatabaseReference mDatabase;
    private final static String TAG = "Remote";

    //Motion keys
    private final static String FORWARD = "1";
    private final static String REVERSE = "2";
    private final static String RIGHT   = "3";
    private final static String LEFT    = "4";
    private final static String STOP    = "0";
    private final static String ENTER   = "b";
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
        setContentView(R.layout.activity_remote_controlfire);

        mForward = (Button)findViewById(R.id.Forward);
        mReverse = (Button)findViewById(R.id.Reverse);
        mRight = (Button)findViewById(R.id.Right);
        mLeft = (Button)findViewById(R.id.Left);


        mApplication = (BTBaseApplication)getApplication(); //getting application varaibles


        mForward.setOnTouchListener(new View.OnTouchListener() {
            @Override

            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    fireForward();
                } else if(event.getAction() == MotionEvent.ACTION_UP){
                    fireStop();
                }
                return true;
            }
        });
        mReverse.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    fireReverse();
                } else if(event.getAction() == MotionEvent.ACTION_UP){
                    fireStop();
                }
                return true;
            }
        });
        mRight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    fireRight();
                } else if(event.getAction() == MotionEvent.ACTION_UP){
                    fireStop();
                }
                return true;
            }
        });
        mLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    fireLeft();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    fireStop();
                }
                return true;
            }
        });

    }

    public void fireForward() {

        String input = "1";
        mDatabase = FirebaseDatabase.getInstance().getReference();
        String key = mDatabase.child("move").push().getKey();
        mDatabase.child("move").setValue(input);

    }

    public void fireReverse() {

        String input = "2";
        mDatabase = FirebaseDatabase.getInstance().getReference();
        String key = mDatabase.child("move").push().getKey();
        mDatabase.child("move").setValue(input);

    }
    public void fireRight() {

        String input = "3";
        mDatabase = FirebaseDatabase.getInstance().getReference();
        String key = mDatabase.child("move").push().getKey();
        mDatabase.child("move").setValue(input);

    }
    public void fireLeft() {

        String input = "4";
        mDatabase = FirebaseDatabase.getInstance().getReference();
        String key = mDatabase.child("move").push().getKey();
        mDatabase.child("move").setValue(input);

    }
    public void fireStop() {

        String input = "0";
        mDatabase = FirebaseDatabase.getInstance().getReference();
        String key = mDatabase.child("move").push().getKey();
        mDatabase.child("move").setValue(input);

    }

}