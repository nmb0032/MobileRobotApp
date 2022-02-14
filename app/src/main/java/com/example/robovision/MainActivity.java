package com.example.robovision;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.robovision.calibration.CameraCalibrationActivity;
import com.example.robovision.calibration.CalibrationResult;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "RV::Main";

    private Button mCalibrationButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCalibrationButton = (Button)findViewById(R.id.calibrate_btn);

        //Check if calibration exists
        if(!CalibrationResult.checkCalibration(getBaseContext())) calibrateDialog();


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

    private void calibrateDialog(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage("Your Camera Must be calibrated to use our AI technology\n" +
                " Would you like to calibrate the camera now?");
        dialog.setTitle("Camera Not Calibrated!");
        dialog.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i(TAG,"User clicked yes");
                        calibrate();
                    }
                });
        dialog.setNegativeButton("Ignore",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Disable AI functionality
                        Log.i(TAG, "AI abilities disabled");
                        //TODO: add disable buttons function
                        Toast.makeText(getApplicationContext(), "AI abilities disabled", Toast.LENGTH_SHORT).show();
                    }
                });
        AlertDialog alertDialog=dialog.create();
        alertDialog.show();
    }
}