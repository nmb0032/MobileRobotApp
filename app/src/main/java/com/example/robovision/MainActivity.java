package com.example.robovision;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;

import org.opencv.android.OpenCVLoader;

import android.util.Log;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Creating OpenCV instance
        if (!OpenCVLoader.initDebug())
            Log.e("OpenCV", "unable to load OpenCV");
        else
            Log.d("OpenCV", "OpenCV opened successfully");

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null) {
            //bluetooth unsupported
            Log.e("Bluetooth","Bluetooth not supported");
        }
        else {
            Log.d("Bluetooth", "Bluetooth adapter initiated");
        }
    }
}