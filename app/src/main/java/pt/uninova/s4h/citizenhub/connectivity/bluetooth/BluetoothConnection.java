package pt.uninova.s4h.citizenhub.connectivity.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import pt.uninova.s4h.citizenhub.BuildConfig;
import pt.uninova.s4h.citizenhub.connectivity.Connection;
import pt.uninova.s4h.citizenhub.connectivity.StateChangedMessage;
import pt.uninova.s4h.citizenhub.data.Device;
import pt.uninova.s4h.citizenhub.util.Pair;
import pt.uninova.s4h.citizenhub.util.Triple;
import pt.uninova.s4h.citizenhub.util.messaging.Dispatcher;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

public class BluetoothConnection extends BluetoothGattCallback implements Connection {

    public static final byte BLE_HCI_STATUS_CODE_SUCCESS = 0x00;
    public static final byte BLE_HCI_STATUS_CODE_UNKNOWN_BTLE_COMMAND = 0x01;
    public static final byte BLE_HCI_STATUS_CODE_UNKNOWN_CONNECTION_IDENTIFIER = 0x02;
    public static final byte BLE_HCI_AUTHENTICATION_FAILURE = 0x05;
    public static final byte BLE_HCI_STATUS_CODE_PIN_OR_KEY_MISSING = 0x06;
    public static final byte BLE_HCI_MEMORY_CAPACITY_EXCEEDED = 0x07;
    public static final byte BLE_HCI_CONNECTION_TIMEOUT = 0x08;
    public static final byte BLE_HCI_STATUS_CODE_COMMAND_DISALLOWED = 0x0C;
    public static final byte BLE_HCI_STATUS_CODE_INVALID_BTLE_COMMAND_PARAMETERS = 0x12;
    public static final byte BLE_HCI_REMOTE_USER_TERMINATED_CONNECTION = 0x13;
    public static final byte BLE_HCI_REMOTE_DEV_TERMINATION_DUE_TO_LOW_RESOURCES = 0x14;
    public static final byte BLE_HCI_REMOTE_DEV_TERMINATION_DUE_TO_POWER_OFF = 0x15;
    public static final byte BLE_HCI_LOCAL_HOST_TERMINATED_CONNECTION = 0x16;
    public static final byte BLE_HCI_UNSUPPORTED_REMOTE_FEATURE = 0x1A;
    public static final byte BLE_HCI_STATUS_CODE_INVALID_LMP_PARAMETERS = 0x1E;
    public static final byte BLE_HCI_STATUS_CODE_UNSPECIFIED_ERROR = 0x1F;
    public static final byte BLE_HCI_STATUS_CODE_LMP_RESPONSE_TIMEOUT = 0x22;
    public static final byte BLE_HCI_STATUS_CODE_LMP_PDU_NOT_ALLOWED = 0x24;
    public static final byte BLE_HCI_INSTANT_PASSED = 0x28;
    public static final byte BLE_HCI_PAIRING_WITH_UNIT_KEY_UNSUPPORTED = 0x29;
    public static final byte BLE_HCI_DIFFERENT_TRANSACTION_COLLISION = 0x2A;
    public static final byte BLE_HCI_CONTROLLER_BUSY = 0x3A;
    public static final byte BLE_HCI_CONN_INTERVAL_UNACCEPTABLE = 0x3B;
    public static final byte BLE_HCI_DIRECTED_ADVERTISER_TIMEOUT = 0x3C;
    public static final byte BLE_HCI_CONN_TERMINATED_DUE_TO_MIC_FAILURE = 0x3D;
    public static final byte BLE_HCI_CONN_FAILED_TO_BE_ESTABLISHED = 0x3E;

    private final Queue<Runnable> runnables;
    private final Map<Pair<UUID, UUID>, Set<CharacteristicListener>> characteristicListenerMap;
    private final Map<Triple<UUID, UUID, UUID>, Set<DescriptorListener>> descriptorListenerMap;
    private final Dispatcher<StateChangedMessage<BluetoothConnectionState, BluetoothConnection>> stateChangedMessageDispatcher;

    private final BluetoothDevice device;
    private BluetoothGatt gatt;

    private BluetoothConnectionState state;

    public BluetoothConnection(BluetoothDevice device) {
        this.device = device;

        runnables = new ConcurrentLinkedQueue<>();
        characteristicListenerMap = new ConcurrentHashMap<>();
        descriptorListenerMap = new ConcurrentHashMap<>();
        stateChangedMessageDispatcher = new Dispatcher<>();

        state = BluetoothConnectionState.DISCONNECTED;
    }

