package pt.uninova.s4h.citizenhub.persistence.repository;

import android.content.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.uninova.s4h.citizenhub.persistence.CitizenHubDatabase;
import pt.uninova.s4h.citizenhub.persistence.dao.SettingDao;
import pt.uninova.s4h.citizenhub.persistence.entity.SettingRecord;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

public class SettingRepository {

    private final SettingDao settingDao;

    public SettingRepository(Context context) {
        final CitizenHubDatabase citizenHubDatabase = CitizenHubDatabase.getInstance(context);

        settingDao = citizenHubDatabase.settingDao();
    }

    public void createOrUpdate(String address, String key, String value) {
        CitizenHubDatabase.executorService().execute(() -> settingDao.insert(address, key, value));
    }

    public void read(String address, Observer<List<SettingRecord>> observer) {
        CitizenHubDatabase.executorService().execute(() -> observer.observe(settingDao.select(address)));
    }

    public void read(String address, String name, Observer<String> observer) {
        CitizenHubDatabase.executorService().execute(() -> {
            String val = settingDao.selectValue(address, name);
            observer.observe(val);
        });
    }

    public void readBySample(long sampleId, Observer<Map<String, String>> observer) {
        CitizenHubDatabase.executorService().execute(() -> {
            final List<SettingRecord> settingRecordList = settingDao.selectBySample(sampleId);
            final Map<String, String> map = new HashMap<>();

            for (final SettingRecord i : settingRecordList) {
                map.put(i.getKey(), i.getValue());
            }

            observer.observe(map);
        });
    }
}