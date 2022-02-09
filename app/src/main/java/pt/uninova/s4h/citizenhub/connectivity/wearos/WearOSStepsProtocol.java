package pt.uninova.s4h.citizenhub.connectivity.wearos;

import android.util.Log;

import java.util.Date;
import java.util.UUID;

import pt.uninova.s4h.citizenhub.connectivity.AbstractMeasuringProtocol;
import pt.uninova.s4h.citizenhub.connectivity.AgentOrchestrator;
import pt.uninova.s4h.citizenhub.persistence.Measurement;
import pt.uninova.s4h.citizenhub.persistence.MeasurementKind;


public class WearOSStepsProtocol extends AbstractMeasuringProtocol {
    final public static UUID ID = AgentOrchestrator.namespaceGenerator().getUUID("wearos.wear.steps");
    final private static MeasurementKind channelName = MeasurementKind.STEPS;
    private static final String TAG = "WearOSStepsProtocol";
    private final WearOSConnection connection;

    protected WearOSStepsProtocol(WearOSConnection connection, WearOSAgent agent) {
        super(ID, agent);
        this.connection = connection;
        Log.d(TAG, "Entered");

        connection.addChannelListener(new BaseChannelListener(channelName) {
            @Override
            public void onChange(double value, Date timestamp) {

                //getSampleDispatcher().dispatch(new Measurement(timestamp, MeasurementKind.STEPS, value));
                Log.d(TAG, "dispatch " + timestamp + " and " + value);
            }
        });
    }
}
