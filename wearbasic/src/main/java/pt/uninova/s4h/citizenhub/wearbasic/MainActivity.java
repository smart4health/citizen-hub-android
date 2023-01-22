package pt.uninova.s4h.citizenhub.wearbasic;

import android.Manifest;
import android.content.BroadcastReceiver;
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

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import pt.uninova.s4h.citizenhub.R;
import pt.uninova.s4h.citizenhub.data.Device;
import pt.uninova.s4h.citizenhub.data.HeartRateMeasurement;
import pt.uninova.s4h.citizenhub.data.Measurement;
import pt.uninova.s4h.citizenhub.data.PostureMeasurement;
import pt.uninova.s4h.citizenhub.data.PostureValue;
import pt.uninova.s4h.citizenhub.data.Sample;
import pt.uninova.s4h.citizenhub.data.StepsSnapshotMeasurement;
import pt.uninova.s4h.citizenhub.persistence.repository.HeartRateMeasurementRepository;
import pt.uninova.s4h.citizenhub.persistence.repository.SampleRepository;
import pt.uninova.s4h.citizenhub.persistence.repository.StepsSnapshotMeasurementRepository;

public class MainActivity extends FragmentActivity {

    //TODO: remake communication with phone (use TAGS (check examples) -> samples)
    //TODO: remake phone communication with watch
    //TODO: Use sync worker to sync to phone
    //TODO: still testing -> day change

    private SensorManager sensorManager;
    private Sensor stepsCounterSensor, heartSensor;
    private SensorEventListener stepsEventListener, heartRateEventListener;
    private boolean sensorsMeasuring = true, firstTime = true;
    private long lastHeartRate;
    private TextView heartRateText, stepsText, sensorsAreMeasuring;
    private ImageView heartRateIcon, citizenHubIcon, stepsIcon, citizenHubNameLogo;
    public StepsSnapshotMeasurementRepository stepsSnapshotMeasurementRepository;
    public HeartRateMeasurementRepository heartRateMeasurementRepository;
    private SampleRepository sampleRepository;
    private SharedPreferences sharedPreferences;
    private  Device wearDevice;
    private String nodeIdString;
    public ArrayList<Integer> currentHRMeasurements = new ArrayList<>();

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
        startListeners(true, true);
        startWorkers();

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

    private void startWorkers(){
        WorkManager workManager = WorkManager.getInstance(getApplicationContext());
        PeriodicWorkRequest syncRequest = new PeriodicWorkRequest.Builder(SyncWorker.class, Duration.ofMinutes(15))
                .build();
        workManager.enqueue(syncRequest);
    }

    private void setViews(){
        heartRateText = findViewById(R.id.textViewHeartRateValue);
        stepsText = findViewById(R.id.textViewStepsValue);
        sensorsAreMeasuring = findViewById(R.id.textViewSensorsMeasuring);
        sensorsAreMeasuring.setText("");
        heartRateIcon = findViewById(R.id.imageIconHeartRate);
        stepsIcon = findViewById(R.id.imageIconSteps);
        citizenHubIcon = findViewById(R.id.imageViewCitizenHub);
        citizenHubNameLogo = findViewById(R.id.imageViewNameLogo);

        setIconClickListeners();
    }

