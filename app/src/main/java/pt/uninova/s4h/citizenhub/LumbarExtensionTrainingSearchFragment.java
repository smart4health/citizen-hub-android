package pt.uninova.s4h.citizenhub;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.nio.ByteBuffer;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.UUID;

import pt.uninova.s4h.citizenhub.connectivity.StateChangedMessage;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.BaseCharacteristicListener;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.BluetoothConnection;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.BluetoothConnectionState;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.BluetoothScanner;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.BluetoothScannerListener;
import pt.uninova.s4h.citizenhub.persistence.ConnectionKind;
import pt.uninova.s4h.citizenhub.persistence.Device;
import pt.uninova.s4h.citizenhub.persistence.LumbarExtensionTraining;
import pt.uninova.s4h.citizenhub.persistence.LumbarExtensionTrainingRepository;
import pt.uninova.s4h.citizenhub.persistence.StateKind;
import pt.uninova.util.messaging.Observer;

public class LumbarExtensionTrainingSearchFragment extends Fragment {

    public final static UUID UUID_SERVICE_HEART_RATE = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_CHARACTERISTIC_HEART_RATE_DATA = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb");
    private static final UUID LUMBARTRAINING_UUID_SERVICE = UUID.fromString("5a46791b-516e-48fd-9d29-a2f18d520aec");
    private static final UUID LUMBARTRAINING_UUID_CHARACTERISTIC = UUID.fromString("38fde8b6-9664-4b8e-8b3a-e52b8809a64c");
    private static final int PERMISSIONS_REQUEST_CODE = 77;
    private static final int FEATURE_BLUETOOTH_STATE = 78;
    LayoutInflater localInflater;
    ViewGroup localContainer;
    private DeviceListAdapter adapter;
    private ArrayList<DeviceListItem> deviceItemList;
    private ArrayList<Device> deviceList;
    private boolean alreadyConnected = false;
    private DeviceViewModel model;
    private BluetoothScanner scanner;
    private BluetoothScannerListener listener;
    private LocationManager locationManager;
    private BluetoothManager bluetoothManager;
    private boolean hasStartedEnableLocationActivity = false;
    private boolean hasStartedEnableBluetoothActivity = false;
    private BluetoothGatt gatt;
    private BluetoothConnection connection;
    private ProgressBar simpleProgressBar;
    private LumbarExtensionTrainingRepository lumbarRepository;
    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            if (!alreadyConnected) {
                buildRecycleView(requireView());

                Device device = new Device(result.getDevice().getName(), result.getDevice().getAddress(), ConnectionKind.BLUETOOTH, StateKind.INACTIVE, null);
                if (!model.isDevicePaired(device)) {
                    DeviceListItem deviceListItem = new DeviceListItem(device, R.drawable.ic_devices_unpaired, R.drawable.ic_settings_off);

                    if (!deviceList.contains(device)) {
                        deviceList.add(device);
                        System.out.println( "TAMANHO DEVICE LIST: " + deviceList.size());
                        System.out.println( "TAMANHO DEVICE LIST ITEM: " + deviceItemList.size());
                        deviceItemList.add(deviceListItem);
                        adapter.notifyItemInserted(0);
                    }
                }
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {

        localInflater = inflater;
        localContainer = container;


        locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        bluetoothManager = (BluetoothManager) requireContext().getSystemService(Context.BLUETOOTH_SERVICE);
        final View result = inflater.inflate(R.layout.fragment_device_search, container, false);

        model = new ViewModelProvider(requireActivity()).get(DeviceViewModel.class);
        System.out.println("Tamanho deviceItemList antes do clean: " + deviceItemList.size());
        cleanList();
        deviceList.clear();

        buildRecycleView(result);

        return result;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        simpleProgressBar = requireView().findViewById(R.id.progressBar);
        lumbarRepository = new LumbarExtensionTrainingRepository(requireActivity().getApplication());


    }

    private void startFilteredScan() {
        scanner.startWithFilter(listener, new ParcelUuid(LUMBARTRAINING_UUID_SERVICE), scanCallback);

    }

    private void startScan() {
        System.out.println("LumbarExtensionTrainingSearchFragment.TryStartScan");

        if (!hasBluetoothEnabled()) {
            enableBluetooth();

        } else if (!hasBluetoothPermissions()) {
            requestBluetoothPermissions();

        } else if (!hasLocationEnabled()) {
            enableLocation();

        } else if (!hasLocationPermissions()) {
            requestLocationPermissions();

        } else {
            scanner = new BluetoothScanner(bluetoothManager);
            System.out.println("LumbarExtensionTrainingSearchFragment.StartFilteredScan");

            startFilteredScan();

        }
    }

    private boolean hasLocationPermissions() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;

    }

