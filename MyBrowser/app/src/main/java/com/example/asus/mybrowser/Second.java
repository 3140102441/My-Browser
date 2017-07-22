package com.example.asus.mybrowser;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class Second extends AppCompatActivity {

    private SensorManager sensorManager;

    private ImageView compass;

    private Button button_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        button_back = (Button)findViewById(R.id.button3);
        compass = (ImageView) findViewById(R.id.compass);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        Sensor magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(listener, magneticSensor, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(listener, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME);

        button_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.button3:
                      onBackPressed();
                        break;
                    default:
                        break;
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sensorManager != null) {
            sensorManager.unregisterListener(listener);
        }
    }

    private SensorEventListener listener = new SensorEventListener() {
        float[] accelerometerValues = new float[3];
        float[] magneticValues = new float[3];
        private float lastRotateDegree;

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                accelerometerValues = event.values.clone();
            } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                magneticValues = event.values.clone();
            }
            float[] R = new float[9];
            float[] values = new float[3];
            SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticValues);
            SensorManager.getOrientation(R, values);
            float rotateDegree = -(float) Math.toDegrees(values[0]);
            if (Math.abs(rotateDegree - lastRotateDegree) > 1) {
                RotateAnimation animation = new RotateAnimation(lastRotateDegree, rotateDegree, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                animation.setFillAfter(true);
                compass.startAnimation(animation);
                lastRotateDegree = rotateDegree;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
}

