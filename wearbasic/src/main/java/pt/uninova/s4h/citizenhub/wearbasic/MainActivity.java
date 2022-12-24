package pt.uninova.s4h.citizenhub.wearbasic;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import pt.uninova.s4h.citizenhub.R;

public class MainActivity extends FragmentActivity {

    SensorManager sensorManager;
    Sensor stepsCounterSensor, heartSensor;
    SensorEventListener stepsEventListener, heartRateEventListener;
    TextView heartRateText, stepsText, initializingSensors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        permissionRequest();
        setViews();
        sensorsManager();
        startListeners();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void permissionRequest() {
        if (checkSelfPermission(Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.BODY_SENSORS}, 21);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 22);
            }
        }
    }

    private void setViews(){
        heartRateText = findViewById(R.id.textViewHeartRateValue);
        stepsText = findViewById(R.id.textViewStepsValue);
        initializingSensors = findViewById(R.id.textViewInitializing);
    }

    private void sensorsManager() {
        sensorManager = ((SensorManager) getSystemService(Context.SENSOR_SERVICE));
        heartSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        stepsCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
    }

    public void startListeners() {
        stepsEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                initializingSensors.setVisibility(View.INVISIBLE);
                if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
                    stepsText.setText(String.valueOf((int) event.values[0]));
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        };
        heartRateEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                initializingSensors.setVisibility(View.INVISIBLE);
                if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
                    heartRateText.setText(String.valueOf((int) event.values[0]));
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        };
        sensorManager.registerListener(heartRateEventListener, heartSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(stepsEventListener, stepsCounterSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }
}