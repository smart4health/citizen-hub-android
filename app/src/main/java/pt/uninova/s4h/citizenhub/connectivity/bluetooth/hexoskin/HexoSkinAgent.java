package pt.uninova.s4h.citizenhub.connectivity.bluetooth.hexoskin;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import pt.uninova.s4h.citizenhub.connectivity.AgentOrchestrator;
import pt.uninova.s4h.citizenhub.connectivity.MeasuringProtocol;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.BluetoothAgent;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.BluetoothConnection;
import pt.uninova.s4h.citizenhub.data.Measurement;

public class HexoSkinAgent extends BluetoothAgent {

    static public final UUID ID = AgentOrchestrator.namespaceGenerator().getUUID("bluetooth.hexoskin");

    static private final Set<Integer> supportedMeasurementKinds = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            Measurement.TYPE_STEPS_SNAPSHOT,
            Measurement.TYPE_HEART_RATE,
            Measurement.TYPE_RESPIRATION_RATE
    )));

    static private final Set<UUID> supportedProtocolsIds = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            HexoSkinAccelerometerProtocol.ID,
            HexoSkinHeartRateProtocol.ID,
            HexoSkinRespirationProtocol.ID
    )));

    public HexoSkinAgent(BluetoothConnection connection) {
        super(ID, HexoSkinAgent.supportedProtocolsIds, HexoSkinAgent.supportedMeasurementKinds, connection);
    }

    @Override
    public Set<Integer> getSupportedMeasurements() {
        return HexoSkinAgent.supportedMeasurementKinds;
    }

    @Override
    public MeasuringProtocol getMeasuringProtocol(int measurementKind) {
        switch (measurementKind) {
            case Measurement.TYPE_STEPS_SNAPSHOT:
                return new HexoSkinAccelerometerProtocol(this.getConnection(), getSampleDispatcher(), this);
            case Measurement.TYPE_HEART_RATE:
                return new HexoSkinHeartRateProtocol(this.getConnection(), getSampleDispatcher(), this);
            case Measurement.TYPE_RESPIRATION_RATE:
                return new HexoSkinRespirationProtocol(this.getConnection(), getSampleDispatcher(), this);
        }

        return null;
    }

    @Override
    public String getName() {
        return "HexoSkin";
    }

}
