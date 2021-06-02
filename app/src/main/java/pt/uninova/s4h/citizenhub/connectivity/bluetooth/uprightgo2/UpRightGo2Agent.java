package pt.uninova.s4h.citizenhub.connectivity.bluetooth.uprightgo2;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import pt.uninova.s4h.citizenhub.connectivity.AgentOrchestrator;
import pt.uninova.s4h.citizenhub.connectivity.AgentState;
import pt.uninova.s4h.citizenhub.connectivity.Protocol;
import pt.uninova.s4h.citizenhub.connectivity.ProtocolState;
import pt.uninova.s4h.citizenhub.connectivity.StateChangedMessage;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.BluetoothAgent;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.BluetoothConnection;
import pt.uninova.util.messaging.Observer;

public class UpRightGo2Agent extends BluetoothAgent {

    final public static UUID ID = AgentOrchestrator.namespaceGenerator().getUUID("bluetooth.uprightgo2");

    public UpRightGo2Agent(BluetoothConnection connection) {
        super(ID, createProtocols(connection), connection);
    }

    private static Map<UUID, Protocol> createProtocols(BluetoothConnection connection) {
        final Map<UUID, Protocol> protocolMap = new HashMap<>();

        System.out.println("FSL - Got here to posture agent");
        protocolMap.put(UpRightGo2Protocol.ID, new UpRightGo2Protocol(connection));

        return protocolMap;
    }

    @Override
    public void disable() {
        for (UUID i : getPublicProtocolIds(ProtocolState.ENABLED)) {
            getProtocol(i).disable();
        }

        setState(AgentState.DISABLED);
    }

    @Override
    public void enable() {
        UpRightGo2Protocol protocol = (UpRightGo2Protocol) getProtocol(UpRightGo2Protocol.ID);

        protocol.getObservers().add(new Observer<StateChangedMessage<ProtocolState>>() {
            @Override
            public void onChanged(StateChangedMessage<ProtocolState> value) {
                if (value.getNewState() == ProtocolState.ENABLED) {
                    UpRightGo2Agent.this.setState(AgentState.ENABLED);

                    protocol.getObservers().remove(this);
                }
            }
        });

        protocol.enable();
    }
}
