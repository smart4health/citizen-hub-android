package pt.uninova.s4h.citizenhub.persistence.dao;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.TypeConverters;

import java.time.LocalDate;
import java.util.List;


import pt.uninova.s4h.citizenhub.persistence.conversion.EpochTypeConverter;
import pt.uninova.s4h.citizenhub.persistence.entity.util.ReportUtil;

@Dao
public interface ReportDao {

    @Query("SELECT dailyCalories.calories, dailyDistance.distance, "
            + " heartRate.maxHeartRate, heartRate.minHeartRate, heartRate.avgHeartRate, "
            + " correctPosture.correctPostureDuration, wrongPosture.wrongPostureDuration, "
            + " dailySteps.steps"
            + " FROM "
            + " (SELECT MAX(value) AS calories "
            + " FROM calories_snapshot_measurement INNER JOIN sample ON calories_snapshot_measurement.sample_id = sample.id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to) dailyCalories, "
            + " (SELECT MAX(value) AS distance "
            + " FROM distance_snapshot_measurement INNER JOIN sample ON distance_snapshot_measurement.sample_id = sample.id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to) dailyDistance, "
            + " (SELECT MAX(value) AS maxHeartRate, MIN(value) AS minHeartRate, AVG(value) AS avgHeartRate "
            + " FROM heart_rate_measurement INNER JOIN sample ON heart_rate_measurement.sample_id = sample.id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to) heartRate, "
            /*+ " (SELECT classification AS classification, SUM(duration) AS dailyPostureDuration "
            + " FROM posture_measurement INNER JOIN sample ON posture_measurement.sample_id = sample.id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to GROUP BY classification) posture, "*/
            + " (SELECT classification AS classification, SUM(duration) AS correctPostureDuration "
            + " FROM posture_measurement INNER JOIN sample ON posture_measurement.sample_id = sample.id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to AND classification = 1) correctPosture, "
            + "(SELECT classification AS classification, SUM(duration) AS wrongPostureDuration "
            + " FROM posture_measurement INNER JOIN sample ON posture_measurement.sample_id = sample.id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to AND classification = 2) wrongPosture, "
            + "(SELECT MAX(value) AS steps "
            + " FROM steps_snapshot_measurement INNER JOIN sample ON steps_snapshot_measurement.sample_id = sample.id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to) dailySteps")
    @TypeConverters(EpochTypeConverter.class)
    List<ReportUtil> getSimpleDailyRecords(LocalDate from, LocalDate to);

