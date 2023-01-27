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

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.time.Duration;
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
import pt.uninova.s4h.citizenhub.data.Sample;
import pt.uninova.s4h.citizenhub.data.StepsSnapshotMeasurement;
import pt.uninova.s4h.citizenhub.persistence.repository.HeartRateMeasurementRepository;
import pt.uninova.s4h.citizenhub.persistence.repository.SampleRepository;
import pt.uninova.s4h.citizenhub.persistence.repository.StepsSnapshotMeasurementRepository;
import pt.uninova.s4h.citizenhub.persistence.repository.TagRepository;
import pt.uninova.s4h.citizenhub.wearbasic.service.ForegroundService;
import pt.uninova.s4h.citizenhub.wearbasic.work.HeartRateWorker;
import pt.uninova.s4h.citizenhub.wearbasic.work.StepsWorker;
import pt.uninova.s4h.citizenhub.wearbasic.work.SyncWorker;

public class MainActivity extends FragmentActivity {

    //TODO: remake communication with phone (use TAGS? for synchronization)
    //TODO: test steps worker
    //confirm service runs with data from observer (number of sensors)
    //implement quick reading when user taps the screen
    //implement feedback message (sensors reading or not)

    //TODO: remake phone communication with watch
    //TODO: Use sync worker to sync to phone
    //TODO: still testing -> day change

    private boolean sensorsMeasuring = true;
    private TextView heartRateText, stepsText, sensorsAreMeasuring;
    private ImageView heartRateIcon, citizenHubIcon, stepsIcon, citizenHubNameLogo;
    public StepsSnapshotMeasurementRepository stepsSnapshotMeasurementRepository;
    public HeartRateMeasurementRepository heartRateMeasurementRepository;
    public TagRepository tagRepository;
    private SampleRepository sampleRepository;
    private Device wearDevice;
    private String nodeIdString;
    private int activeSensors = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        permissionRequest();
        setViews();
        setDevice();
        setDatabases();
        startWorkers();
        setObservers();
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
        sensorsAreMeasuring = findViewById(R.id.textViewSensorsMeasuring);
        sensorsAreMeasuring.setText("");
        heartRateIcon = findViewById(R.id.imageIconHeartRate);
        stepsIcon = findViewById(R.id.imageIconSteps);
        citizenHubIcon = findViewById(R.id.imageViewCitizenHub);
        citizenHubNameLogo = findViewById(R.id.imageViewNameLogo);
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
        sampleRepository.create(sample, sampleId -> {
            //tagRepository.create(sampleId, Tag.LABEL_MEASUREMENT_NOT_SYNCHRONIZED);
        });
    }

    private void saveHeartRateMeasurementLocally(int value){
        if (value > 0) {
            Sample sample = new Sample(wearDevice, new HeartRateMeasurement(value));
            sampleRepository.create(sample, sampleId -> {
                //tagRepository.create(sampleId, Tag.LABEL_MEASUREMENT_NOT_SYNCHRONIZED);
            });
        }
    }

    private void startWorkers(){
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

    private void setObservers(){
        HeartRateWorker.heartRateInstant.observeForever(s -> {
            startService(++activeSensors);
            System.out.println("Value from Instant HeartRate worker: " + s);
            sensorsAreMeasuring.setText(getString(R.string.main_activity_sensors_measuring));
            heartRateIcon.setImageResource(R.drawable.ic_heart);
            heartRateText.setText(String.valueOf(s));
        });
        HeartRateWorker.heartRateToSave.observeForever(s -> {
            startService(--activeSensors);
            System.out.println("Value from Average HeartRate worker: " + s);
            sensorsAreMeasuring.setText(getString(R.string.main_activity_sensors_idle));
            saveHeartRateMeasurementLocally(s);
            heartRateText.setText(String.valueOf(s));
            heartRateIcon.setImageResource(R.drawable.ic_heart_disconnected);
        });
        StepsWorker.stepsInstant.observeForever(s -> {
            startService(++activeSensors);
            System.out.println("Value from Instant Steps worker: " + s);
            sensorsAreMeasuring.setText(getString(R.string.main_activity_sensors_measuring));
            stepsText.setText(String.valueOf(s));
        });
        StepsWorker.stepsToSave.observeForever(s -> {
            startService(--activeSensors);
            System.out.println("Value from Last Steps worker: " + s);
            sensorsAreMeasuring.setText(getString(R.string.main_activity_sensors_idle));
            saveStepsMeasurementLocally(s);
            stepsText.setText(String.valueOf(s));
        });
    }

    public void startService(int sensors) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent serviceIntent = new Intent(getApplicationContext(), ForegroundService.class);
            serviceIntent.putExtra("inputExtra", getString(R.string.notification_sensors_measuring, sensors));
            ContextCompat.startForegroundService(getApplicationContext(), serviceIntent);
        }, 10000);
    }

    private void sendStepsMeasurementToPhoneApplication(int steps){
        new SendMessage(getString(R.string.citizen_hub_path) + nodeIdString, steps + "," + new Date().getTime() + "," + StepsSnapshotMeasurement.TYPE_STEPS_SNAPSHOT).start();
    }

    private void sendHeartRateMeasurementToPhoneApplication(int value){
        System.out.println("Sending HR value to phone: " + value);
        new SendMessage(getString(R.string.citizen_hub_path) + nodeIdString, value + "," + new Date().getTime() + "," + HeartRateMeasurement.TYPE_HEART_RATE).start();
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