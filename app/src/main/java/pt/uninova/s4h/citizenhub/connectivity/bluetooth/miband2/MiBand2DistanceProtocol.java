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
import pt.uninova.s4h.citizenhub.data.Sample;
import pt.uninova.s4h.citizenhub.util.messaging.Dispatcher;

public class MiBand2DistanceProtocol extends BluetoothMeasuringProtocol {

    final public static UUID ID = AgentOrchestrator.namespaceGenerator().getUUID("bluetooth.miband2.distance");
    final public static String name = MiBand2DistanceProtocol.class.getSimpleName();

    final public static UUID UUID_SERVICE = UUID.fromString("0000fee0-0000-1000-8000-00805f9b34fb");
    final private static UUID UUID_CHARACTERISTIC_STEPS = UUID.fromString("00000007-0000-3512-2118-0009af100700");

    private Integer lastSteps;
    private Double lastDistance;
    private Double lastCalories;

    public MiBand2DistanceProtocol(BluetoothConnection connection, Dispatcher<Sample> sampleDispatcher, MiBand2Agent agent) {
        super(ID, connection, sampleDispatcher, agent);

        setState(Protocol.STATE_DISABLED);

        connection.addCharacteristicListener(new BaseCharacteristicListener(UUID_SERVICE, UUID_CHARACTERISTIC_STEPS) {
            @Override
            public void onRead(byte[] value) {
                ByteBuffer val = ByteBuffer.wrap(value);
                val.order(ByteOrder.LITTLE_ENDIAN);

                final int steps = val.getInt(1);
                final double distance;
                final double calories;

                if (value.length > 5) {
                    distance = val.getInt(5);
                    calories = val.getInt(9);
                } else {
                    distance = steps * 0.5;
                    calories = steps * 0.04;
                }


                if (lastSteps != null) {
                    if (steps < lastSteps) {
                        lastSteps = 0;
                        lastDistance = 0.0;
                        lastCalories = 0.0;
                    }
                    /*
                    final Sample sample = new Sample(getAgent().getSource(),
                            new StepsSnapshotMeasurement(steps - lastSteps),
                            new DistanceSnapshotMeasurement(distance - lastDistance),
                            new CaloriesMeasurement(calories - lastCalories));

                    getSampleDispatcher().dispatch(sample);
                     */
                }

                lastSteps = steps;
                lastDistance = distance;
                lastCalories = calories;
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
