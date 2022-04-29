package com.example.robovision;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;

import org.opencv.android.OpenCVLoader;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.robovision.ai.OpenCVActivity;
import com.example.robovision.ai.ColorBlobDetectionActivity;
import com.example.robovision.bluetooth.BTBaseApplication;
import com.example.robovision.bluetooth.BluetoothActivity;
import com.example.robovision.ai.calibration.CameraCalibrationActivity;
import com.example.robovision.ai.calibration.CalibrationResult;

import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "RV::Main";

    private BTBaseApplication mApplication;

    private Button mCalibrationButton;
    private Button mBTActivityBtn;
    private Button mOpenCVActivityBtn;
    private Button mMobileNetBtn;

    private Button mGPSBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCalibrationButton = (Button)findViewById(R.id.calibrate_btn);
        mBTActivityBtn = (Button)findViewById(R.id.bluetooth_btn);
        mOpenCVActivityBtn = (Button)findViewById(R.id.opencv_btn);
        mMobileNetBtn = (Button)findViewById(R.id.mobilenet_btn);
        mGPSBtn=findViewById(R.id.gps);

        mApplication = (BTBaseApplication)getApplication();

        if(mApplication.bluetoothThread == null) bluetoothDialog();
        if(!CalibrationResult.checkCalibration(getBaseContext())) calibrateDialog();


        mBTActivityBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                openBluetoothActivity();
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

        mMobileNetBtn.setOnClickListener(new View.OnClickListener() {
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

        //Creating OpenCV instance
        if (!OpenCVLoader.initDebug())
            Log.e("OpenCV", "unable to load OpenCV");
        else
            Log.d("OpenCV", "OpenCV opened successfully");

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