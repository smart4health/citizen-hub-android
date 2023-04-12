package pt.uninova.s4h.citizenhub.ui.devices;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

import pt.uninova.s4h.citizenhub.R;
import pt.uninova.s4h.citizenhub.connectivity.Agent;
import pt.uninova.s4h.citizenhub.data.Device;

public class DeviceIdentificationFragment extends Fragment {

    DeviceViewModel model;
    private TextView nameDevice;
    private TextView addressDevice;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = new ViewModelProvider(requireActivity()).get(DeviceViewModel.class);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_device_configuration_update_test, container, false);

        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == android.R.id.home) {
                    onBackPressed();
                    requireActivity().onBackPressed();
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);


        nameDevice = view.findViewById(R.id.textConfigurationDeviceNameValue);
        addressDevice = view.findViewById(R.id.textConfigurationAddressValue);
        setHeaderValues(model.getSelectedDevice().getValue());
        Fragment progressBar = new DeviceConfigurationProgressBarFragment();
        addFragment(progressBar);

        model.identifySelectedDevice((Agent agent) -> {
            removeFragment(progressBar);
            if (agent == null) {
                Navigation.findNavController(DeviceIdentificationFragment.this.requireView()).navigate(DeviceIdentificationFragmentDirections.actionDeviceIdentificationFragmentToDeviceUnsupportedFragment());
            } else {
                if (model.getSelectedDeviceAgent() != null) {
                    DeviceIdentificationFragment.this.requireActivity().runOnUiThread(() -> Navigation.findNavController(DeviceIdentificationFragment.this.requireView()).navigate(DeviceIdentificationFragmentDirections.actionDeviceIdentificationFragmentToDeviceConfigurationStreamsFragment()));
                } else {
                    DeviceIdentificationFragment.this.requireActivity().runOnUiThread(() -> {
                        addFragment(new DeviceConfigurationFeaturesFragment(agent));
                        addFragment(new DeviceConfigurationConnectFragment(agent));
                    });
                }
            }

        });

        return view;
    }

    private void addFragment(Fragment fragment) {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.layout_device_configuration_container, fragment);

        transaction.commitNow();
    }

    private void removeFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.remove(fragment).commit();
    }

    private void setHeaderValues(Device device) {
        if (device != null) {
            nameDevice.setText(device.getName());
            addressDevice.setText(device.getAddress());
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        requireView().setFocusableInTouchMode(true);
        requireView().requestFocus();
        requireView().setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                onBackPressed();
                requireActivity().onBackPressed();
                return true;
            }
            return false;
        });

    }


    public void onBackPressed() {

        final DeviceViewModel model = new ViewModelProvider(requireActivity()).get(DeviceViewModel.class);
        model.getDeviceConnection().disconnect();
        model.getDeviceConnection().close();

//        final Handler handler = new Handler(Looper.getMainLooper());
//        handler.postDelayed(() -> {
//            if (model.getDeviceConnection().getConnectionState() != BluetoothConnectionState.DISCONNECTED.ordinal()) {
//
//                model.getDeviceConnection().disconnect();
//                model.getDeviceConnection().close();
//                if (model.getSelectedDeviceAgent() != null) {
//
//                    model.getSelectedDeviceAgent().disable();
//                    model.removeSelectedDevice();
//                }
//            }
//        }, 10000);
    }
}
