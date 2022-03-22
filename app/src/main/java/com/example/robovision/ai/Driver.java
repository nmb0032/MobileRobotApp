package com.example.robovision.ai;

import android.hardware.Camera;
import android.nfc.Tag;
import android.util.Log;

import com.example.robovision.bluetooth.ConnectedThread;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

public class Driver {

    private static final String TAG = "AI:ObjectDetector";
    private static final int HEADING_THRESH = 2;

    private float mFOV_Ratio;
    private int mCenter;

    public Driver(int cameraID, int screen_width){
        Camera camera = Camera.open(0); //assuming 0 for camera number
        Camera.Parameters p = camera.getParameters();
        mFOV_Ratio = screen_width / p.getHorizontalViewAngle();
        mCenter = screen_width / 2;
        Log.i(TAG, String.format("Pixel to degree ratio: " +
                "%.2f", mFOV_Ratio));
    }

    public void FTL(int x, int y, ConnectedThread bluetooth){
        //Given x coordinate of object calculate angle to turn
        int distance_from_center = x - mCenter;
        int heading = (int)(distance_from_center * mFOV_Ratio); //should give heading
        if(Math.abs(heading) >= HEADING_THRESH){
            Log.i(TAG, "Heading within threshold, going to: " + heading);
            execute(bluetooth, heading, 300);
        }else{
            Log.i(TAG, "Heading within threshold continuing movement");
        }

    }


    public static void drawAngle(Mat img, int screen_height, int screen_width){
        int center_x = (int) screen_width / 2;
        int center_y = (int) screen_height / 2;
        Log.i(TAG, "Center of screen: " + center_x + " " + center_y);
        Imgproc.circle(img, new Point(center_x,center_y), 50, new Scalar(0,255,0), 20);
    }

    public static void exit(ConnectedThread bluetooth){
        bluetooth.write(";000#000#"); //Exits FTL activity
        Log.i(TAG, "Exiting FTL activity");
    }

    public static void start(ConnectedThread bluetooth){
        //Enter command to enter FTL activity
        bluetooth.write("a"); //following FTL protocol
        Log.i(TAG, "Entering FTL mode");
    }

    static String buildString(int heading, int speed){
        if(heading < -180 || heading > 180)
            throw new IllegalArgumentException("Heading value out of bounds: " + heading);
        if(speed < -400 || speed > 400)
            throw new IllegalArgumentException("Speed Invalid: " + speed);

        String instruction = (speed > 0)? "+":"-";
        instruction = instruction + String.format("%03d", Math.abs(speed)) + "#";

        instruction = (heading > 0)? instruction + "+": instruction + "-";
        instruction = instruction + String.format("%03d", Math.abs(heading)) + "#";
        //Log.i(TAG, instruction + " Built");
        return instruction;
    }

    /**
     * Executes a follow the leader instruction following protocol on
     * Arduino controller
     * @param heading Must be between -180 and 180
     * @param speed Must be between -400 and 400
     */
    private static void execute(ConnectedThread bluetooth, int heading, int speed){
        bluetooth.write(buildString(heading, speed));
        Log.i(TAG, "FTL command executed");
    }


    public static void pause(ConnectedThread bluetooth){
        //pause robot movement
        bluetooth.write("+000;000;"); //simple way to pause using FTL protocol
        Log.i(TAG, "Robot Paused");
    }
}
