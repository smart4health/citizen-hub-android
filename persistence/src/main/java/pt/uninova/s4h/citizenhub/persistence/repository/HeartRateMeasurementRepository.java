package pt.uninova.s4h.citizenhub.persistence.repository;

import android.content.Context;

import java.time.LocalDate;
import java.util.List;

import androidx.lifecycle.LiveData;
import pt.uninova.s4h.citizenhub.persistence.CitizenHubDatabase;
import pt.uninova.s4h.citizenhub.persistence.dao.HeartRateMeasurementDao;
import pt.uninova.s4h.citizenhub.persistence.entity.HeartRateMeasurementRecord;
import pt.uninova.s4h.citizenhub.persistence.entity.util.DailyHeartRatePanel;
import pt.uninova.s4h.citizenhub.persistence.entity.util.HourlyHeartRatePanel;
import pt.uninova.s4h.citizenhub.persistence.entity.util.AggregateSummary;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

/** Repository used to call queries from the heart rate measurement dao. */
public class HeartRateMeasurementRepository {

    private final HeartRateMeasurementDao heartRateMeasurementDao;

    public HeartRateMeasurementRepository(Context context) {
        final CitizenHubDatabase citizenHubDatabase = CitizenHubDatabase.getInstance(context);

        heartRateMeasurementDao = citizenHubDatabase.heartRateMeasurementDao();
    }

    public void create(HeartRateMeasurementRecord record) {
        CitizenHubDatabase.executorService().execute(() -> heartRateMeasurementDao.insert(record));
    }

    /** Selects heart rate records from the heart rate database. Normally used to constantly update the UI whenever new information is added.
     * @param localDate Date.
     * @return Livedata list with all heart rate records.
     * */
    public LiveData<List<HeartRateMeasurementRecord>> read(LocalDate localDate) {
        return heartRateMeasurementDao.selectLiveData(localDate, localDate.plusDays(1));
    }

    /** Selects live data (Max, Min and Avg) from the heart rate database. Normally used to constantly update the UI whenever new information is added.
     * @param localDate Date.
     * @return Livedata with a summary of the heart rate attributes.
     * */
    public LiveData<AggregateSummary> readAggregate(LocalDate localDate) {
        return heartRateMeasurementDao.selectAggregateLiveData(localDate, localDate.plusDays(1));
    }

    /** Selects live data (only Avg) from the heart rate database. Normally used to constantly update the UI whenever new information is added.
     * @param localDate Date.
     * @return Livedata containing a double with the average daily heart rate.
     * */
    public LiveData<Double> readAverage(LocalDate localDate) {
        return heartRateMeasurementDao.selectAverageLiveData(localDate, localDate.plusDays(1));
    }

    public void readAverageObserved(LocalDate localDate, Observer<Double> observer){
        CitizenHubDatabase.executorService().execute(() -> observer.observe(heartRateMeasurementDao.selectAverage(localDate, localDate.plusDays(1))));
    }

    /** Selects steps information from one specific day.
     * @param localDate Date.
     * @param observer
     * */
    public void selectLastDay(LocalDate localDate, Observer<List<HourlyHeartRatePanel>> observer){
        CitizenHubDatabase.executorService().execute(() -> observer.observe(heartRateMeasurementDao.selectLastDay(localDate)));
    }

    /** Selects heart rate information from a range of days.
     * @param localDate Date.
     * @param days Days range.
     * @param observer
     * */
    public void selectSeveralDays(LocalDate localDate, int days, Observer<List<DailyHeartRatePanel>> observer){
        CitizenHubDatabase.executorService().execute(() -> observer.observe(heartRateMeasurementDao.selectSeveralDays(localDate.minusDays(days - 1), localDate.plusDays(1), days)));
    }

    public void selectBasedOnId(Long sampleId, Observer<Integer> observer){
        CitizenHubDatabase.executorService().execute(() -> observer.observe(heartRateMeasurementDao.selectBasedOnId(sampleId)));
    }

}