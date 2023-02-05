package pt.uninova.s4h.citizenhub.wearbasic;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.time.Duration;
import java.util.concurrent.ExecutionException;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import pt.uninova.s4h.citizenhub.R;
import pt.uninova.s4h.citizenhub.data.Device;
import pt.uninova.s4h.citizenhub.data.HeartRateMeasurement;
import pt.uninova.s4h.citizenhub.data.Sample;
import pt.uninova.s4h.citizenhub.data.StepsSnapshotMeasurement;
import pt.uninova.s4h.citizenhub.data.Tag;
import pt.uninova.s4h.citizenhub.persistence.repository.HeartRateMeasurementRepository;
import pt.uninova.s4h.citizenhub.persistence.repository.SampleRepository;
import pt.uninova.s4h.citizenhub.persistence.repository.StepsSnapshotMeasurementRepository;
import pt.uninova.s4h.citizenhub.persistence.repository.TagRepository;
import pt.uninova.s4h.citizenhub.wearbasic.service.ForegroundService;
import pt.uninova.s4h.citizenhub.wearbasic.work.HeartRateWorker;
import pt.uninova.s4h.citizenhub.wearbasic.work.StepsWorker;
import pt.uninova.s4h.citizenhub.wearbasic.work.SyncWorker;

public class MainActivity extends FragmentActivity {

    //TODO: Finish sync worker (steps done, HR doing)
    //TODO: remake phone side of communication (message is received and displayed, but timestamp may not be the original, but the current)