    @Query("WITH sample_window(id) AS (SELECT id FROM sample WHERE timestamp >= :from AND timestamp < :to), "
            + " sample_tag (id) AS (SELECT sample_window.id FROM sample_window INNER JOIN tag ON sample_window.id = tag.sample_id WHERE tag.label = :tag), "
            + " calories (value) "
            + " AS (SELECT SUM(value) FROM (SELECT SUM(calories_measurement.value) AS value FROM sample_tag "
            + " INNER JOIN calories_measurement ON sample_tag.id = calories_measurement.sample_id "
            + " UNION ALL SELECT MAX(calories_snapshot_measurement.value) AS value FROM sample_tag "
            + " INNER JOIN calories_snapshot_measurement ON sample_tag.id = calories_snapshot_measurement.sample_id)), "
            + " distance (value) "
            + " AS (SELECT SUM(value) FROM (SELECT SUM(distance_measurement.value) AS value FROM sample_tag "
            + " INNER JOIN distance_measurement ON sample_tag.id = distance_measurement.sample_id "
            + " UNION ALL SELECT MAX(distance_snapshot_measurement.value) AS value FROM sample_tag "
            + " INNER JOIN distance_snapshot_measurement ON sample_tag.id = distance_snapshot_measurement.sample_id)), "
            + " heart_rate (maximum, minimum, average) "
            + " AS (SELECT MAX(value) AS maximum, MIN(value) AS minimum, AVG(value) AS average FROM sample_tag "
            + " INNER JOIN heart_rate_measurement ON sample_tag.id = heart_rate_measurement.sample_id), "
            + " posture (classification, duration) "
            + " AS (SELECT classification, SUM(duration) FROM sample_tag "
            + " INNER JOIN posture_measurement ON sample_tag.id = posture_measurement.sample_id GROUP BY classification), "
            + " steps (value) "
            + " AS (SELECT SUM(value) FROM (SELECT SUM(steps_measurement.value) AS value FROM sample_tag "
            + " INNER JOIN steps_measurement ON sample_tag.id = steps_measurement.sample_id "
            + " UNION ALL SELECT MAX(steps_snapshot_measurement.value) AS value FROM sample_tag "
            + " INNER JOIN steps_snapshot_measurement ON sample_tag.id = steps_snapshot_measurement.sample_id)) "
            + " SELECT calories.value AS calories, distance.value AS distance, "
            + " heart_rate.maximum AS maxHeartRate, heart_rate.minimum AS minHeartRate, heart_rate.average AS avgHeartRate, "
            + " correct_posture.duration AS correctPostureDuration, incorrect_posture.duration AS wrongPostureDuration, "
            + " steps.value AS steps "
            + " FROM calories LEFT JOIN distance LEFT JOIN heart_rate "
            + " LEFT JOIN (SELECT duration FROM posture WHERE classification = 1) AS correct_posture "
            + " LEFT JOIN (SELECT duration FROM posture WHERE classification = 2) AS incorrect_posture LEFT JOIN steps;")
    @TypeConverters(EpochTypeConverter.class)
    ReportUtil getSummaryTagged(LocalDate from, LocalDate to, Integer tag);

    @Query("WITH sample_window(id) AS (SELECT id FROM sample WHERE timestamp >= :from AND timestamp < :to), "
            + " sample_tag (id) AS (SELECT sample_window.id FROM sample_window WHERE sample_window.id "
            + " NOT IN (SELECT sample_window.id FROM sample_window INNER JOIN tag ON sample_window.id = tag.sample_id WHERE tag.label = :tag)), "
            + " calories (value) "
            + " AS (SELECT SUM(value) FROM (SELECT SUM(calories_measurement.value) AS value FROM sample_tag "
            + " INNER JOIN calories_measurement ON sample_tag.id = calories_measurement.sample_id "
            + " UNION ALL SELECT MAX(calories_snapshot_measurement.value) AS value FROM sample_tag "
            + " INNER JOIN calories_snapshot_measurement ON sample_tag.id = calories_snapshot_measurement.sample_id)), "
            + " distance (value) "
            + " AS (SELECT SUM(value) FROM (SELECT SUM(distance_measurement.value) AS value FROM sample_tag "
            + " INNER JOIN distance_measurement ON sample_tag.id = distance_measurement.sample_id "
            + " UNION ALL SELECT MAX(distance_snapshot_measurement.value) AS value FROM sample_tag "
            + " INNER JOIN distance_snapshot_measurement ON sample_tag.id = distance_snapshot_measurement.sample_id)), "
            + " heart_rate (maximum, minimum, average) "
            + " AS (SELECT MAX(value) AS maximum, MIN(value) AS minimum, AVG(value) AS average FROM sample_tag "
            + " INNER JOIN heart_rate_measurement ON sample_tag.id = heart_rate_measurement.sample_id), "
            + " posture (classification, duration) "
            + " AS (SELECT classification, SUM(duration) FROM sample_tag "
            + " INNER JOIN posture_measurement ON sample_tag.id = posture_measurement.sample_id GROUP BY classification), "
            + " steps (value) "
            + " AS (SELECT SUM(value) FROM (SELECT SUM(steps_measurement.value) AS value FROM sample_tag "
            + " INNER JOIN steps_measurement ON sample_tag.id = steps_measurement.sample_id "
            + " UNION ALL SELECT MAX(steps_snapshot_measurement.value) AS value FROM sample_tag "
            + " INNER JOIN steps_snapshot_measurement ON sample_tag.id = steps_snapshot_measurement.sample_id)) "
            + " SELECT calories.value AS calories, distance.value AS distance, "
            + " heart_rate.maximum AS maxHeartRate, heart_rate.minimum AS minHeartRate, heart_rate.average AS avgHeartRate, "
            + " correct_posture.duration AS correctPostureDuration, incorrect_posture.duration AS wrongPostureDuration, "
            + " steps.value AS steps "
            + " FROM calories LEFT JOIN distance LEFT JOIN heart_rate "
            + " LEFT JOIN (SELECT duration FROM posture WHERE classification = 1) AS correct_posture "
            + " LEFT JOIN (SELECT duration FROM posture WHERE classification = 2) AS incorrect_posture LEFT JOIN steps;")
    @TypeConverters(EpochTypeConverter.class)
    ReportUtil getSummaryNotTagged(LocalDate from, LocalDate to, Integer tag);

