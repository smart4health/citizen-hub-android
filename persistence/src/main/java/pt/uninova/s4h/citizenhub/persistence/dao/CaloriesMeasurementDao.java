package pt.uninova.s4h.citizenhub.persistence.dao;

import java.time.LocalDate;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.TypeConverters;
import pt.uninova.s4h.citizenhub.persistence.conversion.EpochTypeConverter;
import pt.uninova.s4h.citizenhub.persistence.entity.CaloriesMeasurementRecord;

@Dao
public interface CaloriesMeasurementDao {

    @Insert
    long insert(CaloriesMeasurementRecord record);

    @Query("INSERT INTO calories_measurement (sample_id, value) VALUES (:sampleId, :value)")
    long insert(Long sampleId, Double value);

    @Query("SELECT * FROM calories_measurement WHERE sample_id = :sampleId")
    CaloriesMeasurementRecord select(Long sampleId);

    @Query(value = "SELECT calories_measurement.* FROM calories_measurement INNER JOIN sample ON calories_measurement.sample_id = sample.id WHERE sample.timestamp >= :from AND sample.timestamp < :to ORDER BY timestamp")
    @TypeConverters(EpochTypeConverter.class)
    LiveData<List<CaloriesMeasurementRecord>> selectLiveData(LocalDate from, LocalDate to);

    @Query(value = "SELECT MAX(value) FROM calories_measurement INNER JOIN sample ON calories_measurement.sample_id = sample.id WHERE sample.timestamp >= :from AND sample.timestamp < :to")
    @TypeConverters(EpochTypeConverter.class)
    LiveData<Double> selectMaximumLiveData(LocalDate from, LocalDate to);

    @Query(value = "SELECT IFNULL(calories,0) + IFNULL(snapshotCalories, 0) FROM (SELECT SUM(calories_measurement.value) AS calories FROM calories_measurement INNER JOIN sample ON calories_measurement.sample_id = sample.id WHERE sample.timestamp >= :from AND sample.timestamp < :to ORDER BY timestamp DESC LIMIT 1) LEFT JOIN (SELECT calories_snapshot_measurement.value AS snapshotCalories FROM calories_snapshot_measurement INNER JOIN sample ON calories_snapshot_measurement.sample_id = sample_id WHERE sample.timestamp >= :from AND sample.timestamp < :to ORDER BY timestamp DESC LIMIT 1)")
    @TypeConverters(EpochTypeConverter.class)
    LiveData<Double> getCaloriesAllTypes(LocalDate from, LocalDate to);

}