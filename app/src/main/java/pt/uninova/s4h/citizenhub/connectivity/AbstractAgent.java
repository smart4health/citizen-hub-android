package pt.uninova.s4h.citizenhub.connectivity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import pt.uninova.s4h.citizenhub.data.Device;
import pt.uninova.s4h.citizenhub.data.Sample;
import pt.uninova.s4h.citizenhub.util.messaging.Dispatcher;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

public abstract class AbstractAgent implements Agent {

    private final UUID id;
    private final Device source;

    private int state;

    private final Set<UUID> supportedProtocolsIds;
    private final Set<Integer> supportedMeasurements;

    private final Map<UUID, Protocol> protocolMap;
    private final Map<Integer, MeasuringProtocol> measurementMap;

    private final Dispatcher<StateChangedMessage<Integer, ? extends Agent>> stateChangedDispatcher;
    private final Dispatcher<Sample> sampleDispatcher;

    private final Set<AgentListener> agentListenerSet;

    private final SettingsManager settingsManager;

    protected AbstractAgent(UUID id, Device source, Set<UUID> supportedProtocolsIds, Set<Integer> supportedMeasurements, SettingsManager settingsManager) {
        this.id = id;
        this.source = source;

        this.supportedProtocolsIds = supportedProtocolsIds;
        this.supportedMeasurements = supportedMeasurements;

        this.protocolMap = new HashMap<>(supportedProtocolsIds.size());
        this.measurementMap = new HashMap<>(supportedMeasurements.size());

        this.stateChangedDispatcher = new Dispatcher<>();
        this.sampleDispatcher = new Dispatcher<>();

        this.agentListenerSet = new HashSet<>();

        this.settingsManager = settingsManager;
    }

    @Override
    public void addAgentListener(AgentListener agentListener) {
        agentListenerSet.add(agentListener);
    }

    @Override
    public void addSampleObserver(Observer<Sample> observer) {
        this.sampleDispatcher.addObserver(observer);
    }

    @Override
    public void addStateObserver(Observer<StateChangedMessage<Integer, ? extends Agent>> observer) {
        this.stateChangedDispatcher.addObserver(observer);
    }

    @Override
    public void disable() {
        final Integer[] measurements = this.measurementMap.keySet().toArray(new Integer[0]);

        for (final int i : measurements) {
            disableMeasurement(i);
        }

        for (final Protocol i : this.protocolMap.values()) {
            i.disable();
        }

        protocolMap.clear();

        setState(Agent.AGENT_STATE_DISABLED);
    }

    @Override
    public void disableMeasurement(int measurementKind) {
        final MeasuringProtocol protocol = this.measurementMap.get(measurementKind);

        if (protocol != null) {
            this.measurementMap.remove(measurementKind);
            disableProtocol(protocol);
        }

        tellOnMeasurementDisabled(measurementKind);
    }

    @Override
    public void disableProtocol(Protocol protocol) {
        final UUID protocolId = protocol.getId();
        final Protocol currentProtocol = protocolMap.get(protocolId);

        if (currentProtocol != null && currentProtocol == protocol) {
            protocolMap.remove(protocolId);
            protocol.disable();
        }

        tellOnProtocolDisabled(protocol);
    }

    @Override
    public void enable() {
        setState(Agent.AGENT_STATE_ENABLED);
    }

    @Override
    public void enableMeasurement(int measurementKind) {
        System.out.println("AbstractAgent.enableMeasurement measurementKind=" + measurementKind);

        if (getState() != Agent.AGENT_STATE_ENABLED) {
            this.addStateObserver(new Observer<StateChangedMessage<Integer, ? extends Agent>>() {

                @Override
                public void observe(StateChangedMessage<Integer, ? extends Agent> message) {
                    if (message.getNewState() == Agent.AGENT_STATE_ENABLED) {
                        AbstractAgent.this.enableMeasurement(measurementKind);
                        removeStateObserver(this);
                    }
                }
            });

            return;
        }

        final MeasuringProtocol protocol = getMeasuringProtocol(measurementKind);

        if (protocol != null) {
            this.measurementMap.put(measurementKind, protocol);
            enableProtocol(protocol);
        }

        tellOnMeasurementEnabled(measurementKind);
    }

    @Override
    public void enableProtocol(Protocol protocol) {
        final UUID protocolId = protocol.getId();

        if (!supportedProtocolsIds.contains(protocolId)) {
            return;
        }

        final Protocol currentProtocol = protocolMap.get(protocolId);

        if (currentProtocol == null || currentProtocol != protocol) {
            protocolMap.put(protocolId, protocol);
        }

        if (currentProtocol != null) {
            disableProtocol(currentProtocol);
        }

        protocol.enable();

        tellOnProtocolEnabled(protocol);
    }

    @Override
    public Set<Integer> getEnabledMeasurements() {
        return measurementMap.keySet();
    }

    @Override
    public Set<UUID> getEnabledProtocols(){
        return protocolMap.keySet();
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public Protocol getProtocol(UUID protocolId) {
        final Protocol protocol = protocolMap.get(protocolId);

        if (protocol == null) {
            if (supportedProtocolsIds.contains(protocolId)) {
                return null; //TODO Add exceptions
            }

            return null; //TODO Add factory method
        } else {
            return protocol;
        }
    }

    protected abstract MeasuringProtocol getMeasuringProtocol(int kind);

    protected Dispatcher<Sample> getSampleDispatcher() {
        return sampleDispatcher;
    }

    @Override
    public SettingsManager getSettingsManager() {
        return settingsManager;
    }

    @Override
    public Device getSource() {
        return source;
    }

    @Override
    public int getState() {
        return state;
    }

    @Override
    public Set<Integer> getSupportedMeasurements() {
        return this.supportedMeasurements;
    }

    @Override
    public Set<UUID> getSupportedProtocolsIds() {
        return this.supportedProtocolsIds;
    }

    @Override
    public void removeAgentListener(AgentListener agentListener) {
        agentListenerSet.remove(agentListener);
    }

    @Override
    public void removeAllAgentListeners() {
        agentListenerSet.clear();
    }

    @Override
    public void removeSampleObserver(Observer<Sample> observer) {
        this.sampleDispatcher.removeObserver(observer);
    }

    @Override
    public void removeStateObserver(Observer<StateChangedMessage<Integer, ? extends Agent>> observer) {
        this.stateChangedDispatcher.removeObserver(observer);
    }

    protected synchronized void setState(int value) {
        if (state != value) {
            final int oldState = state;

            state = value;

            stateChangedDispatcher.dispatch(new StateChangedMessage<>(value, oldState, this));
        }
    }

    protected void tellOnMeasurementDisabled(int measurementKind) {
        for (AgentListener i : agentListenerSet) {
            i.onMeasurementDisabled(this, measurementKind);
        }
    }

    protected void tellOnMeasurementEnabled(int measurementKind) {
        for (AgentListener i : agentListenerSet) {
            i.onMeasurementEnabled(this, measurementKind);
        }
    }

    protected void tellOnProtocolDisabled(Protocol protocol) {
        for (AgentListener i : agentListenerSet) {
            i.onProtocolDisabled(this, protocol);
        }
    }

    protected void tellOnProtocolEnabled(Protocol protocol) {
        for (AgentListener i : agentListenerSet) {
            i.onProtocolEnabled(this, protocol);
        }
    }

    protected void tellOnStateChanged(int state){
        for(AgentListener i : agentListenerSet){
            i.onStateChanged(this, state);
        }
    }
}
