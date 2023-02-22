package pt.uninova.s4h.citizenhub.persistence.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.time.LocalDate;
import java.util.List;

import pt.uninova.s4h.citizenhub.persistence.CitizenHubDatabase;
import pt.uninova.s4h.citizenhub.persistence.dao.StepsSnapshotMeasurementDao;
import pt.uninova.s4h.citizenhub.persistence.entity.StepsSnapshotMeasurementRecord;
import pt.uninova.s4h.citizenhub.persistence.entity.util.WalkingInformation;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

/** Repository used to call queries from the steps snapshot measurement dao. */
public class StepsSnapshotMeasurementRepository {

    private final StepsSnapshotMeasurementDao stepsSnapshotMeasurementDao;

    public StepsSnapshotMeasurementRepository(Context context) {
        final CitizenHubDatabase citizenHubDatabase = CitizenHubDatabase.getInstance(context);

        stepsSnapshotMeasurementDao = citizenHubDatabase.stepsSnapshotMeasurementDao();
    }

    /** Inserts a steps sample in the database.
     * @param record Record.
     * */
    public void create(StepsSnapshotMeasurementRecord record) {
        CitizenHubDatabase.executorService().execute(() -> stepsSnapshotMeasurementDao.insert(record));
    }

    /** Selects steps live data for continuous UI update.
     * @param localDate Date.
     * @return Live data list with steps record.
     * */
    public LiveData<List<StepsSnapshotMeasurementRecord>> read(LocalDate localDate) {
        return stepsSnapshotMeasurementDao.selectLiveData(localDate, localDate.plusDays(1));
    }

    /** Selects the steps taken in a day.
     * @param localDate Date.
     * @return Live data containing the total number of steps from one day.
     * */
    public LiveData<Integer> readMaximum(LocalDate localDate) {
        return stepsSnapshotMeasurementDao.selectMaximumLiveData(localDate, localDate.plusDays(1));
    }

    /** Selects total steps from a day.
     * @param localDate Date.
     * @param observer
     * */
    public void readMaximumObserved(LocalDate localDate, Observer<Double> observer) {
        CitizenHubDatabase.executorService().execute(() -> observer.observe(stepsSnapshotMeasurementDao.selectMaximum(localDate, localDate.plusDays(1))));
    }

    public LiveData<WalkingInformation> readLatestWalkingInformation(LocalDate localDate) {
        return stepsSnapshotMeasurementDao.selectLatestWalkingInformationLiveData(localDate, localDate.plusDays(1));
    }

    public void selectBasedOnId(Long sampleId, Observer<Integer> observer) {
        CitizenHubDatabase.executorService().execute(() -> observer.observe(stepsSnapshotMeasurementDao.selectBasedOnId(sampleId)));
    }
}