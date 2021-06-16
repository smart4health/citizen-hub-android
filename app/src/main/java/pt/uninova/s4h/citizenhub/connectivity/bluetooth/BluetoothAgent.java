package pt.uninova.s4h.citizenhub.connectivity.bluetooth;

import java.util.Map;
import java.util.UUID;

import pt.uninova.s4h.citizenhub.connectivity.AbstractAgent;
import pt.uninova.s4h.citizenhub.connectivity.Protocol;
import pt.uninova.s4h.citizenhub.persistence.MeasurementKind;

public abstract class BluetoothAgent extends AbstractAgent {

    final private BluetoothConnection connection;

    protected BluetoothAgent(UUID id, Map<UUID, Protocol> protocolMap, BluetoothConnection connection) {
        super(id, protocolMap);

        this.connection = connection;
    }

    protected BluetoothConnection getConnection() {
        return connection;
    }

    @Override
    public void disableMeasurement(MeasurementKind measurementKind) {

    }
}
