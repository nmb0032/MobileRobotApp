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

    private Button mVoiceControl0;
    private Button mVoiceControl1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mVoiceControl0 = (Button)findViewById(R.id.Voice_btn0);
        mVoiceControl1 = (Button)findViewById(R.id.Voice_btn1);

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
            mVoiceControl0.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    VoiceControl();
                }
            });

            mVoiceControl1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    VoiceControl_Alt();
                }
            });
            Log.d("Bluetooth", "Bluetooth adapter initiated");
        }
    }

    private void VoiceControl() {
        Intent intent = new Intent(getApplicationContext(),VoiceControlActivity.class);
        startActivity(intent);
    }
    private void VoiceControl_Alt() {
        Intent intent = new Intent(getApplicationContext(),VoiceControlActivityAlt.class);
        startActivity(intent);
    }

}