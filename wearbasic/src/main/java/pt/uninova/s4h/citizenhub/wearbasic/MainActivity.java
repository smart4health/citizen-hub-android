package pt.uninova.s4h.citizenhub.wearbasic;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import pt.uninova.s4h.citizenhub.R;
import pt.uninova.s4h.citizenhub.data.Device;
import pt.uninova.s4h.citizenhub.data.HeartRateMeasurement;
import pt.uninova.s4h.citizenhub.data.Sample;
import pt.uninova.s4h.citizenhub.data.StepsSnapshotMeasurement;
import pt.uninova.s4h.citizenhub.persistence.repository.HeartRateMeasurementRepository;
import pt.uninova.s4h.citizenhub.persistence.repository.SampleRepository;
import pt.uninova.s4h.citizenhub.persistence.repository.StepsSnapshotMeasurementRepository;

public class MainActivity extends FragmentActivity {

    SensorManager sensorManager;
    Sensor stepsCounterSensor, heartSensor;
    SensorEventListener stepsEventListener, heartRateEventListener;
    boolean sensorsMeasuring, firstTime = true;
    long lastHeartRate;
    TextView heartRateText, stepsText, initializingSensors, sensorsAreMeasuring;
    ImageView heartRateIcon, citizenHubIcon;
    StepsSnapshotMeasurementRepository stepsSnapshotMeasurementRepository;
    HeartRateMeasurementRepository heartRateMeasurementRepository;
    SampleRepository sampleRepository;
    SharedPreferences sharedPreferences;
    Device wearDevice;
    String nodeIdString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        sharedPreferences = this.getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);

        permissionRequest();
        setViews();
        setDevice();
        setDatabases();
        sensorsManager();
        startListeners();

        startTimerLastHeartRate();
        listenersHandling();
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
        sensorsAreMeasuring = findViewById(R.id.textViewSensorsMeasuring);
        sensorsAreMeasuring.setVisibility(View.GONE);
        heartRateIcon = findViewById(R.id.imageIconHeartRate);
        citizenHubIcon = findViewById(R.id.imageViewCitizenHub);

        citizenHubIcon.setOnClickListener(view -> {
            if (!sensorsMeasuring)
                startListeners();
        });
    }

    private void setDatabases(){
        sampleRepository = new SampleRepository(getApplication());
        heartRateMeasurementRepository = new HeartRateMeasurementRepository(getApplication());
        stepsSnapshotMeasurementRepository = new StepsSnapshotMeasurementRepository(getApplication());
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
                initializingSensors.setVisibility(View.GONE);
                int stepCounter = (int) event.values[0];

                if (checkStepsReset(stepCounter)) {
                    sharedPreferences.edit().putInt("lastStepCounter", 0).apply();
                    sharedPreferences.edit().putInt("offsetStepCounter", -stepCounter).apply();
                }

                if (stepCounter < getLastStepCounter())
                    sharedPreferences.edit().putInt("offsetStepCounter", getLastStepCounter() + getOffsetStepCounter()).apply();
                sharedPreferences.edit().putInt("lastStepCounter", stepCounter).apply();
                sharedPreferences.edit().putLong("dayLastStepCounter", new Date().getTime()).apply();

                if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
                    stepsText.setText(String.valueOf((int) event.values[0]));
                    saveStepsMeasurementLocally();
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        };
        heartRateEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                initializingSensors.setVisibility(View.GONE);
                if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
                    heartRateText.setText(String.valueOf((int) event.values[0]));
                    heartRateIcon.setImageResource(R.drawable.ic_heart);
                    lastHeartRate = System.currentTimeMillis();
                    saveHeartRateMeasurementLocally((int) event.values[0]);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        };
        sensorManager.registerListener(heartRateEventListener, heartSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(stepsEventListener, stepsCounterSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorsMeasuring = true;

        startService(2);
        sensorsAreMeasuring.setVisibility(View.VISIBLE);
        sensorsAreMeasuring.setText(getString(R.string.main_activity_sensors_measuring));
    }

    private void stopListeners(){
        sensorManager.unregisterListener(heartRateEventListener);
        sensorManager.unregisterListener(stepsEventListener);
        sensorsMeasuring = false;

        startService(0);
        sensorsAreMeasuring.setVisibility(View.VISIBLE);
        sensorsAreMeasuring.setText(getString(R.string.main_activity_sensors_idle));
    }

    private void startTimerLastHeartRate(){
        Handler handler = new Handler();
        Runnable run = new Runnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                if(currentTime > (lastHeartRate + 5*60000))
                {
                    heartRateText.setText("--");
                    heartRateIcon.setImageResource(R.drawable.ic_heart_disconnected);
                }
                handler.postDelayed(this, 5*60000);
            }
        };
        handler.post(run);
    }

    private void listenersHandling(){
        Handler handler = new Handler();
        Runnable run = new Runnable() {
            @Override
            public void run() {
                if (firstTime)
                    firstTime = false;
                else if(sensorsMeasuring)
                    stopListeners();
                else
                    startListeners();
                handler.postDelayed(this, 5*60000);
            }
        };
        handler.post(run);
    }

    private boolean checkStepsReset(int steps){
        //TODO test this and rebooting device (if it keeps counted steps)
        long recordedDate = sharedPreferences.getLong("dayLastStepCounter", 0);
        if (recordedDate == 0) {
            if (steps > 0)
                sharedPreferences.edit().putInt("offsetStepCounter", -steps).apply();
            return false;
        }
        Date dateRecorded = new Date(recordedDate);
        Calendar calendarRecordedDate = Calendar.getInstance();
        calendarRecordedDate.setTime(dateRecorded);

        Date currentDay = new Date();
        Calendar calendarCurrentDate = Calendar.getInstance();
        calendarCurrentDate.setTime(currentDay);

        return !(calendarRecordedDate.get(Calendar.DAY_OF_YEAR) == calendarCurrentDate.get(Calendar.DAY_OF_YEAR)
                && calendarRecordedDate.get(Calendar.YEAR) == calendarCurrentDate.get(Calendar.YEAR));
    }

    private int getOffsetStepCounter() {
        return sharedPreferences.getInt("offsetStepCounter", 0);
    }

    private int getLastStepCounter() {
        return sharedPreferences.getInt("lastStepCounter", 0);
    }

    private void saveStepsMeasurementLocally(){
        Sample sample = new Sample(wearDevice, new StepsSnapshotMeasurement(StepsSnapshotMeasurement.TYPE_STEPS_SNAPSHOT, getLastStepCounter() + getOffsetStepCounter()));
        sampleRepository.create(sample, sampleId -> {
        });
    }

    private void saveHeartRateMeasurementLocally(int value){
        Sample sample = new Sample(wearDevice, new HeartRateMeasurement(value));
        sampleRepository.create(sample, sampleId -> {
        });
    }

    private void sendMeasurementToPhoneApplication(){
        //TODO, include communication check
    }

    private void setDevice() {
        new Thread(() -> {
            try {
                Node localNode = Tasks.await(Wearable.getNodeClient(getApplicationContext()).getLocalNode());
                nodeIdString = localNode.getId();
                wearDevice = new Device(nodeIdString, "WearOS Device", 2);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void startService(int sensors) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent serviceIntent = new Intent(getApplicationContext(), ForegroundService.class);
            serviceIntent.putExtra("inputExtra", getString(R.string.notification_sensors_measuring, sensors));
            ContextCompat.startForegroundService(getApplicationContext(), serviceIntent);
        }, 10000);
    }
}