package pt.uninova.s4h.citizenhub;

import static java.util.Objects.requireNonNull;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import pt.uninova.s4h.citizenhub.connectivity.Agent;
import pt.uninova.s4h.citizenhub.connectivity.StateChangedMessage;
import pt.uninova.s4h.citizenhub.data.Device;
import pt.uninova.s4h.citizenhub.ui.devices.DeviceViewModel;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

public class DeviceListFragment extends Fragment {

    private DeviceListAdapter adapter;
    private DeviceViewModel model;

    private final Observer<StateChangedMessage<Integer, ? extends Agent>> agentStateObserver = (StateChangedMessage<Integer, ? extends Agent> value) -> {
        updateItemAgentState(value.getSource(), value.getNewState());
    };

    private void buildRecycleView(View view) {
        final RecyclerView recyclerView = view.findViewById(R.id.recyclerView_devicesList);
        final RecyclerView.LayoutManager layoutManager = new CustomLinearLayoutManager(requireContext());

        adapter = new DeviceListAdapter(item -> {

            model.selectDevice(item.getDevice());

            Navigation.findNavController(requireView()).navigate(DeviceListFragmentDirections.actionDeviceListFragmentToDeviceConfigurationTestFragment());
        });

        adapter.setHasStableIds(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        recyclerView.setItemAnimator(null);
    }

    public static class CustomLinearLayoutManager extends LinearLayoutManager {
        public CustomLinearLayoutManager(Context context) {
            super(context);
        }

        @Override
        public boolean supportsPredictiveItemAnimations() {
            return false;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View result = inflater.inflate(R.layout.fragment_device_list, container, false);

        model = new ViewModelProvider(requireActivity()).get(DeviceViewModel.class);

        Button searchDevices = result.findViewById(R.id.searchButton);

        buildRecycleView(result);

        searchDevices.setOnClickListener(view -> Navigation.findNavController(requireView()).navigate(DeviceListFragmentDirections.actionDeviceListFragmentToDeviceConnectionMethodFragment()));

        return result;
    }

    @Override
    public void onResume() {
        super.onResume();
        model.getDeviceList().observe(getViewLifecycleOwner(), this::onDeviceListChanged);

        TextView noDevices = requireActivity().findViewById(R.id.fragment_device_list_no_data);

        if (adapter.getItemCount() == 0) {
            noDevices.setVisibility(View.VISIBLE);
        } else {
            noDevices.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        for (int j = 0; j < adapter.getItemCount(); j++) {

            model.getAttachedAgent(adapter.getItem(j).getDevice()).removeStateObserver(agentStateObserver);
        }
    }

    public void updateItemAgentState(Agent agent, int state) {
        int pos = requireNonNull(model.getDeviceList().getValue()).indexOf(agent.getSource());
        if (state == 1) {
            adapter.getItem(pos).setImageResource(R.drawable.ic_devices_connected);
        } else {
            adapter.getItem(pos).setImageResource(R.drawable.ic_devices_unpaired);
        }

        requireActivity().runOnUiThread(() -> adapter.updateItem(pos, adapter.getItem(pos)));
    }

    public void onDeviceListChanged(List<Device> deviceList) {
        for (int j = 0; j < adapter.getItemCount(); j++) {

            model.getAttachedAgent(adapter.getItem(j).getDevice()).removeStateObserver(agentStateObserver);
        }
        adapter.clear();

        if (deviceList.size() > 0) {
            for (Device i : deviceList) {
                final Agent agent = model.getAttachedAgent(i);

                if (agent == null || agent.getState() != Agent.AGENT_STATE_ENABLED) {
                    if (agent != null) {
                        agent.addStateObserver(agentStateObserver);
                    }
                    adapter.addItem(new DeviceListItem(i, R.drawable.ic_devices_unpaired));

                } else {
                    adapter.addItem(new DeviceListItem(i, R.drawable.ic_devices_connected));
                    agent.addStateObserver(agentStateObserver);
                }
            }
        }
    }
}
