package pt.uninova.s4h.citizenhub.connectivity.bluetooth.uprightgo2;

import android.os.Handler;
import android.os.Looper;

import java.time.Duration;
import java.util.UUID;

import pt.uninova.s4h.citizenhub.connectivity.AgentOrchestrator;
import pt.uninova.s4h.citizenhub.connectivity.Protocol;
import pt.uninova.s4h.citizenhub.connectivity.StateChangedMessage;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.BaseCharacteristicListener;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.BluetoothConnection;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.BluetoothConnectionState;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.BluetoothMeasuringProtocol;
import pt.uninova.s4h.citizenhub.data.Measurement;
import pt.uninova.s4h.citizenhub.data.PostureMeasurement;
import pt.uninova.s4h.citizenhub.data.PostureValue;
import pt.uninova.s4h.citizenhub.data.Sample;
import pt.uninova.s4h.citizenhub.util.Pair;
import pt.uninova.s4h.citizenhub.util.messaging.Dispatcher;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;
import pt.uninova.s4h.citizenhub.util.time.Accumulator;
import pt.uninova.s4h.citizenhub.util.time.FlushingAccumulator;

public class UprightGo2PostureProtocol extends BluetoothMeasuringProtocol {

    final public static UUID ID = AgentOrchestrator.namespaceGenerator().getUUID("bluetooth.uprightgo2.posture");
    final public static UUID MEASUREMENTS_SERVICE = UUID.fromString("0000bac0-0000-1000-8000-00805f9b34fb"); //bac0
    final private static UUID POSTURE_CORRECTION = UUID.fromString("0000bac3-0000-1000-8000-00805f9b34fb"); //bac3
    final private static UUID CHARACTERISTIC = UUID.fromString("0000bac4-0000-1000-8000-00805f9b34fb"); //bac4
    final public static UUID BATTERY_SERVICE = UUID.fromString("0000bad0-0000-1000-8000-00805f9b34fb"); //bab0

    final private static UUID BATTERY = UUID.fromString("0000bad2-0000-1000-8000-00805f9b34fb"); //bad2
    private int isCharging = 0;
    private Observer<Pair<Boolean, Duration>> observer = new Observer<Pair<Boolean, Duration>>() {
        @Override
        public void observe(Pair<Boolean, Duration> value) {
            final int classification = value.getFirst() ? PostureValue.CLASSIFICATION_CORRECT : PostureValue.CLASSIFICATION_INCORRECT;
            final Measurement<?> measurement = new PostureMeasurement(new PostureValue(classification, value.getSecond()));
            final Sample sample = new Sample(UprightGo2PostureProtocol.this.getAgent().getSource(), measurement);

            UprightGo2PostureProtocol.this.getSampleDispatcher().dispatch(sample);
        }
    };

