package pt.uninova.s4h.citizenhub.connectivity.wearos;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import pt.uninova.s4h.citizenhub.connectivity.AbstractAgent;
import pt.uninova.s4h.citizenhub.connectivity.AgentOrchestrator;
import pt.uninova.s4h.citizenhub.connectivity.AgentState;
import pt.uninova.s4h.citizenhub.connectivity.Device;
import pt.uninova.s4h.citizenhub.connectivity.MeasuringProtocol;
import pt.uninova.s4h.citizenhub.persistence.ConnectionKind;
import pt.uninova.s4h.citizenhub.persistence.MeasurementKind;
import pt.uninova.s4h.citizenhub.service.CitizenHubService;


public class WearOSAgent extends AbstractAgent {

    final public static UUID ID = AgentOrchestrator.namespaceGenerator().getUUID("wearos.wear");

    static private final Set<MeasurementKind> supportedMeasurementKinds = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            MeasurementKind.ACTIVITY,
            MeasurementKind.HEART_RATE
    )));

    static private final Set<UUID> supportedProtocolsIds = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            WearOSHeartRateProtocol.ID,
            WearOSStepsProtocol.ID
    )));

    final private WearOSConnection connection;
    CitizenHubService service;

    public WearOSAgent(WearOSConnection connection, CitizenHubService service) {
        super(ID, new Device(connection.getAddress(), ConnectionKind.WEAROS), supportedProtocolsIds, supportedMeasurementKinds);
        this.service = service;
        this.connection = connection;
    }

    @Override
    public void disable() {
        setState(AgentState.DISABLED);
        service.getWearOSMessageService().sendMessage("WearOSAgent","false");
    }

    @Override
    public void enable() {
        setState(AgentState.ENABLED);
        service.getWearOSMessageService().sendMessage("WearOSAgent","true");
    }

    @Override
    public Set<MeasurementKind> getSupportedMeasurements() {
        return supportedMeasurementKinds;
    }

    @Override
    protected MeasuringProtocol getMeasuringProtocol(MeasurementKind kind) {
        switch (kind) {
            case ACTIVITY:
                return new WearOSStepsProtocol(this.connection, getSampleDispatcher(), this, service);
            case HEART_RATE:
                return new WearOSHeartRateProtocol(this.connection, getSampleDispatcher(), this, service);
        }

        return null;
    }

    @Override
    public String getName() {
        return "WearOS";
    }

}

