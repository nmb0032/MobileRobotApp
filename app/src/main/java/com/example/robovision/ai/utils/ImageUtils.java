package com.example.robovision.ai.utils;


import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

public class ImageUtils {
    public static void transpose(Mat frame) {
        Size size = frame.size();
        Core.transpose(frame, frame); //for actual phone
        Core.flip(frame, frame, 1);
        Imgproc.resize(frame, frame, size);
    }

    public static int[] findCenter(MatOfPoint object){
        Moments m = Imgproc.moments(object);
        return new int[]{(int)(m.m10/m.m00), (int)(m.m01/m.m00)};
    }
}
