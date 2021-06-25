package pt.uninova.s4h.citizenhub.connectivity.bluetooth.uprightgo;

import pt.uninova.s4h.citizenhub.connectivity.AgentOrchestrator;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.BaseCharacteristicListener;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.BluetoothConnection;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.BluetoothMeasuringProtocol;
import pt.uninova.s4h.citizenhub.persistence.Measurement;
import pt.uninova.s4h.citizenhub.persistence.MeasurementKind;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

public class UprightGoProtocol extends BluetoothMeasuringProtocol {

    final public static UUID ID = AgentOrchestrator.namespaceGenerator().getUUID("bluetooth.kbzposture.raw");
    final public static UUID MEASUREMENTS_SERVICE = UUID.fromString("0000bac0-0000-1000-8000-00805f9b34fb"); //bac0
    final private static UUID TRIGGER_CALIBRATION = UUID.fromString("0000bac1-0000-1000-8000-00805f9b34fb"); //bac1
    final private static UUID POSTURE_CORRECTION = UUID.fromString("0000bac3-0000-1000-8000-00805f9b34fb"); //bac3
    final private static UUID CHARACTERISTIC = UUID.fromString("0000bac4-0000-1000-8000-00805f9b34fb"); //bac4
    final private static UUID VIBRATION_SERVICE = UUID.fromString("0000bab0-0000-1000-8000-00805f9b34fb"); //bab0
    final private static UUID VIBRATION_CHARACTERISTIC = UUID.fromString("0000bab5-0000-1000-8000-00805f9b34fb"); //bab5
    final private static UUID VIBRATION_INTERVAL_CHARACTERISTIC = UUID.fromString("0000bab2-0000-1000-8000-00805f9b34fb"); //bab2

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
        0x02 0x01 0x00 - Bad Posture, vibrating
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

        STILL DOING:
        - get minutes with good posture while bluetooth off
        TODO last test data
        11 - 13 BT OFF - 16 - 17 BT OFF - 20
     */

    private MeasurementKind lastBodyPosition;
    private MeasurementKind lastPosture;
    private LocalDateTime lastTimestamp;
    private byte[] vibrationON= {0x00,0x00};
    private byte[] vibrationOFF= {0x01,0x01};
    private byte[] calibrationTrigger = {0x00};
    private Boolean ignoreAccValues = true; //if true, ignores Acc values for evaluating good
    //posture, therefore using only the sensor evaluation (considering its current calibration)
    private Boolean ignoreSensorEvaluation = true; //similar to ignoreAccValues
    /* this may be helpful for testing, otherwise ignore
    private byte[] vibrationAngle6= {0x06,0x2c,0x01,0x01,0x01,0x02,0x64,0x00};
     */

    public UprightGoProtocol(BluetoothConnection connection) {
        super(ID, connection);
    }

