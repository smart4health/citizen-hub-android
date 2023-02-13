package pt.uninova.s4h.citizenhub.ui.devices;

import android.app.Application;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pt.uninova.s4h.citizenhub.connectivity.Agent;
import pt.uninova.s4h.citizenhub.connectivity.AgentOrchestrator;
import pt.uninova.s4h.citizenhub.connectivity.AgentOrchestratorListener;
import pt.uninova.s4h.citizenhub.connectivity.StateChangedMessage;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.BluetoothAgent;
import pt.uninova.s4h.citizenhub.data.Device;
import pt.uninova.s4h.citizenhub.service.CitizenHubService;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

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
        selectedAgentLiveData = new MutableLiveData<>();
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
            public void onAgentAttached(Device device, Agent agent) {
                agent.addStateObserver(new Observer<StateChangedMessage<Integer, ? extends Agent>>() {
                    @Override
                    public void observe(StateChangedMessage<Integer, ? extends Agent> value) {
                        selectedAgentLiveData.postValue(agent);
                    }
                });
                selectedAgentLiveData.postValue(agent);
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
        if (agentOrchestratorLiveData.getValue() != null) {
            if (agentOrchestratorLiveData.getValue().getDevices().contains(selectedDeviceLiveData.getValue())) {
                selectedAgentLiveData.postValue(agentOrchestratorLiveData.getValue().getAgent(selectedDeviceLiveData.getValue()));
            } else {
                agentOrchestratorLiveData.getValue().identify(selectedDeviceLiveData.getValue(), selectedAgentLiveData::postValue);

            }
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

    public Agent getAttachedAgent(Device device) {
        return agentOrchestratorLiveData.getValue().getAgent(device);
    }

    public int getAttachedAgentState(Device device) {
        if (hasAgentAttached(device)) {
            return agentOrchestratorLiveData.getValue().getAgent(device).getState();
        }
        return 0;
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

            if (agent instanceof BluetoothAgent) {
                BluetoothAgent bluetoothAgent = (BluetoothAgent) agent;

                bluetoothAgent.getConnection().close();
            }
        }

        agentOrchestrator.remove(device);
    }

    public void reconnectDevice(Device device) {
        final AgentOrchestrator agentOrchestrator = agentOrchestratorLiveData.getValue();
        if (agentOrchestrator != null)
            agentOrchestrator.enableDevice(device);
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
