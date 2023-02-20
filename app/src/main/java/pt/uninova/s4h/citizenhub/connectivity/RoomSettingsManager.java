package pt.uninova.s4h.citizenhub.connectivity;

import android.content.Context;

import pt.uninova.s4h.citizenhub.persistence.repository.SettingRepository;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

public class RoomSettingsManager implements SettingsManager {

    private final String address;

    private final SettingRepository settingRepository;

    public RoomSettingsManager(Context context, String address) {
        this.address = address;
        this.settingRepository = new SettingRepository(context);
    }

    @Override
    public void get(String name, Observer<String> observer) {
        settingRepository.read(address, name, observer);
    }

    @Override
    public void set(String name, String value) {
        settingRepository.createOrUpdate(address, name, value);
    }
}
