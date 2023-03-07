package pt.uninova.s4h.citizenhub.connectivity.bluetooth.miband2;

import android.os.Handler;
import android.os.Looper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

import pt.uninova.s4h.citizenhub.connectivity.AgentOrchestrator;
import pt.uninova.s4h.citizenhub.connectivity.Protocol;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.BaseCharacteristicListener;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.BluetoothConnection;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.BluetoothMeasuringProtocol;
import pt.uninova.s4h.citizenhub.data.CaloriesSnapshotMeasurement;
import pt.uninova.s4h.citizenhub.data.DistanceSnapshotMeasurement;
import pt.uninova.s4h.citizenhub.data.Measurement;
import pt.uninova.s4h.citizenhub.data.Sample;
import pt.uninova.s4h.citizenhub.data.SnapshotMeasurement;
import pt.uninova.s4h.citizenhub.data.StepsSnapshotMeasurement;
import pt.uninova.s4h.citizenhub.util.messaging.Dispatcher;

public class MiBand2StepsProtocol extends BluetoothMeasuringProtocol {

    final public static UUID ID = AgentOrchestrator.namespaceGenerator().getUUID("bluetooth.miband2.steps");
    final public static String name = MiBand2StepsProtocol.class.getSimpleName();

    final public static UUID UUID_SERVICE = UUID.fromString("0000fee0-0000-1000-8000-00805f9b34fb");
    final private static UUID UUID_CHARACTERISTIC_STEPS = UUID.fromString("00000007-0000-3512-2118-0009af100700");

    public MiBand2StepsProtocol(BluetoothConnection connection, Dispatcher<Sample> sampleDispatcher, MiBand2Agent agent) {
        super(ID, connection, sampleDispatcher, agent);

        setState(Protocol.STATE_DISABLED);

        connection.addCharacteristicListener(new BaseCharacteristicListener(UUID_SERVICE, UUID_CHARACTERISTIC_STEPS) {
            private int steps;
            private double distance;
            private double calories;

            @Override
            public void onRead(byte[] value) {
                final ByteBuffer val = ByteBuffer.wrap(value);
                val.order(ByteOrder.LITTLE_ENDIAN);

                int newSteps = val.getInt(1);
                double newDistance = 0;
                double newCalories = 0;

                if (value.length > 5) {
                    newDistance = val.getInt(5);
                    newCalories = val.getInt(9);
                } else {
                    newDistance = newSteps * 0.5;
                    newCalories = newSteps * 0.04;
                }

                if (newSteps != steps || newDistance != distance || newCalories != calories) {
                    final Measurement<?>[] measurements = new Measurement[3];

                    measurements[0] = new StepsSnapshotMeasurement(SnapshotMeasurement.TYPE_DAY, newSteps);
                    measurements[1] = new DistanceSnapshotMeasurement(SnapshotMeasurement.TYPE_DAY, newDistance);
                    measurements[2] = new CaloriesSnapshotMeasurement(SnapshotMeasurement.TYPE_DAY, newCalories);

                    final Sample sample = new Sample(getAgent().getSource(), measurements);

                    getSampleDispatcher().dispatch(sample);

                    steps = newSteps;
                    distance = newDistance;
                    calories = newCalories;
                }
            }
        });
    }

    @Override
    public void disable() {
        setState(Protocol.STATE_DISABLED);
    }

    @Override
    public void enable() {
        final Handler h = new Handler(Looper.getMainLooper());

        setState(Protocol.STATE_ENABLED);

        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                getConnection().readCharacteristic(UUID_SERVICE, UUID_CHARACTERISTIC_STEPS);

                if (getState() == Protocol.STATE_ENABLED) {
                    h.postDelayed(this, 10000);
                }
            }
        }, 0);
    }

}
