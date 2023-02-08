package pt.uninova.s4h.citizenhub.persistence.dao;

import java.time.LocalDate;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.TypeConverters;

import pt.uninova.s4h.citizenhub.persistence.conversion.EpochTypeConverter;
import pt.uninova.s4h.citizenhub.persistence.entity.StepsMeasurementRecord;
import pt.uninova.s4h.citizenhub.persistence.entity.util.DailyStepsPanel;
import pt.uninova.s4h.citizenhub.persistence.entity.util.HourlyStepsPanel;
import pt.uninova.s4h.citizenhub.persistence.entity.util.WalkingInformation;

@Dao
public interface StepsMeasurementDao {

    @Insert
    long insert(StepsMeasurementRecord record);

    @Query("INSERT INTO steps_measurement (sample_id, value) VALUES (:sampleId, :value)")
    long insert(Long sampleId, Double value);

    @Query("SELECT * FROM steps_measurement WHERE sample_id = :sampleId")
    StepsMeasurementRecord select(Long sampleId);

    @Query(value = "SELECT steps_measurement.* FROM steps_measurement INNER JOIN sample ON steps_measurement.sample_id = sample.id WHERE sample.timestamp >= :from AND sample.timestamp < :to ORDER BY timestamp")
    @TypeConverters(EpochTypeConverter.class)
    LiveData<List<StepsMeasurementRecord>> selectLiveData(LocalDate from, LocalDate to);

    @Query(value = "SELECT MAX(value) FROM steps_measurement INNER JOIN sample ON steps_measurement.sample_id = sample.id WHERE sample.timestamp >= :from AND sample.timestamp < :to")
    @TypeConverters(EpochTypeConverter.class)
    LiveData<Double> selectMaximumLiveData(LocalDate from, LocalDate to);

    @Query(value = "SELECT steps_measurement.value AS steps, distance_measurement.value AS distance, calories_measurement.value AS calories FROM steps_measurement INNER JOIN sample ON steps_measurement.sample_id = sample.id LEFT JOIN distance_measurement ON sample.id = distance_measurement.sample_id LEFT JOIN calories_measurement ON sample.id = calories_measurement.sample_id WHERE sample.timestamp >= :from AND sample.timestamp < :to ORDER BY timestamp DESC LIMIT 1")
    @TypeConverters(EpochTypeConverter.class)
    LiveData<WalkingInformation> selectLatestWalkingInformationLiveData(LocalDate from, LocalDate to);

    @Query(value = "WITH sample_window(id) AS (SELECT id FROM sample WHERE timestamp >= :from AND timestamp < :to), discreet (value) AS (SELECT SUM(steps_measurement.value) AS value FROM sample_window INNER JOIN steps_measurement ON sample_window.id = steps_measurement.sample_id), snapshot (value) AS (SELECT MAX(steps_snapshot_measurement.value) AS value FROM sample_window INNER JOIN steps_snapshot_measurement ON sample_window.id = steps_snapshot_measurement.sample_id) SELECT SUM(value) AS value FROM (SELECT value FROM discreet UNION ALL SELECT value FROM snapshot);")
    @TypeConverters(EpochTypeConverter.class)
    LiveData<Integer> getStepsAllTypes(LocalDate from, LocalDate to);

    @Query(value = " WITH agg AS (SELECT ((sample.timestamp - :localDate) / 3600000) % 24 AS hour, MAX(steps_snapshot_measurement.value) AS value "
            + " FROM sample INNER JOIN steps_snapshot_measurement ON steps_snapshot_measurement.sample_id = sample.id "
            + " WHERE sample.timestamp >= :localDate AND sample.timestamp < :localDate + 86400000 GROUP BY hour "
            + " UNION ALL SELECT ((sample.timestamp - :localDate) / 3600000) % 24 AS hour, SUM(steps_measurement.value) AS value "
            + " FROM sample INNER JOIN steps_measurement ON steps_measurement.sample_id = sample.id "
            + " WHERE sample.timestamp >= :localDate AND sample.timestamp < :localDate + 86400000 GROUP BY hour) "
            + " SELECT SUM(value) AS steps, hour AS hourOfDay FROM agg GROUP BY hour ")
    @TypeConverters(EpochTypeConverter.class)
    List<HourlyStepsPanel> selectLastDay(LocalDate localDate);

    @Query(value = "WITH agg AS (SELECT ((sample.timestamp - :from) / 86400000) % :days AS days, MAX(steps_snapshot_measurement.value) AS value "
            + " FROM steps_snapshot_measurement INNER JOIN sample ON steps_snapshot_measurement.sample_id = sample.id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to GROUP BY days "
            + " UNION ALL SELECT ((sample.timestamp - :from) / 86400000) % :days AS days, SUM(steps_measurement.value) AS value "
            + " FROM steps_measurement INNER JOIN sample ON steps_measurement.sample_id = sample.id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to GROUP BY days) "
            + " SELECT SUM(value) AS steps, days AS day FROM agg GROUP BY days")
    @TypeConverters(EpochTypeConverter.class)
    List<DailyStepsPanel> selectSeveralDays(LocalDate from, LocalDate to, int days);

}