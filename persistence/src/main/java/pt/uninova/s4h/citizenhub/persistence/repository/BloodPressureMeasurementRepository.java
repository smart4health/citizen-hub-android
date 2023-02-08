package pt.uninova.s4h.citizenhub.persistence.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.time.LocalDate;
import java.util.List;

import pt.uninova.s4h.citizenhub.persistence.CitizenHubDatabase;
import pt.uninova.s4h.citizenhub.persistence.dao.BloodPressureMeasurementDao;
import pt.uninova.s4h.citizenhub.persistence.entity.BloodPressureMeasurementRecord;
import pt.uninova.s4h.citizenhub.persistence.entity.util.SummaryDetailBloodPressureUtil;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

public class BloodPressureMeasurementRepository {

    private final BloodPressureMeasurementDao bloodPressureMeasurementDao;

    public BloodPressureMeasurementRepository(Context context) {
        final CitizenHubDatabase citizenHubDatabase = CitizenHubDatabase.getInstance(context);

        bloodPressureMeasurementDao = citizenHubDatabase.bloodPressureMeasurementDao();
    }

    public void create(BloodPressureMeasurementRecord record) {
        CitizenHubDatabase.executorService().execute(() -> bloodPressureMeasurementDao.insert(record));
    }

    public LiveData<List<BloodPressureMeasurementRecord>> read(LocalDate localDate) {
        return bloodPressureMeasurementDao.selectLiveData(localDate, localDate.plusDays(1));
    }

    public void readLastDay(LocalDate localDate, Observer<List<SummaryDetailBloodPressureUtil>> observer) {
        CitizenHubDatabase.executorService().execute(() -> observer.observe(bloodPressureMeasurementDao.selectLastDay(localDate)));
    }

    public void selectSeveralDays(LocalDate localDate, int days, Observer<List<SummaryDetailBloodPressureUtil>> observer){
        CitizenHubDatabase.executorService().execute(() -> observer.observe(bloodPressureMeasurementDao.selectSeveralDays(localDate.minusDays(days - 1), localDate.plusDays(1), days)));
    }

    public LiveData<BloodPressureMeasurementRecord> readLatest(LocalDate localDate) {
        return bloodPressureMeasurementDao.selectLatestLiveData(localDate, localDate.plusDays(1));
    }
}