package pt.uninova.s4h.citizenhub.persistence.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.time.LocalDate;
import java.util.List;

import pt.uninova.s4h.citizenhub.persistence.CitizenHubDatabase;
import pt.uninova.s4h.citizenhub.persistence.dao.BreathingRateMeasurementDao;
import pt.uninova.s4h.citizenhub.persistence.entity.BreathingRateMeasurementRecord;
import pt.uninova.s4h.citizenhub.persistence.entity.util.AggregateSummary;

public class BreathingRateMeasurementRepository {

    private final BreathingRateMeasurementDao breathingRateMeasurementDao;

    public BreathingRateMeasurementRepository(Context context) {
        final CitizenHubDatabase citizenHubDatabase = CitizenHubDatabase.getInstance(context);

        breathingRateMeasurementDao = citizenHubDatabase.breathingRateMeasurementDao();
    }

    /** Inserts an entry into the database.
     * @param record Entry to insert.
     * */
    public void create(BreathingRateMeasurementRecord record) {
        CitizenHubDatabase.executorService().execute(() -> breathingRateMeasurementDao.insert(record));
    }

    /** Selects live data from the breathing database. Normally used to constantly update the UI whenever new information is added.
     * @param localDate Date.
     * @return Live data list containing breathing records.
     * */
    public LiveData<List<BreathingRateMeasurementRecord>> read(LocalDate localDate) {
        return breathingRateMeasurementDao.selectLiveData(localDate, localDate.plusDays(1));
    }

    public LiveData<AggregateSummary> readAggregate(LocalDate localDate) {
        return breathingRateMeasurementDao.selectAggregateLiveData(localDate, localDate.plusDays(1));
    }

    /** Selects the daily breathing average rate.
     * @param localDate Date.
     * @return Live data containing the daily average breathig rate.
     * */
    public LiveData<Double> readAverage(LocalDate localDate) {
        return breathingRateMeasurementDao.selectAverageLiveData(localDate, localDate.plusDays(1));
    }
}