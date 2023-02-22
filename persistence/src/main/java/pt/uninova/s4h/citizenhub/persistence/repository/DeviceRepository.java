package pt.uninova.s4h.citizenhub.persistence.repository;

import android.content.Context;

import java.util.List;

import pt.uninova.s4h.citizenhub.persistence.CitizenHubDatabase;
import pt.uninova.s4h.citizenhub.persistence.dao.DeviceDao;
import pt.uninova.s4h.citizenhub.persistence.entity.DeviceRecord;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

/** Repository used to call queries from the calories measurement dao. */
public class DeviceRepository {

    private final DeviceDao deviceDao;

    public DeviceRepository(Context context) {
        final CitizenHubDatabase citizenHubDatabase = CitizenHubDatabase.getInstance(context);

        deviceDao = citizenHubDatabase.deviceDao();
    }

    /** Inserts a new entry into the database.
     * @param record Entry to insert.
     * */
    public void create(DeviceRecord record) {
        CitizenHubDatabase.executorService().execute(() -> deviceDao.insert(record));
    }

    /** Deletes the whole table. */
    public void delete() {
        CitizenHubDatabase.executorService().execute(deviceDao::delete);
    }

    /** Deletes a specific record from the database.
     * @param record Record to delete.
     * */
    public void delete(DeviceRecord record) {
        CitizenHubDatabase.executorService().execute(() -> deviceDao.delete(record));
    }

    /** Deletes a specific record from the database given an address.
     * @param address Record's address to delete.
     * */
    public void delete(String address) {
        CitizenHubDatabase.executorService().execute(() -> deviceDao.delete(address));
    }

    /** Selects all the devices saved in the devices table.
     * @param observer */
    public void read(Observer<List<DeviceRecord>> observer) {
        CitizenHubDatabase.executorService().execute(() -> observer.observe(deviceDao.select()));
    }

    /** Selects a specific device given a unique address.
     * @param address Device's address.
     * @param observer
     * */
    public void read(String address, Observer<DeviceRecord> observer) {
        CitizenHubDatabase.executorService().execute(() -> observer.observe(deviceDao.select(address)));
    }

    /** Updates a device record.
     * @param record Device record to update. */
    public void update(DeviceRecord record) {
        CitizenHubDatabase.executorService().execute(() -> deviceDao.update(record));
    }

    /** Updates a device record based on its address.
     * @param address Device address.
     * @param agent New device agent.
     * */
    public void updateAgent(String address, String agent) {
        CitizenHubDatabase.executorService().execute(() -> deviceDao.updateAgent(address, agent));
    }
}