package com.example.robovision;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.robovision.ai.ColorBlobDetectionActivity;
import com.example.robovision.ai.OpenCVActivity;
import com.example.robovision.ai.calibration.CalibrationResult;
import com.example.robovision.ai.calibration.CameraCalibrationActivity;
import com.example.robovision.bluetooth.BTBaseApplication;
import com.example.robovision.bluetooth.BluetoothActivity;
import com.google.firebase.FirebaseApp;

public class
MainActivity extends AppCompatActivity {
    private final static String TAG = "RV::Main";

    private BTBaseApplication mApplication;

    private Button mCalibrationButton;
    private Button mDriveBtn;
    private Button mOpenCVActivityBtn;
    private Button mServerBtn;

    private Button mGPSBtn;

    private Button mRemoteBtn;
    private Button mFirebaseBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_main);
        mCalibrationButton = (Button)findViewById(R.id.calibrate_btn);
        mDriveBtn = (Button)findViewById(R.id.drive_btn);
        mOpenCVActivityBtn = (Button)findViewById(R.id.opencv_btn);
        mServerBtn = (Button)findViewById(R.id.server_btn);
        mRemoteBtn = (Button)findViewById(R.id.remote_connection);
        mGPSBtn=findViewById(R.id.gps);
        mFirebaseBtn=findViewById(R.id.fire_base);

        mApplication = (BTBaseApplication)getApplication();

        if(mApplication.bluetoothThread == null) bluetoothDialog();
        if(!CalibrationResult.checkCalibration(getBaseContext())) calibrateDialog();


        mDriveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                openBluetoothActivity();
            }
        });
        mFirebaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                openFireActivity();
            }
        });

        mCalibrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calibrate();
            }
        });

        mOpenCVActivityBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openOpenCVActivity();
            }
        });

        mServerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMobileNetActivity();
            }
        });

        mGPSBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGpsActivity();
            }
        });

        mRemoteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openRemoteActivity();
            }
        });

    }

    private void calibrate(){
        Intent intent = new Intent(this, CameraCalibrationActivity.class);
        startActivity(intent);
    }

    private void openBluetoothActivity(){
        Intent intent = new Intent(this, BluetoothActivity.class);
        startActivity(intent);
    }

    private void openOpenCVActivity() {
        Intent intent = new Intent(this, ColorBlobDetectionActivity.class);
        startActivity(intent);
    }

    private void openMobileNetActivity() {
        Intent intent = new Intent(this, OpenCVActivity.class);
        startActivity(intent);
    }

    private void openGpsActivity() {
        Intent intent = new Intent(this, gps.class);
        startActivity(intent);
    }

    private void openRemoteActivity() {
        Intent intent = new Intent(this, RemoteConnection.class);
        startActivity(intent);
    }
    private void openFireActivity() {
        Intent intent = new Intent(this, firebaseConnection.class);
        startActivity(intent);
    }


    private void bluetoothDialog(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage("The Robot must be connected through bluetooth to use any modes.\n" +
                "Would you like to connect to bluetooth now?");
        dialog.setTitle("Bluetooth Not Connected!");
        dialog.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i(TAG,"User clicked yes");
                        openBluetoothActivity();
                    }
                });
        dialog.setNegativeButton("Ignore",
                (dialog1, which) -> {
                    //Disable AI functionality
                    Log.i(TAG, "AI abilities disabled");
                    //TODO: add disable buttons function
                    Toast.makeText(getApplicationContext(), "AI abilities disabled", Toast.LENGTH_SHORT).show();
                });
        AlertDialog alertDialog=dialog.create();
        alertDialog.show();
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