package com.example.robovision;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class gps<mDatabase> extends AppCompatActivity implements LocationListener {
    private double latitude, longitude, altitude,accuracy,speed;
    private TextView txtLatitude, txtLongitude,txtaltitude,txtaccuracy,txtspeed,sw_updates1;

    Switch sw_locationupdates;

    LocationManager locationManager;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("message");





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_gps);

        txtLatitude = (TextView) findViewById(R.id.txtLat);
        txtLongitude = (TextView) findViewById(R.id.txtLong);
        txtaltitude = (TextView) findViewById(R.id.txtalt);
        txtaccuracy = (TextView) findViewById(R.id.txtacc);
        txtspeed = (TextView) findViewById(R.id.txtsp);
        //sw_updates1 = (TextView) findViewById(R.id.sw_updates1);
        //sw_locationupdates = findViewById(R.id.sw_locationupdates);
      //  sw_locationupdates.setOnClickListener(new View.OnClickListener(){
       //     @Override
        //    public void onClick(View v) {
         //       if (sw_locationupdates.isChecked()){
         //           startLocationupdates();                 //on
         //       }
         //       else{
         //           stopLocationupdates();                 //off

         //       }
         //   }


        //});




        locationManager = (LocationManager)  getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(gps.this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(gps.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,0, this);




    }


    //private void stopLocationupdates() {
    //    sw_updates1.setText("GPS off");


    //}

    //private void startLocationupdates() {
    //    sw_updates1.setText("GPS on");

    //}
    

    @Override
    public void onLocationChanged(@NonNull Location location) {
        Log.d("GPS","Location changed, obtaining new coordinates");
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        altitude = location.getAltitude();
        accuracy = location.getAccuracy();
        speed = location.getSpeed();


        txtLatitude.setText(String.valueOf(latitude));
        txtLongitude.setText(String.valueOf(longitude));
        txtaltitude.setText(String.valueOf(altitude));
        txtaccuracy.setText(String.valueOf(accuracy));
        txtspeed.setText(String.valueOf(speed));

        //save battery
        //locationManager.removeUpdates(this);
        String msg ="GPS( " +
                Double.toString(location.getLatitude()) + "," +Double.toString(location.getLongitude())
                + ")";
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

        gpsfirebase fb = new gpsfirebase(
                location.getLatitude(),
                location.getLongitude()
        );
        FirebaseDatabase.getInstance().getReference("GPS location")
                .setValue(fb).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(gps.this, "Location Updated", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(gps.this, "Unable to Update the location", Toast.LENGTH_SHORT).show();
                }
            }
       });
    }





    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }
}

