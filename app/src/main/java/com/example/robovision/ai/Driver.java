package com.example.robovision.ai;

import android.hardware.Camera;
import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

public class Driver {

    private static final String TAG = "AI:ObjectDetector";

    private float mFOV_Ratio;

    public Driver(int cameraID, int screen_width){
        Camera camera = Camera.open(0);
        Camera.Parameters p = camera.getParameters();
        mFOV_Ratio = screen_width / p.getHorizontalViewAngle();
        Log.i(TAG, String.format("Pixel to degree ratio: %.2f", mFOV_Ratio));
    }

    public static void drawAngle(Mat img, int screen_height, int screen_width){
        int center_x = (int) screen_width / 2;
        int center_y = (int) screen_height / 2;
        Log.i(TAG, "Center of screen: " + center_x + " " + center_y);
        Imgproc.circle(img, new Point(center_x,center_y), 50, new Scalar(0,255,0), 20);
    }
}
