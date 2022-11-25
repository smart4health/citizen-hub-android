package pt.uninova.s4h.citizenhub.connectivity.bluetooth.medx;

import android.content.Context;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import pt.uninova.s4h.citizenhub.connectivity.AgentOrchestrator;
import pt.uninova.s4h.citizenhub.connectivity.MeasuringProtocol;
import pt.uninova.s4h.citizenhub.connectivity.RoomSettingsManager;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.BluetoothAgent;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.BluetoothConnection;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.StreamsFragment;
import pt.uninova.s4h.citizenhub.data.Measurement;
import pt.uninova.s4h.citizenhub.ui.devices.DeviceConfigurationUniqueIdentifierFragment;

public class MedXAgent extends BluetoothAgent {

    public static final UUID ID = AgentOrchestrator.namespaceGenerator().getUUID("bluetooth.medx");

    public static final Set<Integer> supportedMeasurementKinds = Collections.unmodifiableSet(new HashSet<>(Collections.singletonList(
            Measurement.TYPE_LUMBAR_EXTENSION_TRAINING
    )));

    private static final Set<UUID> supportedProtocolsIds = Collections.unmodifiableSet(new HashSet<>(Collections.singletonList(
            LumbarExtensionTrainingProtocol.ID
    )));


    public MedXAgent(BluetoothConnection connection, Context context) {
        super(ID, supportedProtocolsIds, supportedMeasurementKinds, connection, new RoomSettingsManager(context, connection.getAddress()));
    }

    @Override
    protected MeasuringProtocol getMeasuringProtocol(int kind) {
        if (kind == Measurement.TYPE_LUMBAR_EXTENSION_TRAINING) {
            return new LumbarExtensionTrainingProtocol(getConnection(), getSampleDispatcher(), this);
        }

        return null;
    }

    @Override
    public String getName() {
        return "MedX";
    }

    @Override
    public List<Fragment> getConfigurationFragments() {
        List<Fragment> medXList = new ArrayList<>();
        medXList.add(new StreamsFragment(this));
        medXList.add(new DeviceConfigurationUniqueIdentifierFragment(this));
        return medXList;
    }

    @Override
    public Fragment getPairingHelper() {
        return null;
    }
}
