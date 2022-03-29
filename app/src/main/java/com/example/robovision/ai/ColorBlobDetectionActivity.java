package com.example.robovision.ai;

import java.util.Collections;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.Toast;

//Importing calibration package
import com.example.robovision.MainActivity;
import com.example.robovision.ai.calibration.CalibrationFrameRender;
import com.example.robovision.ai.calibration.OnCameraFrameRender;
import com.example.robovision.ai.calibration.CalibrationResult;
import com.example.robovision.ai.calibration.CameraCalibrator;

import com.example.robovision.R;
import com.example.robovision.ai.utils.ImageUtils;
import com.example.robovision.bluetooth.BTBaseApplication;
import com.example.robovision.bluetooth.ConnectedThread;


public class ColorBlobDetectionActivity extends CameraActivity implements OnTouchListener, CvCameraViewListener2 {
    private static final String  TAG              = "AI:ColorFilterActivity";
    private static final int     DRIVER_DELAY     = 120;
    private static final double  AREA_THRESH      = .65;

    private CameraCalibrator     mCameraCalibrator; //Calibrator for distortion matrix
    private OnCameraFrameRender  mOnCameraFrameRender; //Holds calibrator
    private ConnectedThread      mBluetooth;
    private Driver               mDriver;
    private boolean              mIsColorSelected = false;
    private Mat                  mRgba;
    private Scalar               mBlobColorRgba;
    private Scalar               mBlobColorHsv;
    private ColorBlobDetector    mDetector;
    private Mat                  mSpectrum;
    private Size                 SPECTRUM_SIZE;
    private Scalar               CONTOUR_COLOR;
    private int                  mCount;


    private CameraBridgeViewBase mOpenCvCameraView;

    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(ColorBlobDetectionActivity.this);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public ColorBlobDetectionActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.color_blob_detection_surface_view);
        driverSetup();

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.color_blob_detection_activity_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);


    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(mOpenCvCameraView);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        //Setting Distortion matrix
        mDriver.setup(width);
        mCameraCalibrator = new CameraCalibrator(width, height, false);
        if(!CalibrationResult.tryLoad(this, mCameraCalibrator.getCameraMatrix(), mCameraCalibrator.getDistortionCoefficients(), getBaseContext())){
            Log.e(TAG, "Camera not calibrated, returning home");
            Toast.makeText(getApplicationContext(), "Please calibrate camera", Toast.LENGTH_LONG).show();
            mainActivity();
        }
        mOnCameraFrameRender = new OnCameraFrameRender(new CalibrationFrameRender(mCameraCalibrator)); //Renderer with distortion matrix
        //////////////////////////////////////////////////

        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mDetector = new ColorBlobDetector();
        mSpectrum = new Mat();
        mBlobColorRgba = new Scalar(255);
        mBlobColorHsv = new Scalar(255);
        SPECTRUM_SIZE = new Size(200, 64);
        CONTOUR_COLOR = new Scalar(255,0,0,255);
    }

    public void onCameraViewStopped() {
        Driver.exit(mBluetooth);
        mRgba.release();
    }

    public boolean onTouch(View v, MotionEvent event) {
        int cols = mRgba.cols();
        int rows = mRgba.rows();

        int xOffset = (mOpenCvCameraView.getWidth() - cols) / 2;
        int yOffset = (mOpenCvCameraView.getHeight() - rows) / 2;

        int x = (int)event.getX() - xOffset;
        int y = (int)event.getY() - yOffset;

        Log.i(TAG, "Touch image coordinates: (" + x + ", " + y + ")");

        if ((x < 0) || (y < 0) || (x > cols) || (y > rows)) return false;

        Rect touchedRect = new Rect();

        touchedRect.x = (x>4) ? x-4 : 0;
        touchedRect.y = (y>4) ? y-4 : 0;

        touchedRect.width = (x+4 < cols) ? x + 4 - touchedRect.x : cols - touchedRect.x;
        touchedRect.height = (y+4 < rows) ? y + 4 - touchedRect.y : rows - touchedRect.y;

        Mat touchedRegionRgba = mRgba.submat(touchedRect);

        Mat touchedRegionHsv = new Mat();
        Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);

        // Calculate average color of touched region
        mBlobColorHsv = Core.sumElems(touchedRegionHsv);
        int pointCount = touchedRect.width*touchedRect.height;
        for (int i = 0; i < mBlobColorHsv.val.length; i++)
            mBlobColorHsv.val[i] /= pointCount;

        mBlobColorRgba = convertScalarHsv2Rgba(mBlobColorHsv);

        Log.i(TAG, "Touched rgba color: (" + mBlobColorRgba.val[0] + ", " + mBlobColorRgba.val[1] +
                ", " + mBlobColorRgba.val[2] + ", " + mBlobColorRgba.val[3] + ")");

        mDetector.setHsvColor(mBlobColorHsv);

        Imgproc.resize(mDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE, 0, 0, Imgproc.INTER_LINEAR_EXACT);

        mIsColorSelected = true;

        touchedRegionRgba.release();
        touchedRegionHsv.release();

        return false; // don't need subsequent touch events
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        // Distortion application
        Imgproc.cvtColor(mOnCameraFrameRender.render(inputFrame), mRgba, Imgproc.COLOR_RGB2RGBA); //apply distortion matrix and Convert from BGR to RGBA
        // End of distortion application//
        //TODO: Fix the null pointer exception
        ImageUtils.transpose(mRgba); /** Transpose for phone **/

        if (mIsColorSelected) {
            MatOfPoint target = null;
            mDetector.process(mRgba);
            List<MatOfPoint> contours = mDetector.getContours();
            Log.i(TAG, "Contours count: " + contours.size());
            //Imgproc.drawContours(mRgba, contours, -1, CONTOUR_COLOR); Draws each contour
            if(contours.size() > 0){
                target = mDetector.getTopTarget();
                ColorBlobDetector.boxTarget(mRgba, target);
                Moments m = Imgproc.moments(target);
                int center_x = (int)(m.m10/m.m00);
                mDriver.drawAngle(mRgba, center_x);
            }

            Mat colorLabel = mRgba.submat(4, 68, 4, 68);
            colorLabel.setTo(mBlobColorRgba);

            Mat spectrumLabel = mRgba.submat(4, 4 + mSpectrum.rows(), 70, 70 + mSpectrum.cols());
            mSpectrum.copyTo(spectrumLabel);

            /** Implementing driving logic **/
            if(mCount == DRIVER_DELAY){
                if(target != null && Imgproc.contourArea(target) < mRgba.total() * AREA_THRESH) {
                    int[] center = ImageUtils.findCenter(target);
                    mDriver.FTL(center[0],center[1],mBluetooth);
                } else Driver.pause(mBluetooth);
                mCount = 0;
            } else mCount++;
        }

        return mRgba;
    }

    private Scalar convertScalarHsv2Rgba(Scalar hsvColor) {
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);

        return new Scalar(pointMatRgba.get(0, 0));
    }

    public void mainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void driverSetup(){
        mCount = 0;
        mDriver = new Driver(0); //set FOV for driver before use
        BTBaseApplication app = (BTBaseApplication)getApplication(); //getting application varaibles
        mBluetooth = app.bluetoothThread;
        Driver.start(mBluetooth); //starting FTL command
    }
}
