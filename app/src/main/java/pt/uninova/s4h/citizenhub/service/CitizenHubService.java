package pt.uninova.s4h.citizenhub.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LifecycleService;
import androidx.preference.PreferenceManager;
import androidx.work.WorkManager;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import pt.uninova.s4h.citizenhub.R;
import pt.uninova.s4h.citizenhub.WorkTimeRangeConverter;
import pt.uninova.s4h.citizenhub.connectivity.Agent;
import pt.uninova.s4h.citizenhub.connectivity.AgentFactory;
import pt.uninova.s4h.citizenhub.connectivity.AgentListener;
import pt.uninova.s4h.citizenhub.connectivity.AgentOrchestrator;
import pt.uninova.s4h.citizenhub.connectivity.AgentOrchestratorListener;
import pt.uninova.s4h.citizenhub.connectivity.Connection;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.BluetoothAgentFactory;
import pt.uninova.s4h.citizenhub.connectivity.wearos.WearOsAgentFactory;
import pt.uninova.s4h.citizenhub.data.Device;
import pt.uninova.s4h.citizenhub.data.Measurement;
import pt.uninova.s4h.citizenhub.data.Sample;
import pt.uninova.s4h.citizenhub.data.Tag;
import pt.uninova.s4h.citizenhub.persistence.entity.DeviceRecord;
import pt.uninova.s4h.citizenhub.persistence.entity.EnabledMeasurementRecord;
import pt.uninova.s4h.citizenhub.persistence.repository.DeviceRepository;
import pt.uninova.s4h.citizenhub.persistence.repository.EnabledMeasurementRepository;
import pt.uninova.s4h.citizenhub.persistence.repository.LumbarExtensionTrainingRepository;
import pt.uninova.s4h.citizenhub.persistence.repository.SampleRepository;
import pt.uninova.s4h.citizenhub.persistence.repository.TagRepository;
import pt.uninova.s4h.citizenhub.service.work.SmartBearUploadWorker;
import pt.uninova.s4h.citizenhub.service.work.WorkOrchestrator;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

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

    private WorkOrchestrator workOrchestrator;

    private SharedPreferences preferences;

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

    private void initAgentOrchestrator() {
        final Map<Integer, AgentFactory<? extends Agent>> agentFactoryMap = new HashMap<>();
        final DeviceRepository deviceRepository = new DeviceRepository(getApplication());
        final EnabledMeasurementRepository enabledMeasurementRepository = new EnabledMeasurementRepository(getApplication());
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
            });
        };

        orchestrator = new AgentOrchestrator(agentFactoryMap, databaseWriter);
        orchestrator.addListener(new AgentOrchestratorListener() {
            @Override
            public void onAgentAttached(Device device, Agent agent) {
                deviceRepository.updateAgent(device.getAddress(), agent.getClass().getCanonicalName());

                agent.addAgentListener(new AgentListener() {
                    @Override
                    public void onMeasurementDisabled(Agent agent, int measurementType) {
                        enabledMeasurementRepository.delete(agent.getSource().getAddress(), measurementType);
                    }

                    @Override
                    public void onMeasurementEnabled(Agent agent, int measurementType) {
                        enabledMeasurementRepository.create(agent.getSource().getAddress(), measurementType);
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
                orchestrator.add(new Device(i.getAddress(), i.getName(), i.getConnectionKind()), agent -> {
                    agent.enable();

                    enabledMeasurementRepository.read(i.getAddress(), enabledMeasurements -> {
                        for (final EnabledMeasurementRecord j : enabledMeasurements) {
                            agent.enableMeasurement(j.getMeasurementType());
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

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        initAgentOrchestrator();
        initWorkOrchestrator();
    }

    private void initWorkOrchestrator() {
        workOrchestrator = new WorkOrchestrator(WorkManager.getInstance(this));

        workOrchestrator.addPeriodicWork(SmartBearUploadWorker.class, 12, TimeUnit.HOURS);
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

}
