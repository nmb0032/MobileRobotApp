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

    private Button mBTActivityBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Assigning GUI variables initialized above
        mBTActivityBtn = (Button)findViewById(R.id.bluetooth_btn);

        //Creating OpenCV instance
        if (!OpenCVLoader.initDebug())
            Log.e("OpenCV", "unable to load OpenCV");
        else
            Log.d("OpenCV", "OpenCV opened successfully");

        //Onclick listeners


        mBTActivityBtn.setOnClickListener(new View.OnClickListener() {
            /**
             * Switches to bluetooth activity on button press
             */
            @Override
            public void onClick(View v){
                openBluetoothActivity();
            }
        });

    }

    private void openBluetoothActivity(){
        Intent intent = new Intent(this, BluetoothActivity.class);
        startActivity(intent);
    }
}