package pt.uninova.s4h.citizenhub.connectivity.wearos;

import pt.uninova.s4h.citizenhub.connectivity.AgentFactory;
import pt.uninova.s4h.citizenhub.connectivity.Connection;
import pt.uninova.s4h.citizenhub.data.Device;
import pt.uninova.s4h.citizenhub.service.CitizenHubService;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

public class WearOsAgentFactory implements AgentFactory<WearOSAgent> {

    private final CitizenHubService service;

    public WearOsAgentFactory(CitizenHubService citizenHubService) {
        this.service = citizenHubService;
    }

    @Override
    public void create(String address, Observer<WearOSAgent> observer) {
        WearOSConnection wearOSConnection = service.getWearOSMessageService().connect(address, address, service);

        wearOSConnection.addConnectionStateChangeListener(value -> {
            if (value.getNewState() == WearOSConnectionState.READY) {
                observer.observe(new WearOSAgent(wearOSConnection, service, service.getApplicationContext()));
            }
        });

        wearOSConnection.enable();
    }

    @Override
    public void create(String address, Class<?> c, Observer<WearOSAgent> observer) {
        create(address, observer);
    }

    @Override
    public void create(Device device, Observer<WearOSAgent> observer) {
        WearOSConnection wearOSConnection = service.getWearOSMessageService().connect(device.getAddress(), device.getName(), service);

        wearOSConnection.addConnectionStateChangeListener(value -> {
            if (value.getNewState() == WearOSConnectionState.READY) {
                observer.observe(new WearOSAgent(wearOSConnection, service, service.getApplicationContext()));
            }
        });

        wearOSConnection.enable();
    }

    @Override
    public void create(Device device, Class<?> c, Observer<WearOSAgent> observer) {

    }

    @Override
    public void create(Connection connection, Observer<WearOSAgent> observer) {

    }
}
