package pt.uninova.s4h.citizenhub.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LifecycleService;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import pt.uninova.s4h.citizenhub.R;
import pt.uninova.s4h.citizenhub.connectivity.Agent;
import pt.uninova.s4h.citizenhub.connectivity.AgentFactory;
import pt.uninova.s4h.citizenhub.connectivity.AgentListener;
import pt.uninova.s4h.citizenhub.connectivity.AgentOrchestrator;
import pt.uninova.s4h.citizenhub.connectivity.AgentOrchestratorListener;
import pt.uninova.s4h.citizenhub.connectivity.Device;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.BluetoothAgentFactory;
import pt.uninova.s4h.citizenhub.connectivity.wearos.WearOsAgentFactory;
import pt.uninova.s4h.citizenhub.data.BloodPressureValue;
import pt.uninova.s4h.citizenhub.data.Measurement;
import pt.uninova.s4h.citizenhub.data.MedXTrainingValue;
import pt.uninova.s4h.citizenhub.data.Sample;
import pt.uninova.s4h.citizenhub.persistence.ConnectionKind;
import pt.uninova.s4h.citizenhub.persistence.DeviceRecord;
import pt.uninova.s4h.citizenhub.persistence.DeviceRepository;
import pt.uninova.s4h.citizenhub.persistence.Feature;
import pt.uninova.s4h.citizenhub.persistence.FeatureRepository;
import pt.uninova.s4h.citizenhub.persistence.LumbarExtensionTraining;
import pt.uninova.s4h.citizenhub.persistence.LumbarExtensionTrainingRepository;
import pt.uninova.s4h.citizenhub.persistence.MeasurementKind;
import pt.uninova.s4h.citizenhub.persistence.MeasurementRepository;
import pt.uninova.s4h.citizenhub.persistence.StateKind;
import pt.uninova.util.messaging.Observer;

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

        context.startService(intent);
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

    public CitizenHubService() {
        this.binder = new Binder();
    }

    private Notification buildNotification() {
        return new NotificationCompat.Builder(this, Objects.requireNonNull(CitizenHubService.class.getCanonicalName()))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(NOTIFICATION_TITLE)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CitizenHubService.class.getCanonicalName(), NOTIFICATION_TITLE, NotificationManager.IMPORTANCE_DEFAULT);

            notificationManager.createNotificationChannel(channel);
        }
    }

    public AgentOrchestrator getAgentOrchestrator() {
        return orchestrator;
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

    private void initAgentOrchestrator() {
        final Map<ConnectionKind, AgentFactory<? extends Agent>> agentFactoryMap = new HashMap<>();
        final DeviceRepository deviceRepository = new DeviceRepository(getApplication());
        final FeatureRepository featureRepository = new FeatureRepository(getApplication());
        final LumbarExtensionTrainingRepository lumbarExtensionTrainingRepository = new LumbarExtensionTrainingRepository(getApplication());
        final MeasurementRepository measurementRepository = new MeasurementRepository(getApplication());

        agentFactoryMap.put(ConnectionKind.BLUETOOTH, new BluetoothAgentFactory(this));
        agentFactoryMap.put(ConnectionKind.WEAROS, new WearOsAgentFactory(this));

        final Observer<Sample> databaseWriter = (Sample sample) -> {
            for (Measurement<?> m : sample.getMeasurements()) {
                if (m.getType() == Measurement.LUMBAR_EXTENSION_TRAINING) {
                    final MedXTrainingValue value = (MedXTrainingValue) m.getValue();

                    lumbarExtensionTrainingRepository.add(new LumbarExtensionTraining(value.getTimestamp().toEpochMilli(), (int) value.getDuration().getSeconds(), value.getScore(), value.getRepetitions(), value.getWeight()));
                } else if (m.getType() == Measurement.BLOOD_PRESSURE) {
                    final BloodPressureValue value = (BloodPressureValue) m.getValue();

                    measurementRepository.add(new pt.uninova.s4h.citizenhub.persistence.Measurement(Date.from(sample.getTimestamp()), MeasurementKind.BLOOD_PRESSURE_SBP, value.getSystolic()));
                    measurementRepository.add(new pt.uninova.s4h.citizenhub.persistence.Measurement(Date.from(sample.getTimestamp()), MeasurementKind.BLOOD_PRESSURE_DBP, value.getDiastolic()));
                    measurementRepository.add(new pt.uninova.s4h.citizenhub.persistence.Measurement(Date.from(sample.getTimestamp()), MeasurementKind.BLOOD_PRESSURE_MEAN_AP, value.getMeanArterialPressure()));
                } else {
                    try {
                        measurementRepository.add(new pt.uninova.s4h.citizenhub.persistence.Measurement(Date.from(sample.getTimestamp()), MeasurementKind.find(m.getType()), parseMeasurementValue(m)));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        orchestrator = new AgentOrchestrator(agentFactoryMap, databaseWriter);
        orchestrator.addListener(new AgentOrchestratorListener() {
            @Override
            public void onAgentAttached(Device device, Agent agent) {
                agent.addAgentListener(new AgentListener() {
                    @Override
                    public void onMeasurementDisabled(Agent agent, MeasurementKind measurementKind) {
                        featureRepository.remove(new Feature(agent.getSource().getAddress(), measurementKind));
                    }

                    @Override
                    public void onMeasurementEnabled(Agent agent, MeasurementKind measurementKind) {
                        featureRepository.add(new Feature(agent.getSource().getAddress(), measurementKind));
                    }
                });
            }

            @Override
            public void onDeviceAdded(Device device) {
                deviceRepository.add(new DeviceRecord(device.getName(), device.getAddress(), device.getConnectionKind(), StateKind.ACTIVE, ""));
            }

            @Override
            public void onDeviceRemoved(Device device) {
                final Agent agent = getAgentOrchestrator().getAgent(device);

                if (agent != null) {
                    agent.removeAllAgentListeners();
                }

                deviceRepository.remove(new DeviceRecord(device.getName(), device.getAddress(), device.getConnectionKind(), StateKind.ACTIVE, ""));
            }
        });

        deviceRepository.obtainAll(value -> {
            for (DeviceRecord i : value) {
                orchestrator.add(new Device(i.getAddress(), i.getName(), i.getConnectionKind()), agent -> {
                    agent.enable();
                    featureRepository.obtainKindsFromDevice(i.getAddress(), kindList -> {
                        for (MeasurementKind kind : kindList) {
                            agent.enableMeasurement(kind);
                        }
                    });
                });
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        super.onBind(intent);

        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();

        startForeground(1, buildNotification());
        wearOSMessageService = new WearOSMessageService();

        initAgentOrchestrator();
    }


    @Override
    public void onDestroy() {
        orchestrator.clear();

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

    public WorkOrchestrator showMethods() throws ExecutionException, InterruptedException {

        //just for testing purposes. TODO erase this when fully implemented
        WorkOrchestrator workOrchestrator = new WorkOrchestrator();
        workOrchestrator.addOneTimeWork(getApplicationContext());
        workOrchestrator.addPeriodicWork(getApplicationContext(),24, TimeUnit.HOURS, "reportWork");
        workOrchestrator.getWorkers(getApplicationContext(),"reportWork");
        workOrchestrator.cancelWork("reportWork");

        return workOrchestrator;
    }
}