    @Query("SELECT dailyCalories.calories, dailyDistance.distance, "
            + " heartRate.maxHeartRate, heartRate.minHeartRate, heartRate.avgHeartRate, "
            + " correctPosture.correctPostureDuration, wrongPosture.wrongPostureDuration, "
            + " dailySteps.steps "
            + " FROM "

            + " (SELECT MAX(value) AS calories FROM calories_snapshot_measurement "
            + " INNER JOIN sample ON calories_snapshot_measurement.sample_id = sample.id "
            + " INNER JOIN tag ON calories_snapshot_measurement.sample_id = tag.sample_id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to AND tag.label = 1) dailyCalories, "

            + " (SELECT MAX(value) AS distance FROM distance_snapshot_measurement "
            + " INNER JOIN sample ON distance_snapshot_measurement.sample_id = sample.id "
            + " INNER JOIN tag ON distance_snapshot_measurement.sample_id = tag.sample_id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to AND tag.label = 1) dailyDistance, "

            + " (SELECT MAX(value) AS maxHeartRate, MIN(value) AS minHeartRate, AVG(value) AS avgHeartRate FROM heart_rate_measurement "
            + " INNER JOIN sample ON heart_rate_measurement.sample_id = sample.id "
            + " INNER JOIN tag ON heart_rate_measurement.sample_id = tag.sample_id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to AND tag.label = 1) heartRate, "
            /*+ " (SELECT classification AS classification, SUM(duration) AS dailyPostureDuration "
            + " FROM posture_measurement INNER JOIN sample ON posture_measurement.sample_id = sample.id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to GROUP BY classification) posture, "*/
            + " (SELECT classification AS classification, SUM(duration) AS correctPostureDuration FROM posture_measurement "
            + " INNER JOIN sample ON posture_measurement.sample_id = sample.id "
            + " INNER JOIN tag ON posture_measurement.sample_id = tag.sample_id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to AND tag.label = 1 AND classification = 1) correctPosture, "

            + "(SELECT classification AS classification, SUM(duration) AS wrongPostureDuration FROM posture_measurement "
            + " INNER JOIN sample ON posture_measurement.sample_id = sample.id "
            + " INNER JOIN tag ON posture_measurement.sample_id = tag.sample_id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to AND tag.label = 1 AND classification = 2) wrongPosture, "

            + "(SELECT MAX(value) AS steps FROM steps_snapshot_measurement "
            + " INNER JOIN sample ON steps_snapshot_measurement.sample_id = sample.id "
            + " INNER JOIN tag ON steps_snapshot_measurement.sample_id = tag.sample_id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to AND tag.label = 1) dailySteps")
    @TypeConverters(EpochTypeConverter.class)
    ReportUtil getWorkTimeSimpleRecords(LocalDate from, LocalDate to);

