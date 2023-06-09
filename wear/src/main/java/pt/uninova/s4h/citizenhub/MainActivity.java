package pt.uninova.s4h.citizenhub;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import pt.uninova.s4h.citizenhub.data.Device;
import pt.uninova.s4h.citizenhub.data.HeartRateMeasurement;
import pt.uninova.s4h.citizenhub.data.Sample;
import pt.uninova.s4h.citizenhub.data.StepsSnapshotMeasurement;
import pt.uninova.s4h.citizenhub.persistence.repository.HeartRateMeasurementRepository;
import pt.uninova.s4h.citizenhub.persistence.repository.SampleRepository;
import pt.uninova.s4h.citizenhub.persistence.repository.StepsSnapshotMeasurementRepository;
import pt.uninova.s4h.citizenhub.ui.ScreenSlidePagerAdapter;
import pt.uninova.s4h.citizenhub.ui.ZoomOutPageTransformer;

public class MainActivity extends FragmentActivity {

    public static String nodeIdString;
    public static StepsSnapshotMeasurementRepository stepsSnapshotMeasurementRepository;
    public static HeartRateMeasurementRepository heartRateMeasurementRepository;
    private Device wearDevice;
    private static final String citizenHubPath = "/citizenhub_";
    public static int stepsTotal = 0;
    public static SensorManager sensorManager;
    public static Sensor stepsCounterSensor, heartSensor;
    private ViewPager2 viewPager;
    static MutableLiveData<String> listenHeartRate = new MutableLiveData<>();
    static MutableLiveData<String> listenHeartRateAverage = new MutableLiveData<>();
    static MutableLiveData<String> listenSteps = new MutableLiveData<>();
    static MutableLiveData<Boolean> protocolHeartRate = new MutableLiveData<>();
    static MutableLiveData<Boolean> protocolSteps = new MutableLiveData<>();
    static MutableLiveData<Boolean> protocolPhoneConnected = new MutableLiveData<>();
    static MutableLiveData<Integer> heartRateIcon = new MutableLiveData<>();
    public static SensorEventListener stepsListener, heartRateListener;
    public static SharedPreferences sharedPreferences;
    SampleRepository sampleRepository;
    DecimalFormat f = new DecimalFormat("###");
    public static long lastTimeConnected;
    long lastHeartRate = System.currentTimeMillis();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        sharedPreferences = this.getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);

        IntentFilter newFilter = new IntentFilter(Intent.ACTION_SEND);
        Receiver messageReceiver = new Receiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, newFilter);

        permissionRequest();
        sensorsManager();
        setDevice();

        sampleRepository = new SampleRepository(getApplication());
        heartRateMeasurementRepository = new HeartRateMeasurementRepository(getApplication());
        stepsSnapshotMeasurementRepository = new StepsSnapshotMeasurementRepository(getApplication());

        startListeners();

        viewPager = findViewById(R.id.viewPager);
        FragmentStateAdapter pagerAdapter = new ScreenSlidePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setPageTransformer(new ZoomOutPageTransformer());
        viewPager.setCurrentItem(2);
        viewPager.setCurrentItem(1);
        viewPager.setCurrentItem(0);

        new SendMessage(citizenHubPath + nodeIdString, "Ready");

        final LocalDate now = LocalDate.now();
        stepsSnapshotMeasurementRepository.readMaximumObserved(now, value -> {
            if (value != null)
                listenSteps.postValue(String.valueOf(value.intValue()));
            else
                listenSteps.postValue("0");
        });
        listenHeartRate.postValue("--");

        startService();
        startStateCheckTimer();
    }

    public void startService() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent serviceIntent = new Intent(getApplicationContext(), ForegroundService.class);
            serviceIntent.putExtra("inputExtra", "2 " + getString(R.string.notification_sensors_measuring));
            ContextCompat.startForegroundService(getApplicationContext(), serviceIntent);
        }, 10000);
    }

    private void startStateCheckTimer() {
        Handler handler = new Handler();
        Runnable run = new Runnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                if (currentTime > (lastHeartRate + 5 * 60000)) {
                    listenHeartRateAverage.postValue("--");
                    heartRateIcon.postValue(R.drawable.ic_heart_disconnected);
                }
                handler.postDelayed(this, 5 * 60000);
            }
        };
        handler.post(run);
    }

    public void startListeners() {
        heartRateListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
                    if (Boolean.TRUE.equals(protocolHeartRate.getValue())) {
                        checkIfConnected();

                        listenHeartRate.setValue(String.valueOf(event.values[0]));
                        new SendMessage(citizenHubPath + nodeIdString, event.values[0] + "," + new Date().getTime() + "," + HeartRateMeasurement.TYPE_HEART_RATE).start();

                        Sample sample = new Sample(wearDevice, new HeartRateMeasurement((int) event.values[0]));
                        sampleRepository.create(sample, sampleId -> {
                        });

                        listenHeartRateAverage.postValue(f.format((int) event.values[0]));
                        heartRateIcon.postValue(R.drawable.ic_heart);
                        lastHeartRate = System.currentTimeMillis();
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        };

        stepsListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
                    if (Boolean.TRUE.equals(protocolSteps.getValue())) {
                        checkIfConnected();
                        int stepCounter = (int) event.values[0];
                        final LocalDate now = LocalDate.now();

                        if (resetSteps(stepCounter)) {
                            sharedPreferences.edit().putInt("lastStepCounter", 0).apply();
                            sharedPreferences.edit().putInt("offsetStepCounter", -stepCounter).apply();
                        }

                        if (stepCounter < getLastStepCounter())
                            sharedPreferences.edit().putInt("offsetStepCounter", getLastStepCounter() + getOffsetStepCounter()).apply();
                        sharedPreferences.edit().putInt("lastStepCounter", stepCounter).apply();
                        sharedPreferences.edit().putLong("dayLastStepCounter", new Date().getTime()).apply();

                        Sample sample = new Sample(wearDevice, new StepsSnapshotMeasurement(StepsSnapshotMeasurement.TYPE_STEPS_SNAPSHOT, getLastStepCounter() + getOffsetStepCounter()));
                        sampleRepository.create(sample, sampleId -> {
                        });

                        stepsSnapshotMeasurementRepository.readMaximumObserved(now, value -> {
                            if (value != null)
                                stepsTotal = value.intValue();
                            else
                                stepsTotal = 0;
                            listenSteps.postValue(String.valueOf(stepsTotal));
                            new SendMessage(citizenHubPath + nodeIdString, stepsTotal + "," + new Date().getTime() + "," + StepsSnapshotMeasurement.TYPE_STEPS_SNAPSHOT).start();
                        });
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        };
    }

    private void checkIfConnected() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTimeConnected > (30 * 1000)) {
            protocolPhoneConnected.setValue(false);
        } else {
            protocolPhoneConnected.setValue(true);
        }
    }

    private Boolean resetSteps(int steps) {
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

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
    }

    public void permissionRequest() {
        ArrayList<String> permissions = new ArrayList<>(2);

        if (checkSelfPermission(Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.BODY_SENSORS);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACTIVITY_RECOGNITION);
            }
        }

        if (permissions.size() > 0) {
            String[] p = new String[permissions.size()];
            permissions.toArray(p);
            requestPermissions(p, 21);
        }
    }

    private void sensorsManager() {
        sensorManager = ((SensorManager) getSystemService(Context.SENSOR_SERVICE));
        heartSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        stepsCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
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

    public static class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("WearOSHeartRateProtocol")) {
                protocolHeartRate.setValue(Boolean.parseBoolean(intent.getStringExtra("WearOSHeartRateProtocol")));
                if (Boolean.parseBoolean(intent.getStringExtra("WearOSHeartRateProtocol")))
                    protocolPhoneConnected.setValue(true);
            }
            if (intent.hasExtra("WearOSStepsProtocol")) {
                protocolSteps.setValue(Boolean.parseBoolean(intent.getStringExtra("WearOSStepsProtocol")));
                if (Boolean.parseBoolean(intent.getStringExtra("WearOSStepsProtocol")))
                    protocolPhoneConnected.setValue(true);
            }
            if (intent.hasExtra("WearOSAgent")) {
                protocolPhoneConnected.setValue(Boolean.parseBoolean(intent.getStringExtra("WearOSAgent")));
            }
            if (intent.hasExtra("WearOSConnected")) {
                lastTimeConnected = System.currentTimeMillis();
            }
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

    @Override
    protected void onPause() {
        super.onPause();
    }
}