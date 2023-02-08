package pt.uninova.s4h.citizenhub.persistence.repository;

import android.content.Context;

import java.time.LocalDate;
import java.util.List;

import androidx.lifecycle.LiveData;
import pt.uninova.s4h.citizenhub.persistence.CitizenHubDatabase;
import pt.uninova.s4h.citizenhub.persistence.dao.HeartRateMeasurementDao;
import pt.uninova.s4h.citizenhub.persistence.entity.HeartRateMeasurementRecord;
import pt.uninova.s4h.citizenhub.persistence.entity.util.SummaryDetailHeartRateUtil;
import pt.uninova.s4h.citizenhub.persistence.entity.util.SummaryDetailUtil;
import pt.uninova.s4h.citizenhub.persistence.entity.util.AggregateSummary;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

public class HeartRateMeasurementRepository {

    private final HeartRateMeasurementDao heartRateMeasurementDao;

    public HeartRateMeasurementRepository(Context context) {
        final CitizenHubDatabase citizenHubDatabase = CitizenHubDatabase.getInstance(context);

        heartRateMeasurementDao = citizenHubDatabase.heartRateMeasurementDao();
    }

    public void create(HeartRateMeasurementRecord record) {
        CitizenHubDatabase.executorService().execute(() -> heartRateMeasurementDao.insert(record));
    }

    public LiveData<List<HeartRateMeasurementRecord>> read(LocalDate localDate) {
        return heartRateMeasurementDao.selectLiveData(localDate, localDate.plusDays(1));
    }

    public LiveData<AggregateSummary> readAggregate(LocalDate localDate) {
        return heartRateMeasurementDao.selectAggregateLiveData(localDate, localDate.plusDays(1));
    }

    public LiveData<Double> readAverage(LocalDate localDate) {
        return heartRateMeasurementDao.selectAverageLiveData(localDate, localDate.plusDays(1));
    }

    public void readAverageObserved(LocalDate localDate, Observer<Double> observer){
        CitizenHubDatabase.executorService().execute(() -> observer.observe(heartRateMeasurementDao.selectAverage(localDate, localDate.plusDays(1))));
    }

    public void selectLastDay(LocalDate localDate, Observer<List<SummaryDetailHeartRateUtil>> observer){
        CitizenHubDatabase.executorService().execute(() -> observer.observe(heartRateMeasurementDao.selectLastDay(localDate)));
    }

    public void selectSeveralDays(LocalDate localDate, int days, Observer<List<SummaryDetailHeartRateUtil>> observer){
        CitizenHubDatabase.executorService().execute(() -> observer.observe(heartRateMeasurementDao.selectSeveralDays(localDate.minusDays(days - 1), localDate.plusDays(1), days)));
    }

    public void readAvgLastDay(Observer<List<SummaryDetailUtil>> observer, LocalDate localDate){
        CitizenHubDatabase.executorService().execute(() -> observer.observe(heartRateMeasurementDao.selectAvgLastDay(localDate)));
    }

    public void readMaxLastDay(Observer<List<SummaryDetailUtil>> observer, LocalDate localDate){
        CitizenHubDatabase.executorService().execute(() -> observer.observe(heartRateMeasurementDao.selectMaxLastDay(localDate)));
    }

    public void readMinLastDay(Observer<List<SummaryDetailUtil>> observer, LocalDate localDate){
        CitizenHubDatabase.executorService().execute(() -> observer.observe(heartRateMeasurementDao.selectMinLastDay(localDate)));
    }

    public void readAvgLastSevenDays(Observer<List<SummaryDetailUtil>> observer , LocalDate localDate){
        CitizenHubDatabase.executorService().execute(() -> observer.observe(heartRateMeasurementDao.selectAvgLastSevenDays(localDate.minusDays(6), localDate)));
    }

    public void readMaxLastSevenDays(Observer<List<SummaryDetailUtil>> observer , LocalDate localDate){
        CitizenHubDatabase.executorService().execute(() -> observer.observe(heartRateMeasurementDao.selectMaxLastSevenDays(localDate.minusDays(6), localDate)));
    }

    public void readMinLastSevenDays(Observer<List<SummaryDetailUtil>> observer , LocalDate localDate){
        CitizenHubDatabase.executorService().execute(() -> observer.observe(heartRateMeasurementDao.selectMinLastSevenDays(localDate.minusDays(6), localDate)));
    }

    public void readAvgLastThirtyDays(Observer<List<SummaryDetailUtil>> observer, LocalDate localDate){
        CitizenHubDatabase.executorService().execute(() -> observer.observe(heartRateMeasurementDao.selectAvgLastThirtyDays(localDate.minusDays(29), localDate)));
    }

    public void readMaxLastThirtyDays(Observer<List<SummaryDetailUtil>> observer , LocalDate localDate){
        CitizenHubDatabase.executorService().execute(() -> observer.observe(heartRateMeasurementDao.selectMaxLastThirtyDays(localDate.minusDays(29), localDate)));
    }

    public void readMinLastThirtyDays(Observer<List<SummaryDetailUtil>> observer, LocalDate localDate){
        CitizenHubDatabase.executorService().execute(() -> observer.observe(heartRateMeasurementDao.selectMinLastThirtyDays(localDate.minusDays(29), localDate)));
    }

}