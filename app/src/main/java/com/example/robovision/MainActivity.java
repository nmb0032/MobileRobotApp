package com.example.robovision;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;


import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    //GUI
    private Button mOpenCVActivityBtn;
    private Button mMobilenetctivityBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Setup button to switch to OpenCV color detector
        mMobilenetctivityBtn = (Button)findViewById(R.id.mobilenet_btn);

        mMobilenetctivityBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mobileMobilenetActivity();
            }
        });

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

    private void mobileMobilenetActivity() {
        Intent intent = new Intent(this, OpenCVActivity.class);
        startActivity(intent);
    }
}