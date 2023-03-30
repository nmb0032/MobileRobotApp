package com.example.robovision;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;

public class RemoteConnection extends AppCompatActivity {

    private Button mUserBtn;
    private Button mRobotBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_connection);

        mUserBtn = (Button) findViewById(R.id.user_btn);
        mRobotBtn = (Button)findViewById(R.id.robot_btn);


        mUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                openRemoteControlActivity();
            }
        });

        mRobotBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMainActivity();
            }
        });
    }

    private void openRemoteControlActivity() {
        Intent intent = new Intent(this, RemoteControl.class);
        startActivity(intent);
    }

    private void openMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


}
