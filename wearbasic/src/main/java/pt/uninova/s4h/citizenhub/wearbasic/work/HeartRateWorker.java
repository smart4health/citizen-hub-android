package pt.uninova.s4h.citizenhub.wearbasic.work;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.view.View;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import pt.uninova.s4h.citizenhub.R;

public class HeartRateWorker extends Worker {

    public static MutableLiveData<Integer> heartRateToSave = new MutableLiveData<>();
    public static MutableLiveData<Integer> heartRateInstant = new MutableLiveData<>();
    public static MutableLiveData<Long> lastHeartRate = new MutableLiveData<>();
    public static MutableLiveData<String> sensorsAreMeasuring = new MutableLiveData<>();
    private SensorEventListener heartRateEventListener;
    private SensorManager sensorManager;
    private Sensor heartSensor;
    private ArrayList<Integer> currentHRMeasurements = new ArrayList<>();

    public HeartRateWorker(
            @NonNull Context appContext,
            @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try{
            System.out.println("HeartRate Worker is doing work.");
            startListener();
            return Result.success();
        }
        catch (Throwable throwable)
        {
            System.out.println("HeartRate Worker failed to do work.");
            return Result.failure();
        }
    }

    private void startListener(){
        System.out.println("Starting HR Listener.");
        heartRateEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                sensorsAreMeasuring.postValue(getApplicationContext().getString(R.string.main_activity_sensors_measuring));
                if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
                    int heartRate = (int) event.values[0];
                    System.out.println("Heart Rate Measurement: " + heartRate);

                    heartRateText.setText(String.valueOf(heartRate)); //TODO set them as live data
                    heartRateIcon.setImageResource(R.drawable.ic_heart);
                    lastHeartRate.postValue(System.currentTimeMillis());
                    currentHRMeasurements.add(heartRate);
                    heartRateInstant.postValue(heartRate);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        };
        sensorManager = ((SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE));
        heartSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        sensorManager.registerListener(heartRateEventListener, heartSensor, SensorManager.SENSOR_DELAY_FASTEST);
        Handler handler = new Handler();
        Runnable run = new Runnable() {
            @Override
            public void run() {
                System.out.println("Finished 1 minute readings for the HR measurement.");
                stopListener();
                handler.postDelayed(this, 60000);
            }
        };
        handler.post(run);
    }

    private Result stopListener(){
        int numberOfSensors = 2;
        sensorManager.unregisterListener(heartRateEventListener);
        System.out.println("Stopped Heart Rate Listener.");
        numberOfSensors--;
        System.out.println("Storing and sending Heart Rate values.");
        saveHeartRateMeasurementLocally(currentHRMeasurements);
        sendHeartRateMeasurementToPhoneApplication(currentHRMeasurements);
        currentHRMeasurements.clear();
        sensorsMeasuring = false;
        startService(numberOfSensors);
        sensorsAreMeasuring.setVisibility(View.VISIBLE);
        sensorsAreMeasuring.setText(getString(R.string.main_activity_sensors_idle));
        heartRateToSave.postValue(0); //TODO
        return Result.success();
    }

}
