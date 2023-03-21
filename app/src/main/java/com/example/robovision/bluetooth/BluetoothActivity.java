package com.example.robovision.bluetooth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.robovision.R;
import com.example.robovision.RemoteControl;
import com.example.robovision.VoiceControlActivity;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;

public class BluetoothActivity extends AppCompatActivity {

    private final String TAG = BluetoothActivity.class.getSimpleName(); //for logging

    //GUI components
    private TextView mBluetoothStatus;
    private TextView mReadBuffer;
    private Button mBluetoothOn;
    private Button mBluetoothOff;
    private Button mShowPairedDevices;
    private Button mDiscoverNewDevices;
    private Button mRemoteControl;
    private Button mVoiceControl;
    private Set<BluetoothDevice> mPairedDevices;
    private ArrayAdapter<String> mBluetoothArrayAdapter;
    private ListView mDevicesListView;
    private CheckBox mTestBit;

    //Bluetooth handler, worker, adapter, and socket
    private Handler mHandler; //this object will receive callbacks from BT thread
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mBluetoothSocket = null;
    private static final UUID BT_MODULE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    //BT codes for shared types between bluetooth module
    private final static int REQUEST_ENABLE_BT = 1;
    public final static int MESSAGE_READ = 2;
    private final static int CONNECTING_STATUS = 3;