    private static final int selfUpdatingInterval = 5000;
    private final Accumulator<Boolean> posture;
    private final Observer<StateChangedMessage<BluetoothConnectionState, BluetoothConnection>> connectionStateObserver = new Observer<StateChangedMessage<BluetoothConnectionState, BluetoothConnection>>() {
        @Override
        public void observe(StateChangedMessage<BluetoothConnectionState, BluetoothConnection> value) {
            if (value.getNewState() == BluetoothConnectionState.READY) {
                UprightGo2PostureProtocol.this.setState(Protocol.STATE_ENABLED);
                getConnection().addCharacteristicListener(isChargingListener);

                Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    public void run() {

                        UprightGo2PostureProtocol.this.getConnection().enableNotifications(MEASUREMENTS_SERVICE, POSTURE_CORRECTION);
                        UprightGo2PostureProtocol.this.getConnection().enableNotifications(BATTERY_SERVICE, BATTERY);

                    }
                }, 5000);

            } else {
                getConnection().removeCharacteristicListener(isChargingListener);

                UprightGo2PostureProtocol.this.setState(Protocol.STATE_SUSPENDED);
                posture.stop();
            }
        }
    };

    private final BaseCharacteristicListener isChargingListener = new BaseCharacteristicListener(BATTERY_SERVICE, BATTERY) {
        @Override
        public void onChange(byte[] value) {
            if (value[0] == 0) {
                getConnection().addCharacteristicListener(postureChangedListener);
                posture.addObserver(observer);
                isCharging = 0;
            } else {
                posture.clear();
                posture.removeObserver(observer);
                isCharging = 1;
            }

        }
    };
    /*
    ALL KNOWN SERVICES
    0x1800 -> Generic Access Service
        0x2A00 -> Device Name (UprightGO2)
    0x180A -> Generic Information Service
        0x2A24 -> Model Number
        0x2A25 -> Serial Number
        0x2A26 -> Firmware Revision
        0x2A27 -> Hardware Revision
        0x2A28 -> Software Revision
        0x2A29 -> Manufacturer Name
    BAA0 -> ???
        BAA6 -> Counter
    BAC0 -> Measurements Service
        BAC1 -> Calibration (Current User Posture will be new Good Posture Reference)
        0x00 to trigger/start
        BAC3 -> Posture Correction
        Vibration Status | Posture OK or NOT OK | Correcting Posture
        0x00 0x00 0x00 - Good Posture, not vibrating
        0x01 0x01 0x00 - Bad Posture, not vibrating (in countdown to vibrating)
        0x02 0x01 0x01 - Bad Posture, vibrating
        0x00 0x00 0x01 - Good Posture, User corrected position while vibrating, so the vibration
        was interrupted
        BAC4 -> Accelerometer Raw Data (acc1,acc2,acc3)
    BAB0 -> Vibration Service
        BAB2 -> Time Interval (5sec/15sec/30sec/60sec) + Vibration Angle(1 to 6, strict to relaxed)
        + Vibration Strength (1-3) 1-gentle, 2-medium, 3-strong
        + Vibration Pattern (9 different, check functions below, 00 defines patern 0, 80
        makes sensor show pattern 0 and also defines it)
        ! ONLY WORKS WHEN VIBRATION ON !
        BAB3 -> AUTO-CALIBRATION ON or OFF
        BAB5 -> Vibration On/off (0000/0101)

        DONE WITHIN THE UPRIGHT APP:
        - Minutes with Good Posture (It only counts when using the Upright App, we should set our
        own counters, BAA6 is an internal counter)
     */

    private final BaseCharacteristicListener postureChangedListener = new BaseCharacteristicListener(MEASUREMENTS_SERVICE, POSTURE_CORRECTION) {
        @Override
        public void onChange(byte[] value) {
            System.out.println("ISCHARGINGGGGGGGG" + isCharging);
            if (isCharging == 0) {
                posture.set(value[0] == 0);
            }
        }
    };


    public UprightGo2PostureProtocol(BluetoothConnection connection, Dispatcher<Sample> dispatcher, UprightGo2Agent agent) {
        super(ID, connection, dispatcher, agent);
        this.posture = new FlushingAccumulator<>(5000);
        posture.addObserver(observer);

    }

    @Override
    public void disable() {

        setState(Protocol.STATE_DISABLING);

        final BluetoothConnection connection = getConnection();
        connection.removeCharacteristicListener(isChargingListener);
        getConnection().removeConnectionStateChangeListener(connectionStateObserver);
        getConnection().removeCharacteristicListener(postureChangedListener);

        connection.disableNotifications(MEASUREMENTS_SERVICE, POSTURE_CORRECTION);
        connection.disableNotifications(BATTERY_SERVICE, BATTERY);

        posture.stop();

        super.disable();
    }

    @Override
    public void enable() {
        setState(Protocol.STATE_ENABLING);

        final BluetoothConnection connection = getConnection();
        getConnection().addConnectionStateChangeListener(connectionStateObserver);
        getConnection().addCharacteristicListener(isChargingListener);

        getConnection().addCharacteristicListener(postureChangedListener);

        connection.enableNotifications(MEASUREMENTS_SERVICE, POSTURE_CORRECTION);
        connection.enableNotifications(BATTERY_SERVICE, BATTERY);
        super.enable();
    }

    public void close() {

        posture.clear();
        super.close();
    }

}