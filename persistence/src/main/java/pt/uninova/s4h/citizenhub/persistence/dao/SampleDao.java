package pt.uninova.s4h.citizenhub.persistence.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.TypeConverters;

import java.time.Instant;
import java.time.LocalDate;

import pt.uninova.s4h.citizenhub.data.BloodPressureMeasurement;
import pt.uninova.s4h.citizenhub.data.BloodPressureValue;
import pt.uninova.s4h.citizenhub.data.CaloriesMeasurement;
import pt.uninova.s4h.citizenhub.data.CaloriesSnapshotMeasurement;
import pt.uninova.s4h.citizenhub.data.DistanceSnapshotMeasurement;
import pt.uninova.s4h.citizenhub.data.HeartRateMeasurement;
import pt.uninova.s4h.citizenhub.data.LumbarExtensionTrainingMeasurement;
import pt.uninova.s4h.citizenhub.data.LumbarExtensionTrainingValue;
import pt.uninova.s4h.citizenhub.data.Measurement;
import pt.uninova.s4h.citizenhub.data.PostureMeasurement;
import pt.uninova.s4h.citizenhub.data.PostureValue;
import pt.uninova.s4h.citizenhub.data.PulseRateMeasurement;
import pt.uninova.s4h.citizenhub.data.Sample;
import pt.uninova.s4h.citizenhub.data.StepsSnapshotMeasurement;
import pt.uninova.s4h.citizenhub.persistence.CitizenHubDatabase;
import pt.uninova.s4h.citizenhub.persistence.conversion.EpochTypeConverter;
import pt.uninova.s4h.citizenhub.persistence.entity.SampleRecord;

@Dao
public abstract class SampleDao {

    private final BloodPressureMeasurementDao bloodPressureMeasurementDao;
    private final CaloriesMeasurementDao caloriesMeasurementDao;
    private final CaloriesSnapshotMeasurementDao caloriesSnapshotMeasurementDao;
    private final DistanceSnapshotMeasurementDao distanceSnapshotMeasurementDao;
    private final HeartRateMeasurementDao heartRateMeasurementDao;
    private final LumbarExtensionTrainingDao lumbarExtensionTrainingDao;
    private final PostureMeasurementDao postureMeasurementDao;
    private final PulseRateMeasurementDao pulseRateMeasurementDao;
    private final StepsSnapshotMeasurementDao stepsSnapshotMeasurementDao;

    public SampleDao(CitizenHubDatabase database) {
        bloodPressureMeasurementDao = database.bloodPressureMeasurementDao();
        caloriesMeasurementDao = database.caloriesMeasurementDao();
        caloriesSnapshotMeasurementDao = database.caloriesSnapshotMeasurementDao();
        distanceSnapshotMeasurementDao = database.distanceSnapshotMeasurementDao();
        heartRateMeasurementDao = database.heartRateMeasurementDao();
        lumbarExtensionTrainingDao = database.lumbarExtensionTrainingDao();
        postureMeasurementDao = database.postureMeasurementDao();
        pulseRateMeasurementDao = database.pulseRateMeasurementDao();
        stepsSnapshotMeasurementDao = database.stepsSnapshotMeasurementDao();
    }

    @Query("SELECT COUNT(*) > 0 FROM sample WHERE timestamp >= :from AND timestamp < :to")
    @TypeConverters(EpochTypeConverter.class)
    public abstract LiveData<Boolean> hasRows(LocalDate from, LocalDate to);

    @Insert
    public abstract long insert(SampleRecord sampleRecord);

    @Query("INSERT INTO sample (device_id, timestamp) VALUES ((SELECT id FROM device WHERE device.address = :address), :timestamp)")
    @TypeConverters(EpochTypeConverter.class)
    public abstract long insert(String address, Instant timestamp);

    @Transaction
    public long insert(Sample sample) {
        final long sampleId = insert(sample.getSource().getAddress(), sample.getTimestamp());

        for (Measurement<?> measurement : sample.getMeasurements()) {
            switch (measurement.getType()) {
                case Measurement.TYPE_BLOOD_PRESSURE:
                    final BloodPressureValue bloodPressureValue = ((BloodPressureMeasurement) measurement).getValue();

                    bloodPressureMeasurementDao.insert(sampleId, bloodPressureValue.getSystolic(), bloodPressureValue.getDiastolic(), bloodPressureValue.getMeanArterialPressure());
                    break;
                case Measurement.TYPE_CALORIES:
                    final CaloriesMeasurement caloriesMeasurement = (CaloriesMeasurement) measurement;

                    caloriesMeasurementDao.insert(sampleId, caloriesMeasurement.getValue());
                    break;
                case Measurement.TYPE_CALORIES_SNAPSHOT:
                    final CaloriesSnapshotMeasurement caloriesSnapshotMeasurement = (CaloriesSnapshotMeasurement) measurement;

                    caloriesSnapshotMeasurementDao.insert(sampleId, caloriesSnapshotMeasurement.getSnapshotType(), caloriesSnapshotMeasurement.getValue());
                    break;
                case Measurement.TYPE_DISTANCE_SNAPSHOT:
                    final DistanceSnapshotMeasurement distanceSnapshotMeasurement = (DistanceSnapshotMeasurement) measurement;

                    distanceSnapshotMeasurementDao.insert(sampleId, distanceSnapshotMeasurement.getSnapshotType(), distanceSnapshotMeasurement.getValue());
                    break;
                case Measurement.TYPE_HEART_RATE:
                    final Integer heartRateValue = ((HeartRateMeasurement) measurement).getValue();

                    heartRateMeasurementDao.insert(sampleId, heartRateValue);
                    break;
                case Measurement.TYPE_LUMBAR_EXTENSION_TRAINING:
                    final LumbarExtensionTrainingValue lumbarExtensionTrainingValue = ((LumbarExtensionTrainingMeasurement) measurement).getValue();

                    lumbarExtensionTrainingDao.insert(sampleId, lumbarExtensionTrainingValue.getDuration(), lumbarExtensionTrainingValue.getScore(), lumbarExtensionTrainingValue.getRepetitions(), lumbarExtensionTrainingValue.getWeight());
                    break;
                case Measurement.TYPE_POSTURE:
                    final PostureValue postureValue = ((PostureMeasurement) measurement).getValue();

                    postureMeasurementDao.insert(sampleId, postureValue.getClassification(), postureValue.getDuration());
                    break;
                case Measurement.TYPE_PULSE_RATE:
                    final Double pulseRateValue = ((PulseRateMeasurement) measurement).getValue();

                    pulseRateMeasurementDao.insert(sampleId, pulseRateValue);
                    break;
                case Measurement.TYPE_STEPS_SNAPSHOT:
                    final StepsSnapshotMeasurement stepsSnapshotMeasurement = (StepsSnapshotMeasurement) measurement;

                    stepsSnapshotMeasurementDao.insert(sampleId, stepsSnapshotMeasurement.getSnapshotType(), stepsSnapshotMeasurement.getValue());
                    break;
            }
        }

        return sampleId;
    }
}

