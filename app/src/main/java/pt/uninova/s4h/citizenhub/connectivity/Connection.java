package pt.uninova.s4h.citizenhub.connectivity;

public interface Connection {

    public static final int CONNECTION_KIND_UNKNOWN = 0;
    public static final int CONNECTION_KIND_BLUETOOTH = 1;
    public static final int CONNECTION_KIND_WEAROS = 2;

    int getConnectionKind();

    String getAddress();

    void connect();

    void disconnect();

    void close();
}
