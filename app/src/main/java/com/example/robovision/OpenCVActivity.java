package com.example.robovision;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCamera2View;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class OpenCVActivity extends MainActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static String TAG = "MainActivity";

    JavaCameraView javaCameraView;
    Mat mRGBA, mRGBAT;
    boolean startCanny = false;



    private final int PERMISSIONS_READ_CAMERA=1;

    public void Canny(View Button){

        if (startCanny == false){
            startCanny = true;
        }

        else{

            startCanny = false;


        }




    }

    BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(OpenCVActivity.this) {
        @Override
        public void onManagerConnected(int status) {
            Log.d(TAG, "callbacksuccess");
            switch (status)
            {
                case BaseLoaderCallback.SUCCESS:
                {
                    Log.d(TAG, "case success");
                    javaCameraView.enableView();
                    break;
                }
                default:
                {
                    Log.d(TAG, "case default");
                    super.onManagerConnected(status);
                    break;
                }

            }

        }
    };



    Button b2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_opencv);
        javaCameraView = (JavaCameraView)findViewById(R.id.my_camera_view);
        javaCameraView.setVisibility(SurfaceView.VISIBLE);
        javaCameraView.setCvCameraViewListener(OpenCVActivity.this);

        //Creating OpenCV instance
        if (!OpenCVLoader.initDebug())
            Log.e("OpenCV", "unable to load OpenCV");
        else
            Log.d("OpenCV", "OpenCV opened successfully");

        /* switch to new activity start */
        b2 = findViewById(R.id.page2);
        b2.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(OpenCVActivity.this,MainActivity.class);
                        startActivity(i);
                    }
                }
        );
        /* switch to new activity end */

// Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        PERMISSIONS_READ_CAMERA);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            Log.d(TAG, "PERMISSIOns granted");
            javaCameraView.setCameraPermissionGranted();
        }


    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        Log.d(TAG, "onCameraViewStarted");
        mRGBA = new Mat(height, width, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {
        Log.d(TAG, "onCameraViewStopped");
        mRGBA.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame)
    {
        Log.d(TAG, "onCameraFrame");
        mRGBA = inputFrame.rgba();
        mRGBAT = mRGBA.t();
        Core.transpose(mRGBA, mRGBAT);
        Core.flip(mRGBA.t(), mRGBAT, 1);
        Imgproc.resize(mRGBAT, mRGBAT, mRGBA.size());

        if (startCanny == true) {

            Imgproc.cvtColor(mRGBA, mRGBA, Imgproc.COLOR_RGBA2GRAY);
            Imgproc.Canny(mRGBA, mRGBA, 100, 80);

        }

        return mRGBA;

        /*
        mRGBA = inputFrame.rgba();
        Core.transpose(mRGBA, mRGBAT);
        Imgproc.resize(mRGBAT, mRGBAT, mRGBAT.size(),0,0,0);
        Core.flip(mRGBA.t(), mRGBA, 1);

        return mRGBA;
        */

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        if (javaCameraView != null)
        {
            javaCameraView.disableView();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        if (javaCameraView != null)
        {
            javaCameraView.disableView();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if (OpenCVLoader.initDebug())
        {
            Log.d(TAG, "OpenCV is intialised again");
            baseLoaderCallback.onManagerConnected((BaseLoaderCallback.SUCCESS));
        }
        else
        {
            Log.d(TAG, "OpenCV is not working");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, baseLoaderCallback);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        // Ensure that this result is for the camera permission request
        if (requestCode == PERMISSIONS_READ_CAMERA) {
            // Check if the request was granted or denied
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // The request was granted -> tell the camera view
                javaCameraView.setCameraPermissionGranted();
            } else {
                // The request was denied -> tell the user and exit the application
                Toast.makeText(this, "Camera permission required.",
                        Toast.LENGTH_LONG).show();
                this.finish();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
