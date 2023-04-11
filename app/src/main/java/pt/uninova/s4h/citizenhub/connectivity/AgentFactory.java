package pt.uninova.s4h.citizenhub.connectivity;

import pt.uninova.s4h.citizenhub.data.Device;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

public interface AgentFactory<T extends Agent> {

    void create(String address, Observer<T> observer);

    void create(String address, Class<?> c, Observer<T> observer);

    void create(Device device, Observer<T> observer);

    void create(Device device, Class<?> c, Observer<T> observer);

    void create(Connection connection, Class<?> c, Observer<T> observer);

}
