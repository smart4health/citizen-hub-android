package pt.uninova.s4h.citizenhub.persistence.dao;

import java.time.LocalDate;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.TypeConverters;

import java.time.LocalDate;
import java.util.List;

import pt.uninova.s4h.citizenhub.persistence.conversion.EpochTypeConverter;
import pt.uninova.s4h.citizenhub.persistence.entity.StepsSnapshotMeasurementRecord;
import pt.uninova.s4h.citizenhub.persistence.entity.util.SummaryDetailUtil;
import pt.uninova.s4h.citizenhub.persistence.entity.util.WalkingInformation;

@Dao
public interface StepsSnapshotMeasurementDao {

    @Insert
    long insert(StepsSnapshotMeasurementRecord record);

    @Query("INSERT INTO steps_snapshot_measurement (sample_id, type, value) VALUES (:sampleId, :type, :value)")
    long insert(Long sampleId, Integer type, Integer value);

    @Query(value = "SELECT steps_snapshot_measurement.* FROM steps_snapshot_measurement INNER JOIN sample ON steps_snapshot_measurement.sample_id = sample.id WHERE sample.timestamp >= :from AND sample.timestamp < :to ORDER BY timestamp")
    @TypeConverters(EpochTypeConverter.class)
    LiveData<List<StepsSnapshotMeasurementRecord>> selectLiveData(LocalDate from, LocalDate to);

    @Query(value = "SELECT steps_snapshot_measurement.value AS steps, distance_snapshot_measurement.value AS distance, calories_snapshot_measurement.value AS calories FROM steps_snapshot_measurement INNER JOIN sample ON steps_snapshot_measurement.sample_id = sample.id LEFT JOIN distance_snapshot_measurement ON sample.id = distance_snapshot_measurement.sample_id LEFT JOIN calories_snapshot_measurement ON sample.id = calories_snapshot_measurement.sample_id WHERE sample.timestamp >= :from AND sample.timestamp < :to ORDER BY timestamp DESC LIMIT 1")
    @TypeConverters(EpochTypeConverter.class)
    LiveData<WalkingInformation> selectLatestWalkingInformationLiveData(LocalDate from, LocalDate to);

    @Query(value = "SELECT MAX(value) FROM steps_snapshot_measurement INNER JOIN sample ON steps_snapshot_measurement.sample_id = sample.id WHERE sample.timestamp >= :from AND sample.timestamp < :to")
    @TypeConverters(EpochTypeConverter.class)
    LiveData<Integer> selectMaximumLiveData(LocalDate from, LocalDate to);

    @Query(value = "SELECT MAX(value) FROM steps_snapshot_measurement INNER JOIN sample ON steps_snapshot_measurement.sample_id = sample.id WHERE sample.timestamp >= :from AND sample.timestamp < :to")
    @TypeConverters(EpochTypeConverter.class)
    Double selectMaximum(LocalDate from, LocalDate to);
    
    @Query(value = "WITH agg AS(SELECT ((sample.timestamp - :localDate) / 3600000) % 24 AS hour, steps_snapshot_measurement.value AS value "
            + " FROM steps_snapshot_measurement INNER JOIN sample ON steps_snapshot_measurement.sample_id = sample.id "
            + " WHERE sample.timestamp >= :localDate AND sample.timestamp < :localDate + 86400000) "
            + " SELECT MAX(value) AS value1, hour AS time FROM agg GROUP BY hour")
    @TypeConverters(EpochTypeConverter.class)
    List<SummaryDetailUtil> selectLastDay(LocalDate localDate);

    @Query(value = "WITH agg AS(SELECT ((sample.timestamp - :from) / 86400000) % 7 AS day, steps_snapshot_measurement.value AS value "
            + " FROM steps_snapshot_measurement INNER JOIN sample ON steps_snapshot_measurement.sample_id = sample.id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to + 86400000) "
            + " SELECT MAX(value) AS value1, day AS time FROM agg GROUP BY day")
    @TypeConverters(EpochTypeConverter.class)
    List<SummaryDetailUtil> selectLastSevenDays(LocalDate from, LocalDate to);

    @Query(value = "WITH agg AS(SELECT ((sample.timestamp - :from) / 86400000) % 30 AS day, steps_snapshot_measurement.value AS value "
            + " FROM steps_snapshot_measurement INNER JOIN sample ON steps_snapshot_measurement.sample_id = sample.id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to + 86400000) "
            + " SELECT MAX(value) AS value1, day AS time FROM agg GROUP BY day")
    @TypeConverters(EpochTypeConverter.class)
    List<SummaryDetailUtil> selectLastThirtyDays(LocalDate from, LocalDate to);

    @Query(value = "WITH agg AS(SELECT ((sample.timestamp - :from) / 86400000) % :days AS day, steps_snapshot_measurement.value AS value "
            + " FROM steps_snapshot_measurement INNER JOIN sample ON steps_snapshot_measurement.sample_id = sample.id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to) "
            + " SELECT MAX(value) AS value1, day AS time FROM agg GROUP BY day ")
    @TypeConverters(EpochTypeConverter.class)
    List<SummaryDetailUtil> selectSeveralDays(LocalDate from, LocalDate to, int days);

}