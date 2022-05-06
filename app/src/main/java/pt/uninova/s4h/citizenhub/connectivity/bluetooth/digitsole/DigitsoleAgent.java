package pt.uninova.s4h.citizenhub.connectivity.bluetooth.digitsole;

import android.content.Context;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import pt.uninova.s4h.citizenhub.connectivity.AgentOrchestrator;
import pt.uninova.s4h.citizenhub.connectivity.MeasuringProtocol;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.BluetoothAgent;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.BluetoothConnection;
import pt.uninova.s4h.citizenhub.persistence.MeasurementKind;

public class DigitsoleAgent extends BluetoothAgent {

    public static final UUID ID = AgentOrchestrator.namespaceGenerator().getUUID("bluetooth.digitsole");

    public static final Set<MeasurementKind> supportedMeasurementKinds = Collections.unmodifiableSet(new HashSet<>(Collections.singletonList(
            MeasurementKind.STEPS
    )));

    private static final Set<UUID> supportedProtocolsIds = Collections.unmodifiableSet(new HashSet<>(Collections.singletonList(
            DigitsoleActivityProtocol.ID
    )));

    private Context context;

    public DigitsoleAgent(BluetoothConnection connection, Context context) {
        super(ID, supportedProtocolsIds, supportedMeasurementKinds, connection);
        this.context = context;
    }

    @Override
    protected MeasuringProtocol getMeasuringProtocol(MeasurementKind kind) {
        if (kind == MeasurementKind.STEPS) {
            return new DigitsoleActivityProtocol(getConnection(), getSampleDispatcher(), this, context);
        }
        return null;
    }

    @Override
    public String getName() {
        return "Digitsole";
    }
}