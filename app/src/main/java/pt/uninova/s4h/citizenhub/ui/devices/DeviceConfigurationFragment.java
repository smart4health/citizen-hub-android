package pt.uninova.s4h.citizenhub.ui.devices;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import java.util.List;

import pt.uninova.s4h.citizenhub.R;
import pt.uninova.s4h.citizenhub.connectivity.Agent;
import pt.uninova.s4h.citizenhub.connectivity.StateChangedMessage;
import pt.uninova.s4h.citizenhub.data.Device;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

public class DeviceConfigurationFragment extends Fragment {
    private LinearLayout advancedConfigurationLayout;
    private DeviceViewModel model;
    private TextView nameDevice;
    private TextView addressDevice;
    private final Observer<StateChangedMessage<Integer, ? extends Agent>> agentStateObserver = value -> {
        requireActivity().runOnUiThread(() -> {
            setChildrenEnabled(advancedConfigurationLayout, value.getNewState() == 1);
        });

    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = new ViewModelProvider(requireActivity()).get(DeviceViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_device_configuration_listview, container, false);

        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.device_configuration_fragment, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();

                if (id == R.id.device_configuration_menu_reconnect_item) {
                    model.reconnectDevice(model.getSelectedDevice().getValue());
                    return true;

                } else if (id == R.id.device_configuration_menu_remove_item) {
                    model.removeSelectedDevice();
                    Navigation.findNavController(DeviceConfigurationFragment.this.requireView()).navigate(DeviceConfigurationFragmentDirections.actionDeviceConfigurationStreamsFragmentToDeviceListFragment());
                    return true;

                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);


        nameDevice = view.findViewById(R.id.textConfigurationDeviceNameValue);
        addressDevice = view.findViewById(R.id.textConfigurationAddressValue);
        setHeaderValues(model.getSelectedDevice().getValue());

        advancedConfigurationLayout = view.findViewById(R.id.layout_device_configuration_container);
        if (model.getSelectedDeviceAgent() != null) {

            List<Fragment> fragmentList = model.getSelectedDeviceAgent().getConfigurationFragments();
            FragmentTransaction ft = getChildFragmentManager().beginTransaction();

            if (fragmentList != null && getChildFragmentManager().getFragments().size() < 1) {
                for (Fragment fragment : fragmentList) {
                    ft.add(R.id.layout_device_configuration_container, fragment);

                }
            }
            ft.commitNow();
        }
        return view;
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
