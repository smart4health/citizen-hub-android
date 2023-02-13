package pt.uninova.s4h.citizenhub.persistence.dao;

import java.time.LocalDate;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.TypeConverters;
import pt.uninova.s4h.citizenhub.persistence.conversion.EpochTypeConverter;
import pt.uninova.s4h.citizenhub.persistence.entity.CaloriesSnapshotMeasurementRecord;

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

}