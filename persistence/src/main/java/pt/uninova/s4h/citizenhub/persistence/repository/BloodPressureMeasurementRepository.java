package pt.uninova.s4h.citizenhub.persistence.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.time.LocalDate;
import java.util.List;

import pt.uninova.s4h.citizenhub.persistence.CitizenHubDatabase;
import pt.uninova.s4h.citizenhub.persistence.dao.BloodPressureMeasurementDao;
import pt.uninova.s4h.citizenhub.persistence.entity.BloodPressureMeasurementRecord;
import pt.uninova.s4h.citizenhub.persistence.entity.util.DailyBloodPressurePanel;
import pt.uninova.s4h.citizenhub.persistence.entity.util.HourlyBloodPressurePanel;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

/** Repository used to call queries from the blood pressure measurement dao. */
public class BloodPressureMeasurementRepository {

    private final BloodPressureMeasurementDao bloodPressureMeasurementDao;

    public BloodPressureMeasurementRepository(Context context) {
        final CitizenHubDatabase citizenHubDatabase = CitizenHubDatabase.getInstance(context);

        bloodPressureMeasurementDao = citizenHubDatabase.bloodPressureMeasurementDao();
    }

    /** Inserts an entry into the database.
     * @param record Entry to insert.
     * */
    public void create(BloodPressureMeasurementRecord record) {
        CitizenHubDatabase.executorService().execute(() -> bloodPressureMeasurementDao.insert(record));
    }

    /** Selects blood pressure records from the blood pressure database. Normally used to constantly update the UI whenever new information is added.
     * @param localDate Date.
     * @return Live data list with all heart rate records.
     * */
    public LiveData<List<BloodPressureMeasurementRecord>> read(LocalDate localDate) {
        return bloodPressureMeasurementDao.selectLiveData(localDate, localDate.plusDays(1));
    }

    /** Selects blood pressure information from one specific day, grouped by hour.
     * @param localDate Date.
     * @param observer
     * */
    public void readLastDay(LocalDate localDate, Observer<List<HourlyBloodPressurePanel>> observer) {
        CitizenHubDatabase.executorService().execute(() -> observer.observe(bloodPressureMeasurementDao.selectLastDay(localDate)));
    }

    /** Selects blood pressure information from a range of days, grouped by days.
     * @param localDate Date.
     * @param days Days range.
     * @param observer
     * */
    public void selectSeveralDays(LocalDate localDate, int days, Observer<List<DailyBloodPressurePanel>> observer){
        CitizenHubDatabase.executorService().execute(() -> observer.observe(bloodPressureMeasurementDao.selectSeveralDays(localDate.minusDays(days - 1), localDate.plusDays(1), days)));
    }

    /** Selects last blood pressure reading from a day.
     * @param localDate Date.
     * @return Live data list with the latest blood pressure measurement.
     * */
    public LiveData<BloodPressureMeasurementRecord> readLatest(LocalDate localDate) {
        return bloodPressureMeasurementDao.selectLatestLiveData(localDate, localDate.plusDays(1));
    }
}