package pt.uninova.s4h.citizenhub.persistence.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.TypeConverters;

import java.time.LocalDate;
import java.util.List;

import pt.uninova.s4h.citizenhub.persistence.conversion.EpochTypeConverter;
import pt.uninova.s4h.citizenhub.persistence.entity.CaloriesMeasurementRecord;

@Dao
public interface CaloriesMeasurementDao {

    @Query(value = "SELECT calories_measurement.* FROM calories_measurement INNER JOIN sample ON calories_measurement.sample_id = sample.id WHERE sample.timestamp >= :from AND sample.timestamp < :to ORDER BY timestamp")
    @TypeConverters(EpochTypeConverter.class)
    LiveData<List<CaloriesMeasurementRecord>> get(LocalDate from, LocalDate to);

    @Query(value = "SELECT MAX(value) FROM calories_measurement INNER JOIN sample ON calories_measurement.sample_id = sample.id WHERE sample.timestamp >= :from AND sample.timestamp < :to")
    @TypeConverters(EpochTypeConverter.class)
    LiveData<Double> getMaximum(LocalDate from, LocalDate to);

    @Insert
    void insert(CaloriesMeasurementRecord record);

    @Query("INSERT INTO calories_measurement (sample_id, value) VALUES (:sampleId, :value)")
    void insert(Long sampleId, Double value);
}
