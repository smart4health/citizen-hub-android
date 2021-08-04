package pt.uninova.s4h.citizenhub.connectivity;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import pt.uninova.s4h.citizenhub.persistence.MeasurementKind;
import pt.uninova.util.messaging.Observer;

public interface Agent {

    void disable();

    void enable();

    UUID getId();

    Protocol getProtocol(UUID protocolId);

    Set<UUID> getPublicProtocolIds();

    Set<UUID> getPublicProtocolIds(ProtocolState state);

    Set<Observer<StateChangedMessage<AgentState, Class<?>>>> getObservers();

    List<MeasurementKind> getSupportedMeasurements();

    void enableMeasurement(MeasurementKind measurementKind);

    void disableMeasurement(MeasurementKind measurementKind);

    AgentState getState();

    String getName();

}
