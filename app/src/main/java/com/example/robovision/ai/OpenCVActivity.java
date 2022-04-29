package com.example.robovision.ai;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.example.robovision.MainActivity;
import com.example.robovision.R;
import com.example.robovision.ai.calibration.CalibrationFrameRender;
import com.example.robovision.ai.calibration.CalibrationResult;
import com.example.robovision.ai.calibration.CameraCalibrator;
import com.example.robovision.ai.calibration.OnCameraFrameRender;
import com.example.robovision.ai.utils.ImageUtils;
import com.example.robovision.bluetooth.BTBaseApplication;
import com.example.robovision.bluetooth.ConnectedThread;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
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
    private static final String TAG = "AI:MobileNetActivity";
    private static final int    DETECTION_DELAY = 30;
    private final int           PERMISSIONS_READ_CAMERA = 1;

    private Button btnSelect_class;             //for drop down select
    private String mSelectedClass = "person";  //person by default but can be changed later.

    private CameraCalibrator    mCameraCalibrator; //Calibrator for distortion matrix
    private OnCameraFrameRender mOnCameraFrameRender; //Holds calibrator
    private ConnectedThread     mBluetooth; //For bluetooth connection
    private Driver              mDriver; //Holds driver for FTL
    private Net                 net;

    //private static final String TAG = "OpenCV/Sample/MobileNet";
    private static final String[] CLASSES = {
            "background",
            "aeroplane",
            "bicycle",
            "bird",
            "boat",
            "bottle",
            "bus",
            "car",
            "cat",
            "chair",
            "cow",
            "diningtable",
            "dog",
            "horse",
            "motorbike",
            "person",
            "pottedplant",
            "sheep",
            "sofa",
            "train",
            "tvmonitor"
    };

    int counter = 0;

    JavaCameraView javaCameraView;
    Mat frame;

    BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(OpenCVActivity.this) {
        @Override
        public void onManagerConnected(int status) {
            Log.d(TAG, "call back success");
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_opencv);
        addDropDownMenu();
        driverSetup();
        cameraViewSetup();
        String msg = OpenCVLoader.initDebug() ? "OpenCV opened successfully" : "Unable to load OpenCV";
        Log.d(TAG, msg);
        checkPermissions();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        Log.d(TAG, "onCameraViewStarted with width: " + width + " Height: " + height);
        //mRGBA = new Mat(height, width, CvType.CV_8UC4);
        frame     = new Mat(height, width, CvType.CV_8UC4);

        mDriver.setup(width);

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
            Driver.exit(mBluetooth);
         }



    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame)
    {
        Imgproc.cvtColor(mOnCameraFrameRender.render(inputFrame), frame, Imgproc.COLOR_RGB2RGBA);
        ImageUtils.transpose(frame); /** for physical device **/


        if (counter == DETECTION_DELAY){
            final int IN_WIDTH = 300;
            final int IN_HEIGHT = 300;
            final float WH_RATIO = (float)IN_WIDTH / IN_HEIGHT;
            final double IN_SCALE_FACTOR = 0.007843;
            final double MEAN_VAL = 127.5;
            final double THRESHOLD = 0.2;
            // Get a new frame
            //Mat frame = inputFrame.rgba();
            Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGBA2RGB);
            // Forward image through network.
            Mat blob = Dnn.blobFromImage(frame, IN_SCALE_FACTOR,
                    new Size(IN_WIDTH, IN_HEIGHT),
                    new Scalar(MEAN_VAL, MEAN_VAL, MEAN_VAL), false);
            net.setInput(blob);
            Mat detections = net.forward();
            int cols = frame.cols();
            int rows = frame.rows();
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
            Mat subFrame = frame.submat(y1, y2, x1, x2);
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

                    // Draw rectangle around detected object.
                    Imgproc.rectangle(subFrame, new Point(xLeftBottom, yLeftBottom),
                            new Point(xRightTop, yRightTop),
                            new Scalar(0, 255, 0));
                    String label = CLASSES[classId] + ": " + confidence;
                    int[] baseLine = new int[1];
                    Size labelSize = Imgproc.getTextSize(label, Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, 1, baseLine);
                    // Draw background for label.
                    Imgproc.rectangle(subFrame, new Point(xLeftBottom, yLeftBottom - labelSize.height),
                            new Point(xLeftBottom + labelSize.width, yLeftBottom + baseLine[0]),
                            new Scalar(255, 255, 255), Core.FILLED);
                    // Write class name and confidence.
                    Imgproc.putText(subFrame, label, new Point(xLeftBottom, yLeftBottom),
                            Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(0, 0, 0));

                    if (CLASSES[classId].equals(mSelectedClass)) {
                        int length = xRightTop - xLeftBottom;
                        int width = yRightTop - yLeftBottom;
                        int length_frame = x2 - x1;
                        int width_frame = y2 - y1;
                        int area_frame = length_frame * width_frame; //assuming this is camera frame area.
                        int area = length * width;

                        if (area > area_frame * .75) {
                            Driver.pause(mBluetooth); //Pause robot
                        } else {
                            int xCenter = (xLeftBottom + xRightTop) / 2;
                            int yCenter = (yLeftBottom + yRightTop) / 2;
                            //Driver.drawAngle(subFrame,length, width); //Draw location of object
                            mDriver.FTL(xCenter,yCenter,mBluetooth); //Drive to location
                            Log.d(TAG, "Object located at " + xCenter + ", " + yCenter + " ClassID: " + classId);
                        }
                        break;
                    } else {
                        Driver.pause(mBluetooth); //if no detection pause
                    }
                }
            }
            //return frame;
            counter = 0;
        }

        else {
            counter++;
        }
        return frame;
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
            Log.d(TAG, "OpenCV is initialised again");
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

    private void checkPermissions(){
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
            Log.d(TAG, "permissions granted");
            javaCameraView.setCameraPermissionGranted();
        }
    }

    private void mainActivity(){
        Intent i = new Intent(OpenCVActivity.this, MainActivity.class);
        startActivity(i);
    }

    private void driverSetup(){
        mDriver = new Driver(0); //set FOV for driver before use
        BTBaseApplication app = (BTBaseApplication)getApplication(); //getting application varaibles
        mBluetooth = app.bluetoothThread;
        Driver.start(mBluetooth); //starting FTL command
    }

    private void cameraViewSetup(){
        javaCameraView = (JavaCameraView)findViewById(R.id.my_camera_view);
        javaCameraView.setVisibility(SurfaceView.VISIBLE);
        javaCameraView.setCvCameraViewListener(OpenCVActivity.this);
    }

    /**
     * Shows Menu select to change target
     */
    public void addDropDownMenu() {
        btnSelect_class = (Button) findViewById(R.id.btnSelect_class);
        btnSelect_class.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                PopupMenu popupMenu = new PopupMenu(OpenCVActivity.this, btnSelect_class);
                int tmp = 0;
                for(String name: CLASSES){
                    popupMenu.getMenu().add(tmp, tmp, tmp, name);
                    tmp++;
                }

                popupMenu.getMenuInflater().inflate(R.menu.opencv, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        mSelectedClass = (String) menuItem.getTitle();
                        Toast.makeText(OpenCVActivity.this,
                                "Target Set to " + menuItem.getTitle(), Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });
                popupMenu.show();
            }
        });

    }
}