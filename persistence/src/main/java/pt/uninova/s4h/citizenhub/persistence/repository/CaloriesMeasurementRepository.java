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
import pt.uninova.s4h.citizenhub.persistence.entity.util.SummaryDetailUtil;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

public class CaloriesMeasurementRepository {

    private final CaloriesMeasurementDao caloriesMeasurementDao;

    public CaloriesMeasurementRepository(Context context) {
        final CitizenHubDatabase citizenHubDatabase = CitizenHubDatabase.getInstance(context);

        caloriesMeasurementDao = citizenHubDatabase.caloriesMeasurementDao();
    }

    public void create(CaloriesMeasurementRecord record) {
        CitizenHubDatabase.executorService().execute(() -> caloriesMeasurementDao.insert(record));
    }

    public LiveData<List<CaloriesMeasurementRecord>> read(LocalDate localDate) {
        return caloriesMeasurementDao.selectLiveData(localDate, localDate.plusDays(1));
    }

    public LiveData<Double> readMaximum(LocalDate localDate) {
        return caloriesMeasurementDao.selectMaximumLiveData(localDate, localDate.plusDays(1));
    }

    public LiveData<Double> getCaloriesAllTypes (LocalDate localDate) {
        return caloriesMeasurementDao.getCaloriesAllTypes(localDate, localDate.plusDays(1));
    }

    public void readLastDay(LocalDate localDate, Observer<List<HourlyCaloriesPanel>> observer){
        CitizenHubDatabase.executorService().execute(() -> observer.observe(caloriesMeasurementDao.selectLastDay(localDate)));
    }

    public void readSeveralDays(LocalDate localDate, int days, Observer<List<DailyCaloriesPanel>> observer){
        CitizenHubDatabase.executorService().execute(() -> observer.observe(caloriesMeasurementDao.selectSeveralDays(localDate.minusDays(days - 1), localDate.plusDays(1), days)));
    }

}