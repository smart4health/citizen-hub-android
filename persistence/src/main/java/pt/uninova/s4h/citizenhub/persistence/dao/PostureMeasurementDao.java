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
import pt.uninova.s4h.citizenhub.persistence.entity.util.ActivityDetailUtil;
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
    @Query("WITH agg AS( SELECT ((sample.timestamp - :localDate) / 3600000) % 24 AS hour, posture_measurement.classification AS classification, SUM(posture_measurement.duration) AS duration FROM posture_measurement INNER JOIN sample ON posture_measurement.sample_id = sample.id WHERE sample.timestamp >= :localDate AND sample.timestamp < :localDate + 86400000 GROUP BY classification, hour) SELECT agg.hour AS hour, (SELECT duration FROM agg AS aggi WHERE classification = 1 AND aggi.hour = agg.hour) AS correct_posture_duration, (SELECT duration FROM agg AS aggi WHERE classification = 2 AND aggi.hour = agg.hour) AS incorrect_posture_duration FROM agg GROUP BY agg.hour;")
    List<HourlyPosture> selectHourlyPosture(LocalDate localDate);

    @TypeConverters({EpochTypeConverter.class, DurationTypeConverter.class})
    @Query("WITH agg AS( SELECT ((sample.timestamp - :localDate - 3600000) / 3600000) % 24 AS hour, posture_measurement.classification AS classification, "
            + " SUM(posture_measurement.duration) AS duration FROM posture_measurement INNER JOIN sample ON posture_measurement.sample_id = sample.id "
            + " WHERE sample.timestamp >= :localDate AND sample.timestamp < :localDate + 86400000 AND classification = 1 GROUP BY hour) "
            + " SELECT duration AS value, hour AS time FROM agg")
    List<ActivityDetailUtil> selectLastDayCorrectPosture(LocalDate localDate);

    @TypeConverters({EpochTypeConverter.class, DurationTypeConverter.class})
    @Query("WITH agg AS( SELECT ((sample.timestamp - :localDate - 3600000) / 3600000) % 24 AS hour, posture_measurement.classification AS classification, "
            + " SUM(posture_measurement.duration) AS duration FROM posture_measurement INNER JOIN sample ON posture_measurement.sample_id = sample.id "
            + " WHERE sample.timestamp >= :localDate AND sample.timestamp < :localDate + 86400000 AND classification = 2 GROUP BY hour) "
            + " SELECT duration AS value, hour AS time FROM agg")
    List<ActivityDetailUtil> selectLastDayIncorrectPosture(LocalDate localDate);

    @TypeConverters({EpochTypeConverter.class, DurationTypeConverter.class})
    @Query("WITH agg AS( SELECT ((sample.timestamp - :from - 86400000) / 86400000) % 7 AS day, posture_measurement.classification AS classification, "
            + " SUM(posture_measurement.duration) AS duration FROM posture_measurement INNER JOIN sample ON posture_measurement.sample_id = sample.id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to + 86400000 AND classification = 1 GROUP BY day) "
            + " SELECT duration AS value, day AS time FROM agg")
    List<ActivityDetailUtil> selectLastSevenDaysCorrectPosture(LocalDate from, LocalDate to);

    @TypeConverters({EpochTypeConverter.class, DurationTypeConverter.class})
    @Query("WITH agg AS( SELECT ((sample.timestamp - :from - 86400000) / 86400000) % 7 AS day, posture_measurement.classification AS classification, "
            + " SUM(posture_measurement.duration) AS duration FROM posture_measurement INNER JOIN sample ON posture_measurement.sample_id = sample.id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to + 86400000 AND classification = 2 GROUP BY day) "
            + " SELECT duration AS value, day AS time FROM agg")
    List<ActivityDetailUtil> selectLastSevenDaysIncorrectPosture(LocalDate from, LocalDate to);

    @TypeConverters({EpochTypeConverter.class, DurationTypeConverter.class})
    @Query("WITH agg AS( SELECT ((sample.timestamp - :from - 86400000) / 86400000) % 30 AS hour, posture_measurement.classification AS classification, "
            + " SUM(posture_measurement.duration) AS duration FROM posture_measurement INNER JOIN sample ON posture_measurement.sample_id = sample.id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to + 86400000 AND classification = 1 GROUP BY hour) "
            + " SELECT duration AS value, hour AS time FROM agg")
    List<ActivityDetailUtil> selectLastThirtyDaysCorrectPosture(LocalDate from, LocalDate to);

    @TypeConverters({EpochTypeConverter.class, DurationTypeConverter.class})
    @Query("WITH agg AS( SELECT ((sample.timestamp - :from - 86400000) / 86400000) % 30 AS day, posture_measurement.classification AS classification, "
            + " SUM(posture_measurement.duration) AS duration FROM posture_measurement INNER JOIN sample ON posture_measurement.sample_id = sample.id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to + 86400000 AND classification = 2 GROUP BY day) "
            + " SELECT duration AS value, day AS time FROM agg")
    List<ActivityDetailUtil> selectLastThirtyDaysIncorrectPosture(LocalDate from, LocalDate to);

}