package pt.uninova.s4h.citizenhub.persistence.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.time.LocalDate;
import java.util.List;

import pt.uninova.s4h.citizenhub.persistence.CitizenHubDatabase;
import pt.uninova.s4h.citizenhub.persistence.dao.PostureMeasurementDao;
import pt.uninova.s4h.citizenhub.persistence.entity.PostureMeasurementRecord;
import pt.uninova.s4h.citizenhub.persistence.entity.util.SummaryDetailUtil;
import pt.uninova.s4h.citizenhub.persistence.entity.util.HourlyPosture;
import pt.uninova.s4h.citizenhub.persistence.entity.util.PostureClassificationSum;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

public class PostureMeasurementRepository {

    private final PostureMeasurementDao postureMeasurementDao;

    public PostureMeasurementRepository(Context context) {
        final CitizenHubDatabase citizenHubDatabase = CitizenHubDatabase.getInstance(context);

        postureMeasurementDao = citizenHubDatabase.postureMeasurementDao();
    }

    public void create(PostureMeasurementRecord record) {
        CitizenHubDatabase.executorService().execute(() -> postureMeasurementDao.insert(record));
    }

    public LiveData<List<PostureMeasurementRecord>> read(LocalDate localDate) {
        return postureMeasurementDao.selectLiveData(localDate, localDate.plusDays(1));
    }

    public void read(LocalDate localDate, Observer<List<HourlyPosture>> observer) {
        CitizenHubDatabase.executorService().execute(() -> observer.observe(postureMeasurementDao.selectHourlyPosture(localDate)));
    }

    public LiveData<List<PostureClassificationSum>> readClassificationSum(LocalDate localDate) {
        return postureMeasurementDao.selectClassificationSumLiveData(localDate, localDate.plusDays(1));
    }

    public void readLastDayCorrectPosture(LocalDate localDate, Observer<List<SummaryDetailUtil>> observer){
        CitizenHubDatabase.executorService().execute(() -> observer.observe(postureMeasurementDao.selectLastDayCorrectPosture(localDate)));
    }

    public void readLastDayIncorrectPosture(LocalDate localDate, Observer<List<SummaryDetailUtil>> observer){
        CitizenHubDatabase.executorService().execute(() -> observer.observe(postureMeasurementDao.selectLastDayIncorrectPosture(localDate)));
    }

    public void readLastSevenDaysCorrectPosture(LocalDate localDate, Observer<List<SummaryDetailUtil>> observer){
        CitizenHubDatabase.executorService().execute(() -> observer.observe(postureMeasurementDao.selectLastSevenDaysCorrectPosture(localDate.minusDays(6), localDate)));
    }

    public void readLastSevenDaysIncorrectPosture(LocalDate localDate, Observer<List<SummaryDetailUtil>> observer){
        CitizenHubDatabase.executorService().execute(() -> observer.observe(postureMeasurementDao.selectLastSevenDaysIncorrectPosture(localDate.minusDays(6), localDate)));
    }

    public void readLastThirtyDaysCorrectPosture(LocalDate localDate, Observer<List<SummaryDetailUtil>> observer){
        CitizenHubDatabase.executorService().execute(() -> observer.observe(postureMeasurementDao.selectLastThirtyDaysCorrectPosture(localDate.minusDays(29), localDate)));
    }

    public void readLastThirtyDaysIncorrectPosture(LocalDate localDate, Observer<List<SummaryDetailUtil>> observer){
        CitizenHubDatabase.executorService().execute(() -> observer.observe(postureMeasurementDao.selectLastThirtyDaysIncorrectPosture(localDate.minusDays(29), localDate)));
    }

    public void readSeveralDaysCorrectPosture(LocalDate localDate, int days, Observer<List<SummaryDetailUtil>> observer){
        CitizenHubDatabase.executorService().execute(() -> observer.observe(postureMeasurementDao.selectSeveralDaysCorrectPosture(localDate.minusDays(days - 1), localDate.plusDays(1), days)));
    }

    public void readSeveralDaysIncorrectPosture(LocalDate localDate, int days, Observer<List<SummaryDetailUtil>> observer){
        CitizenHubDatabase.executorService().execute(() -> observer.observe(postureMeasurementDao.selectSeveralDaysIncorrectPosture(localDate.minusDays(days - 1), localDate.plusDays(1), days)));
    }

}