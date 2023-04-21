package com.example.robovision;

import static android.content.ContentValues.TAG;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class firebaseConnection extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private Button mConnBtn;
    private TextView T1;
    private Button mNoBtn;
    private Button mConnectBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fire_connect);
        handler.post(runnable);
        T1 = (TextView) findViewById(R.id.title);
        mConnBtn = (Button) findViewById(R.id.conn_btn);
        mNoBtn = (Button) findViewById(R.id.no_btn);
        mConnectBtn = (Button) findViewById(R.id.connect_btn);
        mConnBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override

            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    handler.post(sendding);
                } else if(event.getAction() == MotionEvent.ACTION_UP){
                    handler.removeCallbacks(sendding);
                    String input = "data" + 0;
                    mDatabase = FirebaseDatabase.getInstance().getReference();
                    String key = mDatabase.child("random_data").push().getKey();
                    mDatabase.child("random_data").setValue(input);
                    T1.setText(String.valueOf(input));
                }
                return true;
            }
        });

        mNoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
               fireBasePull();

            }


        });

    }

    public void firebaseConnection() {
        int random = (int )(Math.random() * 50 + 1);
        String input = "data" + random;
        mDatabase = FirebaseDatabase.getInstance().getReference();
        String key = mDatabase.child("random_data").push().getKey();
        mDatabase.child("random_data").setValue(input);
        T1.setText(String.valueOf(input));
    }

    public void fireBasePull() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference messagesRef = mDatabase.child("random_data");


        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                T1.setText(value);
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
    private Runnable sendding = new Runnable() {
        @Override
        public void run() {
            firebaseConnection();
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

}