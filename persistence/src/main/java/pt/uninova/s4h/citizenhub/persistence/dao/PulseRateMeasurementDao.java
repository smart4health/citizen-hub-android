package pt.uninova.s4h.citizenhub.persistence.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.TypeConverters;

import java.time.LocalDate;
import java.util.List;

import pt.uninova.s4h.citizenhub.persistence.conversion.EpochTypeConverter;
import pt.uninova.s4h.citizenhub.persistence.entity.PulseRateMeasurementRecord;

@Dao
public interface PulseRateMeasurementDao {

    @Insert
    long insert(PulseRateMeasurementRecord record);

    @Query("INSERT INTO pulse_rate_measurement (sample_id, value) VALUES (:sampleId, :value)")
    long insert(Long sampleId, Double value);

    @Query(value = "SELECT pulse_rate_measurement.* FROM pulse_rate_measurement INNER JOIN sample ON pulse_rate_measurement.sample_id = sample.id WHERE sample.timestamp >= :from AND sample.timestamp < :to ORDER BY timestamp")
    @TypeConverters(EpochTypeConverter.class)
    LiveData<List<PulseRateMeasurementRecord>> selectLiveData(LocalDate from, LocalDate to);

    @Query("SELECT AVG(value) FROM pulse_rate_measurement INNER JOIN sample ON pulse_rate_measurement.sample_id = sample.id WHERE sample.timestamp >= :from AND sample.timestamp < :to")
    @TypeConverters(EpochTypeConverter.class)
    LiveData<Double> selectAverageLiveData(LocalDate from, LocalDate to);

    /*@Query(value = "WITH agg AS(SELECT ((sample.timestamp - :localDate - 3600000) / 3600000) % 24 AS hour, pulse_rate_measurement.value AS value " +
            " FROM pulse_rate_measurement INNER JOIN sample ON pulse_rate_measurement.sample_id = sample.id " +
            " WHERE sample.timestamp >= :localDate AND sample.timestamp < :localDate + 86400000) " +
            " SELECT value AS value, hour AS time FROM agg GROUP BY hour ")
    @TypeConverters(EpochTypeConverter.class)
    List<SummaryDetailUtil> selectLastDay(LocalDate localDate);

    @Query(value = "WITH agg AS(SELECT ((sample.timestamp - :from - 86400000) / 86400000) % 7 AS day, pulse_rate_measurement.value AS value "
            + " FROM pulse_rate_measurement INNER JOIN sample ON pulse_rate_measurement.sample_id = sample.id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to + 86400000) "
            + " SELECT value AS value, day AS time FROM agg GROUP BY day")
    @TypeConverters(EpochTypeConverter.class)
    List<SummaryDetailUtil> selectLastSevenDays(LocalDate from, LocalDate to);

    @Query(value = "WITH agg AS(SELECT ((sample.timestamp - :from - 86400000) / 86400000) % 30 AS day, pulse_rate_measurement.value AS value "
            + " FROM pulse_rate_measurement INNER JOIN sample ON pulse_rate_measurement.sample_id = sample.id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to + 86400000) "
            + " SELECT value AS value, day AS time FROM agg GROUP BY day")
    @TypeConverters(EpochTypeConverter.class)
    List<SummaryDetailUtil> selectLastThirtyDays(LocalDate from, LocalDate to);*/

}