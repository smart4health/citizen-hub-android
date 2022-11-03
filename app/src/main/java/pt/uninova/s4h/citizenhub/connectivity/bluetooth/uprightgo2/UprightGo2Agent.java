package pt.uninova.s4h.citizenhub.connectivity.bluetooth.uprightgo2;

import android.content.Context;
import android.view.MenuItem;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import pt.uninova.s4h.citizenhub.R;
import pt.uninova.s4h.citizenhub.connectivity.AgentOrchestrator;
import pt.uninova.s4h.citizenhub.connectivity.MeasuringProtocol;
import pt.uninova.s4h.citizenhub.connectivity.RoomSettingsManager;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.AdvancedConfigurationMenuItem;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.BluetoothAgent;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.BluetoothConnection;
import pt.uninova.s4h.citizenhub.data.Measurement;

public class UprightGo2Agent extends BluetoothAgent {

    static public final UUID ID = AgentOrchestrator.namespaceGenerator().getUUID("bluetooth.uprightgo2");

    static private final Set<Integer> supportedMeasurementKinds = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            Measurement.TYPE_POSTURE
    )));

    static private final Set<UUID> supportedProtocolsIds = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            UprightGo2PostureProtocol.ID,
            UprightGo2CalibrationProtocol.ID,
            UprightGo2VibrationProtocol.ID
    )));

    public UprightGo2Agent(BluetoothConnection connection, Context context) {
        super(ID, supportedProtocolsIds, supportedMeasurementKinds, connection, new RoomSettingsManager(context, connection.getAddress()));
    }

    @Override
    public Set<Integer> getSupportedMeasurements() {
        return supportedMeasurementKinds;
    }

    @Override
    public List<Fragment> getConfigurationFragments() {
        List<Fragment> uprightList = new ArrayList<>();
        uprightList.add(UprightGo2ConfigurationFragment.newInstance());
        return uprightList;
    }

    @Override
    public List<AdvancedConfigurationMenuItem> getConfigurationMenuItems() {
        List<AdvancedConfigurationMenuItem> buttonList = new ArrayList<>();
        UprightGo2ConfigurationFragment uprightGo2ConfigurationFragment = new UprightGo2ConfigurationFragment();

                buttonList.add(new AdvancedConfigurationMenuItem(
                        R.string.configuration_uprightgo2_button_text,
                                "Calibration",
                        uprightGo2ConfigurationFragment.getOnMenuItemClickListeners().get(0)));

        return buttonList;
    }


    public List<MenuItem.OnMenuItemClickListener> getConfigurationButtonClickListener() {
        UprightGo2ConfigurationFragment uprightGo2ConfigurationFragment = new UprightGo2ConfigurationFragment();
        return uprightGo2ConfigurationFragment.getOnMenuItemClickListeners();    }

    @Override
    public boolean hasConfigurationButtons() {
        return true;
    }

    @Override
    public Fragment getPairingHelper() {
        return null;
    }

    @Override
    protected MeasuringProtocol getMeasuringProtocol(int kind) {
        System.out.println("UprightGo2Agent.getMeasuringProtocol kind=" + kind);

        if (kind == Measurement.TYPE_POSTURE) {
            return new UprightGo2PostureProtocol(this.getConnection(), getSampleDispatcher(), this);
        }

        return null;
    }

    @Override
    public String getName() {
        return "UprightGO2";
    }
}