    @Query("SELECT breathingRate.maxBreathingRate, breathingRate.minBreathingRate, breathingRate.avgBreathingRate, "
            + " dailyCalories.calories, dailyDistance.distance, "
            + " heartRate.maxHeartRate, heartRate.minHeartRate, heartRate.avgHeartRate, "
            + " correctPosture.correctPostureDuration, wrongPosture.wrongPostureDuration, "
            + " dailySteps.steps"
            + " FROM "

            + "(SELECT MAX(value) AS maxBreathingRate, MIN(value) AS minBreathingRate, AVG(value) AS avgBreathingRate FROM breathing_rate_measurement "
            + " INNER JOIN sample ON breathing_rate_measurement.sample_id = sample.id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to "
            + " AND NOT EXISTS "
            + " (SELECT tag.label FROM tag WHERE tag.sample_id = breathing_rate_measurement.sample_id AND tag.label = 1)) breathingRate,"

            + " (SELECT MAX(value) AS calories FROM calories_snapshot_measurement "
            + " INNER JOIN sample ON calories_snapshot_measurement.sample_id = sample.id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to "
            + " AND NOT EXISTS "
            + " (SELECT tag.label FROM tag WHERE tag.sample_id = calories_snapshot_measurement.sample_id AND tag.label = 1)) dailyCalories, "

            + " (SELECT MAX(value) AS distance FROM distance_snapshot_measurement "
            + " INNER JOIN sample ON distance_snapshot_measurement.sample_id = sample.id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to "
            + " AND NOT EXISTS "
            + " (SELECT tag.label FROM tag WHERE tag.sample_id = distance_snapshot_measurement.sample_id AND tag.label = 1)) dailyDistance, "

            + " (SELECT MAX(value) AS maxHeartRate, MIN(value) AS minHeartRate, AVG(value) AS avgHeartRate FROM heart_rate_measurement "
            + " INNER JOIN sample ON heart_rate_measurement.sample_id = sample.id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to "
            + " AND NOT EXISTS "
            + " (SELECT tag.label FROM tag WHERE tag.sample_id = heart_rate_measurement.sample_id AND tag.label = 1)) heartRate, "
            /*+ " (SELECT classification AS classification, SUM(duration) AS dailyPostureDuration "
            + " FROM posture_measurement INNER JOIN sample ON posture_measurement.sample_id = sample.id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to GROUP BY classification) posture, "*/
            + " (SELECT classification AS classification, SUM(duration) AS correctPostureDuration FROM posture_measurement "
            + " INNER JOIN sample ON posture_measurement.sample_id = sample.id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to AND classification = 1 "
            + " AND NOT EXISTS "
            + " (SELECT tag.label FROM tag WHERE tag.sample_id = posture_measurement.sample_id AND tag.label = 1)) correctPosture, "

            + " (SELECT classification AS classification, SUM(duration) AS wrongPostureDuration FROM posture_measurement "
            + " INNER JOIN sample ON posture_measurement.sample_id = sample.id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to AND classification = 2 "
            + " AND NOT EXISTS "
            + "(SELECT tag.label FROM tag WHERE tag.sample_id = posture_measurement.sample_id AND tag.label = 1)) wrongPosture, "

            + "(SELECT MAX(value) AS steps FROM steps_snapshot_measurement "
            + " INNER JOIN sample ON steps_snapshot_measurement.sample_id = sample.id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to "
            + " AND NOT EXISTS "
            + "(SELECT tag.label FROM tag WHERE tag.sample_id = steps_snapshot_measurement.sample_id AND tag.label = 1)) dailySteps")
    @TypeConverters(EpochTypeConverter.class)
    ReportUtil getNotWorkTimeSimpleRecords(LocalDate from, LocalDate to);

