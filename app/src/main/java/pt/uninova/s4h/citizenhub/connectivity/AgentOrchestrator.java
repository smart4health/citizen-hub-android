package pt.uninova.s4h.citizenhub.connectivity;

import static pt.uninova.s4h.citizenhub.connectivity.Connection.CONNECTION_KIND_BLUETOOTH;

import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import pt.uninova.s4h.citizenhub.connectivity.bluetooth.BluetoothAgent;
import pt.uninova.s4h.citizenhub.data.Device;
import pt.uninova.s4h.citizenhub.data.Sample;
import pt.uninova.s4h.citizenhub.util.UUIDv5;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

public class AgentOrchestrator {

    private static UUIDv5 NAMESPACE_GENERATOR;

    static {
        try {
            NAMESPACE_GENERATOR = new UUIDv5("pt.uninova");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private final Map<Device, Agent> agentMap;
    private final Map<Integer, AgentFactory<? extends Agent>> agentFactoryMap;

    private final List<AgentOrchestratorListener> listeners;

    private final Observer<Sample> ingester;

    public AgentOrchestrator(Observer<Sample> ingester) {
        this(new HashMap<>(), ingester);
    }

    public AgentOrchestrator(Map<Integer, AgentFactory<? extends Agent>> agentFactoryMap, Observer<Sample> ingester) {
        this.agentMap = new HashMap<>();
        this.agentFactoryMap = agentFactoryMap;
        this.listeners = new LinkedList<>();
        this.ingester = ingester;
    }

    public static UUIDv5 namespaceGenerator() {
        return NAMESPACE_GENERATOR;
    }

    private final Observer<StateChangedMessage<Integer, ? extends Agent>> agentStateObserver = value -> {
        tellOnAgentStateChanged(value.getSource());
    };

    public void add(Device device) {
        add(device, value -> {
        });
    }

    public void add(Device device, Observer<Agent> observer) {
        final AgentFactory<? extends Agent> factory = agentFactoryMap.get(device.getConnectionKind());

        if (factory != null) {
            put(device, null);
            tellOnDeviceAdded(device);

            factory.create(device.getAddress(), (agent) -> {
                put(device, agent);
                agent.addSampleObserver(ingester);
                tellOnAgentAttached(device, agent);
                observer.observe(agent);
            });
        } else {
            observer.observe(null);
        }
    }

    public void add(Device device, Class<? extends Agent> agentClass, Observer<Agent> observer) {
        final AgentFactory<? extends Agent> factory = agentFactoryMap.get(device.getConnectionKind());

        if (factory != null) {
            put(device, null);
            tellOnDeviceAdded(device);

            factory.create(device.getAddress(), agentClass, (agent) -> {
                put(device, agent);
                agent.addSampleObserver(ingester);
                tellOnAgentAttached(device, agent);
                agent.addStateObserver(agentStateObserver);
                observer.observe(agent);
            });
        } else {
            observer.observe(null);
        }
    }

    public void add(Agent agent) {
        final Device device = agent.getSource();

        put(device, agent);
        tellOnDeviceAdded(device);

        agent.addSampleObserver(ingester);
        tellOnAgentAttached(device, agent);
    }

    public void addListener(AgentOrchestratorListener listener) {
        this.listeners.add(listener);
    }

    public void clear() {
        this.agentMap.clear();
        this.agentFactoryMap.clear();
        this.listeners.clear();
    }

    public Agent getAgent(Device device) {
        return agentMap.get(device);
    }

    public Set<Device> getDevices() {
        return Collections.unmodifiableSet(new TreeSet<>(agentMap.keySet()));
    }

    public void enableDevice(Device device) {
        if (device.getConnectionKind() == CONNECTION_KIND_BLUETOOTH) {
            BluetoothAgent agent = ((BluetoothAgent) getAgent(device));
            agent.getConnection().reconnect();
        }
    }

    public int getRunnableSize(Device device) {
        if (device.getConnectionKind() == CONNECTION_KIND_BLUETOOTH) {
            BluetoothAgent agent = ((BluetoothAgent) getAgent(device));
            return agent.getConnection().getRunnableSize();
        } else return 0;
    }

    public void enableAll(int connectionType) {
        for (Device device : getDevices()) {
            if (device.getConnectionKind() == connectionType) {
                final BluetoothAgent agent = ((BluetoothAgent) getAgent(device));

                if (agent != null) {
                    agent.getConnection().reconnect();
                }
            }
        }
    }

    public void identify(Device device, Observer<Agent> observer) {
        final AgentFactory<? extends Agent> factory = agentFactoryMap.get(device.getConnectionKind());

        if (factory != null) {
            factory.create(device, observer::observe);
        }
    }

    private void put(Device device, Agent agent) {
        agentMap.put(device, agent);
    }

    public void remove(Device device) {
        final Agent agent = getAgent(device);

        if (agent != null) {
            agent.removeStateObserver(agentStateObserver);
            agent.removeSampleObserver(ingester);
            tellOnAgentRemoved(device, agent);
        }

        agentMap.remove(device);
        tellOnDeviceRemoved(device);
    }

    public void removeListener(AgentOrchestratorListener listener) {
        this.listeners.remove(listener);
    }

    private void tellOnAgentStateChanged(Agent agent) {
        for (AgentOrchestratorListener i : listeners) {
            i.onAgentStateChanged(agent);
        }
    }

    private void tellOnAgentAttached(Device device, Agent agent) {
        for (AgentOrchestratorListener i : listeners) {
            i.onAgentAttached(device, agent);
        }
    }

    private void tellOnAgentRemoved(Device device, Agent agent) {
        for (AgentOrchestratorListener i : listeners) {
            i.onAgentRemoved(device, agent);
        }
    }

    private void tellOnDeviceAdded(Device device) {
        for (AgentOrchestratorListener i : listeners) {
            i.onDeviceAdded(device);
        }
    }

    private void tellOnDeviceRemoved(Device device) {
        for (AgentOrchestratorListener i : listeners) {
            i.onDeviceRemoved(device);
        }
    }
}
