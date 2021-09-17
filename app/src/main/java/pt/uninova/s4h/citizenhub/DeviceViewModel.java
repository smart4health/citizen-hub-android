package pt.uninova.s4h.citizenhub;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import pt.uninova.s4h.citizenhub.connectivity.Agent;
import pt.uninova.s4h.citizenhub.connectivity.AgentFactory;
import pt.uninova.s4h.citizenhub.connectivity.AgentOrchestrator;
import pt.uninova.s4h.citizenhub.connectivity.MeasuringProtocol;
import pt.uninova.s4h.citizenhub.persistence.ConnectionKind;
import pt.uninova.s4h.citizenhub.persistence.Device;
import pt.uninova.s4h.citizenhub.persistence.DeviceRepository;
import pt.uninova.s4h.citizenhub.persistence.Feature;
import pt.uninova.s4h.citizenhub.persistence.FeatureRepository;
import pt.uninova.s4h.citizenhub.persistence.MeasurementKind;
import pt.uninova.s4h.citizenhub.persistence.MeasurementRepository;
import pt.uninova.s4h.citizenhub.persistence.StateKind;
import pt.uninova.s4h.citizenhub.service.CitizenHubService;
import pt.uninova.s4h.citizenhub.service.CitizenHubServiceBound;
import pt.uninova.util.messaging.Observer;

public class DeviceViewModel extends AndroidViewModel {
    private final MutableLiveData<Device> device;
    final private DeviceRepository deviceRepository;
    private final MutableLiveData<Feature> feature;
    private final LiveData<List<Feature>> featureList;
    final private FeatureRepository featureRepository;
    private List<Device> deviceList;

    public DeviceViewModel(Application application) {
        super(application);
        deviceRepository = new DeviceRepository(application);
        device = new MutableLiveData<>();
        deviceRepository.obtainAll(value -> deviceList = value);

        featureRepository = new FeatureRepository(application);
        feature = new MutableLiveData<>();
        featureList = featureRepository.getAllLive();
    }

    public List<Device> getAllActive() {
        return deviceRepository.getWithState(StateKind.ACTIVE);
    }

    public List<Device> getAllInactive() {
        return deviceRepository.getWithState(StateKind.INACTIVE);
    }

    public List<Device> getWithAgent(String type) {
        return deviceRepository.getWithAgent(type);
    }

    public List<FeatureListItem> getSupportedFeatures(AgentOrchestrator agentOrchestrator) {
        final List<FeatureListItem> supportedFeaturesList = new ArrayList<>();

        final Agent agent = agentOrchestrator.getDeviceAgentMap().get(device.getValue());

        for (MeasurementKind kind : agent.getSupportedMeasurements()) {
            supportedFeaturesList.add(new FeatureListItem(kind));
        }

        return supportedFeaturesList;
    }

    public List<FeatureListItem> getEnabledFeatures(AgentOrchestrator agentOrchestrator) {
        List<FeatureListItem> featureList = new ArrayList<>();
        obtainKindsFromDevice(device.getValue().getAddress(), enabledFeatures -> {

            final Set<MeasurementKind> enabledFeaturesSet = new HashSet<>(enabledFeatures);

            for (MeasurementKind feature : getSelectedAgent(agentOrchestrator).getSupportedMeasurements()) {//(device.getValue().getName())) {

                featureList.add(new FeatureListItem(feature, enabledFeaturesSet.contains(feature)));

            }
        });
        return featureList;

    }

    public LiveData<List<Feature>> getAll() {
        return featureList;
    }

    public void obtainKindsFromDevice(String device_address, Observer<List<MeasurementKind>> observer) {
        featureRepository.obtainKindsFromDevice(device_address, observer);
    }

    public MutableLiveData<Feature> getSelectedFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature.postValue(feature);
    }

    public void apply(Feature feature, AgentOrchestrator agentOrchestrator, MeasurementRepository measurementRepository) {
        getSelectedAgent(agentOrchestrator).enableMeasurement(feature.getKind(), measurementRepository::add);
        featureRepository.add(feature);
    }

    public void delete(Feature feature) {
        featureRepository.remove(feature);
    }

    public List<Device> getDevices() {
        return deviceList;
    }

    public boolean isDevicePaired(Device device) {
        //TODO ir buscar ao orchestrator
        List<Device> pairedDevices = deviceList;
        if (pairedDevices != null) {
            return deviceList.contains(device);
        }
        return false;
    }

    public MutableLiveData<Device> getSelectedDevice() {
        return device;
    }


    public void createAgent(CitizenHubService service, ConnectionKind connectionKind, Observer<Agent> observer) {
        AgentFactory factory = new AgentFactory(service);

        factory.create(connectionKind, device.getValue().getAddress(), observer);
    }

    public Agent getSelectedAgent(AgentOrchestrator agentOrchestrator) {
        Agent agent = agentOrchestrator.getDeviceAgentMap().get(device.getValue());

        if (agent == null) throw new NullPointerException();
        return agent;
    }

    public void setDevice(Device device) {
        this.device.postValue(device);
    }


    public void apply() {
        deviceRepository.add(device.getValue());
        deviceList.add(device.getValue());
    }

    public void delete(Device device) {
        deviceRepository.remove(device);
        deviceList.remove(device);
        //TODO
    }
}