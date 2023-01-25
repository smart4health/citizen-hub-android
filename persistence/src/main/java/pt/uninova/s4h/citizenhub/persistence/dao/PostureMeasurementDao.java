package pt.uninova.s4h.citizenhub.persistence.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.TypeConverters;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import pt.uninova.s4h.citizenhub.persistence.conversion.DurationTypeConverter;
import pt.uninova.s4h.citizenhub.persistence.conversion.EpochTypeConverter;
import pt.uninova.s4h.citizenhub.persistence.entity.PostureMeasurementRecord;
import pt.uninova.s4h.citizenhub.persistence.entity.util.DailyPosturePanel;
import pt.uninova.s4h.citizenhub.persistence.entity.util.HourlyPosturePanel;
import pt.uninova.s4h.citizenhub.persistence.entity.util.HourlyPosture;
import pt.uninova.s4h.citizenhub.persistence.entity.util.PostureClassificationSum;

@Dao
public interface PostureMeasurementDao {

    @Insert
    long insert(PostureMeasurementRecord record);

    @TypeConverters(DurationTypeConverter.class)
    @Query("INSERT INTO posture_measurement (sample_id, classification, duration) VALUES (:sampleId, :classification, :duration)")
    long insert(Long sampleId, Integer classification, Duration duration);

    @TypeConverters(EpochTypeConverter.class)
    @Query(value = "SELECT posture_measurement.* FROM posture_measurement INNER JOIN sample ON posture_measurement.sample_id = sample.id WHERE sample.timestamp >= :from AND sample.timestamp < :to ORDER BY timestamp")
    LiveData<List<PostureMeasurementRecord>> selectLiveData(LocalDate from, LocalDate to);

    @TypeConverters(EpochTypeConverter.class)
    @Query("SELECT posture_measurement.classification AS classification, SUM(posture_measurement.duration) AS duration FROM posture_measurement INNER JOIN sample ON posture_measurement.sample_id = sample.id WHERE sample.timestamp >= :from AND sample.timestamp < :to GROUP BY classification")
    LiveData<List<PostureClassificationSum>> selectClassificationSumLiveData(LocalDate from, LocalDate to);

    @TypeConverters({EpochTypeConverter.class, DurationTypeConverter.class})
    @Query("WITH agg AS (SELECT ((sample.timestamp - :localDate) / 3600000) % 24 AS hour, posture_measurement.classification AS classification, SUM(posture_measurement.duration) AS duration FROM posture_measurement INNER JOIN sample ON posture_measurement.sample_id = sample.id WHERE sample.timestamp >= :localDate AND sample.timestamp < :localDate + 86400000 GROUP BY classification, hour) SELECT agg.hour AS hour, COALESCE((SELECT IFNULL(duration, 0) FROM agg AS aggi WHERE classification = 1 AND aggi.hour = agg.hour), 0) AS correctPostureDuration, COALESCE((SELECT IFNULL(duration, 0) FROM agg AS aggi WHERE classification = 2 AND aggi.hour = agg.hour), 0) AS incorrectPostureDuration FROM agg GROUP BY agg.hour;")
    List<HourlyPosture> selectHourlyPosture(LocalDate localDate);

    @Query("WITH agg AS (SELECT ((sample.timestamp - :localDate) / 3600000) % 24 AS hour, posture_measurement.classification AS classification, "
            + " SUM(posture_measurement.duration) AS duration FROM posture_measurement INNER JOIN sample ON posture_measurement.sample_id = sample.id "
            + " WHERE sample.timestamp >= :localDate AND sample.timestamp < :localDate + 86400000 GROUP BY classification, hour) "
            + " SELECT agg.hour AS hour, "
            + " COALESCE((SELECT IFNULL(duration, 0) FROM agg AS aggi WHERE classification = 1 AND aggi.hour = agg.hour), 0) AS correctPosture, "
            + " COALESCE((SELECT IFNULL(duration, 0) FROM agg AS aggi WHERE classification = 2 AND aggi.hour = agg.hour), 0) AS incorrectPosture "
            + " FROM agg GROUP BY agg.hour;")
    @TypeConverters({EpochTypeConverter.class, DurationTypeConverter.class})
    List<HourlyPosturePanel> selectLastDayPosture(LocalDate localDate);

    @Query("WITH agg AS (SELECT ((sample.timestamp - :from) / 86400000) % :days AS hour, posture_measurement.classification AS classification, "
            + " SUM(posture_measurement.duration) AS duration FROM posture_measurement INNER JOIN sample ON posture_measurement.sample_id = sample.id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to GROUP BY classification, hour) "
            + " SELECT agg.hour AS hour, "
            + " COALESCE((SELECT IFNULL(duration, 0) FROM agg AS aggi WHERE classification = 1 AND aggi.hour = agg.hour), 0) AS correctPosture, "
            + " COALESCE((SELECT IFNULL(duration, 0) FROM agg AS aggi WHERE classification = 2 AND aggi.hour = agg.hour), 0) AS incorrectPosture "
            + " FROM agg GROUP BY agg.hour;")
    @TypeConverters({EpochTypeConverter.class, DurationTypeConverter.class})
    List<DailyPosturePanel> selectSeveralDaysPosture(LocalDate from, LocalDate to, int days);

}