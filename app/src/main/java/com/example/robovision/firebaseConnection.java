package com.example.robovision;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.RecursiveAction;


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
        T1 = (TextView) findViewById(R.id.title);
        mConnBtn = (Button) findViewById(R.id.conn_btn);
        mNoBtn = (Button) findViewById(R.id.no_btn);
        mConnectBtn = (Button) findViewById(R.id.connect_btn);
        mConnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                 firebaseConnection();

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
      Query query = messagesRef.limitToLast(1);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    for (DataSnapshot messageSnapshot: dataSnapshot.getChildren()) {
                        String data = messageSnapshot.getValue(String.class);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                readData(data);
                            }
                        });
                    }
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
    private void readData(String message) {
        T1.setText(message);
    }

}