    private void attachObservers() {
        final BluetoothConnection connection = getConnection();

        //setting up the vibration (initial), configure as needed
        //write vibration on
        connection.writeCharacteristic(VIBRATION_SERVICE,VIBRATION_CHARACTERISTIC,vibrationON);
        //write vibration parameters/settings
        connection.writeCharacteristic(VIBRATION_SERVICE,VIBRATION_INTERVAL_CHARACTERISTIC,
                vibrationMessage(1,5, false, 0, 3));
        //calibrate for current position (current position is considered good posture)
        connection.writeCharacteristic(MEASUREMENTS_SERVICE,TRIGGER_CALIBRATION,calibrationTrigger);

        connection.addCharacteristicListener(new BaseCharacteristicListener(MEASUREMENTS_SERVICE, CHARACTERISTIC) {
            @Override
            public void onWrite(byte[] value) {
                System.out.println("GETTING VALUE: " + value);
            }
        });

        //setting up accelerometer measurements
        if(connection.hasService(MEASUREMENTS_SERVICE)) {
            connection.enableNotifications(MEASUREMENTS_SERVICE, CHARACTERISTIC);
        }
        //setting up sensor evaluation of good posture (posture correction)
        if(connection.hasService(MEASUREMENTS_SERVICE)) {
            connection.enableNotifications(MEASUREMENTS_SERVICE, POSTURE_CORRECTION);
        }
        //setting up notifications for the sensor vibration settings
        /*
        if(connection.hasService(VIBRATION_SERVICE)) {
            connection.enableNotifications(VIBRATION_SERVICE, VIBRATION_INTERVAL_CHARACTERISTIC);
        }*/

        //handle new accelerometer measurements
        connection.addCharacteristicListener(new BaseCharacteristicListener(MEASUREMENTS_SERVICE, CHARACTERISTIC) {
            @Override
            public void onChange(byte[] value) {
                if (ignoreAccValues)
                {
                    updateCurrentPostureValues(connection);
                    return;
                }
                final LocalDateTime now = LocalDateTime.now();

                byte[] bytes = value;
                ByteBuffer byteBuffer = ByteBuffer.wrap(bytes).asReadOnlyBuffer();
                String s = Arrays.toString(bytes);

                /*final double[] parsed = new double[]{
                        byteBuffer.get(0),byteBuffer.get(1),byteBuffer.get(2),byteBuffer.get(3),
                        byteBuffer.get(4),byteBuffer.get(5)
                };

                short c = (short) ((byteBuffer.get(1)<<8)+byteBuffer.get(0));*/

                final short[] parsed_sh = new short[]{
                        (short) ((byteBuffer.get(1)<<8)+byteBuffer.get(0)),
                        (short) ((byteBuffer.get(3)<<8)+byteBuffer.get(2)),
                        (short) ((byteBuffer.get(5)<<8)+byteBuffer.get(4))
                };

                //this is here for testing, can be called anytime for current Vibration settings
                //but only returns != null after it has been changed
                updateCurrentVibrationValues(connection);
                System.out.println("Result: " + s + parsed_sh[0] + "|" + parsed_sh[1] + "|" + parsed_sh[2]);

                if (isGoodPosture_Accelerometer(parsed_sh[0], parsed_sh[1], parsed_sh[2])) {
                    if (lastPosture == MeasurementKind.GOOD_POSTURE) {
                        final Duration duration = Duration.between(lastTimestamp, now);
                        getMeasurementDispatcher().dispatch(new Measurement(Date.from(Instant.from(now.atZone(ZoneId.systemDefault()))), MeasurementKind.GOOD_POSTURE, duration.toNanos() * 0.000000001));
                    } else {
                        lastPosture = MeasurementKind.GOOD_POSTURE;
                    }
                } else {
                    if (lastPosture == MeasurementKind.BAD_POSTURE) {
                        final Duration duration = Duration.between(lastTimestamp, now);
                        getMeasurementDispatcher().dispatch(new Measurement(Date.from(Instant.from(now.atZone(ZoneId.systemDefault()))), MeasurementKind.BAD_POSTURE, duration.toNanos() * 0.000000001));
                    } else {
                        lastPosture = MeasurementKind.BAD_POSTURE;
                    }
                }

                System.out.println(lastPosture.toString());
                lastTimestamp = now;
            }
        });
        //handle sensor evaluation of good posture
        connection.addCharacteristicListener(new BaseCharacteristicListener(MEASUREMENTS_SERVICE,POSTURE_CORRECTION){
                                                 @Override
                                                 public void onChange(byte[] value) {
                                                     if (ignoreSensorEvaluation)
                                                         return;

                                                     final LocalDateTime now = LocalDateTime.now();

                                                     byte[] bytes = value;
                                                     ByteBuffer byteBuffer = ByteBuffer.wrap(bytes).asReadOnlyBuffer();
                                                     String s = Arrays.toString(bytes);

                                                     final double[] parsed = new double[]{
                                                             byteBuffer.get(0),byteBuffer.get(1),byteBuffer.get(2)
                                                     };

                                                     System.out.println("Result: " + s + parsed[0] + "|" + parsed[1] + "|" + parsed[2]);

                                                     if (isGoodPosture_Sensor(parsed[1])) { //new reading is good posture
                                                         if (lastPosture != null) {
                                                             final Duration duration = Duration.between(lastTimestamp, now);
                                                             getMeasurementDispatcher().dispatch(new Measurement(Date.from(Instant.from(now.atZone(ZoneId.systemDefault()))), MeasurementKind.BAD_POSTURE, duration.toNanos() * 0.000000001));
                                                         } else { //if no previous reading
                                                             lastPosture = MeasurementKind.GOOD_POSTURE;
                                                         }
                                                     } else { //new reading is bad posture
                                                         if (lastPosture !=null) {
                                                             final Duration duration = Duration.between(lastTimestamp, now);
                                                             getMeasurementDispatcher().dispatch(new Measurement(Date.from(Instant.from(now.atZone(ZoneId.systemDefault()))), MeasurementKind.GOOD_POSTURE, duration.toNanos() * 0.000000001));
                                                         } else { //if no previous reading
                                                             lastPosture = MeasurementKind.BAD_POSTURE;
                                                         }
                                                     }
                                                     System.out.println(lastPosture.toString());
                                                     lastTimestamp = now;
                                                 }
                                             }
        );
    }

