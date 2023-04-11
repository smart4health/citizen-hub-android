package pt.uninova.s4h.citizenhub.connectivity.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import pt.uninova.s4h.citizenhub.connectivity.AgentFactory;
import pt.uninova.s4h.citizenhub.connectivity.Connection;
import pt.uninova.s4h.citizenhub.connectivity.StateChangedMessage;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.and.BloodPressureMonitorAgentMatcher;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.digitsole.DigitSoleAgentMatcher;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.hexoskin.HexoSkinAgentMatcher;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.kbzposture.KbzPostureAgentMatcher;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.medx.MedXAgentMatcher;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.miband2.MiBand2AgentMatcher;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.uprightgo2.UprightGo2AgentMatcher;
import pt.uninova.s4h.citizenhub.data.Device;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

public class BluetoothAgentFactory implements AgentFactory<BluetoothAgent> {

    private final Context context;
    private static final List<AgentMatcher> agentList;

    static {
        agentList = Collections.unmodifiableList(Arrays.asList(new MiBand2AgentMatcher(),
                new BloodPressureMonitorAgentMatcher(),
                new KbzPostureAgentMatcher(),
                new DigitSoleAgentMatcher(),
                new HexoSkinAgentMatcher(),
                new MedXAgentMatcher(),
                new UprightGo2AgentMatcher()));
    }

    public BluetoothAgentFactory(Context context) {
        this.context = context;
    }

    private Class<?> identifyAgent(BluetoothConnection connection) {
        for (AgentMatcher agent : BluetoothAgentFactory.agentList
        ) {
            if (agent.doesMatch(connection, agent.getAgentServices())) {
                return agent.getAgentClass();
            }
        }
        return null;
    }


    public void create(String address, Observer<BluetoothAgent> observer) {
        BluetoothManager bluetoothManager = context.getSystemService(BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
        BluetoothConnection bluetoothConnection = new BluetoothConnection(bluetoothDevice);

        bluetoothConnection.addConnectionStateChangeListener(new Observer<StateChangedMessage<BluetoothConnectionState, BluetoothConnection>>() {

            @Override
            public void observe(StateChangedMessage<BluetoothConnectionState, BluetoothConnection> value) {
                if (value.getNewState() == BluetoothConnectionState.READY) {
                    final BluetoothConnection source = value.getSource();

                    value.getSource().removeConnectionStateChangeListener(this);

                    initAgent((identifyAgent(source)), source, observer);
                }
            }
        });

        bluetoothConnection.connect();
    }

    private void initAgent(Class<?> agentClass, BluetoothConnection source, Observer<BluetoothAgent> observer) {
        try {
            if (agentClass == null) {
                observer.observe(null);
            } else {
                Class<?> agent = Class.forName(agentClass.getName());
                Class<?>[] parameters = new Class[]{BluetoothConnection.class, Context.class};
                Constructor<?> constructor = agent.getConstructor(parameters);

                Object o = constructor.newInstance(source, context);
                observer.observe((BluetoothAgent) o);
            }
        } catch (IllegalAccessException | InstantiationException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void create(String address, Class<?> c, Observer<BluetoothAgent> observer) {
        try {
            final Constructor<?> constructor = c.getConstructor(BluetoothConnection.class, Context.class);

            final BluetoothManager bluetoothManager = context.getSystemService(BluetoothManager.class);
            final BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
            final BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
            final BluetoothConnection bluetoothConnection = new BluetoothConnection(bluetoothDevice);

            observer.observe((BluetoothAgent) constructor.newInstance(bluetoothConnection, context));
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void create(Device device, Observer<BluetoothAgent> observer) {
        create(device.getAddress(), observer);
    }

    @Override
    public void create(Device device, Class<?> c, Observer<BluetoothAgent> observer) {
        create(device.getAddress(), c, observer);
    }

    @Override
    public void create(Connection connection, Observer<BluetoothAgent> observer) {
        connection.connect();
    }

}
