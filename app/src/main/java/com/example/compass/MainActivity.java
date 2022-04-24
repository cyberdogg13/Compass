package com.example.compass;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener, AdapterView.OnItemSelectedListener {

    private ImageView imageview;
    private float[] mgravity = new float[3];
    private float[] mgeomatic = new float[3];
    private float azimuth, currentazimuth = 0f;
    private SensorManager msensormanager;
    private RelativeLayout mainscreen;
    private TextView styletext, Bgtext, longitudetext, lattitudetext, locationtext;
    private Spinner spinner, Bgspinner;
    private Button locationbutton, mapbutton;
    private String currentlongitude, currentlattitude;
    FusedLocationProviderClient fusedLocationProviderClient;

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationbutton = findViewById(R.id.button);
        mapbutton = findViewById(R.id.button2);
        longitudetext = findViewById(R.id.textlongitude);
        lattitudetext = findViewById(R.id.textalttitude);
        locationtext = findViewById(R.id.textadress);
        styletext = findViewById(R.id.spinnertitle);
        Bgtext = findViewById(R.id.spinner2title);
        spinner = findViewById(R.id.stylemenu);
        Bgspinner = findViewById(R.id.Bgmenu);
        imageview = (ImageView) findViewById(R.id.compass);
        msensormanager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mainscreen = (RelativeLayout) findViewById(R.id.mainScreen);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        mapbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentlongitude != null){
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("geo:" + currentlattitude + "," + currentlongitude ));
                    Intent chooser = Intent.createChooser(intent, "open maps");
                    startActivity(chooser);
                }
            }
        });
        locationbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    getlocation();
                    locationbutton.setBackgroundColor(getResources().getColor(R.color.green));
                    mapbutton.setBackgroundColor(getResources().getColor(R.color.green));
                    lattitudetext.setVisibility(View.VISIBLE);
                    longitudetext.setVisibility(View.VISIBLE);
                    locationtext.setVisibility(View.VISIBLE);
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
                }
            }
        });

        ArrayAdapter<CharSequence> Bgadapter = ArrayAdapter.createFromResource(this,
                R.array.backgrounds, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.compasses, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Bgadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        Bgspinner.setAdapter(Bgadapter);
        spinner.setOnItemSelectedListener(this);
        Bgspinner.setOnItemSelectedListener(this);


    }

    @SuppressLint("MissingPermission")
    private void getlocation() {
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                if (location != null) {
                    try {
                        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                        List<Address> addresses =
                                geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        lattitudetext.setText("Lattitude: " + addresses.get(0).getLatitude());
                        currentlattitude = String.valueOf(addresses.get(0).getLatitude());
                        longitudetext.setText("Longitude: " + addresses.get(0).getLongitude());
                        currentlongitude = String.valueOf(addresses.get(0).getLongitude());
                        locationtext.setText("Location: " + addresses.get(0).getAddressLine (0));

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        msensormanager.registerListener(this, msensormanager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_GAME);
        msensormanager.registerListener(this, msensormanager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        msensormanager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        final float alpha = 0.97f;
        synchronized (this) {
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                mgravity[0] = alpha * mgravity[0] + (1 - alpha) * sensorEvent.values[0];
                mgravity[1] = alpha * mgravity[1] + (1 - alpha) * sensorEvent.values[1];
                mgravity[2] = alpha * mgravity[2] + (1 - alpha) * sensorEvent.values[2];
            }

            if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                mgeomatic[0] = alpha * mgeomatic[0] + (1 - alpha) * sensorEvent.values[0];
                mgeomatic[1] = alpha * mgeomatic[1] + (1 - alpha) * sensorEvent.values[1];
                mgeomatic[2] = alpha * mgeomatic[2] + (1 - alpha) * sensorEvent.values[2];
            }

            float R[] = new float[9];
            float I[] = new float[9];
            boolean succes = SensorManager.getRotationMatrix(R, I, mgravity, mgeomatic);

            if (succes) {
                float oriantation[] = new float[3];
                SensorManager.getOrientation(R, oriantation);
                azimuth = (float) Math.toDegrees(oriantation[0]);
                azimuth = (azimuth + 360) % 360;
                Animation anim = new RotateAnimation(-currentazimuth, -azimuth, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                currentazimuth = azimuth;
                anim.setDuration(500);
                anim.setRepeatCount(0);
                anim.setFillAfter(true);
                imageview.startAnimation(anim);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
        String testtext = parent.getItemAtPosition(position).toString();
        if (testtext.equals("classic")) {
            imageview.setImageResource(R.drawable.compass1);
            Toast.makeText(MainActivity.this, "classic", Toast.LENGTH_SHORT).show();
        } else if (testtext.equals("modern")) {
            imageview.setImageResource(R.drawable.compass2);
            Toast.makeText(MainActivity.this, "modern", Toast.LENGTH_SHORT).show();
        } else if (testtext.equals("funky")) {
            imageview.setImageResource(R.drawable.compass3);
            Toast.makeText(MainActivity.this, "funky", Toast.LENGTH_SHORT).show();
        }
        if (testtext.equals("white")) {
            mainscreen.setBackgroundColor(Color.parseColor("#ffffff"));
            styletext.setTextColor(Color.parseColor("#000000"));
            Bgtext.setTextColor(Color.parseColor("#000000"));
            locationtext.setTextColor(Color.parseColor("#000000"));
            longitudetext.setTextColor(Color.parseColor("#000000"));
            lattitudetext.setTextColor(Color.parseColor("#000000"));
            Toast.makeText(MainActivity.this, "white", Toast.LENGTH_SHORT).show();
        } else if (testtext.equals("black")) {
            mainscreen.setBackgroundColor(Color.parseColor("#000000"));
            styletext.setTextColor(Color.parseColor("#ffffff"));
            locationtext.setTextColor(Color.parseColor("#ffffff"));
            longitudetext.setTextColor(Color.parseColor("#ffffff"));
            lattitudetext.setTextColor(Color.parseColor("#ffffff"));
            Bgtext.setTextColor(Color.parseColor("#ffffff"));
            spinner.setBackgroundColor(Color.parseColor("#ffffff"));
            Bgspinner.setBackgroundColor(Color.parseColor("#ffffff"));
            Toast.makeText(MainActivity.this, "black", Toast.LENGTH_SHORT).show();
        } else if (testtext.equals("wood")){
            mainscreen.setBackgroundResource(R.drawable.wood);
            styletext.setTextColor(Color.parseColor("#000000"));
            Bgtext.setTextColor(Color.parseColor("#000000"));
            locationtext.setTextColor(Color.parseColor("#000000"));
            longitudetext.setTextColor(Color.parseColor("#000000"));
            lattitudetext.setTextColor(Color.parseColor("#000000"));
            Toast.makeText(MainActivity.this, "wood", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }
}