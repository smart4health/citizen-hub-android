package pt.uninova.s4h.citizenhub;

import android.Manifest;
import android.app.Application;
import android.os.Bundle;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import pt.uninova.s4h.citizenhub.persistence.Device;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DeviceListFragment extends Fragment {

    private RecyclerView recyclerView;
    private DeviceListAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<DeviceListItem> deviceList;
    private Application app;
    private View resultView;
    public static Device deviceForSettings;

    private DeviceViewModel model;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_device_list, menu);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (Application) requireActivity().getApplication();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View result = inflater.inflate(R.layout.fragment_device_list, container, false);
        resultView = result;

        model = new ViewModelProvider(requireActivity()).get(DeviceViewModel.class);
        model.getDevices().observe(getViewLifecycleOwner(), this::onDeviceUpdate);

        cleanList();
        buildRecycleView(result);

        setHasOptionsMenu(true);

        return result;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        model = new ViewModelProvider(requireActivity()).get(DeviceViewModel.class);
    }

    private void onDeviceUpdate(List<Device> devices) {
        cleanList();
        for(Device device : devices) {
            deviceList.add(new DeviceListItem(R.drawable.ic_watch, device, R.drawable.ic_settings));
        }
        buildRecycleView(resultView);
    }

    private void cleanList(){
        deviceList = new ArrayList<>();
    }

    private void buildRecycleView(View result){
        recyclerView = (RecyclerView) result.findViewById(R.id.recyclerView_devicesList);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        adapter = new DeviceListAdapter(deviceList);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

        adapter.setOnItemClickListener(new DeviceListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                //insertItem(position);
                //removeItem(position);
                //onoffItem(position);
            }

            @Override
            public void onSettingsClick(int position) {
                Navigation.findNavController(getView()).navigate(DeviceListFragmentDirections.actionDeviceListFragmentToDeviceDetailFragment());
                //address_for_settings = deviceList.get(position).getmTextDescription();
                deviceForSettings = new Device (deviceList.get(position).getmTextTitle(),
                        deviceList.get(position).getmTextDescription(), null,null);
            }
        });
    }

    public void insertItem(int position){ //this is for the recyclerview testing
        deviceList.add(position, new DeviceListItem(R.drawable.ic_about_fragment, new Device(), R.drawable.ic_settings));
        adapter.notifyItemInserted(position);
    }
    public void removeItem(int position){ //this is for the recyclerview testing
        deviceList.remove(position);
        adapter.notifyItemRemoved(position);
    }

    public void onoffItem(int position){ //this is for the recyclerview testing
        if (deviceList.get(position).getmImageResource() == R.drawable.ic_watch) {
            deviceList.get(position).changeImageResource(R.drawable.ic_watch_off);
            deviceList.get(position).changeImageSettings(R.drawable.ic_settings_off);
        }
        else {
            deviceList.get(position).changeImageResource(R.drawable.ic_watch);
            deviceList.get(position).changeImageSettings(R.drawable.ic_settings);
        }
        adapter.notifyItemChanged(position);
    }

    ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, 0 /*ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT*/) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) { //this inactive
            int position = viewHolder.getAdapterPosition();
            //removeItem(position);
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_fragment_device_list_search) {
            Navigation.findNavController(requireView()).navigate(DeviceListFragmentDirections.actionDeviceListFragmentToDeviceSearchFragment());
        }
        return super.onOptionsItemSelected(item);
    }



}