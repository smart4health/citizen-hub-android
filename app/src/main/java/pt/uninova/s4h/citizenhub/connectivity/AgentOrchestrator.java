package pt.uninova.s4h.citizenhub.connectivity;

import pt.uninova.s4h.citizenhub.persistence.Device;
import pt.uninova.s4h.citizenhub.persistence.DeviceRepository;
import pt.uninova.s4h.citizenhub.persistence.FeatureRepository;
import pt.uninova.s4h.citizenhub.persistence.MeasurementRepository;
import pt.uninova.s4h.citizenhub.service.CitizenHubService;
import pt.uninova.util.UUIDv5;

import java.security.NoSuchAlgorithmException;
import java.util.*;

public class AgentOrchestrator {

    private static final String TAG = "AgentOrchestrator";
    private static UUIDv5 NAMESPACE_GENERATOR;

    static {
        try {
            NAMESPACE_GENERATOR = new UUIDv5("pt.uninova");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private final CitizenHubService service;
    private final DeviceRepository deviceRepository;
    private final FeatureRepository featureRepository;
    private final MeasurementRepository measurementRepository;
    private final AgentFactory agentFactory;

    private final Map<Device, Agent> deviceAgentMap;

    public AgentOrchestrator(CitizenHubService service) {
        this.service = service;

        deviceRepository = new DeviceRepository(service.getApplication());
        featureRepository = new FeatureRepository(service.getApplication());
        measurementRepository = new MeasurementRepository(service.getApplication());
        agentFactory = new AgentFactory(service);

        deviceAgentMap = new HashMap<>();


        /*
        PlaceboAllProtocol placeboAllProtocol = new PlaceboAllProtocol(null);
        placeboAllProtocol.getMeasurementObservers().add(measurementRepository::add);
        placeboAllProtocol.enable();
        */

        deviceRepository.getAll().observe(service, devices -> {
            final Set<Device> found = new HashSet<>(devices.size());
            System.out.println("=== Change in device list ===");
            for (Device i : devices) {
                System.out.println("== " + i.getName() + ":" + i.getAddress() + " ==");
                found.add(i);

                if (!deviceAgentMap.containsKey(i)) {
                    System.out.println("= First time found =");
                    agentFactory.create(i, agent -> {
                        for (UUID j : agent.getPublicProtocolIds()) {

                            MeasuringProtocol p = (MeasuringProtocol) agent.getProtocol(j);

                            p.getMeasurementObservers().add(measurementRepository::add);
                        }

                        agent.enable();

                        deviceAgentMap.put(i, agent);
                    });
                }
            }

            System.out.println("=== Done ===");
            trimAgents(found);
        });
    }

    public static UUIDv5 namespaceGenerator() {
        return NAMESPACE_GENERATOR;
    }

    private void trimAgents(Set<Device> devices) {
        for (Device i : deviceAgentMap.keySet()) {
            if (!devices.contains(i)) {
                final Agent agent = deviceAgentMap.get(i);

                if (agent != null) {
                    agent.disable();
                }

                deviceAgentMap.remove(i);
            }
        }
    }

    public void close() {
        for (Device i : deviceAgentMap.keySet()) {
            deviceAgentMap.get(i).disable();
        }
    }
}
