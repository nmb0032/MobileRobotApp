package com.example.robovision;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;

import org.opencv.android.OpenCVLoader;

import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //button
        button=findViewById(R.id.gps);

          button.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {

                  Intent i=new Intent(MainActivity.this,gps.class);
                  startActivity(i);

              }
          });

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