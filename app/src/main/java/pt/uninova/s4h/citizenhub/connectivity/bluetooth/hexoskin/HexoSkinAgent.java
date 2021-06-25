package pt.uninova.s4h.citizenhub.connectivity.bluetooth.hexoskin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import pt.uninova.s4h.citizenhub.connectivity.AgentOrchestrator;
import pt.uninova.s4h.citizenhub.connectivity.Protocol;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.BluetoothAgent;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.BluetoothConnection;
import pt.uninova.s4h.citizenhub.persistence.MeasurementKind;

public class HexoSkinAgent extends BluetoothAgent {

    final public static UUID ID = AgentOrchestrator.namespaceGenerator().getUUID("bluetooth.hexoskin");
//    final private static  List<MeasurementKind> measurementKindList = Collections.unmodifiableList(Arrays.asList(
//            MeasurementKind.HEART_RATE,
//            MeasurementKind.RESPIRATION_RATE,
//            MeasurementKind.INSPIRATION,
//            MeasurementKind.EXPIRATION,
//            MeasurementKind.CADENCE,
//
//            ));

    public HexoSkinAgent(BluetoothConnection connection, String name) {
        super(ID, createProtocols(connection), connection);
    }

    public HexoSkinAgent() {
        super(null, null, null);
    }

    private static Map<UUID, Protocol> createProtocols(BluetoothConnection connection) {
        final Map<UUID, Protocol> protocolMap = new HashMap<>();

        protocolMap.put(HexoSkinHeartRateProtocol.ID, new HexoSkinHeartRateProtocol(connection));
        protocolMap.put(HexoSkinAccelerometerProtocol.ID, new HexoSkinAccelerometerProtocol(connection));
        protocolMap.put(HexoSkinRespirationProtocol.ID, new HexoSkinRespirationProtocol(connection));

        return protocolMap;
    }

    @Override
    public void disable() {
    }

    @Override
    public void enable() {
        //TODO setState enable
    }


    //fazer um private static cenas

    @Override
    public List<MeasurementKind> getSupportedMeasurements() {
        List<MeasurementKind> measurementKindList = new ArrayList<>();
        measurementKindList.add(MeasurementKind.HEART_RATE);
        measurementKindList.add(MeasurementKind.RESPIRATION_RATE);
        measurementKindList.add(MeasurementKind.INSPIRATION);
        measurementKindList.add(MeasurementKind.EXPIRATION);
        measurementKindList.add(MeasurementKind.CADENCE);
        measurementKindList.add(MeasurementKind.DISTANCE);
        measurementKindList.add(MeasurementKind.STEPS);
        measurementKindList.add(MeasurementKind.STEPS_PER_MINUTE);
        measurementKindList.add(MeasurementKind.CALORIES);
        measurementKindList.add(MeasurementKind.ACTIVITY);

        return measurementKindList;
    }

    @Override
    public void enableMeasurement(MeasurementKind measurementKind) {
        switch (measurementKind) {
            case HEART_RATE:
                getProtocol(HexoSkinHeartRateProtocol.ID).enable();
                break;
            case RESPIRATION_RATE:
            case INSPIRATION:
            case EXPIRATION:
                getProtocol(HexoSkinRespirationProtocol.ID).enable();
                break;
            case ACTIVITY:
            case STEPS:
            case STEPS_PER_MINUTE:
            case DISTANCE:
            case CADENCE:
            case CALORIES:
                getProtocol(HexoSkinAccelerometerProtocol.ID).enable();
            default:
                break;
        }
    }

    @Override
    public String getName() {
        return null;
    }
}
