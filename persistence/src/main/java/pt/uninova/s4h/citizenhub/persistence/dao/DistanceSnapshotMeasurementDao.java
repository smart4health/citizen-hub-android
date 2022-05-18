package pt.uninova.s4h.citizenhub.persistence.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.TypeConverters;

import java.time.LocalDate;
import java.util.List;

import pt.uninova.s4h.citizenhub.persistence.conversion.EpochTypeConverter;
import pt.uninova.s4h.citizenhub.persistence.entity.DistanceSnapshotMeasurementRecord;

@Dao
public interface DistanceSnapshotMeasurementDao {

    @Query(value = "SELECT distance_snapshot_measurement.* FROM distance_snapshot_measurement INNER JOIN sample ON distance_snapshot_measurement.sample_id = sample.id WHERE sample.timestamp >= :from AND sample.timestamp < :to ORDER BY timestamp")
    @TypeConverters(EpochTypeConverter.class)
    LiveData<List<DistanceSnapshotMeasurementRecord>> get(LocalDate from, LocalDate to);

    @Query(value = "SELECT MAX(value) FROM distance_snapshot_measurement INNER JOIN sample ON distance_snapshot_measurement.sample_id = sample.id WHERE sample.timestamp >= :from AND sample.timestamp < :to")
    @TypeConverters(EpochTypeConverter.class)
    LiveData<Double> getMaximum(LocalDate from, LocalDate to);

    @Insert
    void insert(DistanceSnapshotMeasurementRecord record);

    @Query("INSERT INTO distance_snapshot_measurement (sample_id, type, value) VALUES (:sampleId, :type, :value)")
    void insert(Long sampleId, Integer type, Double value);
}