    private void setIconClickListeners(){
        citizenHubIcon.setOnClickListener(view -> {
            System.out.println("tapped" + " " + sensorsMeasuring);
            if (!sensorsMeasuring)
                startListeners(true, false);
        });
        citizenHubNameLogo.setOnClickListener(view -> {
            System.out.println("tapped" + " " + sensorsMeasuring);
            if (!sensorsMeasuring)
                startListeners(true, false);
        });
        heartRateIcon.setOnClickListener(view -> {
            System.out.println("tapped" + " " + sensorsMeasuring);
            if (!sensorsMeasuring)
                startListeners(true, false);
        });
        stepsIcon.setOnClickListener(view -> {
            System.out.println("tapped" + " " + sensorsMeasuring);
            if (!sensorsMeasuring)
                startListeners(true, false);
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

    public void startListeners(boolean heartRate, boolean steps) {
        if (heartRate){
            System.out.println("Starting HR Listener.");
            heartRateEventListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    sensorsAreMeasuring.setText(getString(R.string.main_activity_sensors_measuring));
                    if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
                        int heartRate = (int) event.values[0];
                        System.out.println("Heart Rate Measurement: " + heartRate);

                        heartRateText.setText(String.valueOf(heartRate));
                        heartRateIcon.setImageResource(R.drawable.ic_heart);
                        lastHeartRate = System.currentTimeMillis();
                        currentHRMeasurements.add(heartRate);
                    }
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int i) {
                }
            };
        }
        if (steps){
            System.out.println("Starting Steps Listener.");
            stepsEventListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
                        sensorsAreMeasuring.setText(getString(R.string.main_activity_sensors_measuring));
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
                        stepsText.setText(String.valueOf(steps));
                        saveStepsMeasurementLocally();
                        sendStepsMeasurementToPhoneApplication();
                    }
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int i) {
                }
            };
        }
        sensorManager.registerListener(heartRateEventListener, heartSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(stepsEventListener, stepsCounterSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorsMeasuring = true;

        startService(2);
        sensorsAreMeasuring.setVisibility(View.VISIBLE);
        sensorsAreMeasuring.setText(getString(R.string.main_activity_initializing_sensors));
    }

    @SuppressWarnings("SameParameterValue")
    private void stopListeners(boolean heartRate, boolean steps){
        int numberOfSensors = 2;
        if (heartRate){
            sensorManager.unregisterListener(heartRateEventListener);
            System.out.println("Stopped Heart Rate Listener.");
            numberOfSensors--;
            System.out.println("Storing and sending Heart Rate values.");
            saveHeartRateMeasurementLocally(currentHRMeasurements);
            sendHeartRateMeasurementToPhoneApplication(currentHRMeasurements);
            currentHRMeasurements.clear();
        }
        if (steps){
            sensorManager.unregisterListener(stepsEventListener);
            System.out.println("Stopped Steps Listener.");
            numberOfSensors--;
        }
        sensorsMeasuring = false;
        startService(numberOfSensors);
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
                    heartRateText.setText(getString(R.string.main_activity_no_measurement));
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
                System.out.println("Listeners Handling");
                int timeOn = 60000;
                if (firstTime)
                    firstTime = false;
                else if(sensorsMeasuring) {
                    stopListeners(true, false);
                    timeOn = 9 * 60000;
                }
                else
                    startListeners(true, false);
                handler.postDelayed(this, timeOn);
            }
        };
        handler.post(run);
    }

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

    private int getOffsetStepCounter() {
        return sharedPreferences.getInt("offsetStepCounter", 0);
    }

    private int getLastStepCounter() {
        return sharedPreferences.getInt("lastStepCounter", 0);
    }

    private long getDayLastStepCounter(){return sharedPreferences.getLong("dayLastStepCounter", 0); }

    private void saveStepsMeasurementLocally(){
        Sample sample = new Sample(wearDevice, new StepsSnapshotMeasurement(StepsSnapshotMeasurement.TYPE_STEPS_SNAPSHOT, getLastStepCounter() + getOffsetStepCounter()));
        sampleRepository.create(sample, sampleId -> {
        });
    }

    private void saveHeartRateMeasurementLocally(ArrayList<Integer> measurements){
        int total = 0, avg = 0;
        System.out.println("Measured values:");
        for(int i = 0; i < measurements.size(); i++)
        {
            total += measurements.get(i);
            avg = total / measurements.size();
            System.out.println(measurements.get(i));
        }
        if (avg > 0) {
            System.out.println("The Average HR is: " + avg);
            Sample sample = new Sample(wearDevice, new HeartRateMeasurement(avg));
            sampleRepository.create(sample, sampleId -> {
            });
        }
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

    private void sendStepsMeasurementToPhoneApplication(){
        int steps = getLastStepCounter() + getOffsetStepCounter();
        new SendMessage(getString(R.string.citizen_hub_path) + nodeIdString, steps + "," + new Date().getTime() + "," + StepsSnapshotMeasurement.TYPE_STEPS_SNAPSHOT).start();
    }

    public static class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("Got message from phone.");
            if (intent.hasExtra("WearOSHeartRateProtocol")) {
                System.out.println("Got message WearOSHeartRateProtocol.");
            }
            if (intent.hasExtra("WearOSStepsProtocol")) {
                System.out.println("Got message WearOSStepsProtocol.");
            }
            if (intent.hasExtra("WearOSAgent")) {
                System.out.println("Got message WearOSAgent.");
            }
            if (intent.hasExtra("WearOSConnected"))
            {
                System.out.println("Got message WearOSConnected.");
            }
        }
    }

    private void sendHeartRateMeasurementToPhoneApplication(ArrayList<Integer> measurements){
        int total = 0, avg = 0;
        for(int i = 0; i < measurements.size(); i++)
        {
            total += measurements.get(i);
            avg = total / measurements.size();

        }
        if (avg > 0)
        {
            System.out.println("The Average HR is: " + avg);
            new SendMessage(getString(R.string.citizen_hub_path) + nodeIdString, avg + "," + new Date().getTime() + "," + HeartRateMeasurement.TYPE_HEART_RATE).start();
        }
    }

    class SendMessage extends Thread {
        String path;
        String message;

        SendMessage(String p, String msg) {
            path = p;
            message = msg;
        }

        public void run() {
            Task<List<Node>> nodeListTask = Wearable.getNodeClient(getApplicationContext()).getConnectedNodes();
            try {
                Task<Node> t = Wearable.getNodeClient(getApplicationContext()).getLocalNode();
                Node n = Tasks.await(t);
                nodeIdString = n.getId();
                System.out.println("Node associated: " + n.getId() + " Message: " + message);
                List<Node> nodes = Tasks.await(nodeListTask);
                for (Node node : nodes) {
                    Task<Integer> sendMessageTask = Wearable.getMessageClient(MainActivity.this).sendMessage(node.getId(), path, message.getBytes());
                    try {
                        Tasks.await(sendMessageTask);
                    } catch (ExecutionException | InterruptedException exception) {
                        exception.printStackTrace();
                    }
                }
            } catch (ExecutionException | InterruptedException exception) {
                exception.printStackTrace();
            }
        }
    }
}