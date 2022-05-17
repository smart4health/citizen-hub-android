package pt.uninova.s4h.citizenhub.persistence.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.time.LocalDate;
import java.util.List;

import pt.uninova.s4h.citizenhub.persistence.CitizenHubDatabase;
import pt.uninova.s4h.citizenhub.persistence.dao.BloodPressureMeasurementDao;
import pt.uninova.s4h.citizenhub.persistence.entity.BloodPressureMeasurementRecord;

public class BloodPressureMeasurementRepository {

    private final BloodPressureMeasurementDao bloodPressureMeasurementDao;

    public BloodPressureMeasurementRepository(Context context) {
        final CitizenHubDatabase citizenHubDatabase = CitizenHubDatabase.getInstance(context);

        bloodPressureMeasurementDao = citizenHubDatabase.bloodPressureMeasurementDao();
    }

    public void add(BloodPressureMeasurementRecord record) {
        CitizenHubDatabase.executorService().execute(() -> bloodPressureMeasurementDao.insert(record));
    }

    public LiveData<List<BloodPressureMeasurementRecord>> get(LocalDate localDate) {
        return bloodPressureMeasurementDao.get(localDate, localDate.plusDays(1));
    }

}