    public void addCharacteristicListener(CharacteristicListener listener) {
        if (BuildConfig.DEBUG) {
            System.out.println("BluetoothConnection.addCharacteristicListener " + listener.getServiceUuid() + " " + listener.getCharacteristicUuid());
        }

        final Pair<UUID, UUID> key = characteristicKey(listener);

        synchronized (characteristicListenerMap) {
            if (!characteristicListenerMap.containsKey(key)) {
                characteristicListenerMap.put(key, Collections.newSetFromMap(new ConcurrentHashMap<CharacteristicListener, Boolean>()));
            }
        }

        characteristicListenerMap.get(key).add(listener);
    }

    public void addConnectionStateChangeListener(Observer<StateChangedMessage<BluetoothConnectionState, BluetoothConnection>> listener) {
        if (BuildConfig.DEBUG) {
            System.out.println("BluetoothConnection.addConnectionStateChangeListener");
        }

        stateChangedMessageDispatcher.addObserver(listener);
    }

    public void addDescriptorListener(DescriptorListener listener) {
        if (BuildConfig.DEBUG) {
            System.out.println("BluetoothConnection.addDescriptorListener " + listener.getServiceUuid() + " " + listener.getCharacteristicUuid() + " " + listener.getDescriptorUuid());
        }

        final Triple<UUID, UUID, UUID> key = descriptorKey(listener);

        synchronized (descriptorListenerMap) {
            if (!descriptorListenerMap.containsKey(key)) {
                descriptorListenerMap.put(key, Collections.newSetFromMap(new ConcurrentHashMap<DescriptorListener, Boolean>()));
            }
        }

        descriptorListenerMap.get(key).add(listener);
    }

    private Pair<UUID, UUID> characteristicKey(BluetoothGattCharacteristic characteristic) {
        return characteristicKey(characteristic.getService().getUuid(), characteristic.getUuid());
    }

    private Pair<UUID, UUID> characteristicKey(CharacteristicListener listener) {
        return characteristicKey(listener.getServiceUuid(), listener.getCharacteristicUuid());
    }

    private Pair<UUID, UUID> characteristicKey(UUID serviceUuid, UUID characteristicUuid) {
        return new Pair<>(serviceUuid, characteristicUuid);
    }

    public void clear() {
        runnables.clear();
    }

    public void connect() {
        device.connectGatt(null, true, this, BluetoothDevice.TRANSPORT_LE);
    }

    public void connect(int bluetoothTransport) {
        device.connectGatt(null, true, this, bluetoothTransport);
    }

    public void close() {
        characteristicListenerMap.clear();
        descriptorListenerMap.clear();
        runnables.clear();

        stateChangedMessageDispatcher.close();

        if (gatt != null) {
            disconnect();
            gatt.close();
        }
    }

    private Triple<UUID, UUID, UUID> descriptorKey(BluetoothGattDescriptor descriptor) {
        return descriptorKey(descriptor.getCharacteristic().getService().getUuid(), descriptor.getCharacteristic().getUuid(), descriptor.getUuid());
    }

    private Triple<UUID, UUID, UUID> descriptorKey(DescriptorListener listener) {
        return descriptorKey(listener.getServiceUuid(), listener.getCharacteristicUuid(), listener.getDescriptorUuid());
    }

    private Triple<UUID, UUID, UUID> descriptorKey(final UUID serviceUuid, final UUID characteristicUuid, final UUID descriptorUuid) {
        return new Triple<>(serviceUuid, characteristicUuid, descriptorUuid);
    }