    @Override
    public void disable() {
        super.disable();
    }

    @Override
    public void enable() {
        attachObservers();

        lastBodyPosition = null;
        lastPosture = null;
        lastTimestamp = null;

        getConnection().writeCharacteristic(MEASUREMENTS_SERVICE, CHARACTERISTIC, new byte[]{1 & 0xFF});

        super.enable();
    }

    private void updateCurrentVibrationValues(BluetoothConnection connection)
    {
        //to update vibration values
        for(int i=0; i<connection.getServices().size();i++) {
            for (int j = 0; j < connection.getServices().get(i).getCharacteristics().size(); j++) {
                if(connection.getServices().get(i).getCharacteristics().get(j).getValue() != null && connection.getServices().get(i).getCharacteristics().get(j).getUuid().equals(VIBRATION_INTERVAL_CHARACTERISTIC)) {
                    byte[] bytes2 = connection.getServices().get(i).getCharacteristics().get(j).getValue();
                    ByteBuffer byteBuffer2 = ByteBuffer.wrap(bytes2).asReadOnlyBuffer();
                    final double[] parsed2 = new double[]{
                            byteBuffer2.get(0), byteBuffer2.get(1), byteBuffer2.get(2), byteBuffer2.get(3),
                            byteBuffer2.get(4), byteBuffer2.get(5), byteBuffer2.get(6), byteBuffer2.get(7)
                    };
                    System.out.println("This is from the vibration settings: " +
                            parsed2[0] + "|" + parsed2[1] + "|" + parsed2[2] + "|" + parsed2[3] + "|" + parsed2[4] + "|" + parsed2[5] + "|" +
                            parsed2[6] + "|" + parsed2[7]);
                }
            }
        }
    }

    private void updateCurrentPostureValues(BluetoothConnection connection)
    {
        //to update posture values, because the sensor only does it when something changes (notification)
        for(int i=0; i<connection.getServices().size();i++) {
            for (int j = 0; j < connection.getServices().get(i).getCharacteristics().size(); j++) {
                if(connection.getServices().get(i).getCharacteristics().get(j).getValue() != null && connection.getServices().get(i).getCharacteristics().get(j).getUuid().equals(POSTURE_CORRECTION)) {
                    byte[] bytes2 = connection.getServices().get(i).getCharacteristics().get(j).getValue();
                    ByteBuffer byteBuffer2 = ByteBuffer.wrap(bytes2).asReadOnlyBuffer();
                    final double[] parsed2 = new double[]{
                            byteBuffer2.get(0), byteBuffer2.get(1), byteBuffer2.get(2)
                    };
                    System.out.println("This is from the sensor posture evaluation: " +
                            parsed2[0] + "|" + parsed2[1] + "|" + parsed2[2]);
                    countTimePosture(parsed2[1]);
                }
            }
        }
    }