    private boolean hasBluetoothPermissions() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasLocationEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private boolean hasBluetoothEnabled() {
        return bluetoothManager.getAdapter().isEnabled();
    }

    private void enableLocation() {
        if (hasStartedEnableLocationActivity) {
            new AlertDialog.Builder(getContext())
                    .setMessage(R.string.fragment_device_search_location_warning_title)
                    .setPositiveButton(R.string.fragment_device_search_location_open_settings_button, (paramDialogInterface, paramInt) -> {
                        requireContext().startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        paramDialogInterface.dismiss();
                    })
                    .setNegativeButton(R.string.fragment_device_search_cancel_option, (dialog, which) -> Navigation.findNavController(requireView()).navigate(DeviceSearchFragmentDirections.actionDeviceSearchFragmentToDeviceListFragment()))
                    .show();
        } else {
            hasStartedEnableLocationActivity = true;
            new AlertDialog.Builder(getContext())
                    .setMessage(R.string.fragment_device_search_location_not_enabled_title)
                    .setPositiveButton(R.string.fragment_device_search_location_open_settings_button, (paramDialogInterface, paramInt) -> {
                        requireContext().startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        paramDialogInterface.dismiss();
                    })
                    .setNegativeButton(R.string.fragment_device_search_cancel_option, (paramDialogInterface, paramInt) -> startScan())
                    .show();
        }
    }

    private void enableBluetooth() {
        if (hasStartedEnableBluetoothActivity) {
            new AlertDialog.Builder(getContext())
                    .setMessage(R.string.fragment_device_search_bluetooth_warning_title)
                    .setPositiveButton(R.string.fragment_device_search_bluetooth_open_settings_button, (paramDialogInterface, paramInt) -> {
                        requireContext().startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
                        paramDialogInterface.dismiss();
                    })
                    .setNegativeButton(R.string.fragment_device_search_cancel_option, (dialog, which) -> Navigation.findNavController(requireView()).navigate(DeviceSearchFragmentDirections.actionDeviceSearchFragmentToDeviceListFragment()))
                    .show();

        } else {
            hasStartedEnableBluetoothActivity = true;
            new AlertDialog.Builder(getContext())
                    .setMessage(R.string.fragment_device_search_bluetooth_not_enabled_title)
                    .setPositiveButton(R.string.fragment_device_search_bluetooth_open_settings_button, (paramDialogInterface, paramInt) -> {
                        requireContext().startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
                        paramDialogInterface.dismiss();
                    })
                    .setNegativeButton(R.string.fragment_device_search_cancel_option, (paramDialogInterface, paramInt) -> startScan())
                    .show();
        }
    }