    @Query("SELECT breathingRate.maxBreathingRate, breathingRate.minBreathingRate, breathingRate.avgBreathingRate, "
            + " caloriesData.calories, distanceData.distance, "
            + " correctPosture.correctPostureDuration, wrongPosture.wrongPostureDuration, "
            + " stepsData.steps "
            + " FROM "

            + "(SELECT MAX(value) AS maxBreathingRate, MIN(value) AS minBreathingRate, AVG(value) AS avgBreathingRate FROM breathing_rate_measurement "
            + " INNER JOIN sample ON breathing_rate_measurement.sample_id = sample.id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to "
            + " AND NOT EXISTS "
            + " (SELECT tag.label FROM tag WHERE tag.sample_id = breathing_rate_measurement.sample_id AND tag.label = 1)) breathingRate,"

            + " (SELECT SUM(value) AS calories FROM (SELECT ((sample.timestamp - :from) / 86400000) % :days AS days, MAX(calories_snapshot_measurement.value) AS value "
            + " FROM calories_snapshot_measurement INNER JOIN sample ON calories_snapshot_measurement.sample_id = sample.id "
            + " INNER JOIN tag ON calories_snapshot_measurement.sample_id = tag.sample_id "
            + " UNION ALL SELECT ((sample.timestamp - :from) / 86400000) % :days AS days, SUM(calories_measurement.value) AS value "
            + " FROM calories_measurement INNER JOIN sample ON calories_measurement.sample_id = sample.id "
            + " INNER JOIN tag ON calories_measurement.sample_id = tag.sample_id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to AND tag.label = 1 GROUP BY days)) caloriesData, "

            + " (SELECT SUM(value) AS distance FROM (SELECT ((sample.timestamp - :from) / 86400000) % :days AS days, MAX(distance_snapshot_measurement.value) AS value "
            + " FROM distance_snapshot_measurement INNER JOIN sample ON distance_snapshot_measurement.sample_id = sample.id "
            + " INNER JOIN tag ON distance_snapshot_measurement.sample_id = tag.sample_id "
            + " UNION ALL SELECT ((sample.timestamp - :from) / 86400000) % :days AS days, SUM(distance_measurement.value) AS value "
            + " FROM distance_measurement INNER JOIN sample ON distance_measurement.sample_id = sample.id "
            + " INNER JOIN tag ON distance_measurement.sample_id = tag.sample_id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to AND tag.label = 1 GROUP BY days)) distanceData, "

            /*+ " (SELECT classification AS classification, SUM(duration) AS dailyPostureDuration "
            + " FROM posture_measurement INNER JOIN sample ON posture_measurement.sample_id = sample.id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to GROUP BY classification) posture, "*/
            + " (SELECT classification AS classification, SUM(duration) AS correctPostureDuration FROM posture_measurement "
            + " INNER JOIN sample ON posture_measurement.sample_id = sample.id "
            + " INNER JOIN tag ON posture_measurement.sample_id = tag.sample_id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to AND tag.label = 1 AND classification = 1) correctPosture, "

            + "(SELECT classification AS classification, SUM(duration) AS wrongPostureDuration FROM posture_measurement "
            + " INNER JOIN sample ON posture_measurement.sample_id = sample.id "
            + " INNER JOIN tag ON posture_measurement.sample_id = tag.sample_id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to AND tag.label = 1 AND classification = 2) wrongPosture, "

            + " (SELECT SUM(value) AS steps FROM (SELECT ((sample.timestamp - :from) / 86400000) % :days AS days, MAX(steps_snapshot_measurement.value) AS value "
            + " FROM steps_snapshot_measurement INNER JOIN sample ON steps_snapshot_measurement.sample_id = sample.id "
            + " INNER JOIN tag ON steps_snapshot_measurement.sample_id = tag.sample_id "
            + " UNION ALL SELECT ((sample.timestamp - :from) / 86400000) % :days AS days, SUM(steps_measurement.value) AS value "
            + " FROM steps_measurement INNER JOIN sample ON steps_measurement.sample_id = sample.id "
            + " INNER JOIN tag ON steps_measurement.sample_id = tag.sample_id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to AND tag.label = 1 GROUP BY days)) stepsData ")
    @TypeConverters(EpochTypeConverter.class)
    ReportUtil getWeeklyOrMonthlyWorkTimeSimpleRecords(LocalDate from, LocalDate to, int days);