    public void disableNotifications(final UUID serviceUuid, final UUID characteristicUuid) {
        if (gatt == null)
            return;

        final BluetoothGattService service = gatt.getService(serviceUuid);

        if (service != null) {
            final BluetoothGattCharacteristic characteristic = gatt.getService(serviceUuid).getCharacteristic(characteristicUuid);

            if (characteristic != null) {
                gatt.setCharacteristicNotification(characteristic, false);
                writeDescriptor(serviceUuid, characteristicUuid, BluetoothAgent.UUID_DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION, BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            }
        }
    }

    public void disconnect() {
        if (gatt != null) {
            gatt.disconnect();
        }
    }

    public void enableNotifications(final UUID serviceUuid, final UUID characteristicUuid) {
        enableNotifications(serviceUuid, characteristicUuid, false);
    }

    public void enableNotifications(final UUID serviceUuid, final UUID characteristicUuid, boolean acknowledge) {
        if (gatt != null) {
            final BluetoothGattService service = gatt.getService(serviceUuid);

            if (service != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicUuid);

                if (characteristic != null) {
                    gatt.setCharacteristicNotification(characteristic, true);
                    writeDescriptor(serviceUuid, characteristicUuid, BluetoothAgent.UUID_DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION, acknowledge ? BluetoothGattDescriptor.ENABLE_INDICATION_VALUE : BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                }
            }
        }
    }

    @Override
    public String getAddress() {
        return getDevice().getAddress();
    }

    @Override
    public int getConnectionKind() {
        return Connection.CONNECTION_KIND_BLUETOOTH;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public BluetoothConnectionState getState() {
        return this.state;
    }

    public Device getSource() {
        final BluetoothDevice device = getDevice();
        final String address = device.getAddress();
        final String name = device.getName();

        return new Device(address, name != null ? name : address, CONNECTION_KIND_BLUETOOTH);
    }

    public List<BluetoothGattService> getServices() {
        return gatt.getServices();
    }

    public boolean hasService(UUID uuid) {
        return gatt.getService(uuid) != null;
    }

    private synchronized void next() {
        if (BuildConfig.DEBUG)
            System.out.println("BluetoothConnection.next " + runnables.size());

        runnables.poll();

        if (!runnables.isEmpty()) {
            runnables.element().run();
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        if (BuildConfig.DEBUG) {
            System.out.println("BluetoothConnection.onCharacteristicChanged " + gatt.getDevice().getAddress());
            System.out.println("    " + characteristic.getService().getUuid() + " " + characteristic.getUuid());
            System.out.println("    " + Arrays.toString(characteristic.getValue()));
        }

        final Pair<UUID, UUID> key = characteristicKey(characteristic);

        if (characteristicListenerMap.containsKey(key)) {
            for (CharacteristicListener i : characteristicListenerMap.get(key)) {
                i.onChange(characteristic.getValue());
            }
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if (BuildConfig.DEBUG) {
            System.out.println("BluetoothConnection.onCharacteristicRead " + gatt.getDevice().getAddress() + " status=" + status);
            System.out.println("    " + characteristic.getService().getUuid() + " " + characteristic.getUuid());
            System.out.println("    " + Arrays.toString(characteristic.getValue()));
        }

        final Pair<UUID, UUID> key = characteristicKey(characteristic);

        if (characteristicListenerMap.containsKey(key)) {
            for (CharacteristicListener i : characteristicListenerMap.get(key)) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    i.onRead(characteristic.getValue());
                } else {
                    i.onReadFailure(characteristic.getValue(), status);
                }
            }
        }

        next();
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if (BuildConfig.DEBUG) {
            System.out.println("BluetoothConnection.onCharacteristicWrite " + gatt.getDevice().getAddress() + " status=" + status);
            System.out.println("    " + characteristic.getService().getUuid() + " " + characteristic.getUuid());
            System.out.println("    " + Arrays.toString(characteristic.getValue()));
        }

        final Pair<UUID, UUID> key = characteristicKey(characteristic);

        if (characteristicListenerMap.containsKey(key)) {
            for (CharacteristicListener i : characteristicListenerMap.get(key)) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    i.onWrite(characteristic.getValue());
                } else {
                    i.onWriteFailure(characteristic.getValue(), status);
                }
            }
        }

        next();
    }

