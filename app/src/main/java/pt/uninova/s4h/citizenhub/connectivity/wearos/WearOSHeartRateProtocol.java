package pt.uninova.s4h.citizenhub.connectivity.wearos;

import android.util.Log;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

import pt.uninova.s4h.citizenhub.connectivity.AbstractMeasuringProtocol;
import pt.uninova.s4h.citizenhub.connectivity.AgentOrchestrator;
import pt.uninova.s4h.citizenhub.connectivity.Protocol;
import pt.uninova.s4h.citizenhub.data.HeartRateMeasurement;
import pt.uninova.s4h.citizenhub.data.Measurement;
import pt.uninova.s4h.citizenhub.data.Sample;
import pt.uninova.s4h.citizenhub.service.CitizenHubService;
import pt.uninova.s4h.citizenhub.util.messaging.Dispatcher;

public class WearOSHeartRateProtocol extends AbstractMeasuringProtocol {

    final public static UUID ID = AgentOrchestrator.namespaceGenerator().getUUID("wearos.wear.heartrate");
    final private static int kind = Measurement.TYPE_HEART_RATE;
    final private static int wearProtocolDisable = 100000, wearProtocolEnable = 100001;
    final String TAG = "WearOSHeartRateProtocol";
    CitizenHubService service;

    protected WearOSHeartRateProtocol(WearOSConnection connection, Dispatcher<Sample> sampleDispatcher, WearOSAgent agent, CitizenHubService service) {
        super(ID, agent,sampleDispatcher);
        Log.d(TAG, "Entered");
        this.service = service;

        connection.addChannelListener(new BaseChannelListener(kind) {
            @Override
            public void onChange(double value, Date timestamp) {
                service.getWearOSMessageService().sendMessage("WearOSConnected","true");
                final int heartRate = (int) value;
                Set<Integer> enabledMeasurements = getAgent().getEnabledMeasurements();
                if(value<wearProtocolDisable){
                    final Sample sample = new Sample(timestamp.toInstant(), getAgent().getSource(),
                            new HeartRateMeasurement(heartRate));
                    getSampleDispatcher().dispatch(sample);

                    if(!enabledMeasurements.contains(Measurement.TYPE_HEART_RATE)){
                        getAgent().enableMeasurement(Measurement.TYPE_HEART_RATE);
                    }
                }
                else{
                    if(value==wearProtocolDisable && enabledMeasurements.contains(Measurement.TYPE_HEART_RATE))
                        getAgent().disableMeasurement(Measurement.TYPE_HEART_RATE);
                    else if(value==wearProtocolEnable && !enabledMeasurements.contains(Measurement.TYPE_HEART_RATE))
                        getAgent().enableMeasurement(Measurement.TYPE_HEART_RATE);
                }
            }
        });
    }

    @Override
    public void disable() {
        setState(Protocol.STATE_DISABLED);
        service.getWearOSMessageService().sendMessage("WearOSHeartRateProtocol","false");
    }

    @Override
    public void enable() {
        setState(Protocol.STATE_ENABLED);
        service.getWearOSMessageService().sendMessage("WearOSHeartRateProtocol","true");
    }
}