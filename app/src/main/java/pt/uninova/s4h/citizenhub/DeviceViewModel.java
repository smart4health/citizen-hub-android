package pt.uninova.s4h.citizenhub;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import pt.uninova.s4h.citizenhub.persistence.Device;
import pt.uninova.s4h.citizenhub.persistence.DeviceRepository;

import static java.util.Objects.requireNonNull;

public class DeviceViewModel extends AndroidViewModel {
    private MutableLiveData<Device> device;
    private LiveData<List<Device>> deviceList;
    final private DeviceRepository deviceRepository;
    private List<Device> pairedDevices;
    private Set<String> setAddress = new HashSet<>();

    public DeviceViewModel(Application application) {
        super(application);
        deviceRepository = new DeviceRepository(application);
        device = new MutableLiveData<>();
        deviceList = deviceRepository.getAll();
        pairedDevices = deviceList.getValue();
    }

    public LiveData<List<Device>> getDevices() {
        return deviceList;
    }

    public boolean isDevicePaired(String address) {
        pairedDevices = deviceList.getValue();
        setAddress = requireNonNull(pairedDevices).stream()
                .map(Device::getAddress)
                .collect(Collectors.toSet());
        return setAddress.contains(address);
    }

    public MutableLiveData<Device> getSelectedDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device.postValue(device);
    }

    public void apply() {
        setAddress.add(requireNonNull(device.getValue()).getAddress());
        deviceRepository.add(device.getValue());
    }

    public void delete(Device device) {
        setAddress.remove(device.getAddress());
        deviceRepository.remove(device);
    }
}