    private TextView heartRateText, stepsText, sensorsMeasuringMessage;
    private ImageView heartRateIcon, citizenHubIcon, stepsIcon, citizenHubNameLogo;
    public static StepsSnapshotMeasurementRepository stepsSnapshotMeasurementRepository;
    public static HeartRateMeasurementRepository heartRateMeasurementRepository;
    public static TagRepository tagRepository;
    public static SampleRepository sampleRepository;
    private Device wearDevice;
    public static String nodeIdString;
    private boolean sensorsAreMeasuring = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        permissionRequest();
        setViews();
        setIconClickListeners();
        setDevice();
        setDatabases();
        startPeriodicWorkers();
        setObservers();
        startService(2);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!sensorsAreMeasuring)
            startOneTimeWorkers();
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
        sensorsMeasuringMessage = findViewById(R.id.textViewSensorsMeasuring);
        sensorsMeasuringMessage.setText(getString(R.string.main_activity_sensors_initializing));
        heartRateIcon = findViewById(R.id.imageIconHeartRate);
        stepsIcon = findViewById(R.id.imageIconSteps);
        citizenHubIcon = findViewById(R.id.imageViewCitizenHub);
        citizenHubNameLogo = findViewById(R.id.imageViewNameLogo);
    }

    private void setIconClickListeners(){
        citizenHubIcon.setOnClickListener(view -> {
            if(!sensorsAreMeasuring)
                startOneTimeWorkers();
        });
        citizenHubNameLogo.setOnClickListener(view -> {
            if(!sensorsAreMeasuring)
                startOneTimeWorkers();
        });
        heartRateIcon.setOnClickListener(view -> {
            if(!sensorsAreMeasuring)
                startOneTimeWorkers();
        });
        stepsIcon.setOnClickListener(view -> {
            if(!sensorsAreMeasuring)
                startOneTimeWorkers();
        });
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


    private void setDatabases(){
        sampleRepository = new SampleRepository(getApplication());
        heartRateMeasurementRepository = new HeartRateMeasurementRepository(getApplication());
        stepsSnapshotMeasurementRepository = new StepsSnapshotMeasurementRepository(getApplication());
        tagRepository = new TagRepository(getApplication());
    }

    private void saveStepsMeasurementLocally(int steps) {
        Sample sample = new Sample(wearDevice, new StepsSnapshotMeasurement(StepsSnapshotMeasurement.TYPE_STEPS_SNAPSHOT, steps));
        sampleRepository.create(sample, sampleId -> tagRepository.create(sampleId, Tag.LABEL_MEASUREMENT_NOT_SYNCHRONIZED));
    }

    private void saveHeartRateMeasurementLocally(int value){
        if (value > 0) {
            Sample sample = new Sample(wearDevice, new HeartRateMeasurement(value));
            sampleRepository.create(sample, sampleId -> tagRepository.create(sampleId, Tag.LABEL_MEASUREMENT_NOT_SYNCHRONIZED));
        }
    }

    private void startPeriodicWorkers(){
        WorkManager workManager = WorkManager.getInstance(getApplicationContext());
        PeriodicWorkRequest stepsRequest = new PeriodicWorkRequest.Builder(StepsWorker.class, Duration.ofMinutes(15))
                .build();
        workManager.enqueue(stepsRequest);
        PeriodicWorkRequest heartRateRequest = new PeriodicWorkRequest.Builder(HeartRateWorker.class, Duration.ofMinutes(15))
                .build();
        workManager.enqueue(heartRateRequest);
        PeriodicWorkRequest syncRequest = new PeriodicWorkRequest.Builder(SyncWorker.class, Duration.ofMinutes(15))
                .build();
        workManager.enqueue(syncRequest);
    }

    private void startOneTimeWorkers(){
        WorkManager workManager = WorkManager.getInstance(getApplicationContext());
        OneTimeWorkRequest stepsRequest = new OneTimeWorkRequest.Builder(StepsWorker.class)
                .build();
        workManager.enqueue(stepsRequest);
        OneTimeWorkRequest heartRateRequest = new OneTimeWorkRequest.Builder(HeartRateWorker.class)
                .build();
        workManager.enqueue(heartRateRequest);
        OneTimeWorkRequest syncRequest = new OneTimeWorkRequest.Builder(SyncWorker.class)
                .build();
        workManager.enqueue(syncRequest);
    }

    private void setObservers(){
        HeartRateWorker.heartRateInstant.observeForever(s -> {
            if (!sensorsAreMeasuring)
                startService(2);
            sensorsMeasuringMessage.setText(getString(R.string.main_activity_sensors_measuring));
            sensorsAreMeasuring = true;
            heartRateIcon.setImageResource(R.drawable.ic_heart);
            heartRateText.setText(String.valueOf(s));
            System.out.println("Got HR Instant: " + s);
        });
        HeartRateWorker.heartRateToSave.observeForever(s -> {
            startService(0);
            sensorsMeasuringMessage.setText(getString(R.string.main_activity_sensors_idle));
            sensorsAreMeasuring = false;
            saveHeartRateMeasurementLocally(s);
            heartRateText.setText(String.valueOf(s));
            heartRateIcon.setImageResource(R.drawable.ic_heart_disconnected);
            System.out.println("Got HR Avg: " + s);
        });
        StepsWorker.stepsInstant.observeForever(s -> {
            if (!sensorsAreMeasuring)
                startService(2);
            sensorsMeasuringMessage.setText(getString(R.string.main_activity_sensors_measuring));
            sensorsAreMeasuring = true;
            stepsText.setText(String.valueOf(s));
            System.out.println("Got Steps Instant: " + s);
        });
        StepsWorker.stepsToSave.observeForever(s -> {
            startService(0);
            sensorsMeasuringMessage.setText(getString(R.string.main_activity_sensors_idle));
            sensorsAreMeasuring = false;
            saveStepsMeasurementLocally(s);
            stepsText.setText(String.valueOf(s));
            System.out.println("Got Steps to save: " + s);
        });
    }

    public void startService(int sensors) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent serviceIntent = new Intent(getApplicationContext(), ForegroundService.class);
            if (sensors > 0)
                serviceIntent.putExtra("inputExtra", getString(R.string.notification_sensors_measuring_active));
            else
                serviceIntent.putExtra("inputExtra", getString(R.string.notification_sensors_measuring_idle));
            ContextCompat.startForegroundService(getApplicationContext(), serviceIntent);
        }, 10000);
    }
}