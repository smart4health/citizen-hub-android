package pt.uninova.s4h.citizenhub.connectivity.bluetooth.digitsole;

import android.os.Handler;
import android.os.Looper;

import java.util.UUID;

import pt.uninova.s4h.citizenhub.connectivity.AgentOrchestrator;
import pt.uninova.s4h.citizenhub.connectivity.Protocol;
import pt.uninova.s4h.citizenhub.connectivity.StateChangedMessage;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.BaseCharacteristicListener;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.BluetoothAgent;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.BluetoothConnection;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.BluetoothConnectionState;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.BluetoothMeasuringProtocol;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.CharacteristicListener;
import pt.uninova.s4h.citizenhub.data.CaloriesMeasurement;
import pt.uninova.s4h.citizenhub.data.DistanceMeasurement;
import pt.uninova.s4h.citizenhub.data.Sample;
import pt.uninova.s4h.citizenhub.data.StepsMeasurement;
import pt.uninova.s4h.citizenhub.util.messaging.Dispatcher;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

public class DigitsoleActivityProtocol extends BluetoothMeasuringProtocol {

    private static final UUID UUID_SERVICE_DATA = UUID.fromString("99ddcdab-a80c-4f94-be5d-c66b9fba40cf");
    private static final UUID UUID_CHARACTERISTIC_ACTIVITYLOG = UUID.fromString("99dd0106-a80c-4f94-be5d-c66b9fba40cf");
    private static final UUID UUID_CHARACTERISTIC_COLLECTINGSTATE = UUID.fromString("99dd0014-a80c-4f94-be5d-c66b9fba40cf");

    public static final UUID ID = AgentOrchestrator.namespaceGenerator().getUUID("bluetooth.digitsole.activity");

    long lastTime = 0;
    int miliForTimer = 120000; //2 minutes

    private final CharacteristicListener activationListener = new BaseCharacteristicListener(UUID_SERVICE_DATA, UUID_CHARACTERISTIC_COLLECTINGSTATE) {
        private boolean once0x02 = false;

        @Override
        public void onWrite(byte[] value) {
            switch (value[0]) {
                case 0x00:
                    getConnection().writeCharacteristic(UUID_SERVICE_DATA, UUID_CHARACTERISTIC_COLLECTINGSTATE, new byte[]{0x02});
                    break;
                case 0x02:
                    if (once0x02) {
                        getConnection().writeCharacteristic(UUID_SERVICE_DATA, UUID_CHARACTERISTIC_COLLECTINGSTATE, new byte[]{0x01});
                    } else {
                        once0x02 = true;
                        getConnection().writeCharacteristic(UUID_SERVICE_DATA, UUID_CHARACTERISTIC_COLLECTINGSTATE, new byte[]{0x02});
                    }
                    break;
                case 0x01:
                    once0x02 = false;
                    setState(Protocol.STATE_ENABLED);
                    break;
            }
        }
    };

    private final CharacteristicListener dataListener = new BaseCharacteristicListener(UUID_SERVICE_DATA, UUID_CHARACTERISTIC_ACTIVITYLOG) {
        @Override
        public void onChange(byte[] value) {
            int steps = value[4] & 0xff;
            final Sample sampleSteps = new Sample(getAgent().getSource(), new StepsMeasurement((double)steps));
            getSampleDispatcher().dispatch(sampleSteps);

            int distance = value[40];
            if (distance != -128)
            {
                distance = value[40] & 0xff;
                final Sample sampleDistance = new Sample(getAgent().getSource(), new DistanceMeasurement((double)distance));
                getSampleDispatcher().dispatch(sampleDistance);
            }

            double calories = steps * 0.04; //sending kcal
            final Sample sampleCalories = new Sample(getAgent().getSource(), new CaloriesMeasurement(calories));
            getSampleDispatcher().dispatch(sampleCalories);

            //TODO delete this
            final Sample samplefakeCalories = new Sample(getAgent().getSource(), new CaloriesMeasurement((double)10));
            getSampleDispatcher().dispatch(samplefakeCalories);
            //final Sample sampleStepsFake = new Sample(getAgent().getSource(), new StepsSnapshotMeasurement(SnapshotMeasurement.TYPE_DAY, 20));
            //getSampleDispatcher().dispatch(sampleStepsFake);

            lastTime = System.currentTimeMillis();

            System.out.println("Steps: " + steps);
            System.out.println("Distance: " + distance);
            System.out.println("Calories: " + calories);
        }
    };

    private final Observer<StateChangedMessage<BluetoothConnectionState, BluetoothConnection>> reconnectionListener = new Observer<StateChangedMessage<BluetoothConnectionState, BluetoothConnection>>() {
        @Override
        public void observe(StateChangedMessage<BluetoothConnectionState, BluetoothConnection> value) {
            if (value.getNewState() == BluetoothConnectionState.READY) {
                value.getSource().enableNotifications(UUID_SERVICE_DATA, UUID_CHARACTERISTIC_ACTIVITYLOG, true);
            }
        }
    };

    public DigitsoleActivityProtocol(BluetoothConnection connection, Dispatcher<Sample> dispatcher, BluetoothAgent agent) {
        super(ID, connection, dispatcher, agent);
    }

    @Override
    public void disable() {
        final BluetoothConnection connection = getConnection();
        connection.disableNotifications(UUID_SERVICE_DATA, UUID_CHARACTERISTIC_ACTIVITYLOG);
        connection.removeCharacteristicListener(dataListener);
        connection.removeCharacteristicListener(activationListener);
        super.disable();
    }

    @Override
    public void enable() {
        long currentTime = System.currentTimeMillis();
        //Last segment is recent, restarting timer...
        if (currentTime - lastTime < (miliForTimer)) {
            runTimer();
            return;
        }
        //Last segment is old or first time connecting, enabling...
        runTimer();
        final BluetoothConnection connection = getConnection();
        connection.addCharacteristicListener(activationListener);
        connection.addCharacteristicListener(dataListener);
        connection.enableNotifications(UUID_SERVICE_DATA, UUID_CHARACTERISTIC_ACTIVITYLOG, true);
        connection.writeCharacteristic(UUID_SERVICE_DATA, UUID_CHARACTERISTIC_COLLECTINGSTATE, new byte[]{0x00});
    }

    private void runTimer() {
        new Handler(Looper.getMainLooper()).postDelayed(this::enable, 1000);
    }
}