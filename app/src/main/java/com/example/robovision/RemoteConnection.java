package com.example.robovision;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.robovision.bluetooth.BluetoothActivity;


public class RemoteConnection extends AppCompatActivity {

    private Button mUserBtn;
    private Button mRobotBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_connection);

        mUserBtn = findViewById(R.id.user_btn);
        mRobotBtn = findViewById(R.id.robot_btn);
        
        mUserBtn.setOnClickListener(v -> openUserConnection());

        mRobotBtn.setOnClickListener(v -> openRobotConnection());
    }

    private void openUserConnection() {
        Intent intent = new Intent(this, BluetoothActivity.class);
        startActivity(intent);
    }

    private void openRobotConnection() {
        Intent intent = new Intent(this, firebasePull.class);
        startActivity(intent);
    }
}