    private void countTimePosture(double value)
    {
        final LocalDateTime now = LocalDateTime.now();

        if (isGoodPosture_Sensor(value)) {
            if (lastPosture == MeasurementKind.GOOD_POSTURE) {
                final Duration duration = Duration.between(lastTimestamp, now);
                getMeasurementDispatcher().dispatch(new Measurement(Date.from(Instant.from(now.atZone(ZoneId.systemDefault()))), MeasurementKind.GOOD_POSTURE, duration.toNanos() * 0.000000001));
            } else {
                lastPosture = MeasurementKind.GOOD_POSTURE;
            }
        } else {
            if (lastPosture == MeasurementKind.BAD_POSTURE) {
                final Duration duration = Duration.between(lastTimestamp, now);
                getMeasurementDispatcher().dispatch(new Measurement(Date.from(Instant.from(now.atZone(ZoneId.systemDefault()))), MeasurementKind.BAD_POSTURE, duration.toNanos() * 0.000000001));
            } else {
                lastPosture = MeasurementKind.BAD_POSTURE;
            }
        }
        System.out.println(lastPosture.toString());
        lastTimestamp = now;
    }

    private byte[] vibrationMessage (int angle,int interval, boolean showPattern, int pattern, int strength)
    {
        byte[] message = new byte [8];
        //[angle, interval1, interval2, pattern

        //angle: can be 1 to 6, 1 is the most strict, 6 is the most relaxed
        if (angle < 1 || angle > 6)
        { //default: 1
            message[0] = 0x01;
        }
        else
        {
            message[0] = (byte)angle;
        }
        //interval: can be 5, 15, 30 or 60 seconds
        if (interval == 5) {
            message[1] = 0x32;
            message[2] = 0x00;
        }
        else if (interval == 15)
        {
            message[1] = (byte)0x96;
            message[2] = 0x00;
        }
        else if (interval == 30)
        {
            message[1] = 0x2c;
            message[2] = 0x01;
        }
        else if (interval == 60)
        {
            message[1] = 0x58;
            message[2] = 0x02;
        }
        else
        { //default: 5 seconds
            message[1] = 0x32;
            message[2] = 0x00;
        }
        //pattern: 2 numbers
        //0-only setup pattern, 8-sensor vibrates pattern and setup (used in upright app settings)
        //0-long,1-medium,2-short,3-rampup,4-knockknock,5-heartbeat,6-tuktuk,7-ecstatic,8-muzzle
        if (showPattern)
        {
            message[3] = (byte) (((byte)0x80)+pattern);
        }
        else
        {
            message[3] = (byte) (((byte)0x00)+pattern);
        }
        //strength: 1 to 3, vibration strength, 1 -> gentle, 2 -> medium, 3 -> strong
        if (strength == 1)
        {
            message[4] = 0x46;
        }
        else if (strength == 2)
        {
            message[4] = 0x23;
        }
        else if (strength == 3)
        {
            message[4] = 0x01;
        }
        else
        { //default: 1
            message[4] = 0x46;
        }

        //still undefined, same for all TODO
        message[5] = 0x02;
        message[6] = 0x64;
        message[7] = 0x00;

        byte[] finalMessage = new byte[]{message[0],message[1],message[2],message[3],message[4],message[5],message[6],message[7]};
        return finalMessage;
    }

    //definition of good posture for the citizen hub (time shown), may be changed as desired.
    private boolean isGoodPosture_Accelerometer(double acc1, double acc2, double acc3) {
        int threshold_acc1 = 0, threshold_acc2 = -200, threshold_acc3;
        return acc2>threshold_acc2;
        //change this condition to consider Good Posture, after calibration
    }
    //
    private boolean isGoodPosture_Sensor(double posture)
    {
        //here only the good posture is tested, but the sensor also provides information
        //of when it's vibrating, etc... This can also be included later in the UI
        if (posture == 0) //good
            return true;
        else if (posture == 1) //bad
            return false;
        else
        {
            //we can check for errors later here
            return false;
        }
    }
}
