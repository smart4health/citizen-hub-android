package pt.uninova.s4h.citizenhub.persistence.repository;

import android.content.Context;

import java.time.LocalDate;
import java.util.List;

import androidx.lifecycle.LiveData;
import pt.uninova.s4h.citizenhub.persistence.CitizenHubDatabase;
import pt.uninova.s4h.citizenhub.persistence.dao.DistanceMeasurementDao;
import pt.uninova.s4h.citizenhub.persistence.entity.DistanceMeasurementRecord;
import pt.uninova.s4h.citizenhub.persistence.entity.util.DailyDistancePanel;
import pt.uninova.s4h.citizenhub.persistence.entity.util.HourlyDistancePanel;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

/** Repository used to call queries from the distance measurement dao. */
public class DistanceMeasurementRepository {

    private final DistanceMeasurementDao distanceMeasurementDao;

    public DistanceMeasurementRepository(Context context) {
        final CitizenHubDatabase citizenHubDatabase = CitizenHubDatabase.getInstance(context);

        distanceMeasurementDao = citizenHubDatabase.distanceMeasurementDao();
    }

    public void create(DistanceMeasurementRecord record) {
        CitizenHubDatabase.executorService().execute(() -> distanceMeasurementDao.insert(record));
    }

    public LiveData<List<DistanceMeasurementRecord>> read(LocalDate localDate) {
        return distanceMeasurementDao.selectLiveData(localDate, localDate.plusDays(1));
    }

    public LiveData<Double> readMaximum(LocalDate localDate) {
        return distanceMeasurementDao.selectMaximumLiveData(localDate, localDate.plusDays(1));
    }

    public LiveData<Double> getDistanceAllTypes (LocalDate localDate) {
        return distanceMeasurementDao.getDistanceAllTypes(localDate, localDate.plusDays(1));
    }

    /** Selects steps information from one specific day.
     * @param localDate Date.
     * @param observer
     * @return
     * */
    public void readLastDay(LocalDate localDate, Observer<List<HourlyDistancePanel>> observer){
        CitizenHubDatabase.executorService().execute(() -> observer.observe(distanceMeasurementDao.selectLastDay(localDate)));
    }

    /** Selects distance information from a range of days.
     * @param localDate Date.
     * @param days Days range.
     * @param observer
     * @return
     * */
    public void readSeveralDays(LocalDate localDate, int days, Observer<List<DailyDistancePanel>> observer){
        CitizenHubDatabase.executorService().execute(() -> observer.observe(distanceMeasurementDao.selectSeveralDays(localDate.minusDays(days - 1), localDate.plusDays(1), days)));
    }

}