package pt.uninova.s4h.citizenhub;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import persistence.MeasurementKind;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

/*** Wear works with Bluetooth, ensure phone connectivity & permissions ***/

public class MainActivity extends FragmentActivity implements SensorEventListener {

    public static String nodeIdString;
    private String citizenHubPath = "/citizenhub_";
    public static int stepsTotal = 0;
    public static double heartRate = 0;
    private SensorManager mSensorManager;
    private Sensor mStepSensor, mHeartSensor;
    private FragmentStateAdapter pagerAdapter;
    private ViewPager2 viewPager;
    static MutableLiveData<String> listenHeartRate = new MutableLiveData<>();
    static MutableLiveData<String> listenSteps = new MutableLiveData<>();
    static MutableLiveData<Boolean> protocolHeartRate = new MutableLiveData<>();
    static MutableLiveData<Boolean> protocolSteps = new MutableLiveData<>();
    static MutableLiveData<Boolean> protocolPhoneConnected = new MutableLiveData<>();

    private void sensorsManager() {
        mSensorManager = ((SensorManager) getSystemService(Context.SENSOR_SERVICE));
        mHeartSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        mStepSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        IntentFilter newFilter = new IntentFilter(Intent.ACTION_SEND);
        Receiver messageReceiver = new Receiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, newFilter);

        permissionRequest();
        sensorsManager();

        viewPager = findViewById(R.id.viewPager);
        pagerAdapter = new ScreenSlidePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setPageTransformer(new ZoomOutPageTransformer());
    }

    @Override
    protected void onResume() {
        super.onResume();

        new Thread() {
            @Override
            public void run() {
                try {
                    Node localNode = Tasks.await(Wearable.getNodeClient(getApplicationContext()).getLocalNode());
                    nodeIdString = localNode.getId();
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }

                if (mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null) {
                    mSensorManager.registerListener(MainActivity.this, mStepSensor, mSensorManager.SENSOR_DELAY_NORMAL);
                }
                if (mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE) != null) {
                    mSensorManager.registerListener(MainActivity.this, mHeartSensor, mSensorManager.SENSOR_DELAY_NORMAL);
                }
            }
        }.start();
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
    }

    public void permissionRequest() {
        if (checkSelfPermission(Manifest.permission.BODY_SENSORS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.BODY_SENSORS},
                    21);
            Log.d("Permissions", "REQUESTED");
        } else {
            Log.d("Permissions", "ALREADY GRANTED");
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Date now = new Date();
        double value = event.values[0];
        MeasurementKind kind;
        String msg = "";

        switch (event.sensor.getType()) {

            case Sensor.TYPE_HEART_RATE:
                kind = MeasurementKind.HEART_RATE;
                listenHeartRate.setValue(getString(R.string.show_data_heartrate, value));
                msg = value + "," + now.getTime() + "," + kind.getId();
                break;
            case Sensor.TYPE_STEP_DETECTOR:
                kind = MeasurementKind.STEPS;
                listenSteps.setValue(getString(R.string.show_data_steps, (stepsTotal+= value)));
                msg = stepsTotal + "," + now.getTime() + "," + kind.getId();
                break;
        }
        String dataPath = citizenHubPath + nodeIdString;
        new SendMessage(dataPath, msg).start();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    public class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("WearOSHeartRateProtocol")){
                protocolHeartRate.setValue(Boolean.parseBoolean(intent.getStringExtra("WearOSHeartRateProtocol")));
            }
            if (intent.hasExtra("WearOSStepsProtocol")){
                protocolSteps.setValue(Boolean.parseBoolean(intent.getStringExtra("WearOSStepsProtocol")));
            }
            if (intent.hasExtra("WearOSAgent")){
                protocolPhoneConnected.setValue(Boolean.parseBoolean(intent.getStringExtra("WearOSHeartRateProtocol")));
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
                System.out.println("Node associated: " + n.getId() + " Message: " + message);
                List<Node> nodes = Tasks.await(nodeListTask);
                for (Node node : nodes) {
                    Task<Integer> sendMessageTask =
                            Wearable.getMessageClient(MainActivity.this).sendMessage(node.getId(), path, message.getBytes());
                    try {
                        Integer result = Tasks.await(sendMessageTask);
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