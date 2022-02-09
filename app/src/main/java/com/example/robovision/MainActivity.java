package com.example.robovision;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;


import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    //GUI
    private Button mOpenCVActivityBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Setup button to switch to OpenCV color detector
        mOpenCVActivityBtn = (Button)findViewById(R.id.opencv_btn);

        mOpenCVActivityBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openOpenCVActivity();
            }
        });

    }

    private void openOpenCVActivity() {
        Intent intent = new Intent(this, ColorBlobDetectionActivity.class);
        startActivity(intent);
    }
}