    @Query("SELECT breathingRate.maxBreathingRate, breathingRate.minBreathingRate, breathingRate.avgBreathingRate, "
            + " caloriesData.calories, distanceData.distance, "
            + " correctPosture.correctPostureDuration, wrongPosture.wrongPostureDuration, "
            + " stepsData.steps "
            + " FROM "

            + "(SELECT MAX(value) AS maxBreathingRate, MIN(value) AS minBreathingRate, AVG(value) AS avgBreathingRate FROM breathing_rate_measurement "
            + " INNER JOIN sample ON breathing_rate_measurement.sample_id = sample.id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to "
            + " AND NOT EXISTS "
            + " (SELECT tag.label FROM tag WHERE tag.sample_id = breathing_rate_measurement.sample_id AND tag.label = 1)) breathingRate,"

            + " (SELECT SUM(value) AS calories FROM (SELECT ((sample.timestamp - :from) / 86400000) % :days AS days, MAX(calories_snapshot_measurement.value) AS value "
            + " FROM calories_snapshot_measurement INNER JOIN sample ON calories_snapshot_measurement.sample_id = sample.id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to "
            + " AND NOT EXISTS "
            + "(SELECT tag.label FROM tag WHERE tag.sample_id = sample.id AND tag.label = 1) GROUP BY days "
            + " UNION ALL SELECT ((sample.timestamp - :from) / 86400000) % :days AS days, SUM(calories_measurement.value) AS value "
            + " FROM calories_measurement INNER JOIN sample ON calories_measurement.sample_id = sample.id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to "
            + " AND NOT EXISTS "
            + "(SELECT tag.label FROM tag WHERE tag.sample_id = sample.id AND tag.label = 1) GROUP BY days)) caloriesData, "

            + " (SELECT SUM(value) AS distance FROM (SELECT ((sample.timestamp - :from) / 86400000) % :days AS days, MAX(distance_snapshot_measurement.value) AS value "
            + " FROM distance_snapshot_measurement INNER JOIN sample ON distance_snapshot_measurement.sample_id = sample.id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to "
            + " AND NOT EXISTS "
            + "(SELECT tag.label FROM tag WHERE tag.sample_id = sample.id AND tag.label = 1) GROUP BY days "
            + " UNION ALL SELECT ((sample.timestamp - :from) / 86400000) % :days AS days, SUM(distance_measurement.value) AS value "
            + " FROM distance_measurement INNER JOIN sample ON distance_measurement.sample_id = sample.id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to "
            + " AND NOT EXISTS "
            + "(SELECT tag.label FROM tag WHERE tag.sample_id = sample.id AND tag.label = 1) GROUP BY days)) distanceData, "

            /*+ " (SELECT classification AS classification, SUM(duration) AS dailyPostureDuration "
            + " FROM posture_measurement INNER JOIN sample ON posture_measurement.sample_id = sample.id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to GROUP BY classification) posture, "*/
            + " (SELECT classification AS classification, SUM(duration) AS correctPostureDuration FROM posture_measurement "
            + " INNER JOIN sample ON posture_measurement.sample_id = sample.id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to AND classification = 1 "
            + " AND NOT EXISTS "
            + " (SELECT tag.label FROM tag WHERE tag.sample_id = posture_measurement.sample_id AND tag.label = 1)) correctPosture, "

            + " (SELECT classification AS classification, SUM(duration) AS wrongPostureDuration FROM posture_measurement "
            + " INNER JOIN sample ON posture_measurement.sample_id = sample.id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to AND classification = 2 "
            + " AND NOT EXISTS "
            + "(SELECT tag.label FROM tag WHERE tag.sample_id = posture_measurement.sample_id AND tag.label = 1)) wrongPosture, "

            + " (SELECT SUM(value) AS steps FROM (SELECT ((sample.timestamp - :from) / 86400000) % :days AS days, MAX(steps_snapshot_measurement.value) AS value "
            + " FROM steps_snapshot_measurement INNER JOIN sample ON steps_snapshot_measurement.sample_id = sample.id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to "
            + " AND NOT EXISTS "
            + "(SELECT tag.label FROM tag WHERE tag.sample_id = sample.id AND tag.label = 1) GROUP BY days "
            + " UNION ALL SELECT ((sample.timestamp - :from) / 86400000) % :days AS days, SUM(steps_measurement.value) AS value "
            + " FROM steps_measurement INNER JOIN sample ON steps_measurement.sample_id = sample.id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to "
            + " AND NOT EXISTS "
            + "(SELECT tag.label FROM tag WHERE tag.sample_id = sample.id AND tag.label = 1) GROUP BY days)) stepsData")
    @TypeConverters(EpochTypeConverter.class)
    ReportUtil getWeeklyOrMonthlyNotWorkTimeSimpleRecords(LocalDate from, LocalDate to, int days);

