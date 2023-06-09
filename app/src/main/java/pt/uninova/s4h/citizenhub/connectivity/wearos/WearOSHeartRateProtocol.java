package pt.uninova.s4h.citizenhub.connectivity.wearos;

import java.util.Date;
import java.util.UUID;

import pt.uninova.s4h.citizenhub.connectivity.AbstractMeasuringProtocol;
import pt.uninova.s4h.citizenhub.connectivity.AgentOrchestrator;
import pt.uninova.s4h.citizenhub.connectivity.Protocol;
import pt.uninova.s4h.citizenhub.data.HeartRateMeasurement;
import pt.uninova.s4h.citizenhub.data.Measurement;
import pt.uninova.s4h.citizenhub.data.Sample;
import pt.uninova.s4h.citizenhub.service.CitizenHubService;
import pt.uninova.s4h.citizenhub.util.messaging.Dispatcher;

public class WearOSHeartRateProtocol extends AbstractMeasuringProtocol {

    final public static UUID ID = AgentOrchestrator.namespaceGenerator().getUUID("wearos.wear.heartrate");
    final private static int kind = Measurement.TYPE_HEART_RATE;
    String heartRatePath = "heartrate";
    CitizenHubService service;

    protected WearOSHeartRateProtocol(WearOSConnection connection, Dispatcher<Sample> sampleDispatcher, WearOSAgent agent, CitizenHubService service) {
        super(ID, agent,sampleDispatcher);
        this.service = service;

        connection.addChannelListener(new BaseChannelListener(kind) {
            @Override
            public void onChange(double value, Date timestamp, long wear_sample_id) {
                final int heartRate = (int) value;
                final Sample sample = new Sample(timestamp.toInstant(), getAgent().getSource(),
                        new HeartRateMeasurement(heartRate));
                getSampleDispatcher().dispatch(sample);
                service.getWearOSMessageService().sendMessage(heartRatePath, String.valueOf(wear_sample_id));
            }
        });
    }

    @Override
    public void disable() {
        setState(Protocol.STATE_DISABLED);
    }

    @Override
    public void enable() {
        setState(Protocol.STATE_ENABLED);
    }
}