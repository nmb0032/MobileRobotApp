package com.example.robovision;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class RemoteControl extends AppCompatActivity {
    //GUI components, contains four direction buttons
    private Button mForward;
    private Button mReverse;
    private Button mRight;
    private Button mLeft;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_control);

        mForward = (Button)findViewById(R.id.Forward);
        mReverse = (Button)findViewById(R.id.Reverse);
        mRight = (Button)findViewById(R.id.Right);
        mLeft = (Button)findViewById(R.id.Left);
        
        mForward.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Forward();
            }
        });
        mReverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Reverse();
            }
        });
        mRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Right();
            }
        });
        mLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Left();
            }
        });
    }

    private void Forward(){
        Log.d("Remote","Forward pressed");
    }
    private void Reverse(){
        Log.d("Remote","Reverse pressed");
    }
    private void Right(){
        Log.d("Remote","Right pressed");
    }
    private void Left(){
        Log.d("Remote","Left pressed");
    }
}
