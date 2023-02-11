package com.example.robovision.bluetooth;

import android.app.Application;

public class BTBaseApplication extends Application {
    public ConnectedThread bluetoothThread;

    @Override
    public void onCreate()
    {
        super.onCreate();
        bluetoothThread = null; //holds bluetooth thread visible to everyone
    }
}
