package pt.uninova.s4h.citizenhub.persistence.repository;

import android.content.Context;

import java.time.LocalDate;
import java.util.List;

import androidx.lifecycle.LiveData;
import pt.uninova.s4h.citizenhub.persistence.CitizenHubDatabase;
import pt.uninova.s4h.citizenhub.persistence.dao.StepsMeasurementDao;
import pt.uninova.s4h.citizenhub.persistence.entity.StepsMeasurementRecord;
import pt.uninova.s4h.citizenhub.persistence.entity.util.SummaryDetailUtil;
import pt.uninova.s4h.citizenhub.persistence.entity.util.WalkingInformation;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

public class StepsMeasurementRepository {

    private final StepsMeasurementDao stepsMeasurementDao;

    public StepsMeasurementRepository(Context context) {
        final CitizenHubDatabase citizenHubDatabase = CitizenHubDatabase.getInstance(context);

        stepsMeasurementDao = citizenHubDatabase.stepsMeasurementDao();
    }

    public void create(StepsMeasurementRecord record) {
        CitizenHubDatabase.executorService().execute(() -> stepsMeasurementDao.insert(record));
    }

    public LiveData<List<StepsMeasurementRecord>> read(LocalDate localDate) {
        return stepsMeasurementDao.selectLiveData(localDate, localDate.plusDays(1));
    }

    public LiveData<WalkingInformation> readLatestWalkingInformation(LocalDate localDate) {
        return stepsMeasurementDao.selectLatestWalkingInformationLiveData(localDate, localDate.plusDays(1));
    }

    public LiveData<Integer> getStepsAllTypes (LocalDate localDate) {
        return stepsMeasurementDao.getStepsAllTypes(localDate, localDate.plusDays(1));
    }

    public void readLastDay(LocalDate localDate, Observer<List<SummaryDetailUtil>> observer){
        CitizenHubDatabase.executorService().execute(() -> observer.observe(stepsMeasurementDao.selectLastDay(localDate)));
    }

    public void readLastSevenDays(LocalDate localDate, int days, Observer<List<SummaryDetailUtil>> observer){
        CitizenHubDatabase.executorService().execute(() -> observer.observe(stepsMeasurementDao.selectSeveralDays(localDate.minusDays(days - 1), localDate, days)));
    }
}