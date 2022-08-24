package pt.uninova.s4h.citizenhub.persistence.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.TypeConverters;

import java.time.LocalDate;
import java.util.List;

import pt.uninova.s4h.citizenhub.persistence.conversion.EpochTypeConverter;
import pt.uninova.s4h.citizenhub.persistence.entity.CaloriesSnapshotMeasurementRecord;
import pt.uninova.s4h.citizenhub.persistence.entity.util.ActivityDetailUtil;

@Dao
public interface CaloriesSnapshotMeasurementDao {

    @Insert
    long insert(CaloriesSnapshotMeasurementRecord record);

    @Query("INSERT INTO calories_snapshot_measurement (sample_id, type, value) VALUES (:sampleId, :type, :value)")
    long insert(Long sampleId, Integer type, Double value);

    @Query(value = "SELECT calories_snapshot_measurement.* FROM calories_snapshot_measurement INNER JOIN sample ON calories_snapshot_measurement.sample_id = sample.id WHERE sample.timestamp >= :from AND sample.timestamp < :to ORDER BY timestamp")
    @TypeConverters(EpochTypeConverter.class)
    LiveData<List<CaloriesSnapshotMeasurementRecord>> selectLiveData(LocalDate from, LocalDate to);

    @Query(value = "SELECT MAX(value) FROM calories_snapshot_measurement INNER JOIN sample ON calories_snapshot_measurement.sample_id = sample.id WHERE sample.timestamp >= :from AND sample.timestamp < :to")
    @TypeConverters(EpochTypeConverter.class)
    LiveData<Double> selectMaximumLiveData(LocalDate from, LocalDate to);

    @Query(value = "WITH agg AS(SELECT ((sample.timestamp - :localDate - 3600000) / 3600000) % 24 AS hour, calories_snapshot_measurement.value AS value "
            + " FROM calories_snapshot_measurement INNER JOIN sample ON calories_snapshot_measurement.sample_id = sample.id "
            + " WHERE sample.timestamp >= :localDate AND sample.timestamp < :localDate + 86400000) "
            + " SELECT MAX(value) AS value, hour AS time FROM agg GROUP BY hour")
    @TypeConverters(EpochTypeConverter.class)
    List<ActivityDetailUtil> selectLastDay(LocalDate localDate);

    @Query(value = "WITH agg AS(SELECT ((sample.timestamp - :from - 86400000) / 86400000) % 7 AS day, calories_snapshot_measurement.value AS value "
            + " FROM calories_snapshot_measurement INNER JOIN sample ON calories_snapshot_measurement.sample_id = sample.id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to + 86400000) "
            + " SELECT MAX(value) AS value, day AS time FROM agg GROUP BY day")
    @TypeConverters(EpochTypeConverter.class)
    List<ActivityDetailUtil> selectLastSevenDays(LocalDate from, LocalDate to);


    @Query(value = "WITH agg AS(SELECT ((sample.timestamp - :from - 86400000) / 86400000) % 30 AS day, calories_snapshot_measurement.value AS value "
            + " FROM calories_snapshot_measurement INNER JOIN sample ON calories_snapshot_measurement.sample_id = sample.id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to + 86400000) "
            + " SELECT MAX(value) AS value, day AS time FROM agg GROUP BY day")
    @TypeConverters(EpochTypeConverter.class)
    List<ActivityDetailUtil> selectLastThirtyDays(LocalDate from, LocalDate to);
}