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
import pt.uninova.s4h.citizenhub.persistence.entity.util.SummaryDetailUtil;

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

    @Query(value = "WITH sample_window(id) AS (SELECT id FROM sample WHERE timestamp >= :from AND timestamp < :to), discreet (value) AS (SELECT SUM(calories_measurement.value) AS value FROM sample_window INNER JOIN calories_measurement ON sample_window.id = calories_measurement.sample_id), snapshot (value) AS (SELECT MAX(calories_snapshot_measurement.value) AS value FROM sample_window INNER JOIN calories_snapshot_measurement ON sample_window.id = calories_snapshot_measurement.sample_id) SELECT SUM(value) AS value FROM (SELECT value FROM discreet UNION ALL SELECT value FROM snapshot);")
    @TypeConverters(EpochTypeConverter.class)
    LiveData<Double> getCaloriesAllTypes(LocalDate from, LocalDate to);

    @Query(value = "WITH agg AS (SELECT ((sample.timestamp - :localDate) / 3600000) % 24 AS hour, MAX(calories_snapshot_measurement.value) AS value "
            + " FROM calories_snapshot_measurement INNER JOIN sample ON calories_snapshot_measurement.sample_id = sample.id "
            + " UNION ALL SELECT ((sample.timestamp - :localDate) / 3600000) % 24 AS hour, SUM(calories_measurement.value) AS value "
            + " FROM calories_measurement INNER JOIN sample ON calories_measurement.sample_id = sample.id "
            + " WHERE sample.timestamp >= :localDate AND sample.timestamp < :localDate + 86400000) "
            + " SELECT value AS value1, hour AS time FROM agg GROUP BY hour")
    @TypeConverters(EpochTypeConverter.class)
    List<SummaryDetailUtil> selectLastDay(LocalDate localDate);

    @Query(value = "WITH agg AS (SELECT ((sample.timestamp - :from) / 86400000) % :days AS days, MAX(calories_snapshot_measurement.value) AS value "
            + " FROM calories_snapshot_measurement INNER JOIN sample ON calories_snapshot_measurement.sample_id = sample.id "
            + " UNION ALL SELECT ((sample.timestamp - :from) / 86400000) % :days AS days, SUM(calories_measurement.value) AS value "
            + " FROM calories_measurement INNER JOIN sample ON calories_measurement.sample_id = sample.id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to) "
            + " SELECT value AS value1 FROM agg GROUP BY days")
    @TypeConverters(EpochTypeConverter.class)
    List<SummaryDetailUtil> selectSeveralDays(LocalDate from, LocalDate to, int days);
}