    private void requestBluetoothPermissions() {

        requestPermissions(new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN},
                PERMISSIONS_REQUEST_CODE);
    }

    private void requestLocationPermissions() {
        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSIONS_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE) {

            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
            }

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == FEATURE_BLUETOOTH_STATE) {
        System.out.println("LumbarExtensionTrainingSearchFragment.onActivityResult");
        startScan();
//        }

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (scanner != null) {
            scanner.stop();
        }
        if (connection != null)
            connection.close();

    }


    @Override
    public void onStop() {
        super.onStop();
        if (scanner != null) {
            scanner.stop();
        }
        if (connection != null)
            connection.close();
    }


    @Override
    public void onResume() {
        super.onResume();
        System.out.println("LumbarExtensionTrainingSearchFragment.onResume");
        startScan();

    }

    private void buildRecycleView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView_searchList);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        adapter = new DeviceListAdapter(deviceItemList);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        recyclerView.setItemAnimator(new DefaultItemAnimator());

        adapter.setOnItemClickListener(new DeviceListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                System.out.println("LumbarExtensionTrainingSearchFragment.onItemClick");
                model.setDevice(deviceItemList.get(position).getDevice());
                simpleProgressBar.setVisibility(View.VISIBLE);

                if (scanner != null) {
                    scanner.stop();
                }

                connection = new BluetoothConnection();

                connection.addConnectionStateChangeListener(new Observer<StateChangedMessage<BluetoothConnectionState, BluetoothConnection>>() {
                    @Override
                    public void observe(StateChangedMessage<BluetoothConnectionState, BluetoothConnection> value) {
                        System.out.println("LumbarExtensionTrainingSearchFragment.state");

                        if (value.getNewState() == BluetoothConnectionState.READY) {
                            connection.removeConnectionStateChangeListener(this);
                            LumbarExtensionTrainingRepository lumbarExtensionTrainingRepository = new LumbarExtensionTrainingRepository(getActivity().getApplication());
                            connection.addCharacteristicListener(new BaseCharacteristicListener(LUMBARTRAINING_UUID_SERVICE, LUMBARTRAINING_UUID_CHARACTERISTIC) {
                                @Override
                                public void onRead(byte[] value) {
                                    System.out.println("LumbarExtensionTrainingSearchFragment.onRead");
                                    ByteBuffer byteBuffer = ByteBuffer.wrap(value).asReadOnlyBuffer();
                                    System.out.println(byteBuffer);

                                    int timestamp = byteBuffer.getInt();
                                    System.out.println("Timestamp: " + timestamp);
                                    int length = byteBuffer.getInt();
                                    System.out.println("Length: " + length);
                                    float score = byteBuffer.getFloat();
                                    System.out.println("Score: " + score);
                                    int repetitions = byteBuffer.getInt();
                                    System.out.println("Repetitions: " + repetitions);
                                    int weight = byteBuffer.getInt();
                                    System.out.println("Weight: " + weight);
//                                    //for timestamp
//                                    byte[] timestampByteArray = new byte[4];
//                                    System.arraycopy(value, 0, timestampByteArray, 0, 4);
//                                    ByteBuffer timestampByteBuffer = ByteBuffer.wrap(timestampByteArray).asReadOnlyBuffer();
//                                    int timestampEpoch = timestampByteBuffer.getInt();
//                                    java.util.Date timestamp = new java.util.Date((long) timestampEpoch * 1000);
//                                    System.out.println("Timestamp was: " + timestamp);
//                                    System.out.println("Timestamp long (epoch) is " + (long) timestampEpoch);
//
//                                    //for training length
//                                    final double[] parsed = new double[]{
//                                            byteBuffer.get(4) & 0xFF,
//                                            byteBuffer.get(5) & 0xFF,
//                                            byteBuffer.get(6) & 0xFF,
//                                            byteBuffer.get(7) & 0xFF,
//                                            byteBuffer.get(8) & 0xFF,
//                                            byteBuffer.get(9) & 0xFF
//                                    };
//                                    System.out.println("Training Length was: " + (int) parsed[0] + "h " + (int) parsed[1] + "m " + (int) parsed[2] + "s");
//                                    long trainingLength = (int) (parsed[0] * 60 * 60) + (int) (parsed[1] * 60) + (int) parsed[2]; //milliseconds
//                                    System.out.println("Training Length long is " + trainingLength);
//
//                                    //for score
//                                    Double score = parsed[3] + parsed[4] * 0.01;
//                                    System.out.println("Score was: " + score + "%");
//
//                                    //for repetitions
//                                    int repetitions = (int) parsed[5];
//                                    System.out.println("Repetetions were: " + repetitions);
//
                                    lumbarRepository.add(new LumbarExtensionTraining(timestamp, length, score, repetitions, weight));
                                    connection.disableNotifications(LUMBARTRAINING_UUID_SERVICE, LUMBARTRAINING_UUID_CHARACTERISTIC);
                                    connection.close();
                                    Navigation.findNavController(LumbarExtensionTrainingSearchFragment.this.requireView()).navigate(LumbarExtensionTrainingSearchFragmentDirections.actionLumbarExtensionTrainingSearchFragmentToSummaryFragment());

                                }
                            });
                            connection.readCharacteristic(LUMBARTRAINING_UUID_SERVICE, LUMBARTRAINING_UUID_CHARACTERISTIC);
                            System.out.println(" Reading ");
                        }
                    }
                });
                alreadyConnected = true;
                try {
                    System.out.println("Connecting");
                    bluetoothManager.getAdapter().getRemoteDevice(deviceItemList.get(position).getDevice().getAddress()).connectGatt(getContext(), true, connection, BluetoothDevice.TRANSPORT_LE);
                    simpleProgressBar.setVisibility(View.GONE);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onSettingsClick(int position) {
                model.setDevice(deviceItemList.get(position).getDevice());
                onItemClick(position);
            }
        });
    }

    private void cleanList() {
        deviceItemList = new ArrayList<>();
    }
}

//MedX0009
