package pt.uninova.s4h.citizenhub.persistence.repository;

import android.content.Context;

import java.time.LocalDate;
import java.util.List;

import androidx.lifecycle.LiveData;
import pt.uninova.s4h.citizenhub.persistence.CitizenHubDatabase;
import pt.uninova.s4h.citizenhub.persistence.dao.StepsSnapshotMeasurementDao;
import pt.uninova.s4h.citizenhub.persistence.entity.StepsSnapshotMeasurementRecord;
import pt.uninova.s4h.citizenhub.persistence.entity.util.SummaryDetailUtil;
import pt.uninova.s4h.citizenhub.persistence.entity.util.WalkingInformation;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

public class StepsSnapshotMeasurementRepository {

    private final StepsSnapshotMeasurementDao stepsSnapshotMeasurementDao;

    public StepsSnapshotMeasurementRepository(Context context) {
        final CitizenHubDatabase citizenHubDatabase = CitizenHubDatabase.getInstance(context);

        stepsSnapshotMeasurementDao = citizenHubDatabase.stepsSnapshotMeasurementDao();
    }

    public void create(StepsSnapshotMeasurementRecord record) {
        CitizenHubDatabase.executorService().execute(() -> stepsSnapshotMeasurementDao.insert(record));
    }

    public LiveData<List<StepsSnapshotMeasurementRecord>> read(LocalDate localDate) {
        return stepsSnapshotMeasurementDao.selectLiveData(localDate, localDate.plusDays(1));
    }

    public LiveData<Integer> readMaximum(LocalDate localDate) {
        return stepsSnapshotMeasurementDao.selectMaximumLiveData(localDate, localDate.plusDays(1));
    }

    public void readMaximumObserved(LocalDate localDate, Observer<Double> observer) {
        CitizenHubDatabase.executorService().execute(() -> observer.observe(stepsSnapshotMeasurementDao.selectMaximum(localDate, localDate.plusDays(1))));
    }

    public LiveData<WalkingInformation> readLatestWalkingInformation(LocalDate localDate) {
        return stepsSnapshotMeasurementDao.selectLatestWalkingInformationLiveData(localDate, localDate.plusDays(1));
    }

    public void readLastDay(LocalDate localDate, Observer<List<SummaryDetailUtil>> observer){
        CitizenHubDatabase.executorService().execute(() -> observer.observe(stepsSnapshotMeasurementDao.selectLastDay(localDate)));
    }

    public void readLastSevenDays(LocalDate localDate, Observer<List<SummaryDetailUtil>> observer){
        CitizenHubDatabase.executorService().execute(() -> observer.observe(stepsSnapshotMeasurementDao.selectLastSevenDays(localDate.minusDays(6), localDate)));
    }

    public void readLastThirtyDays(LocalDate localDate, Observer<List<SummaryDetailUtil>> observer){
        CitizenHubDatabase.executorService().execute(() -> observer.observe(stepsSnapshotMeasurementDao.selectLastThirtyDays(localDate.minusDays(29), localDate)));
    }

    public void readSeveralDays(LocalDate localDate, int days, Observer<List<SummaryDetailUtil>> observer){
        CitizenHubDatabase.executorService().execute(() -> observer.observe(stepsSnapshotMeasurementDao.selectSeveralDays(localDate.minusDays(days - 1), localDate.plusDays(1), days)));
    }
}