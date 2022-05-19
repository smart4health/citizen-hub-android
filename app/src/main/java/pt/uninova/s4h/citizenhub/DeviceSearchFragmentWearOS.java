package pt.uninova.s4h.citizenhub;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.List;
import java.util.concurrent.TimeUnit;

import pt.uninova.s4h.citizenhub.connectivity.Connection;
import pt.uninova.s4h.citizenhub.data.Device;
import pt.uninova.s4h.citizenhub.ui.devices.DeviceViewModel;

public class DeviceSearchFragmentWearOS extends Fragment {

    private DeviceListAdapter adapter;
    private DeviceViewModel model;

    LayoutInflater localInflater;
    ViewGroup localContainer;
    private GoogleApiClient client;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {

        localInflater = inflater;
        localContainer = container;

        final View result = inflater.inflate(R.layout.fragment_device_search, container, false);

        model = new ViewModelProvider(requireActivity()).get(DeviceViewModel.class);
        // model.getDevices().observe(getViewLifecycleOwner(), this::onDeviceUpdate);
        buildRecycleView(result);

        //Check if WearOS is installed
        checkWearOSPackage(getActivity().getPackageManager());

        //Retrieve Nodes
        retrieveDeviceNode();


        return result;
    }

    private void checkWearOSPackage(PackageManager packageManager) {
        /*
        try {
            packageManager.getPackageInfo("com.google.android.wearable.app", PackageManager.GET_META_DATA);
            System.out.println("WearOS App is installed!");
        } catch (PackageManager.NameNotFoundException e) {
            //android wear app is not installed
            System.out.println("WearOS App is not installed!");
            new AlertDialog.Builder(getContext())
                    .setMessage("WearOS App is not installed on your device. Please install and try again.")
                    .setTitle("WearOS App not found.")
                    .setPositiveButton("OK", (paramDialogInterface, paramInt) ->
                            Navigation.findNavController(requireView()).navigate(DeviceSearchFragmentWearOSDirections.actionDeviceSearchWearosFragmentToDeviceListFragment()))
                    .show();
        }

         */
    }

    private GoogleApiClient getGoogleApiClient(Context context) {
        if (client == null)
            client = new GoogleApiClient.Builder(context)
                    .addApi(Wearable.API)
                    .build();
        return client;
    }

    private void retrieveDeviceNode() {
        final GoogleApiClient client = getGoogleApiClient(getContext());
        new Thread(new Runnable() {
            @Override
            public void run() {
                client.blockingConnect(1000, TimeUnit.MILLISECONDS);
                NodeApi.GetConnectedNodesResult result =
                        Wearable.NodeApi.getConnectedNodes(client).await();
                List<Node> nodes = result.getNodes();
                if (nodes.size() > 0) {
                    //TODO: showing multiple devices is not tested yet
                    for (Node node : nodes) {
                        String nodeId = node.getId();
                        String nodeName = node.getDisplayName();
                        System.out.println("node found success!" + " " + nodeId + " " + nodeName);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                addItem(nodeName, nodeId);
                            }
                        });
                    }
                } else {
                    System.out.println("nodes no success!");
                }
                client.disconnect();
            }
        }).start();
    }

    private void addItem(String nodeName, String nodeId) {
        buildRecycleView(requireView());
        Device device = new Device(nodeId, nodeName, Connection.CONNECTION_KIND_WEAROS);
        adapter.addItem(new DeviceListItem(device, R.drawable.ic_watch_off));
    }

    private void buildRecycleView(View result) {
        RecyclerView recyclerView = result.findViewById(R.id.recyclerView_searchList);
        //recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        adapter = new DeviceListAdapter(item -> {
            model.selectDevice(item.getDevice());
            Navigation.findNavController(requireView()).navigate(DeviceSearchFragmentWearOSDirections.actionDeviceSearchFragmentToDeviceAddConfigurationFragment());
        });

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
