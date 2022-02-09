package com.example.robovision;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ConnectedThread extends Thread {
    // declaring socket, input stream, output stream, and handler
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private final Handler mHandler;

    public ConnectedThread(BluetoothSocket socket, Handler handler){
        mmSocket = socket;
        mHandler = handler;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        //get the streams
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch(IOException e){
                //Maybe handle later
            }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    @Override
    public void run(){
        byte[] buffer = new byte[1024]; //1024 byte buffer for stream
        int bytes; //bytes to be returned from a read
        //while listening to input stream until exception occurs
        while (true) {
            try {
                //read input stream
                bytes = mmInStream.available();
                if(bytes!=0){
                    buffer = new byte[1024]; //reset buffer
                    SystemClock.sleep(100); //wait for rest of data based on send speed
                    bytes = mmInStream.available(); //how many bytes are ready?
                    bytes = mmInStream.read(buffer, 0, bytes); //record how many bytes we read
                    mHandler.obtainMessage(BluetoothActivity.MESSAGE_READ, bytes, -1, buffer).sendToTarget(); //send bytes to activity
                }

            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    public void write(String input){
        byte[] bytes = input.getBytes();
        try {
            Log.d("Bluetooth", "Sending Data" + bytes.toString());
            mmOutStream.write(bytes);
            mmOutStream.flush(); //Attempting to force bytes to write to stream for possibly speed up?
        } catch (IOException e){
            //do something later to handle
        }
    }

    public void cancel(){
        try {
            mmSocket.close();
        } catch(IOException e){
            // do something later
        }
    }
}