    //Application class
    private BTBaseApplication mApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); //call parent function
        setContentView(R.layout.activity_bluetooth); //sets to bluetooth activity

        //Assigning GUI variables initialized above
        mBluetoothStatus = (TextView) findViewById(R.id.bluetoothStatus);
        mReadBuffer = (TextView) findViewById(R.id.readBuffer);
        mBluetoothOn = (Button) findViewById(R.id.scan);
        mBluetoothOff = (Button) findViewById(R.id.off);
        mShowPairedDevices = (Button) findViewById(R.id.PairedBtn);
        mDiscoverNewDevices = (Button) findViewById(R.id.discover);
        mRemoteControl = (Button) findViewById(R.id.remote);
        mVoiceControl = (Button) findViewById(R.id.voice_control);
        mTestBit = (CheckBox) findViewById(R.id.checkboxTestBit);

        mBluetoothArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1); //Getting list structure for UI devicelist view
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); //BT radio

        mDevicesListView = (ListView) findViewById(R.id.devicesListView);
        mDevicesListView.setAdapter(mBluetoothArrayAdapter); //setting array adapter to list view UI
        mDevicesListView.setOnItemClickListener(mDeviceClickListener);

        mApplication = (BTBaseApplication) getApplication();//Set application variable

        //Location permission request
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);


        mHandler = new Handler(Looper.getMainLooper()) {
            /**
             * Creating Handler with overridden handleMessage function
             */
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MESSAGE_READ) { //Message read returned from thread handler means a message was transmitted to phone
                    //convert msg object to string
                    String readMessage = new String((byte[]) msg.obj, StandardCharsets.UTF_8);
                    mReadBuffer.setText(readMessage);
                }

                if (msg.what == CONNECTING_STATUS) { //Connecting status message returned from thread handler means bluetooth device was connected succesfully
                    if (msg.arg1 == 1) {
                        mBluetoothStatus.setText("Connected to Device: " + msg.obj);
                        mRemoteControl.setEnabled(true); //Enable remote control button for remote control activity
                        mVoiceControl.setEnabled(true); //Enable remote control button for voice control activity
                    } else mBluetoothStatus.setText("Connection Failed");
                }
            }
        };

        if (mBluetoothArrayAdapter == null) {
            mBluetoothStatus.setText("Status: Bluetooth not found");
            Toast.makeText(getApplicationContext(), "Bluetooth device not found!", Toast.LENGTH_SHORT).show();
        } else {
            //Enable all click listeners below

            /**
             * Override of onclick listener writes "1" to BT if test bit button clicked
             * and thread is created
             */
            mTestBit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mApplication.bluetoothThread != null) {
                        mApplication.bluetoothThread.write("1");
                    }
                }
            });

            /**
             * Sets click functionality for scan button
             */

            mBluetoothOn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bluetoothOn();
                }
            });

            /**
             * Sets click functionality for off button
             */
            mBluetoothOff.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bluetoothOff();
                }
            });

            /**
             * Sets click functionality for show pair devices button
             */
            mShowPairedDevices.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listPairedDevices();
                }
            });

            /**
             * Sets click functionality for discover new devices button
             */
            mDiscoverNewDevices.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    discover();
                }
            });

            /**
             * Sets click functionality for remote control button switching activity contexts
             * to remote control activity while passing bluetooth thread.
             */
            mRemoteControl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    remoteControl();
                }
            });

            /**
             * Sets click functionality for voice control activity
             */
            mVoiceControl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    VoiceControl();
                }
            });

        }
    }

    private void bluetoothOn() {
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BT);
            mBluetoothStatus.setText("Bluetooth enabled");
            Toast.makeText(getApplicationContext(), "Bluetooth turned on", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Bluetooth is already on", Toast.LENGTH_SHORT).show();
        }
    }

    private void bluetoothOff() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mBluetoothAdapter.disable();
        mBluetoothStatus.setText("Bluetooth disabled");
        Toast.makeText(getApplicationContext(), "Bluetooth turned off", Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("MissingPermission")
    private void discover(){
        //check if device is already in discovery mode
        if(mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
            Toast.makeText(getApplicationContext(), "Discovery stopped", Toast.LENGTH_SHORT).show();
        } else {
            if(mBluetoothAdapter.isEnabled()){
                mBluetoothArrayAdapter.clear(); //clearing items from discovery list
                mBluetoothAdapter.startDiscovery();
                Toast.makeText(getApplicationContext(), "Starting discovery", Toast.LENGTH_SHORT).show();
                registerReceiver(blReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            }
            else {
                Toast.makeText(getApplicationContext(), "Bluetooth is not turned on", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void listPairedDevices(){
        mBluetoothArrayAdapter.clear();
        mPairedDevices = mBluetoothAdapter.getBondedDevices();
        if(mBluetoothAdapter.isEnabled()) {
            // put it's one to the adapter
            for (BluetoothDevice device : mPairedDevices)
                mBluetoothArrayAdapter.add(device.getName() + "\n" + device.getAddress());

            Toast.makeText(getApplicationContext(), "Show Paired Devices", Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(getApplicationContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
    }

    /**
     * Switch to remote control activity function
     */
    private void remoteControl(){
        Intent intent = new Intent(getApplicationContext(), RemoteControl.class);
        startActivity(intent);
    }

    private void VoiceControl(){
        Intent intent = new Intent(getApplicationContext(), VoiceControlActivity.class);
        startActivity(intent);
    }

    final BroadcastReceiver blReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mBluetoothArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                mBluetoothArrayAdapter.notifyDataSetChanged();

            }
        }
    };

    /**
     * User lead here after they choose to or not to enable bluetooth by prompt
     * @param requestCode
     * @param resultCode
     * @param Data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent Data) {
        //Maybe add super call
        super.onActivityResult(requestCode, resultCode, Data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                mBluetoothStatus.setText("Enabled");
            } else mBluetoothStatus.setText("Disabled");
        }
    }



    /**
     * This whole class provides the connection to a new bluetooth device and all calls and
     * setups necessary to establish the connection. The onItemClick function seen below is called
     * when a Item is clicked in the bluetooth devices list.
     */
    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        /**
         * An override of the OnItemClickListener object changing onItemClick method to fit our
         * purpose displaying "toast" messages on item click if we can connect or not connect
         * @param parent
         * @param view
         * @param position
         * @param id
         */
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            if(!mBluetoothAdapter.isEnabled()) {
                Toast.makeText(getBaseContext(), "Please Enable Bluetooth", Toast.LENGTH_SHORT).show();
            }

            mBluetoothStatus.setText("Connecting...");

            /**
             * Our devices Mac address is the last 17 characters in the View hence
             * we grab the view as a string and grab the substring of the last 17 chars
             */
            String info = ((TextView) view).getText().toString();
            final String address = info.substring(info.length() - 17);
            final String name    = info.substring(0,info.length() - 17);

            /**
             * New thread so GUI is not blocked by our intensive connecting task
             * Overriding threads run function to add bluetooth functionality
             */
            new Thread() {

                @Override
                public void run() {
                    boolean fail = false;

                    //setting address to the mac address we grabbed earlier
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

                    //create socket
                    try {
                        mBluetoothSocket = createBluetoothSocket(device);
                    } catch (IOException e) {
                        fail = true;
                        Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                    }

                    //establish socket connection
                    try {
                        mBluetoothSocket.connect();
                    } catch(IOException e) {

                        try {
                            fail = true;
                            mBluetoothSocket.close();
                            mHandler.obtainMessage(CONNECTING_STATUS, -1, -1).sendToTarget();
                        } catch (IOException e2) {
                            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                    if(!fail) {
                        mApplication.bluetoothThread = new ConnectedThread(mBluetoothSocket, mHandler);
                        mApplication.bluetoothThread.start();

                        mHandler.obtainMessage(CONNECTING_STATUS, 1, -1, name).sendToTarget();
                    }

                }
            }.start();

        }
    };

    /**
     * Function creates a secure rf broadcast or "bluetooth socket" connection
     * @param device Is a BlueTooth device initalized earlier
     * @return A bluetooth socket connection
     * @throws IOException if secure connection cannot be established
     */
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        try {
            final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
            return (BluetoothSocket) m.invoke(device, BT_MODULE_UUID);
        } catch(Exception e) {
            Log.e(TAG, "Could not create RFComm Connection", e);
        }
        return device.createRfcommSocketToServiceRecord(BT_MODULE_UUID);
    }


}
