package com.example.robovision.ai;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.robovision.MainActivity;
import com.example.robovision.R;
import com.example.robovision.ai.calibration.CalibrationFrameRender;
import com.example.robovision.ai.calibration.CalibrationResult;
import com.example.robovision.ai.calibration.CameraCalibrator;
import com.example.robovision.ai.calibration.OnCameraFrameRender;
import com.example.robovision.bluetooth.BTBaseApplication;
import com.example.robovision.bluetooth.ConnectedThread;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCamera2View;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class OpenCVActivity extends MainActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static String TAG = "MobileNetActivity";

    private CameraCalibrator mCameraCalibrator; //Calibrator for distortion matrix
    private OnCameraFrameRender mOnCameraFrameRender; //Holds calibrator

    private ConnectedThread mBluetooth; //For bluetooth connection

    private Driver mDriver; //Holds driver for FTL

    int counter = 0;

    JavaCameraView javaCameraView;
    Mat mRGBA, mRGBAT, frame, frame_t, frame_ret;
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

        //Connecting Bluetooth from application context
        BTBaseApplication app = (BTBaseApplication)getApplication(); //getting application varaibles
        mBluetooth = app.bluetoothThread;
        Driver.start(mBluetooth); //starting FTL command

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
                        mainActivity();
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
        //Log.d(TAG, "onCameraViewStarted");
        //mRGBA = new Mat(height, width, CvType.CV_8UC4);
        frame_t   = new Mat();
        frame_ret = new Mat();
        frame     = new Mat(height, width, CvType.CV_8UC4);

        //Creating Driver Instance
        mDriver = new Driver(0, width);

        String proto = getPath("MobileNetSSD_deploy.prototxt", this);
        String weights = getPath("MobileNetSSD_deploy.caffemodel", this);
        net = Dnn.readNetFromCaffe(proto, weights);
        Log.i(TAG, "Network loaded successfully");

        mCameraCalibrator = new CameraCalibrator(width, height, false);
        if(!CalibrationResult.tryLoad(this, mCameraCalibrator.getCameraMatrix(), mCameraCalibrator.getDistortionCoefficients(), getBaseContext())){
            Log.e(TAG, "Camera not calibrated, returning home");
            Toast.makeText(getApplicationContext(), "Please calibrate camera", Toast.LENGTH_LONG).show();
            mainActivity();
        }
        mOnCameraFrameRender = new OnCameraFrameRender(new CalibrationFrameRender(mCameraCalibrator));
    }

    public void onCameraViewStopped() {
            //Send stop command to robot
            Driver.exit(mBluetooth);
         }



    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame)
    {
        Imgproc.cvtColor(mOnCameraFrameRender.render(inputFrame), frame, Imgproc.COLOR_RGB2RGBA);
        //frame = inputFrame.rgba();
        Core.transpose(frame, frame_t);
        Core.flip(frame_t, frame_t, 1);
        Imgproc.resize(frame_t, frame_ret, frame.size());
        frame.release();
        frame_t.release();


        if (counter == 10){


            final int IN_WIDTH = 300;
            final int IN_HEIGHT = 300;
            final float WH_RATIO = (float)IN_WIDTH / IN_HEIGHT;
            final double IN_SCALE_FACTOR = 0.007843;
            final double MEAN_VAL = 127.5;
            final double THRESHOLD = 0.2;
            // Get a new frame
            //Mat frame = inputFrame.rgba();
            Imgproc.cvtColor(frame_ret, frame_ret, Imgproc.COLOR_RGBA2RGB);
            // Forward image through network.
            Mat blob = Dnn.blobFromImage(frame_ret, IN_SCALE_FACTOR,
                    new Size(IN_WIDTH, IN_HEIGHT),
                    new Scalar(MEAN_VAL, MEAN_VAL, MEAN_VAL), false);
            net.setInput(blob);
            Mat detections = net.forward();
            int cols = frame_ret.cols();
            int rows = frame_ret.rows();
            Size cropSize;
            if ((float)cols / rows > WH_RATIO) {
                cropSize = new Size(rows * WH_RATIO, rows);
            } else {
                cropSize = new Size(cols, cols / WH_RATIO);
            }
            int y1 = (int)(rows - cropSize.height) / 2;
            int y2 = (int)(y1 + cropSize.height);
            int x1 = (int)(cols - cropSize.width) / 2;
            int x2 = (int)(x1 + cropSize.width);
            Mat subFrame = frame_ret.submat(y1, y2, x1, x2);
            cols = subFrame.cols();
            rows = subFrame.rows();
            detections = detections.reshape(1, (int)detections.total() / 7);
            for (int i = 0; i < detections.rows(); ++i) {
                double confidence = detections.get(i, 2)[0];
                if (confidence > THRESHOLD) {
                    int classId = (int)detections.get(i, 1)[0];
                    int xLeftBottom = (int)(detections.get(i, 3)[0] * cols);
                    int yLeftBottom = (int)(detections.get(i, 4)[0] * rows);
                    int xRightTop   = (int)(detections.get(i, 5)[0] * cols);
                    int yRightTop   = (int)(detections.get(i, 6)[0] * rows);

                    //Autonmous driving////////

                    // if the classNames[classId] == "person"
                    //      if calc_area > max area of object:
                    //          driver.pause()
                    //      else
                    //          calc center point
                    //          driver.execute(x,y);
                    //      break;
                    ///////////////////////////////////



                    // Draw rectangle around detected object.
                    Imgproc.rectangle(subFrame, new Point(xLeftBottom, yLeftBottom),
                            new Point(xRightTop, yRightTop),
                            new Scalar(0, 255, 0));
                    String label = classNames[classId] + ": " + confidence;
                    int[] baseLine = new int[1];
                    Size labelSize = Imgproc.getTextSize(label, Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, 1, baseLine);
                    // Draw background for label.
                    Imgproc.rectangle(subFrame, new Point(xLeftBottom, yLeftBottom - labelSize.height),
                            new Point(xLeftBottom + labelSize.width, yLeftBottom + baseLine[0]),
                            new Scalar(255, 255, 255), Core.FILLED);
                    // Write class name and confidence.
                    Imgproc.putText(subFrame, label, new Point(xLeftBottom, yLeftBottom),
                            Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(0, 0, 0));

                    if (classNames[classId].equals("person")) {
                        int length = xRightTop - xLeftBottom;
                        int width = yRightTop - yLeftBottom;
                        int length_frame = x2 - x1;
                        int width_frame = y2 - y1;
                        int area_frame = length_frame * width_frame; //assuming this is camera frame area.
                        int area = length * width;

                        if (area > area_frame) {
                            Driver.pause(mBluetooth);
                        } else {
                            int xCenter = (xLeftBottom + xRightTop) / 2;
                            int yCenter = (yLeftBottom + yRightTop) / 2;
                            Driver.drawAngle(subFrame,length, width); //Draw location of object
                            mDriver.FTL(xCenter,yCenter,mBluetooth);
                            Log.d(TAG, "Object located at " + xCenter + ", " + yCenter + " ClassID: " + classId);
                        }
                        break;
                    }
                }
            }
            //return frame;

            frame.release();
            frame_t.release();
            counter = 0;
        }

        else {

            frame.release();
            frame_t.release();
            counter = counter + 1;
        }





        frame.release();
        frame_t.release();
        return frame_ret;
    }

    // Upload file to storage and return a path.
    private static String getPath(String file, Context context) {
        AssetManager assetManager = context.getAssets();
        BufferedInputStream inputStream = null;
        try {
            // Read data from assets.
            inputStream = new BufferedInputStream(assetManager.open(file));
            byte[] data = new byte[inputStream.available()];
            inputStream.read(data);
            inputStream.close();
            // Create copy file in storage.
            File outFile = new File(context.getFilesDir(), file);
            FileOutputStream os = new FileOutputStream(outFile);
            os.write(data);
            os.close();
            // Return a path to file which may be read in common way.
            return outFile.getAbsolutePath();
        } catch (IOException ex) {
            Log.i(TAG, "Failed to upload a file");
        }
        return "";
    }
    //private static final String TAG = "OpenCV/Sample/MobileNet";
    private static final String[] classNames = {"background",
            "aeroplane", "bicycle", "bird", "boat",
            "bottle", "bus", "car", "cat", "chair",
            "cow", "diningtable", "dog", "horse",
            "motorbike", "person", "pottedplant",
            "sheep", "sofa", "train", "tvmonitor"};
    private Net net;





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

    private void mainActivity(){
        Intent i = new Intent(OpenCVActivity.this, MainActivity.class);
        startActivity(i);
    }

}