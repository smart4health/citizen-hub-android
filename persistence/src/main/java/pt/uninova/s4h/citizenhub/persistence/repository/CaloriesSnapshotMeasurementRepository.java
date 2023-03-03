package pt.uninova.s4h.citizenhub.persistence.repository;

import android.content.Context;

import java.time.LocalDate;
import java.util.List;

import androidx.lifecycle.LiveData;
import pt.uninova.s4h.citizenhub.persistence.CitizenHubDatabase;
import pt.uninova.s4h.citizenhub.persistence.dao.CaloriesSnapshotMeasurementDao;
import pt.uninova.s4h.citizenhub.persistence.entity.CaloriesSnapshotMeasurementRecord;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

public class CaloriesSnapshotMeasurementRepository {

    private final CaloriesSnapshotMeasurementDao caloriesSnapshotMeasurementDao;

    public CaloriesSnapshotMeasurementRepository(Context context) {
        final CitizenHubDatabase citizenHubDatabase = CitizenHubDatabase.getInstance(context);

        caloriesSnapshotMeasurementDao = citizenHubDatabase.caloriesSnapshotMeasurementDao();
    }

    public void create(CaloriesSnapshotMeasurementRecord record) {
        CitizenHubDatabase.executorService().execute(() -> caloriesSnapshotMeasurementDao.insert(record));
    }

    public LiveData<List<CaloriesSnapshotMeasurementRecord>> read(LocalDate localDate) {
        return caloriesSnapshotMeasurementDao.selectLiveData(localDate, localDate.plusDays(1));
    }

    public LiveData<Double> readMaximum(LocalDate localDate) {
        return caloriesSnapshotMeasurementDao.selectMaximumLiveData(localDate, localDate.plusDays(1));
    }
}