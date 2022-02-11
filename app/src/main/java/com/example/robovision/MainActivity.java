package com.example.robovision;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;

import com.example.robovision.calibration.CameraCalibrationActivity;

public class MainActivity extends AppCompatActivity {

    private Button mCalibrationButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCalibrationButton = (Button)findViewById(R.id.calibrate_btn);

        mCalibrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calibrate();
            }
        });


    }

    private void calibrate(){
        Intent intent = new Intent(this, CameraCalibrationActivity.class);
        startActivity(intent);
    }
}