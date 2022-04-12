package pt.uninova.s4h.citizenhub.ui.devices;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.Navigation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pt.uninova.s4h.citizenhub.DeviceConfigurationAddFragment;
import pt.uninova.s4h.citizenhub.DeviceConfigurationAddFragmentDirections;
import pt.uninova.s4h.citizenhub.R;
import pt.uninova.s4h.citizenhub.connectivity.Agent;
import pt.uninova.s4h.citizenhub.connectivity.AgentOrchestrator;
import pt.uninova.s4h.citizenhub.connectivity.AgentOrchestratorListener;
import pt.uninova.s4h.citizenhub.connectivity.Device;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.uprightgo2.UprightGo2Agent;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.uprightgo2.UprightGo2CalibrationProtocol;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.uprightgo2.UprightGo2VibrationProtocol;
import pt.uninova.s4h.citizenhub.service.CitizenHubService;
import pt.uninova.util.messaging.Observer;

public class DeviceViewModel extends AndroidViewModel {

    private final ServiceConnection serviceConnection;
    private final AgentOrchestratorListener agentOrchestratorListener;

    private final MutableLiveData<AgentOrchestrator> agentOrchestratorLiveData;
    private final MutableLiveData<List<Device>> deviceListLiveData;
    private final MutableLiveData<Device> selectedDeviceLiveData;
    private final MutableLiveData<Agent> selectedAgentLiveData;

    public DeviceViewModel(Application application) {
        super(application);

        agentOrchestratorLiveData = new MutableLiveData<>();
        deviceListLiveData = new MutableLiveData<>(Collections.emptyList());
        selectedDeviceLiveData = new MutableLiveData<>();
        selectedAgentLiveData = new MutableLiveData<>(null);

        serviceConnection = new ServiceConnection() {

            private CitizenHubService service;

            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                service = ((CitizenHubService.Binder) iBinder).getService();

                final AgentOrchestrator agentOrchestrator = service.getAgentOrchestrator();

                agentOrchestrator.addListener(agentOrchestratorListener);

                final List<Device> deviceList = new ArrayList<>(agentOrchestrator.getDevices());

                Collections.sort(deviceList);

                agentOrchestratorLiveData.postValue(agentOrchestrator);
                deviceListLiveData.postValue(deviceList);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                service.getAgentOrchestrator().removeListener(agentOrchestratorListener);
                agentOrchestratorLiveData.postValue(null);
            }
        };

        agentOrchestratorListener = new AgentOrchestratorListener() {
            @Override
            public void onDeviceAdded(Device device) {
                final List<Device> deviceList = deviceListLiveData.getValue();

                deviceList.add(device);
                Collections.sort(deviceList);
                deviceListLiveData.postValue(deviceList);
            }

            @Override
            public void onDeviceRemoved(Device device) {
                final List<Device> deviceList = deviceListLiveData.getValue();

                deviceList.remove(device);
                deviceListLiveData.postValue(deviceList);
            }
        };

        CitizenHubService.bind(application, serviceConnection);
    }

    public LiveData<Agent> getSelectedAgentLiveData() {
        if (agentOrchestratorLiveData.getValue().getDevices().contains(selectedDeviceLiveData.getValue())) {
            selectedAgentLiveData.postValue(agentOrchestratorLiveData.getValue().getAgent(selectedDeviceLiveData.getValue()));
        } else {
            agentOrchestratorLiveData.getValue().identify(selectedDeviceLiveData.getValue(), agent -> {
                selectedAgentLiveData.postValue(agent);
            });
        }

        return selectedAgentLiveData;
    }

    public LiveData<List<Device>> getDeviceList() {
        return deviceListLiveData;
    }

    public LiveData<Device> getSelectedDevice() {
        return selectedDeviceLiveData;
    }

    public Agent getSelectedDeviceAgent() {
        final AgentOrchestrator agentOrchestrator = agentOrchestratorLiveData.getValue();
        final Device device = selectedDeviceLiveData.getValue();

        if (device == null) {
            return null;
        }

        return agentOrchestrator.getAgent(device);
    }

    public boolean hasAgentAttached(Device device) {
        return agentOrchestratorLiveData.getValue().getAgent(device) != null;
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        CitizenHubService.unbind(getApplication(), serviceConnection);
    }

    public void removeSelectedDevice() {
        final AgentOrchestrator agentOrchestrator = agentOrchestratorLiveData.getValue();
        final Device device = getSelectedDevice().getValue();
        final Agent agent = agentOrchestrator.getAgent(device);

        if (agent != null) {
            agent.disable();
        }

        agentOrchestrator.remove(device);
    }

    public void selectDevice(Device device) {
        selectedDeviceLiveData.postValue(device);
    }

    public void addAgent(Agent agent) {
        agentOrchestratorLiveData.getValue().add(agent);
    }

    public void identifySelectedDevice(Observer<Agent> observer) {
        agentOrchestratorLiveData.getValue().identify(selectedDeviceLiveData.getValue(), observer);
    }
}