    @Query("SELECT diastolic, systolic, mean_arterial_pressure AS meanArterialPressure, sample.timestamp AS timestamp "
            + " FROM blood_pressure_measurement INNER JOIN sample ON blood_pressure_measurement.sample_id = sample.id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to ORDER BY timestamp")
    @TypeConverters(EpochTypeConverter.class)
    List<ReportUtil> getBloodPressure(LocalDate from, LocalDate to);

    @Query("SELECT diastolic, systolic, mean_arterial_pressure AS meanArterialPressure, sample.timestamp AS timestamp, "
            + " pulse_rate_measurement.value AS pulseRate FROM blood_pressure_measurement "
            + " INNER JOIN sample ON blood_pressure_measurement.sample_id = sample.id "
            + " LEFT JOIN pulse_rate_measurement ON blood_pressure_measurement.sample_id = pulse_rate_measurement.sample_id "
            + " INNER JOIN tag ON blood_pressure_measurement.sample_id = tag.sample_id "
            + "WHERE sample.timestamp >= :from AND sample.timestamp < :to AND tag.label = 1 ORDER BY timestamp")
    @TypeConverters(EpochTypeConverter.class)
    List<ReportUtil> getWorkTimeBloodPressure(LocalDate from, LocalDate to);

    @Query("SELECT diastolic, systolic, mean_arterial_pressure AS meanArterialPressure, sample.timestamp AS timestamp, "
            + " pulse_rate_measurement.value AS pulseRate FROM blood_pressure_measurement "
            + " INNER JOIN sample ON blood_pressure_measurement.sample_id = sample.id "
            + " LEFT JOIN pulse_rate_measurement ON blood_pressure_measurement.sample_id = pulse_rate_measurement.sample_id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to "
            + " AND NOT EXISTS "
            + "(SELECT tag.label FROM tag WHERE tag.sample_id = blood_pressure_measurement.sample_id AND tag.label = 1) ORDER BY timestamp")
    @TypeConverters(EpochTypeConverter.class)
    List<ReportUtil> getNotWorkTimeBloodPressure(LocalDate from, LocalDate to);

    @Query("SELECT MAX(value) AS calories FROM calories_snapshot_measurement INNER JOIN sample ON calories_snapshot_measurement.sample_id = sample.id WHERE sample.timestamp >= :from AND sample.timestamp < :to")
    @TypeConverters(EpochTypeConverter.class)
    List<ReportUtil> getCalories(LocalDate from, LocalDate to);

    @Query("SELECT MAX(value) AS distance FROM distance_snapshot_measurement INNER JOIN sample ON distance_snapshot_measurement.sample_id = sample.id WHERE sample.timestamp >= :from AND sample.timestamp < :to")
    @TypeConverters(EpochTypeConverter.class)
    List<ReportUtil> getDistance(LocalDate from, LocalDate to);

    @Query("SELECT AVG(heart_rate_measurement.value) AS avgHeartRate, MAX(heart_rate_measurement.value) AS maxHeartRate, MIN(heart_rate_measurement.value) AS minHeartRate FROM heart_rate_measurement INNER JOIN sample ON heart_rate_measurement.sample_id = sample.id WHERE sample.timestamp >= :from AND sample.timestamp < :to")
    @TypeConverters(EpochTypeConverter.class)
    List<ReportUtil> getHeartRate(LocalDate from, LocalDate to);

