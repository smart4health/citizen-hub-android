package pt.uninova.s4h.citizenhub;

import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import pt.uninova.s4h.citizenhub.connectivity.Connection;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.BluetoothScanner;
import pt.uninova.s4h.citizenhub.data.Device;
import pt.uninova.s4h.citizenhub.ui.devices.DeviceViewModel;

public class DeviceSearchFragment extends BluetoothFragment {

    private DeviceListAdapter adapter;
    private DeviceViewModel model;
    private LinearLayout progressBar;
    private BluetoothScanner scanner;

    private void buildRecycleView(View view) {
        final RecyclerView recyclerView = view.findViewById(R.id.recyclerView_searchList);
        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(requireContext());

        adapter = new DeviceListAdapter(item -> {
            model.selectDevice(item.getDevice());

            Navigation.findNavController(requireView()).navigate(DeviceSearchFragmentDirections.actionDeviceSearchFragmentToDeviceConfigurationTestFragment());
        });

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = new ViewModelProvider(requireActivity()).get(DeviceViewModel.class);
    }

    @Override
    protected void onBluetoothAllowed() {
        scanner = new BluetoothScanner((BluetoothManager) requireActivity().getSystemService(Context.BLUETOOTH_SERVICE));

        scanner.start((address, name) -> {
            final Device device = new Device(address, name == null ? address : name, Connection.CONNECTION_KIND_BLUETOOTH);

            if (!model.hasAgentAttached(device)) {
                progressBar.setVisibility(View.GONE);
                adapter.addItem(new DeviceListItem(device, R.drawable.ic_devices_unpaired));

            }
        });
    }

    @Override
    protected void onBluetoothDenied() {
        Navigation.findNavController(requireView()).navigate(DeviceSearchFragmentDirections.actionDeviceSearchFragmentToBluetoothDeniedFragment());
    }

    @Override
    protected void onBluetoothDisabled() {
        Navigation.findNavController(requireView()).navigate(DeviceSearchFragmentDirections.actionDeviceSearchFragmentToBluetoothDisabledFragment());
    }

    @Override
    protected void onBluetoothUnsupported() {
        Navigation.findNavController(requireView()).navigate(DeviceSearchFragmentDirections.actionDeviceSearchFragmentToBluetoothNotSupportedFragment());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_device_search, container, false);

        buildRecycleView(view);
        progressBar = view.findViewById(R.id.progressBarLayout);
        progressBar.setVisibility(View.VISIBLE);

        return view;
    }

    @Override
    protected void onLocationDisabled() {
        Navigation.findNavController(requireView()).navigate(DeviceSearchFragmentDirections.actionDeviceSearchFragmentToLocationDisabledFragment());
    }

    @Override
    protected void onLocationUnsupported() {
        Navigation.findNavController(requireView()).navigate(DeviceSearchFragmentDirections.actionDeviceSearchFragmentToLocationNotSupportedFragment());
    }

    @Override
    public void onStop() {
        super.onStop();

        if (scanner != null) {
            scanner.stop();
            scanner = null;
        }
    }


}