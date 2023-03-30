package pt.uninova.s4h.citizenhub.service;

import static pt.uninova.s4h.citizenhub.connectivity.Connection.CONNECTION_KIND_BLUETOOTH;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LifecycleService;
import androidx.preference.PreferenceManager;
import androidx.work.WorkManager;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import care.data4life.sdk.Data4LifeClient;
import care.data4life.sdk.lang.D4LException;
import care.data4life.sdk.listener.ResultListener;
import pt.uninova.s4h.citizenhub.MainActivity;
import pt.uninova.s4h.citizenhub.R;
import pt.uninova.s4h.citizenhub.WorkTimeRangeConverter;
import pt.uninova.s4h.citizenhub.connectivity.Agent;
import pt.uninova.s4h.citizenhub.connectivity.AgentFactory;
import pt.uninova.s4h.citizenhub.connectivity.AgentListener;
import pt.uninova.s4h.citizenhub.connectivity.AgentOrchestrator;
import pt.uninova.s4h.citizenhub.connectivity.AgentOrchestratorListener;
import pt.uninova.s4h.citizenhub.connectivity.Connection;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.BluetoothAgent;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.BluetoothAgentFactory;
import pt.uninova.s4h.citizenhub.connectivity.wearos.WearOsAgentFactory;
import pt.uninova.s4h.citizenhub.data.Device;
import pt.uninova.s4h.citizenhub.data.Measurement;
import pt.uninova.s4h.citizenhub.data.Sample;
import pt.uninova.s4h.citizenhub.data.Tag;
import pt.uninova.s4h.citizenhub.persistence.entity.DeviceRecord;
import pt.uninova.s4h.citizenhub.persistence.entity.StreamRecord;
import pt.uninova.s4h.citizenhub.persistence.repository.DeviceRepository;
import pt.uninova.s4h.citizenhub.persistence.repository.SampleRepository;
import pt.uninova.s4h.citizenhub.persistence.repository.StreamRepository;
import pt.uninova.s4h.citizenhub.persistence.repository.TagRepository;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;
import pt.uninova.s4h.citizenhub.work.WorkOrchestrator;

public class CitizenHubService extends LifecycleService {

    public class Binder extends android.os.Binder {
        public CitizenHubService getService() {
            return CitizenHubService.this;
        }
    }

