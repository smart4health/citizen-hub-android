package pt.uninova.s4h.citizenhub.persistence.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.time.LocalDate;
import java.util.List;

import pt.uninova.s4h.citizenhub.persistence.CitizenHubDatabase;
import pt.uninova.s4h.citizenhub.persistence.dao.PulseRateMeasurementDao;
import pt.uninova.s4h.citizenhub.persistence.entity.PulseRateMeasurementRecord;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

/** Repository used to call queries from the pulse rate measurement dao. */
public class PulseRateMeasurementRepository {

    private final PulseRateMeasurementDao pulseRateMeasurementDao;

    public PulseRateMeasurementRepository(Context context) {
        final CitizenHubDatabase citizenHubDatabase = CitizenHubDatabase.getInstance(context);

        pulseRateMeasurementDao = citizenHubDatabase.pulseRateMeasurementDao();
    }

    /** Inserts a pulse rate sample into the database.
     * @param record Record.
     * */
    public void create(PulseRateMeasurementRecord record) {
        CitizenHubDatabase.executorService().execute(() -> pulseRateMeasurementDao.insert(record));
    }

    /** Selects live data from the pulse rate database. Normally used to constantly update the UI whenever new information is added.
     * @param localDate Date.
     * @return Live data list with full pulse rate records.
     * */
    public LiveData<List<PulseRateMeasurementRecord>> read(LocalDate localDate) {
        return pulseRateMeasurementDao.selectLiveData(localDate, localDate.plusDays(1));
    }

    /** Reads daily pulse rate average.
     * @param localDate Date.
     * @return Live data containing the daily average pulse rate value.
     * */
    public LiveData<Double> readAverage(LocalDate localDate) {
        return pulseRateMeasurementDao.selectAverageLiveData(localDate, localDate.plusDays(1));
    }

}