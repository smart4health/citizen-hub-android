package pt.uninova.s4h.citizenhub.connectivity.wearos;

import android.util.Log;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

import pt.uninova.s4h.citizenhub.connectivity.AbstractMeasuringProtocol;
import pt.uninova.s4h.citizenhub.connectivity.AgentOrchestrator;
import pt.uninova.s4h.citizenhub.connectivity.Protocol;
import pt.uninova.s4h.citizenhub.data.CaloriesSnapshotMeasurement;
import pt.uninova.s4h.citizenhub.data.DistanceSnapshotMeasurement;
import pt.uninova.s4h.citizenhub.data.Measurement;
import pt.uninova.s4h.citizenhub.data.Sample;
import pt.uninova.s4h.citizenhub.data.SnapshotMeasurement;
import pt.uninova.s4h.citizenhub.data.StepsSnapshotMeasurement;
import pt.uninova.s4h.citizenhub.service.CitizenHubService;
import pt.uninova.s4h.citizenhub.util.messaging.Dispatcher;

public class WearOSStepsProtocol extends AbstractMeasuringProtocol {
    final public static UUID ID = AgentOrchestrator.namespaceGenerator().getUUID("wearos.wear.steps");
    final private static int kind = Measurement.TYPE_STEPS_SNAPSHOT;
    private static final String TAG = "WearOSStepsProtocol";
    final private static int wearProtocolDisable = 100000, wearProtocolEnable = 100001;
    CitizenHubService service;

    protected WearOSStepsProtocol(WearOSConnection connection, Dispatcher<Sample> sampleDispatcher, WearOSAgent agent, CitizenHubService service) {
        super(ID, agent, sampleDispatcher);
        Log.d(TAG, "Entered");
        this.service = service;

        connection.addChannelListener(new BaseChannelListener(kind) {
            @Override
            public void onChange(double value, Date timestamp) {
                Set<Integer> enabledMeasurements = getAgent().getEnabledMeasurements();
                service.getWearOSMessageService().sendMessage("WearOSConnected","true");
                if (value < wearProtocolDisable) {
                    final int steps = (int) value;
                    final Sample sample = new Sample(timestamp.toInstant(), getAgent().getSource(),
                            new StepsSnapshotMeasurement(SnapshotMeasurement.TYPE_DAY, steps),
                            new DistanceSnapshotMeasurement(SnapshotMeasurement.TYPE_DAY, steps * 0.762),
                            new CaloriesSnapshotMeasurement(SnapshotMeasurement.TYPE_DAY, steps * 0.04));
                    getSampleDispatcher().dispatch(sample);

                    if(!enabledMeasurements.contains(Measurement.TYPE_STEPS_SNAPSHOT)){
                        getAgent().enableMeasurement(Measurement.TYPE_STEPS_SNAPSHOT);
                    }
                } else {
                    if (value == wearProtocolDisable && enabledMeasurements.contains(Measurement.TYPE_STEPS_SNAPSHOT))
                        getAgent().disableMeasurement(Measurement.TYPE_STEPS_SNAPSHOT);
                    else if (value == wearProtocolEnable && !enabledMeasurements.contains(Measurement.TYPE_STEPS_SNAPSHOT))
                        getAgent().enableMeasurement(Measurement.TYPE_STEPS_SNAPSHOT);
                }
            }
        });
    }

    @Override
    public void disable() {
        setState(Protocol.STATE_DISABLED);
        service.getWearOSMessageService().sendMessage("WearOSStepsProtocol", "false");
    }

    @Override
    public void enable() {
        setState(Protocol.STATE_ENABLED);
        service.getWearOSMessageService().sendMessage("WearOSStepsProtocol", "true");
    }
}
