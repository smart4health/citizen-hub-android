package pt.uninova.s4h.citizenhub.connectivity;

import pt.uninova.s4h.citizenhub.connectivity.bluetooth.BluetoothConnection;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.BluetoothConnectionState;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

public interface Connection {

    public static final int CONNECTION_KIND_UNKNOWN = 0;
    public static final int CONNECTION_KIND_BLUETOOTH = 1;
    public static final int CONNECTION_KIND_WEAROS = 2;

    int getConnectionKind();

    String getAddress();

    void connect();

    void disconnect();

    void close();

    int getConnectionState();

    void addConnectionStateChangeListener(Observer<StateChangedMessage<BluetoothConnectionState, BluetoothConnection>> observer);

}
