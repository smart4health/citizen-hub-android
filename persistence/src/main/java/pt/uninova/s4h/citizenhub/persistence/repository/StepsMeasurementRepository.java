package pt.uninova.s4h.citizenhub.persistence.repository;

import android.content.Context;

import java.time.LocalDate;
import java.util.List;

import androidx.lifecycle.LiveData;
import pt.uninova.s4h.citizenhub.persistence.CitizenHubDatabase;
import pt.uninova.s4h.citizenhub.persistence.dao.StepsMeasurementDao;
import pt.uninova.s4h.citizenhub.persistence.entity.StepsMeasurementRecord;
import pt.uninova.s4h.citizenhub.persistence.entity.util.DailyStepsPanel;
import pt.uninova.s4h.citizenhub.persistence.entity.util.HourlyStepsPanel;
import pt.uninova.s4h.citizenhub.persistence.entity.util.WalkingInformation;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

/** Repository used to call queries from the steps measurement dao. */
public class StepsMeasurementRepository {

    private final StepsMeasurementDao stepsMeasurementDao;

    public StepsMeasurementRepository(Context context) {
        final CitizenHubDatabase citizenHubDatabase = CitizenHubDatabase.getInstance(context);

        stepsMeasurementDao = citizenHubDatabase.stepsMeasurementDao();
    }

    /** Inserts a steps sample in the database.
     * @param record Record.
     * */
    public void create(StepsMeasurementRecord record) {
        CitizenHubDatabase.executorService().execute(() -> stepsMeasurementDao.insert(record));
    }

    /** Selects steps live data for continuous UI update.
     * @param localDate Date.
     * @return Live data list with full steps record.
     * */
    public LiveData<List<StepsMeasurementRecord>> read(LocalDate localDate) {
        return stepsMeasurementDao.selectLiveData(localDate, localDate.plusDays(1));
    }

    public LiveData<WalkingInformation> readLatestWalkingInformation(LocalDate localDate) {
        return stepsMeasurementDao.selectLatestWalkingInformationLiveData(localDate, localDate.plusDays(1));
    }

    /**
     * @param localDate Date.
     * */
    public LiveData<Integer> getStepsAllTypes (LocalDate localDate) {
        return stepsMeasurementDao.getStepsAllTypes(localDate, localDate.plusDays(1));
    }

    /** Selects steps information from one specific day.
     * @param localDate Date.
     * @param observer
     * */
    public void readLastDay(LocalDate localDate, Observer<List<HourlyStepsPanel>> observer){
        CitizenHubDatabase.executorService().execute(() -> observer.observe(stepsMeasurementDao.selectLastDay(localDate)));
    }

    /** Selects steps information from a range of days.
     * @param localDate Date.
     * @param days Days range.
     * @param observer
     * */
    public void readSeveralDays(LocalDate localDate, int days, Observer<List<DailyStepsPanel>> observer){
        CitizenHubDatabase.executorService().execute(() -> observer.observe(stepsMeasurementDao.selectSeveralDays(localDate.minusDays(days - 1), localDate.plusDays(1), days)));
    }
}