    public static void bind(Context context, ServiceConnection connection) {
        final Intent intent = new Intent(context, CitizenHubService.class);

        context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    public static void start(Context context) {
        final Intent intent = new Intent(context, CitizenHubService.class);

        context.startForegroundService(intent);
    }

    public static void stop(Context context) {
        final Intent intent = new Intent(context, CitizenHubService.class);

        context.stopService(intent);
    }

    public static void unbind(Context context, ServiceConnection connection) {
        context.unbindService(connection);
    }

    private final static CharSequence NOTIFICATION_TITLE = "Citizen Hub";
    private AgentOrchestrator orchestrator;
    private NotificationManager notificationManager;
    private WearOSMessageService wearOSMessageService;

    private final IBinder binder;

    private WorkOrchestrator workOrchestrator;

    private SharedPreferences preferences;
    private int devices;

    public CitizenHubService() {
        this.binder = new Binder();
    }

    private Notification buildNotification(int devicesConnected, int totalDevices) {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(this, Objects.requireNonNull(CitizenHubService.class.getCanonicalName()))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(NOTIFICATION_TITLE)
                .setOnlyAlertOnce(true)
                .setContentText(String.format(getString(R.string.service_notification_message), devicesConnected, totalDevices))
                .build();

        notification.contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        return notification;
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(CitizenHubService.class.getCanonicalName(), NOTIFICATION_TITLE, NotificationManager.IMPORTANCE_DEFAULT);

        notificationManager.createNotificationChannel(channel);
    }

    public AgentOrchestrator getAgentOrchestrator() {
        return orchestrator;
    }

    public SharedPreferences getPreferences() {
        return preferences;
    }

    public WearOSMessageService getWearOSMessageService() {
        return wearOSMessageService;
    }

    private Double parseMeasurementValue(Measurement<?> measurement) throws Exception {
        final Class<?> c = measurement.getValue().getClass();

        if (c == Integer.class) {
            return Double.valueOf((Integer) measurement.getValue());
        } else if (c == Double.class || c == Float.class) {
            return (Double) measurement.getValue();
        }

        throw new Exception();
    }

    private int getConnectedDevices() {
        int i = 0;
        for (Device device : orchestrator.getDevices()
        ) {
            System.out.println("GETCONNECTEDDEVICES_TOTAL " + orchestrator.getDevices(CONNECTION_KIND_BLUETOOTH).size());
            if (orchestrator.getAgent(device) != null) {
                if (orchestrator.getAgent(device).getState() == Agent.AGENT_STATE_ENABLED) {
                    i++;

                    System.out.println("CONNECTED DEVICE " + orchestrator.getAgent(device).getName() + i);

                } else {
                    System.out.println("NOT CONNECTED DEVICE " + orchestrator.getAgent(device).getName() + i);

                }
            }
        }
        System.out.println("FINAL DEVICE COUNT " + (orchestrator.getDevices(CONNECTION_KIND_BLUETOOTH).size() - i));
        return i;
    }

    private void initAgentOrchestrator() {
        final Map<Integer, AgentFactory<? extends Agent>> agentFactoryMap = new HashMap<>();
        final DeviceRepository deviceRepository = new DeviceRepository(getApplication());
        final StreamRepository streamRepository = new StreamRepository(getApplication());
        final SampleRepository sampleRepository = new SampleRepository(getApplication());
        final TagRepository tagRepository = new TagRepository(getApplication());

        agentFactoryMap.put(Connection.CONNECTION_KIND_BLUETOOTH, new BluetoothAgentFactory(this));
        agentFactoryMap.put(Connection.CONNECTION_KIND_WEAROS, new WearOsAgentFactory(this));

        final Observer<Sample> databaseWriter = sample -> {
            final WorkTimeRangeConverter workTimeRangeConverter = WorkTimeRangeConverter.getInstance(getApplicationContext());

            sampleRepository.create(sample, sampleId -> {
                if (workTimeRangeConverter.isWorkTime(LocalDateTime.ofInstant(sample.getTimestamp(), ZoneId.systemDefault()))) {
                    tagRepository.create(sampleId, Tag.LABEL_CONTEXT_WORK);
                }

                final int type = sample.getMeasurements()[0].getType();

                if (type == Measurement.TYPE_LUMBAR_EXTENSION_TRAINING || type == Measurement.TYPE_BLOOD_PRESSURE) {
                    Data4LifeClient.getInstance().isUserLoggedIn(new ResultListener<Boolean>() {
                        @Override
                        public void onSuccess(Boolean aBoolean) {
                            if (aBoolean) {
                                if (type == Measurement.TYPE_LUMBAR_EXTENSION_TRAINING && preferences.getBoolean("account.smart4health.report.data.lumbar-extension-training", true)) {
                                    workOrchestrator.enqueueSmart4HealthUniqueWorkLumbarExtension(getApplicationContext(), sampleId);
                                } else if (type == Measurement.TYPE_BLOOD_PRESSURE && preferences.getBoolean("account.smart4health.report.data.blood-pressure", true)) {
                                    workOrchestrator.enqueueSmart4HealthUniqueWorkBloodPressure(getApplicationContext(), sampleId);
                                }
                            }
                        }

                        @Override
                        public void onError(@NonNull D4LException e) {
                            e.printStackTrace();
                        }
                    });
                }
            });
        };

        orchestrator = new AgentOrchestrator(agentFactoryMap, databaseWriter);
        orchestrator.addListener(new AgentOrchestratorListener() {
            @Override
            public void onAgentStateChanged(Agent agent) {
                System.out.println("AGENT STATE CHANGED PLS UPDATE NOTIFICATION");
                updateNotification(getConnectedDevices());
            }

            @Override
            public void onAgentAttached(Device device, Agent agent) {
                deviceRepository.updateAgent(device.getAddress(), agent.getClass().getCanonicalName());

                agent.enable();

                agent.addAgentListener(new AgentListener() {
                    @Override
                    public void onMeasurementDisabled(Agent agent, int measurementType) {
                        streamRepository.delete(agent.getSource().getAddress(), measurementType);
                    }

                    @Override
                    public void onMeasurementEnabled(Agent agent, int measurementType) {
                        streamRepository.create(agent.getSource().getAddress(), measurementType);
                    }
                });
            }

            @Override
            public void onDeviceAdded(Device device) {
                deviceRepository.create(new DeviceRecord(null, device.getAddress(), device.getName(), device.getConnectionKind(), null));
            }

            @Override
            public void onDeviceRemoved(Device device) {
                final Agent agent = getAgentOrchestrator().getAgent(device);

                if (agent != null) {
                    agent.removeAllAgentListeners();

                }
                deviceRepository.delete(device.getAddress());

            }
        });

        deviceRepository.read(value -> {
            for (DeviceRecord i : value) {
                final Observer<Agent> agentObserver = (agent) -> {
                    streamRepository.read(i.getAddress(), enabledMeasurements -> {
                        for (final StreamRecord j : enabledMeasurements) {
                            agent.enableMeasurement(j.getMeasurementType());
                        }
                    });
                };

                try {
                    String agentName = i.getAgent();

                    if (agentName == null) {
                        orchestrator.add(new Device(i.getAddress(), i.getName(), i.getConnectionKind()), agentObserver);
                    } else {
                        orchestrator.add(new Device(i.getAddress(), i.getName(), i.getConnectionKind()), Class.forName(agentName).asSubclass(Agent.class), agentObserver);
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        super.onBind(intent);

        return binder;
    }

    private void updateNotification(int devices) {
        Notification notification;
        if (orchestrator.getDevices() != null) {
            notification = buildNotification(devices, orchestrator.getDevices().size());
        } else {
            notification = buildNotification(devices, 0);

        }
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, notification);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();

        startForeground(1, buildNotification(0, 0));
        wearOSMessageService = new WearOSMessageService();

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());


        IntentFilter bleFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bleStateReceiver, bleFilter);


        initAgentOrchestrator();
        updateNotification(getConnectedDevices());
        initWorkOrchestrator();

    }

    private void initWorkOrchestrator() {
        workOrchestrator = new WorkOrchestrator(WorkManager.getInstance(this));
    }

    @Override
    public void onDestroy() {
        orchestrator.clear();
        unregisterReceiver(bleStateReceiver);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        return START_STICKY;
    }

    public WearOSMessageService getService() {
        return wearOSMessageService;
    }

    private final BroadcastReceiver bleStateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_ON:
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                enableAll(CONNECTION_KIND_BLUETOOTH);
                            }
                        }, 5000);   //5 seconds
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        break;
                }
            }
        }
    };

    private void enableAll(int connectionKind) {
        Set<Device> bluetoothDevices = orchestrator.getDevices(connectionKind);

        for (Device device : bluetoothDevices) {
            final BluetoothAgent agent = ((BluetoothAgent) orchestrator.getAgent(device));

            if (agent != null) {
                agent.getConnection().reconnect();
            }

        }
    }

}
