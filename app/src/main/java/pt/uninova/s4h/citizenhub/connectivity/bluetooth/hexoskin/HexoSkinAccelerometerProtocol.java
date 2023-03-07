package pt.uninova.s4h.citizenhub.connectivity.bluetooth.hexoskin;

import static android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT16;
import static pt.uninova.s4h.citizenhub.connectivity.bluetooth.hexoskin.HexoSkinDataConverter.getIntValue;

import java.util.UUID;

import pt.uninova.s4h.citizenhub.connectivity.AgentOrchestrator;
import pt.uninova.s4h.citizenhub.connectivity.Protocol;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.BaseCharacteristicListener;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.BluetoothConnection;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.BluetoothMeasuringProtocol;
import pt.uninova.s4h.citizenhub.data.ActivityMeasurement;
import pt.uninova.s4h.citizenhub.data.CadenceMeasurement;
import pt.uninova.s4h.citizenhub.data.CaloriesSnapshotMeasurement;
import pt.uninova.s4h.citizenhub.data.DistanceSnapshotMeasurement;
import pt.uninova.s4h.citizenhub.data.Sample;
import pt.uninova.s4h.citizenhub.data.SnapshotMeasurement;
import pt.uninova.s4h.citizenhub.data.StepsSnapshotMeasurement;
import pt.uninova.s4h.citizenhub.util.messaging.Dispatcher;

public class HexoSkinAccelerometerProtocol extends BluetoothMeasuringProtocol {

    final public static UUID ID = AgentOrchestrator.namespaceGenerator().getUUID("bluetooth.hexoskin.accelerometer");
    final public static String name = HexoSkinAccelerometerProtocol.class.getSimpleName();
    public static final UUID ACCELEROMETER_SERVICE_UUID = UUID.fromString("bdc750c7-2649-4fa8-abe8-fbf25038cda3");
    public static final UUID ACCELEROMETER_MEASUREMENT_CHARACTERISTIC_UUID = UUID.fromString("75246a26-237a-4863-aca6-09b639344f43");

    private int lastStepCount;

    public HexoSkinAccelerometerProtocol(BluetoothConnection connection, Dispatcher<Sample> dispatcher, HexoSkinAgent agent) {
        super(ID, connection, dispatcher, agent);
        setState(STATE_DISABLED);
        lastStepCount = 0;

        connection.addCharacteristicListener(new BaseCharacteristicListener(ACCELEROMETER_SERVICE_UUID, ACCELEROMETER_MEASUREMENT_CHARACTERISTIC_UUID) {
            @Override
            public void onChange(byte[] value) {
                byte flag = value[0];
                int format = FORMAT_UINT16;
                int dataIndex = 1;

                boolean isStepCountPresent = (flag & 0x01) != 0;
                boolean isActivityPresent = (flag & 0x02) != 0;
                boolean isCadencePresent = (flag & 0x04) != 0;

                final int measurementSize = (isStepCountPresent ? 3 : 0) + (isActivityPresent ? 1 : 0) + (isCadencePresent ? 1 : 0);
                int measurementIndex = 0;

                pt.uninova.s4h.citizenhub.data.Measurement<?>[] measurements = new pt.uninova.s4h.citizenhub.data.Measurement[measurementSize];

                if (isStepCountPresent) {
                    int stepCount = getIntValue(format, dataIndex, value);
                    dataIndex = dataIndex + 2;

                    if (stepCount < lastStepCount) {
                        lastStepCount = 0;
                    }

                    measurements[measurementIndex++] = new StepsSnapshotMeasurement(SnapshotMeasurement.TYPE_DAY, stepCount);
                    measurements[measurementIndex++] = new DistanceSnapshotMeasurement(SnapshotMeasurement.TYPE_DAY, stepCount * 0.762);
                    measurements[measurementIndex++] = new CaloriesSnapshotMeasurement(SnapshotMeasurement.TYPE_DAY, stepCount * 0.04);

                    lastStepCount = stepCount;
                }

                if (isActivityPresent) {
                    float activity = getIntValue(format, dataIndex, value) / 256.0f;

                    dataIndex = dataIndex + 2;

                    measurements[measurementIndex++] = new ActivityMeasurement(activity);
                }

                if (isCadencePresent) {
                    int cadence = getIntValue(format, dataIndex, value);
                    measurements[measurementIndex] = new CadenceMeasurement(cadence);
                }

                final Sample sample = new Sample(agent.getSource(), measurements);

                getSampleDispatcher().dispatch(sample);
            }
        });
    }

    @Override
    public void disable() {
        setState(Protocol.STATE_DISABLED);
        getConnection().disableNotifications(ACCELEROMETER_SERVICE_UUID, ACCELEROMETER_MEASUREMENT_CHARACTERISTIC_UUID);

    }

    @Override
    public void enable() {
        setState(Protocol.STATE_ENABLED);
        getConnection().enableNotifications(ACCELEROMETER_SERVICE_UUID, ACCELEROMETER_MEASUREMENT_CHARACTERISTIC_UUID);
    }
}
