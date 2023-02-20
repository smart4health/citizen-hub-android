package pt.uninova.s4h.citizenhub.persistence.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.time.LocalDate;
import java.util.List;

import pt.uninova.s4h.citizenhub.persistence.CitizenHubDatabase;
import pt.uninova.s4h.citizenhub.persistence.dao.PostureMeasurementDao;
import pt.uninova.s4h.citizenhub.persistence.entity.PostureMeasurementRecord;
import pt.uninova.s4h.citizenhub.persistence.entity.util.DailyPosturePanel;
import pt.uninova.s4h.citizenhub.persistence.entity.util.HourlyPosturePanel;
import pt.uninova.s4h.citizenhub.persistence.entity.util.HourlyPosture;
import pt.uninova.s4h.citizenhub.persistence.entity.util.PostureClassificationSum;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

/** Repository used to call queries from the posture measurement dao. */
public class PostureMeasurementRepository {

    private final PostureMeasurementDao postureMeasurementDao;

    public PostureMeasurementRepository(Context context) {
        final CitizenHubDatabase citizenHubDatabase = CitizenHubDatabase.getInstance(context);

        postureMeasurementDao = citizenHubDatabase.postureMeasurementDao();
    }

    /** Inserts a posture sample into the database.
     * @param record Record.
     * */
    public void create(PostureMeasurementRecord record) {
        CitizenHubDatabase.executorService().execute(() -> postureMeasurementDao.insert(record));
    }

    /** Selects live data from the heart rate database. Normally used to constantly update the UI whenever new information is added.
     * @param localDate Date.
     * @return Live data list with full posture records.
     * */
    public LiveData<List<PostureMeasurementRecord>> read(LocalDate localDate) {
        return postureMeasurementDao.selectLiveData(localDate, localDate.plusDays(1));
    }

    /**
     * */
    public void read(LocalDate localDate, Observer<List<HourlyPosture>> observer) {
        CitizenHubDatabase.executorService().execute(() -> observer.observe(postureMeasurementDao.selectHourlyPosture(localDate)));
    }

    /** Selects live data from the posture database and groups the posture by classification. Normally used to constantly update the UI whenever new information is added.
     * @param localDate Date.
     * @return Live data list containing correct posture time and incorrect posture time.
     * */
    public LiveData<List<PostureClassificationSum>> readClassificationSum(LocalDate localDate) {
        return postureMeasurementDao.selectClassificationSumLiveData(localDate, localDate.plusDays(1));
    }

    /** Selects posture information from one specific day.
     * @param localDate Date.
     * @param observer
     * */
    public void readLastDayPosture(LocalDate localDate, Observer<List<HourlyPosturePanel>> observer){
        CitizenHubDatabase.executorService().execute(() -> observer.observe(postureMeasurementDao.selectLastDayPosture(localDate)));
    }

    /** Selects posture information from a range of days.
     * @param localDate Date.
     * @param days Days range.
     * @param observer
     * */
    public void readSeveralDaysPosture(LocalDate localDate, int days, Observer<List<DailyPosturePanel>> observer){
        CitizenHubDatabase.executorService().execute(() -> observer.observe(postureMeasurementDao.selectSeveralDaysPosture(localDate.minusDays(days - 1), localDate.plusDays(1), days)));
    }

}