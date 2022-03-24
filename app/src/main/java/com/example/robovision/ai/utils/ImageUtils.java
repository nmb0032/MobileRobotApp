package com.example.robovision.ai.utils;


import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class ImageUtils {
    public static void transpose(Mat frame) {
        Size size = frame.size();
        Core.transpose(frame, frame); //for actual phone
        Core.flip(frame, frame, 1);
        Imgproc.resize(frame, frame, size);
    }
}