    @Query("SELECT duration AS lumbarExtensionDuration, score AS lumbarExtensionScore, repetitions AS lumbarExtensionRepetitions, "
            + " weight AS lumbarExtensionWeight, sample.timestamp AS timestamp, calories_measurement.value AS calories "
            + " FROM lumbar_extension_training_measurement INNER JOIN sample ON lumbar_extension_training_measurement.sample_id = sample.id "
            + " LEFT JOIN calories_measurement ON sample.id = calories_measurement.sample_id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to ORDER BY timestamp")
    @TypeConverters(EpochTypeConverter.class)
    List<ReportUtil> getLumbarExtensionTraining(LocalDate from, LocalDate to);

    @Query("SELECT duration AS lumbarExtensionDuration, score AS lumbarExtensionScore, repetitions AS lumbarExtensionRepetitions, "
            + " weight AS lumbarExtensionWeight, sample.timestamp AS timestamp, calories_measurement.value AS calories "
            + " FROM lumbar_extension_training_measurement "
            + " INNER JOIN sample ON lumbar_extension_training_measurement.sample_id = sample.id "
            + " LEFT JOIN calories_measurement ON lumbar_extension_training_measurement.sample_id = calories_measurement.sample_id "
            + " INNER JOIN tag ON lumbar_extension_training_measurement.sample_id = tag.sample_id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to AND tag.label = 1 ORDER BY timestamp")
    @TypeConverters(EpochTypeConverter.class)
    List<ReportUtil> getWorkTimeLumbarExtensionTraining(LocalDate from, LocalDate to);

    @Query("SELECT duration AS lumbarExtensionDuration, score AS lumbarExtensionScore, repetitions AS lumbarExtensionRepetitions, "
            + " weight AS lumbarExtensionWeight, sample.timestamp AS timestamp, calories_measurement.value AS calories "
            + " FROM lumbar_extension_training_measurement "
            + " INNER JOIN sample ON lumbar_extension_training_measurement.sample_id = sample.id "
            + " LEFT JOIN calories_measurement ON lumbar_extension_training_measurement.sample_id = calories_measurement.sample_id "
            + " WHERE sample.timestamp >= :from AND sample.timestamp < :to AND NOT EXISTS "
            + " (SELECT * FROM tag WHERE tag.sample_id = lumbar_extension_training_measurement.sample_id "
            + " AND tag.label = 1) ORDER BY timestamp")
    @TypeConverters(EpochTypeConverter.class)
    List<ReportUtil> getNotWorkTimeLumbarExtensionTraining(LocalDate from, LocalDate to);

    @Query("SELECT posture_measurement.classification AS postureClassification, SUM(posture_measurement.duration) AS postureDuration FROM posture_measurement INNER JOIN sample ON posture_measurement.sample_id = sample.id WHERE sample.timestamp >= :from AND sample.timestamp < :to GROUP BY classification")
    @TypeConverters(EpochTypeConverter.class)
    List<ReportUtil> getPosture(LocalDate from, LocalDate to);

    @Query("SELECT MAX(value) AS steps FROM steps_snapshot_measurement INNER JOIN sample ON steps_snapshot_measurement.sample_id = sample.id WHERE sample.timestamp >= :from AND sample.timestamp < :to")
    @TypeConverters(EpochTypeConverter.class)
    List<ReportUtil> getSteps(LocalDate from, LocalDate to);

    @Query("SELECT MAX(value) AS calories, sample.id AS id FROM calories_snapshot_measurement INNER JOIN sample ON calories_snapshot_measurement.sample_id = sample.id WHERE sample.timestamp >= :from AND sample.timestamp < :to ")
    @TypeConverters(EpochTypeConverter.class)
    List<ReportUtil> getSampleID(LocalDate from, LocalDate to);
}
