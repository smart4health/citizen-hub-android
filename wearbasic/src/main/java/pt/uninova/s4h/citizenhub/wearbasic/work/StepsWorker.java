package pt.uninova.s4h.citizenhub.wearbasic.work;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;

import java.util.Calendar;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class StepsWorker extends Worker {

    public static MutableLiveData<Integer> stepsInstant = new MutableLiveData<>();
    public static MutableLiveData<Integer> stepsToSave = new MutableLiveData<>();
    private SensorEventListener stepsEventListener;
    private SensorManager sensorManager;
    private SharedPreferences sharedPreferences;


    public StepsWorker(
            @NonNull Context appContext,
            @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try{
            System.out.println("Steps Worker is doing work.");
            sharedPreferences = getApplicationContext().getSharedPreferences(getApplicationContext().getPackageName(), Context.MODE_PRIVATE);
            startListener();
            return Result.success();
        }
        catch (Throwable throwable)
        {
            System.out.println("Steps Worker failed to do work.");
            return Result.failure();
        }
    }

    private void startListener(){
        stepsEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
                    int stepCounter = (int) event.values[0];
                    int steps = getLastStepCounter() + getOffsetStepCounter();
                    System.out.println("Before calculations: Step Counter: " + stepCounter + " | lastStepCounter: " + getLastStepCounter()
                            + " | offsetStepCounter: " + getOffsetStepCounter() + " | dayLastStepCounter: " + getDayLastStepCounter()
                            + " | Steps to show: " + steps);

                    if (checkStepsReset(stepCounter)) {
                        sharedPreferences.edit().putInt("lastStepCounter", 0).apply();
                        sharedPreferences.edit().putInt("offsetStepCounter", -stepCounter).apply();
                    }

                    if (stepCounter < getLastStepCounter())
                        sharedPreferences.edit().putInt("offsetStepCounter", getLastStepCounter() + getOffsetStepCounter()).apply();
                    sharedPreferences.edit().putInt("lastStepCounter", stepCounter).apply();
                    sharedPreferences.edit().putLong("dayLastStepCounter", new Date().getTime()).apply();

                    steps = getLastStepCounter() + getOffsetStepCounter();
                    System.out.println("Before calculations: Step Counter: " + stepCounter + " | lastStepCounter: " + getLastStepCounter()
                            + " | offsetStepCounter: " + getOffsetStepCounter() + " | dayLastStepCounter: " + getDayLastStepCounter()
                            + " | Steps to show: " + steps);
                    stepsInstant.postValue(steps);
                }
            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        };

        sensorManager = ((SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE));
        Sensor stepsSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        sensorManager.registerListener(stepsEventListener, stepsSensor, SensorManager.SENSOR_DELAY_FASTEST);
        Handler handler = new Handler(Looper.getMainLooper());
        Runnable run = this::stopListener;
        handler.postDelayed(run, 60000);
    }

    private void stopListener(){
        sensorManager.unregisterListener(stepsEventListener);
        stepsInstant.postValue(getLastStepCounter() + getOffsetStepCounter());
        stepsToSave.postValue(getLastStepCounter() + getOffsetStepCounter());
    }

    private int getOffsetStepCounter() {
        return sharedPreferences.getInt("offsetStepCounter", 0);
    }

    private int getLastStepCounter() {
        return sharedPreferences.getInt("lastStepCounter", 0);
    }

    private long getDayLastStepCounter(){return sharedPreferences.getLong("dayLastStepCounter", 0); }

    private boolean checkStepsReset(int steps){
        long recordedDate = getDayLastStepCounter();
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
}
