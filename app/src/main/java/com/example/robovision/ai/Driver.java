package com.example.robovision.ai;

import com.example.robovision.bluetooth.ConnectedThread;
import android.hardware.Camera;
import android.util.Log;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

/**
 * Driver class handles all of the FTL protocol and algorithm
 * @author Nicholas Belvin
 */
public class Driver {
    private static final boolean DEBUG = false;
    private static final String TAG = "AI:Driver";
    private static final int HEADING_THRESH = 3;

    private float mFOV;
    private float mFOV_Ratio;
    private int mCenter;

    /**
     * Initializes a Driver object, takes in the cameraID of the camera you'd like to use
     * as it grabs its FOV information and sets it.
     * WARNING: Do not initialize this constructor while the camera is in use by another resource
     * @param cameraID the integer id of the camera you are using usually 0
     */
    public Driver(int cameraID){
        Camera camera = Camera.open(cameraID); //assuming 0 for camera number
        Camera.Parameters p = camera.getParameters();
        mFOV = p.getHorizontalViewAngle();
        Log.i(TAG, "FOV grabbed as: " + mFOV);
        camera.release();
        if(DEBUG) Log.w(TAG, "Debug mode enabled bluetooth disabled");
    }

    /**
     *  To be called within a onCameraFrame function this takes the width of your frame
     *  and uses the camera FOV information to calculate a ratio.
     * @param width the width of the frame
     */
    public void setup(int width){
        mFOV_Ratio = width / mFOV;
        mCenter = width / 2;
        Log.i(TAG, String.format("Pixel to degree ratio: %.2f with a width of %d", mFOV_Ratio, width));
    }

    /**
     * Top level execution of FTL algorithm, calculates heading, if heading is outside thresh
     * performs heading adjustment, if not continues forward.
     * @param x the x-axis coordinate of target
     * @param y the y-axis coordinate of target
     * @param bluetooth An active bluetooth thread
     */
    public void FTL(int x, int y, ConnectedThread bluetooth){
        //Given x coordinate of object calculate angle to turn
        int heading = calcHeading(x);
        if(Math.abs(heading) >= HEADING_THRESH){
            Log.i(TAG, "Heading adjustment, going to: " + heading);
            execute(bluetooth, heading, 80);
        }else{
            Log.i(TAG, "Heading within threshold continuing forward");
        }

    }

    /**
     * Draws the estimated heading for a given target in the top right corner of the screen
     * @param img Current Frame
     * @param target Selected Target
     */
    public void drawAngle(Mat img, int target){
        int heading = calcHeading(target);
        Imgproc.putText(img, "Heading: " + heading,
                        new Point((int) img.width() * .8, (int) img.height() * .1),
                1, 1, new Scalar(0, 255, 0));

    }

    /**
     * Searching mode for when no object is found
     * TODO: Implement
     * @param bluetooth
     */
    public static void search(ConnectedThread bluetooth) {

    }

    /**
     * Sends Exit signal to Arduino
     * @param bluetooth An active bluetooth thread
     */
    public static void exit(ConnectedThread bluetooth){
        if(!DEBUG) bluetooth.write(";000#+000#"); //Exits FTL activity
        Log.i(TAG, "Exiting FTL activity");
    }

    /**
     * Enters the FTL protocol by sending signal to Arduino
     * @param bluetooth An active bluetooth thread
     */
    public static void start(ConnectedThread bluetooth){
        //Enter command to enter FTL activity
        if(!DEBUG) bluetooth.write("a"); //following FTL protocol
        Log.i(TAG, "Entering FTL mode");
    }

    /**
     * Builds a string according to the FTL protocol as specified in our Mobile Robot Document
     * for more information reach out to Jack Glendinning
     * @param heading A heading relative to the center of the camera being 0 left - right +
     * @param speed Value between -400 and 400, backwards is -, forward is +
     * @return The Built String object
     */
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

    /**
     * Sends the pause signal to the arduino as according to the FTL protocol
     * @param bluetooth An active bluetooth thread
     */
    public static void pause(ConnectedThread bluetooth){
        //pause robot movement
        if(!DEBUG) bluetooth.write("+000;000;"); //simple way to pause using FTL protocol
        Log.i(TAG, "Robot Paused");
    }

    /**
     * Estimates the heading at which a target is relative to the center of the camera.
     * Uses a FOV ratio set in initializer to do this.
     * @param x the x-axis coordinate of the target
     * @return the estimated heading
     */
    private int calcHeading(int x) {
        int distance_from_center = -1 * (x - mCenter);
        return (int)(distance_from_center / mFOV_Ratio); //should give heading
    }

}
