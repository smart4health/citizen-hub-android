package pt.uninova.s4h.citizenhub;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import pt.uninova.s4h.citizenhub.connectivity.Connection;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.BluetoothConnection;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.BluetoothConnectionState;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.BluetoothScanner;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.medx.MedXAgent;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.medx.LumbarExtensionTrainingProtocol;
import pt.uninova.s4h.citizenhub.data.Device;
import pt.uninova.s4h.citizenhub.data.Measurement;
import pt.uninova.s4h.citizenhub.data.Sample;
import pt.uninova.s4h.citizenhub.persistence.repository.SampleRepository;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

public class LumbarExtensionTrainingSearchFragment extends BluetoothFragment {

    private DeviceListAdapter adapter;

    private BluetoothScanner scanner;

    private void buildRecycleView(View view) throws SecurityException {
        final RecyclerView recyclerView = view.findViewById(R.id.recyclerView_searchList);
        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(requireContext());
        final SampleRepository sampleRepository = new SampleRepository(requireActivity().getApplication());

        adapter = new DeviceListAdapter(item -> {
            final Device device = item.getDevice();
            final ProgressBar progressBar = requireView().findViewById(R.id.progressBar);

            progressBar.setVisibility(View.VISIBLE);

            if (scanner != null) {
                scanner.stop();
            }

            final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            final BluetoothDevice bluetoothDevice = adapter.getRemoteDevice(device.getAddress());
            final BluetoothConnection bluetoothConnection = new BluetoothConnection(bluetoothDevice);

            bluetoothConnection.addConnectionStateChangeListener(state -> {
                if (state.getOldState() == BluetoothConnectionState.CONNECTED && state.getNewState() == BluetoothConnectionState.READY) {
                    final MedXAgent agent = new MedXAgent(bluetoothConnection);

                    agent.addSampleObserver(new Observer<Sample>() {
                        @Override
                        public void observe(Sample sample) {
                            agent.disable();
                            agent.removeSampleObserver(this);
                            bluetoothConnection.close();

                            sampleRepository.create(sample);

                            navigateToSummaryFragment();
                        }
                    });

                    agent.enable();
                    agent.enableMeasurement(Measurement.TYPE_LUMBAR_EXTENSION_TRAINING);
                }
            });

            bluetoothConnection.connect(BluetoothDevice.TRANSPORT_LE);
        });

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }


    @Override
    protected void onBluetoothAllowed() {
        scanner = new BluetoothScanner((BluetoothManager) requireActivity().getSystemService(Context.BLUETOOTH_SERVICE));

        scanner.start((address, name) -> {
            final Device device = new Device(address, name == null ? address : name, Connection.CONNECTION_KIND_BLUETOOTH);

            adapter.addItem(new DeviceListItem(device, R.drawable.ic_devices_unpaired));
        }, LumbarExtensionTrainingProtocol.UUID_SERVICE);
    }

    @Override
    protected void onBluetoothDenied() {

    }

    @Override
    protected void onBluetoothDisabled() {

    }

    @Override
    protected void onBluetoothUnsupported() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_device_search, container, false);

        buildRecycleView(view);

        return view;
    }

    @Override
    protected void onLocationDisabled() {

    }

    @Override
    protected void onLocationUnsupported() {

    }

    @Override
    public void onStop() {
        super.onStop();

        if (scanner != null) {
            scanner.stop();
            scanner = null;
        }
    }

    private void navigateToSummaryFragment() {
        requireActivity().runOnUiThread(() -> Navigation.findNavController(requireView()).navigate(LumbarExtensionTrainingSearchFragmentDirections.actionLumbarExtensionTrainingSearchFragmentToSummaryFragment()));
    }
}
