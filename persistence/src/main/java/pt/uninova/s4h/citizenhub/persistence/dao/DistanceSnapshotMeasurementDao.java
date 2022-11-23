package pt.uninova.s4h.citizenhub.persistence.dao;

import java.time.LocalDate;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.TypeConverters;
import pt.uninova.s4h.citizenhub.persistence.conversion.EpochTypeConverter;
import pt.uninova.s4h.citizenhub.persistence.entity.DistanceSnapshotMeasurementRecord;
import pt.uninova.s4h.citizenhub.persistence.entity.util.SummaryDetailUtil;

@Dao
public interface DistanceSnapshotMeasurementDao {

    @Insert
    long insert(DistanceSnapshotMeasurementRecord record);

    @Query("INSERT INTO distance_snapshot_measurement (sample_id, type, value) VALUES (:sampleId, :type, :value)")
    long insert(Long sampleId, Integer type, Double value);

    @Query(value = "SELECT distance_snapshot_measurement.* FROM distance_snapshot_measurement INNER JOIN sample ON distance_snapshot_measurement.sample_id = sample.id WHERE sample.timestamp >= :from AND sample.timestamp < :to ORDER BY timestamp")
    @TypeConverters(EpochTypeConverter.class)
    LiveData<List<DistanceSnapshotMeasurementRecord>> selectLiveData(LocalDate from, LocalDate to);

    @Query(value = "SELECT MAX(value) FROM distance_snapshot_measurement INNER JOIN sample ON distance_snapshot_measurement.sample_id = sample.id WHERE sample.timestamp >= :from AND sample.timestamp < :to")
    @TypeConverters(EpochTypeConverter.class)
    LiveData<Double> selectMaximumLiveData(LocalDate from, LocalDate to);

    @Query(value = "WITH agg AS(SELECT ((sample.timestamp - :localDate) / 3600000) % 24 AS hour, distance_snapshot_measurement.value AS value "
            + " FROM distance_snapshot_measurement INNER JOIN sample ON distance_snapshot_measurement.sample_id = sample.id "
            + " WHERE sample.timestamp >= :localDate AND sample.timestamp < :localDate + 86400000) "
            + " SELECT MAX(value) AS value1, hour AS time FROM agg GROUP BY hour")
    @TypeConverters(EpochTypeConverter.class)
    List<SummaryDetailUtil> selectLastDay(LocalDate localDate);

    @Query(value = "WITH agg AS(SELECT ((sample.timestamp - :from) / 86400000) % 7 AS day, distance_snapshot_measurement.value AS value "
            + " FROM distance_snapshot_measurement INNER JOIN sample ON distance_snapshot_measurement.sample_id = sample.id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to + 86400000) "
            + " SELECT MAX(value) AS value1, day AS time FROM agg GROUP BY day")
    @TypeConverters(EpochTypeConverter.class)
    List<SummaryDetailUtil> selectLastSevenDays(LocalDate from, LocalDate to);

    @Query(value = "WITH agg AS(SELECT ((sample.timestamp - :from) / 86400000) % 30 AS day, distance_snapshot_measurement.value AS value "
            + " FROM distance_snapshot_measurement INNER JOIN sample ON distance_snapshot_measurement.sample_id = sample.id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to + 86400000) "
            + " SELECT MAX(value) AS value1, day AS time FROM agg GROUP BY day")
    @TypeConverters(EpochTypeConverter.class)
    List<SummaryDetailUtil> selectLastThirtyDays(LocalDate from, LocalDate to);

    @Query(value = "WITH agg AS(SELECT ((sample.timestamp - :from) / 86400000) % :days AS day, distance_snapshot_measurement.value AS value "
            + " FROM distance_snapshot_measurement INNER JOIN sample ON distance_snapshot_measurement.sample_id = sample.id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to) "
            + " SELECT MAX(value) AS value1, day AS time FROM agg GROUP BY day")
    @TypeConverters(EpochTypeConverter.class)
    List<SummaryDetailUtil> selectSeveralDays(LocalDate from, LocalDate to, int days);

}