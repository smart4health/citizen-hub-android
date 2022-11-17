package pt.uninova.s4h.citizenhub.ui.devices;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import pt.uninova.s4h.citizenhub.R;
import pt.uninova.s4h.citizenhub.connectivity.Agent;
import pt.uninova.s4h.citizenhub.connectivity.StateChangedMessage;
import pt.uninova.s4h.citizenhub.data.Device;
import pt.uninova.s4h.citizenhub.localization.MeasurementKindLocalization;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

public class DeviceConfigurationStreamsFragment extends Fragment {
    private LinearLayout advancedConfigurationLayout;
    private DeviceViewModel model;
    private ListView listViewFeatures;
    private MeasurementKindLocalization measurementKindLocalization;
    private TextView nameDevice;
    private TextView addressDevice;
    private final Observer<StateChangedMessage<Integer, ? extends Agent>> agentStateObserver = value -> {
        listViewFeatures.deferNotifyDataSetChanged();
        requireActivity().runOnUiThread(() -> {
            loadSupportedFeatures();
            setChildrenEnabled(advancedConfigurationLayout, value.getNewState() == 1);

        });

    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = new ViewModelProvider(requireActivity()).get(DeviceViewModel.class);
        measurementKindLocalization = new MeasurementKindLocalization(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_device_configuration_listview, container, false);
        nameDevice = view.findViewById(R.id.textConfigurationDeviceNameValue);
        addressDevice = view.findViewById(R.id.textConfigurationAddressValue);
        setHeaderValues(model.getSelectedDevice().getValue());

        listViewFeatures = view.findViewById(R.id.listViewFeature);

        advancedConfigurationLayout = view.findViewById(R.id.layout_device_configuration_container);
        loadSupportedFeatures();
        if(model.getSelectedDeviceAgent()!=null) {
            List<Fragment> fragmentList = model.getSelectedDeviceAgent().getConfigurationFragments();
            FragmentTransaction ft = getChildFragmentManager().beginTransaction();

            for (Fragment fragment : getChildFragmentManager().getFragments()) {
                if (fragment != null) {
                    getChildFragmentManager().beginTransaction().remove(fragment).commit();
                }
            }

            if (fragmentList != null) {
                for (int i = 0; i < fragmentList.size(); i++) {
                    Fragment newFragment = null;
                    try {
                        newFragment = fragmentList.get(i).getClass().newInstance();

                    } catch (IllegalAccessException | java.lang.InstantiationException e) {
                        e.printStackTrace();
                    }
                    assert newFragment != null;
                    Fragment divider = new DeviceConfigurationDividerFragment();
                    ft.add(R.id.layout_device_configuration_container, newFragment);
                    ft.add(R.id.layout_device_configuration_container, divider);

                }
            }
            ft.commitNow();
        }
        return view;
    }

        protected void loadSupportedFeatures() {
        if (model.getSelectedDeviceAgent() != null) {
            FeatureListAdapter adapter = new FeatureListAdapter(requireActivity(), getSupportedFeatures(), model.getSelectedDeviceAgent().getState() == 1, model.getSelectedDeviceAgent());

            listViewFeatures.setAdapter(adapter);
            adapter.updateResults(getSupportedFeatures());

        } else {
            FeatureListAdapter adapter = new FeatureListAdapter(requireActivity(), getSupportedFeatures(), model.getSelectedDeviceAgent());
            listViewFeatures.setAdapter(adapter);
            adapter.updateResults(getSupportedFeatures());

        }
    }

    protected List<FeatureListItem> getSupportedFeatures() {
        final List<FeatureListItem> featureListItems = new LinkedList<>();
        final Agent agent = model.getSelectedDeviceAgent();
        if (agent != null) {

            if (agent.getState() != 1 && agent.getEnabledMeasurements() != null) {

                for (int i : agent.getSupportedMeasurements()) {
                    featureListItems.add(new FeatureListItem(i, measurementKindLocalization.localize(i), agent.getEnabledMeasurements().contains(i)));
                }
            } else {
                final Set<Integer> measurementKindSet = agent.getEnabledMeasurements();

                for (int i : agent.getSupportedMeasurements()) {
                    featureListItems.add(new FeatureListItem(i, measurementKindLocalization.localize(i), measurementKindSet.contains(i)));
                }
            }
        }

        return featureListItems;
    }
    private static void setChildrenEnabled(ViewGroup layout, boolean state) {
        layout.setEnabled(state);
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            if (child instanceof ViewGroup) {
                setChildrenEnabled((ViewGroup) child, state);
            } else {
                child.setEnabled(state);
                if (!child.isEnabled()) {
                    child.setAlpha(0.5f);
                } else child.setAlpha(1);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (model.getSelectedDeviceAgent() != null) {
            model.getSelectedDeviceAgent().addStateObserver(agentStateObserver);
            setChildrenEnabled(advancedConfigurationLayout, model.getSelectedDeviceAgent().getState() == Agent.AGENT_STATE_ENABLED);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (model.getSelectedDeviceAgent() != null) {
            model.getSelectedDeviceAgent().removeStateObserver(agentStateObserver);
        }
    }
    private void setHeaderValues(Device device) {
        if (device != null) {
            nameDevice.setText(device.getName());
            addressDevice.setText(device.getAddress());
        }
    }

}