    @Override
    public void onConnectionStateChange(final BluetoothGatt gatt, int status, int newState) {
        if (BuildConfig.DEBUG)
            System.out.println("BluetoothConnection.onConnectionStateChange " + gatt.getDevice().getAddress() + " status=" + status + " newState=" + newState);

        if (status == BLE_HCI_STATUS_CODE_SUCCESS) {
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                if (this.gatt == null) {
                    this.gatt = gatt;
                    setState(BluetoothConnectionState.CONNECTED);

                    push(gatt::discoverServices);
                } else {
                    setState(BluetoothConnectionState.READY);
                }
            } else {
                setState(BluetoothConnectionState.DISCONNECTED);
            }
        } else {
            if (newState != BluetoothGatt.STATE_CONNECTED) {
                setState(BluetoothConnectionState.DISCONNECTED);
            }
        }
    }

    @Override
    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        final Triple<UUID, UUID, UUID> key = descriptorKey(descriptor);

        if (descriptorListenerMap.containsKey(key)) {
            for (DescriptorListener i : descriptorListenerMap.get(key)) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    i.onRead(descriptor.getValue());
                } else {
                    i.onReadFailure(descriptor.getValue(), status);
                }
            }
        }

        next();
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        final Triple<UUID, UUID, UUID> key = descriptorKey(descriptor);

        if (descriptorListenerMap.containsKey(key)) {
            for (DescriptorListener i : descriptorListenerMap.get(key)) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    i.onWrite(descriptor.getValue());
                } else {
                    i.onWriteFailure(descriptor.getValue(), status);
                }
            }
        }

        next();
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        if (BuildConfig.DEBUG) {
            System.out.println("BluetoothConnection.onServicesDiscovered status=" + status);
        }

        if (status == BluetoothGatt.GATT_SUCCESS) {
            setState(BluetoothConnectionState.READY);

            if (BuildConfig.DEBUG) {
                for (BluetoothGattService i : gatt.getServices()) {
                    System.out.println(" > " + i.getUuid());

                    for (BluetoothGattCharacteristic j : i.getCharacteristics()) {
                        System.out.println(" > > " + j.getUuid());

                        for (BluetoothGattDescriptor k : j.getDescriptors()) {
                            System.out.println(" > > > " + k.getUuid());
                        }
                    }
                }
            }
        }

        next();
    }

    private synchronized void push(Runnable runnable) {
        if (BuildConfig.DEBUG)
            System.out.println("BluetoothConnection.push " + (runnables.size() + 1));

        runnables.add(runnable);

        if (runnables.size() == 1) {
            runnables.element().run();
        }
    }

    public void readCharacteristic(final UUID serviceUuid, final UUID characteristicUuid) {
        if (BuildConfig.DEBUG)
            System.out.println("BluetoothConnection.readCharacteristic serviceUuid=" + serviceUuid + " characteristicUuid=" + characteristicUuid);

        push(new Runnable() {
            @Override
            public void run() {
                final BluetoothGattCharacteristic characteristic = gatt.getService(serviceUuid).getCharacteristic(characteristicUuid);

                gatt.readCharacteristic(characteristic);
            }
        });
    }

    public void readDescriptor(final UUID serviceUuid, final UUID characteristicUuid, final UUID descriptorUuid) {
        push(new Runnable() {
            @Override
            public void run() {
                final BluetoothGattDescriptor descriptor = gatt.getService(serviceUuid).getCharacteristic(characteristicUuid).getDescriptor(descriptorUuid);

                gatt.readDescriptor(descriptor);
            }
        });
    }

    public void removeCharacteristicListener(CharacteristicListener listener) {
        if (BuildConfig.DEBUG) {
            System.out.println("BluetoothConnection.removeCharacteristicListener " + listener.getServiceUuid() + " " + listener.getCharacteristicUuid());
        }

        final Pair<UUID, UUID> key = characteristicKey(listener);

        if (characteristicListenerMap.containsKey(key)) {
            final Set<CharacteristicListener> listeners = characteristicListenerMap.get(key);

            listeners.remove(listener);

            if (listeners.isEmpty()) {
                characteristicListenerMap.remove(key);
            }
        }
    }

    public void removeConnectionStateChangeListener(Observer<StateChangedMessage<BluetoothConnectionState, BluetoothConnection>> listener) {
        stateChangedMessageDispatcher.removeObserver(listener);
    }

    public void reconnect() {
        if (gatt != null) {
            gatt.disconnect();
            gatt.close();
            gatt = null;
        }

        runnables.clear();
        connect();
    }

    public void removeDescriptorListener(DescriptorListener listener) {
        if (BuildConfig.DEBUG) {
            System.out.println("BluetoothConnection.removeDescriptorListener " + listener.getServiceUuid() + " " + listener.getCharacteristicUuid() + " " + listener.getDescriptorUuid());
        }

        final Triple<UUID, UUID, UUID> key = descriptorKey(listener);

        if (descriptorListenerMap.containsKey(key)) {
            final Set<DescriptorListener> listeners = descriptorListenerMap.get(key);

            listeners.remove(listener);

            if (listeners.isEmpty()) {
                descriptorListenerMap.remove(key);
            }
        }
    }

    private void setState(BluetoothConnectionState value) {
        if (value != state) {
            final BluetoothConnectionState oldValue = state;

            this.state = value;

            stateChangedMessageDispatcher.dispatch(new StateChangedMessage<>(state, oldValue, this));
        }
    }

    public void writeCharacteristic(final UUID serviceUuid, final UUID characteristicUuid, final byte[] value) {
        push(new Runnable() {
            @Override
            public void run() {
                final BluetoothGattCharacteristic characteristic = gatt.getService(serviceUuid).getCharacteristic(characteristicUuid);

                characteristic.setValue(value);
                gatt.writeCharacteristic(characteristic);
            }
        });
    }

    public void writeDescriptor(final UUID serviceUuid, final UUID characteristicUuid, final UUID descriptorUuid, final byte[] value) {
        if (BuildConfig.DEBUG) {
            System.out.println("BluetoothConnection.writeDescriptor " + serviceUuid + " " + characteristicUuid + " " + descriptorUuid + " " + Arrays.toString(value));
        }

        push(new Runnable() {
            @Override
            public void run() {
                final BluetoothGattDescriptor descriptor = gatt.getService(serviceUuid).getCharacteristic(characteristicUuid).getDescriptor(descriptorUuid);

                descriptor.setValue(value);
                gatt.writeDescriptor(descriptor);
            }
        });
    }

}
