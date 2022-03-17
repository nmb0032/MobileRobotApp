package com.example.robovision.ai;

import android.hardware.Camera;
import android.util.Log;

import com.example.robovision.bluetooth.BTBaseApplication;
import com.example.robovision.bluetooth.ConnectedThread;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

public class Driver {

    private static final String TAG = "AI:ObjectDetector";
    //Variables
    private float mFOV_Ratio;
    //application context
    private ConnectedThread mBluetooth;

    public Driver(int cameraID, int screen_width, ConnectedThread bluetooth){
        mBluetooth = bluetooth;

        Camera camera = Camera.open(0);
        Camera.Parameters p = camera.getParameters();
        mFOV_Ratio = screen_width / p.getHorizontalViewAngle();
        Log.i(TAG, String.format("Pixel to degree ratio: %.2f", mFOV_Ratio));
    }

    public static void FTL(int x, int y){

    }

    public static void drawAngle(Mat img, int screen_height, int screen_width){
        int center_x = (int) screen_width / 2;
        int center_y = (int) screen_height / 2;
        Log.i(TAG, "Center of screen: " + center_x + " " + center_y);
        Imgproc.circle(img, new Point(center_x,center_y), 50, new Scalar(0,255,0), 20);
    }

    public void exit(){
        mBluetooth.write(";000#000#"); //Exits FTL activity
        Log.i(TAG, "Exiting FTL activity");
    }

    public void start(){
        //Enter command to enter FTL activity
    }

    /**
     * Executes a follow the leader instruction following protocol on
     * Arduino controller
     * @param heading Must be between -180 and 180
     * @param speed Must be between -400 and 400
     */
    private void execute(int heading, int speed){
        if(heading < -180 || heading > 180)
            throw new IllegalArgumentException("Heading value out of bounds: " + heading);
        if(speed < -400 || speed > 400)
            throw new IllegalArgumentException("Speed Invalid: " + speed);

        String instruction = (speed > 0)? "+":"-";
        instruction = instruction + String.format("%03d", Math.abs(speed)) + ";";

        instruction = (heading > 0)? instruction + "+": instruction + "-";
        instruction = instruction + String.format("%03d", Math.abs(heading)) + ";";

        mBluetooth.write(instruction);
        Log.i(TAG, instruction + " Sent");
    }

    private void pause(){
        //pause robot movement
    }
}
