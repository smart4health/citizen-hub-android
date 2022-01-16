package pt.uninova.s4h.citizenhub;

import android.app.Application;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import pt.uninova.s4h.citizenhub.persistence.DeviceRecord;

public class DeviceAddFragment extends Fragment {

    private Button addDevice;
    private TextView infoDevice;
    private Application app;
    private DeviceViewModel model;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View result = inflater.inflate(R.layout.fragment_device_add, container, false);

        addDevice = result.findViewById(R.id.button_add_device);
        infoDevice = result.findViewById(R.id.text_add_fragment);

        addDevice.setOnClickListener(view -> {
            model.apply();
        });

        TextView detailText = result.findViewById(R.id.text_add_fragment);
        DeviceRecord deviceRecord = DeviceListFragment.deviceRecordForSettings;

        detailText.setText("Name: " + deviceRecord.getName() + "\n" + "Address: " + deviceRecord.getAddress());

        return result;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (Application) requireActivity().getApplication();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        model = new ViewModelProvider(requireActivity()).get(DeviceViewModel.class);
    }
}