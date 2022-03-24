package com.example.robovision.ai;

import android.hardware.Camera;
import android.nfc.Tag;
import android.provider.ContactsContract;
import android.util.Log;

import com.example.robovision.bluetooth.ConnectedThread;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

public class Driver {

    private static final boolean DEBUG = true;
    private static final String TAG = "AI:Driver";
    private static final int HEADING_THRESH = 3;

    private float mFOV;
    private float mFOV_Ratio;
    private int mCenter;

    public Driver(int cameraID){
        Camera camera = Camera.open(0); //assuming 0 for camera number
        Camera.Parameters p = camera.getParameters();
        mFOV = p.getHorizontalViewAngle();
        Log.i(TAG, "FOV grabbed as: " + mFOV);
        camera.release();
        if(DEBUG) Log.w(TAG, "Debug mode enabled bluetooth disabled");
    }

    public void setup(int width){
        mFOV_Ratio = width / mFOV;
        mCenter = width / 2;
        Log.i(TAG, String.format("Pixel to degree ratio: %.2f with a width of %d", mFOV_Ratio, width));
    }

    public void FTL(int x, int y, ConnectedThread bluetooth){
        //Given x coordinate of object calculate angle to turn
        int heading = calcHeading(x);
        if(Math.abs(heading) >= HEADING_THRESH){
            Log.i(TAG, "Heading within threshold, going to: " + heading);
            execute(bluetooth, heading, 50);
        }else{
            Log.i(TAG, "Heading within threshold continuing forward");
        }

    }

    public void drawAngle(Mat img, int target){
        int center_x = (int) img.width() / 2;
        int center_y = (int) img.height() / 2;
        int heading = calcHeading(target);
        Imgproc.circle(img, new Point(center_x,center_y), 1, new Scalar(0,255,0), 1);
        Imgproc.putText(img, "Heading: " + heading,
                        new Point((int) img.width() * .8, (int) img.height() * .1),
                1, 1, new Scalar(0, 255, 0));

    }

    public static void exit(ConnectedThread bluetooth){
        if(!DEBUG) bluetooth.write(";000#+000#"); //Exits FTL activity
        Log.i(TAG, "Exiting FTL activity");
    }

    public static void start(ConnectedThread bluetooth){
        //Enter command to enter FTL activity
        if(!DEBUG) bluetooth.write("a"); //following FTL protocol
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
        Log.i(TAG, instruction + " Built");
        return instruction;
    }

    /**
     * Executes a follow the leader instruction following protocol on
     * Arduino controller
     * @param heading Must be between -180 and 180
     * @param speed Must be between -400 and 400
     */
    private static void execute(ConnectedThread bluetooth, int heading, int speed){
        if(!DEBUG) bluetooth.write(buildString(heading, speed));
        Log.i(TAG, "FTL command executed");
    }


    public static void pause(ConnectedThread bluetooth){
        //pause robot movement
        if(!DEBUG) bluetooth.write("+000;000;"); //simple way to pause using FTL protocol
        Log.i(TAG, "Robot Paused");
    }

    private int calcHeading(int x) {
        int distance_from_center = -1 * (x - mCenter);
        return (int)(distance_from_center / mFOV_Ratio); //should give heading
    }

}
