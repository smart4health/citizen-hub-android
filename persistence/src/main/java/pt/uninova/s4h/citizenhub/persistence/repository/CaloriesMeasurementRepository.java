package pt.uninova.s4h.citizenhub.persistence.repository;

import android.content.Context;

import java.time.LocalDate;
import java.util.List;

import androidx.lifecycle.LiveData;
import pt.uninova.s4h.citizenhub.persistence.CitizenHubDatabase;
import pt.uninova.s4h.citizenhub.persistence.dao.CaloriesMeasurementDao;
import pt.uninova.s4h.citizenhub.persistence.entity.CaloriesMeasurementRecord;
import pt.uninova.s4h.citizenhub.persistence.entity.util.DailyCaloriesPanel;
import pt.uninova.s4h.citizenhub.persistence.entity.util.HourlyCaloriesPanel;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

/** Repository used to call queries from the calories measurement dao. */
public class CaloriesMeasurementRepository {

    private final CaloriesMeasurementDao caloriesMeasurementDao;

    public CaloriesMeasurementRepository(Context context) {
        final CitizenHubDatabase citizenHubDatabase = CitizenHubDatabase.getInstance(context);

        caloriesMeasurementDao = citizenHubDatabase.caloriesMeasurementDao();
    }

    /** Inserts an entry into the database.
     * @param record Entry to insert.
     * */
    public void create(CaloriesMeasurementRecord record) {
        CitizenHubDatabase.executorService().execute(() -> caloriesMeasurementDao.insert(record));
    }

    /** Selects live data from the calories database. Normally used to constantly update the UI whenever new information is added.
     * @param localDate Date.
     * @return Live data list containing calories records.
     * */
    public LiveData<List<CaloriesMeasurementRecord>> read(LocalDate localDate) {
        return caloriesMeasurementDao.selectLiveData(localDate, localDate.plusDays(1));
    }

    /** Selects the total number of calories burned during a day.
     * @param localDate Date.
     * @return Live date with the total number of calories burned.
     * */
    public LiveData<Double> readMaximum(LocalDate localDate) {
        return caloriesMeasurementDao.selectMaximumLiveData(localDate, localDate.plusDays(1));
    }

    /** Selects live data from the calories database. Normally used to constantly update the UI whenever new information is added.
     * @param localDate Date.
     * @return Live data containing the total daily calories from both distance tables (distance snapshot and normal).
     * */
    public LiveData<Double> getCaloriesAllTypes (LocalDate localDate) {
        return caloriesMeasurementDao.getCaloriesAllTypes(localDate, localDate.plusDays(1));
    }

    /** Selects calories information from one specific day.
     * @param localDate Date.
     * @param observer
     * */
    public void readLastDay(LocalDate localDate, Observer<List<HourlyCaloriesPanel>> observer){
        CitizenHubDatabase.executorService().execute(() -> observer.observe(caloriesMeasurementDao.selectLastDay(localDate)));
    }

    /** Selects calories information from a range of days.
     * @param localDate Date.
     * @param days Days range.
     * @param observer
     * */
    public void readSeveralDays(LocalDate localDate, int days, Observer<List<DailyCaloriesPanel>> observer){
        CitizenHubDatabase.executorService().execute(() -> observer.observe(caloriesMeasurementDao.selectSeveralDays(localDate.minusDays(days - 1), localDate.plusDays(1), days)));
    }

}