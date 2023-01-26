package pt.uninova.s4h.citizenhub.wearbasic.work;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import pt.uninova.s4h.citizenhub.R;

public class HeartRateWorker extends Worker {

    public static MutableLiveData<Integer> heartRateToSave = new MutableLiveData<>();
    public static MutableLiveData<Integer> heartRateInstant = new MutableLiveData<>();
    public static MutableLiveData<String> sensorsAreMeasuring = new MutableLiveData<>();
    private SensorEventListener heartRateEventListener;
    private SensorManager sensorManager;
    private final ArrayList<Integer> currentHRMeasurements = new ArrayList<>();

    public HeartRateWorker(
            @NonNull Context appContext,
            @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
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
        heartRateEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                sensorsAreMeasuring.postValue(getApplicationContext().getString(R.string.main_activity_sensors_measuring));
                if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
                    int heartRate = (int) event.values[0];
                    currentHRMeasurements.add(heartRate);
                    heartRateInstant.postValue(heartRate);
                }
            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        };

        sensorManager = ((SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE));
        Sensor heartSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        sensorManager.registerListener(heartRateEventListener, heartSensor, SensorManager.SENSOR_DELAY_FASTEST);
        Handler handler = new Handler(Looper.getMainLooper());
        Runnable run = this::stopListener;
        handler.postDelayed(run, 60000);
    }

    private void stopListener(){
        sensorManager.unregisterListener(heartRateEventListener);
        int total = 0, avg = 0;
        for(int i = 0; i < currentHRMeasurements.size(); i++)
        {
            total += currentHRMeasurements.get(i);
            avg = total / currentHRMeasurements.size();
        }
        if (avg > 0) {
            heartRateToSave.postValue(avg);
            heartRateInstant.postValue(avg);
        }
        currentHRMeasurements.clear();
        sensorsAreMeasuring.postValue(getApplicationContext().getString(R.string.main_activity_sensors_idle));
    }
}