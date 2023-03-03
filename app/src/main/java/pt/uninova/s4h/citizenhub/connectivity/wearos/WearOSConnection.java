package pt.uninova.s4h.citizenhub.connectivity.wearos;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import pt.uninova.s4h.citizenhub.connectivity.Connection;
import pt.uninova.s4h.citizenhub.connectivity.StateChangedMessage;
import pt.uninova.s4h.citizenhub.data.Device;
import pt.uninova.s4h.citizenhub.service.CitizenHubService;
import pt.uninova.s4h.citizenhub.util.messaging.Dispatcher;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

public class WearOSConnection {
    private final String address;
    private final String name;
    private final Dispatcher<StateChangedMessage<WearOSConnectionState, WearOSConnection>> stateChangedMessageDispatcher;
    private final Map<Integer, Set<ChannelListener>> channelListenerMap;
    private WearOSConnectionState state;
    CitizenHubService service;

    public WearOSConnection(String address, String name, CitizenHubService service) {
        this.address = address;
        this.name = name;
        this.service = service;

        state = WearOSConnectionState.DISCONNECTED;
        stateChangedMessageDispatcher = new Dispatcher<>();
        channelListenerMap = new ConcurrentHashMap<>();

    }

    public String getAddress() {
        return address;
    }

    public String getName() {
        return name;
    }

    public Device getSource() {
        return new Device(address, name, Connection.CONNECTION_KIND_WEAROS);
    }

    public void addConnectionStateChangeListener(Observer<StateChangedMessage<WearOSConnectionState, WearOSConnection>> listener) {
        stateChangedMessageDispatcher.addObserver(listener);
    }

    public void addChannelListener(ChannelListener listener) {
        final int key = listener.getChannelName();

        synchronized (channelListenerMap) {
            if (!channelListenerMap.containsKey(key)) {
                channelListenerMap.put(key, Collections.newSetFromMap(new ConcurrentHashMap<>()));
            }
        }
        Objects.requireNonNull(channelListenerMap.get(key)).add(listener);
    }

    public void onCharacteristicChanged(String[] messageArray) {
        final int key = Integer.parseInt(messageArray[2]);

        if (channelListenerMap.containsKey(key)) {
            for (ChannelListener i : Objects.requireNonNull(channelListenerMap.get(key))) {
                i.onChange(getValue(messageArray), getTimeStamp(messageArray), getWearSampleId(messageArray));
            }
        }
    }

    public double getValue(String[] message) {
        return Double.parseDouble(message[0]);
    }


    public Date getTimeStamp(String[] message) {
        String timeStampString = message[1];
        return new Date(Long.parseLong(timeStampString));
    }

    public Long getWearSampleId(String[] message) {
        String timeStampString = message[3];
        return Long.parseLong(timeStampString);
    }

    public void close() {
        channelListenerMap.clear();
        stateChangedMessageDispatcher.close();
    }

    private void setState(WearOSConnectionState value) {
        if (value != state) {
            final WearOSConnectionState oldValue = state;

            this.state = value;
            stateChangedMessageDispatcher.dispatch(new StateChangedMessage<>(state, oldValue, this));
        }
    }

    public void disable() {
        setState(WearOSConnectionState.DISCONNECTED);
    }

    public void enable() {
        setState(WearOSConnectionState.READY);
    }
}
