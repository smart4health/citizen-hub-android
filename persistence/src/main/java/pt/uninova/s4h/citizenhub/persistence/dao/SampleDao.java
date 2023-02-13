package pt.uninova.s4h.citizenhub.persistence.dao;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.TypeConverters;
import pt.uninova.s4h.citizenhub.data.BloodPressureMeasurement;
import pt.uninova.s4h.citizenhub.data.BloodPressureValue;
import pt.uninova.s4h.citizenhub.data.BreathingRateMeasurement;
import pt.uninova.s4h.citizenhub.data.BreathingSequenceMeasurement;
import pt.uninova.s4h.citizenhub.data.BreathingValue;
import pt.uninova.s4h.citizenhub.data.CaloriesMeasurement;
import pt.uninova.s4h.citizenhub.data.CaloriesSnapshotMeasurement;
import pt.uninova.s4h.citizenhub.data.DistanceMeasurement;
import pt.uninova.s4h.citizenhub.data.DistanceSnapshotMeasurement;
import pt.uninova.s4h.citizenhub.data.HeartRateMeasurement;
import pt.uninova.s4h.citizenhub.data.LumbarExtensionTrainingMeasurement;
import pt.uninova.s4h.citizenhub.data.LumbarExtensionTrainingValue;
import pt.uninova.s4h.citizenhub.data.Measurement;
import pt.uninova.s4h.citizenhub.data.PostureMeasurement;
import pt.uninova.s4h.citizenhub.data.PostureValue;
import pt.uninova.s4h.citizenhub.data.PulseRateMeasurement;
import pt.uninova.s4h.citizenhub.data.Sample;
import pt.uninova.s4h.citizenhub.data.StepsMeasurement;
import pt.uninova.s4h.citizenhub.data.StepsSnapshotMeasurement;
import pt.uninova.s4h.citizenhub.persistence.CitizenHubDatabase;
import pt.uninova.s4h.citizenhub.persistence.conversion.EpochTypeConverter;
import pt.uninova.s4h.citizenhub.persistence.entity.SampleRecord;

@Dao
public abstract class SampleDao {

    private final BloodPressureMeasurementDao bloodPressureMeasurementDao;
    private final BreathingMeasurementDao breathingMeasurementDao;
    private final BreathingRateMeasurementDao breathingRateMeasurementDao;
    private final CaloriesMeasurementDao caloriesMeasurementDao;
    private final CaloriesSnapshotMeasurementDao caloriesSnapshotMeasurementDao;
    private final DistanceMeasurementDao distanceMeasurementDao;
    private final DistanceSnapshotMeasurementDao distanceSnapshotMeasurementDao;
    private final HeartRateMeasurementDao heartRateMeasurementDao;
    private final LumbarExtensionTrainingDao lumbarExtensionTrainingDao;
    private final PostureMeasurementDao postureMeasurementDao;
    private final PulseRateMeasurementDao pulseRateMeasurementDao;
    private final StepsMeasurementDao stepsMeasurementDao;
    private final StepsSnapshotMeasurementDao stepsSnapshotMeasurementDao;

    public SampleDao(CitizenHubDatabase database) {
        bloodPressureMeasurementDao = database.bloodPressureMeasurementDao();
        breathingMeasurementDao = database.breathingMeasurementDao();
        breathingRateMeasurementDao = database.breathingRateMeasurementDao();
        caloriesMeasurementDao = database.caloriesMeasurementDao();
        caloriesSnapshotMeasurementDao = database.caloriesSnapshotMeasurementDao();
        distanceMeasurementDao = database.distanceMeasurementDao();
        distanceSnapshotMeasurementDao = database.distanceSnapshotMeasurementDao();
        heartRateMeasurementDao = database.heartRateMeasurementDao();
        lumbarExtensionTrainingDao = database.lumbarExtensionTrainingDao();
        postureMeasurementDao = database.postureMeasurementDao();
        pulseRateMeasurementDao = database.pulseRateMeasurementDao();
        stepsMeasurementDao = database.stepsMeasurementDao();
        stepsSnapshotMeasurementDao = database.stepsSnapshotMeasurementDao();
    }

    @Insert
    public abstract long insert(SampleRecord record);

    @Query("INSERT INTO sample (device_id, timestamp) VALUES ((SELECT id FROM device WHERE device.address = :address), :timestamp)")
    @TypeConverters(EpochTypeConverter.class)
    public abstract long insert(String address, Instant timestamp);

    @Transaction
    public long insert(Sample sample) {
        if(sample == null || sample.getSource() == null || sample.getSource().getAddress() == null){
            return -1;
        }
        final long sampleId = insert(sample.getSource().getAddress(), sample.getTimestamp());

        for (Measurement<?> measurement : sample.getMeasurements()) {
            switch (measurement.getType()) {
                case Measurement.TYPE_BLOOD_PRESSURE:
                    final BloodPressureValue bloodPressureValue = ((BloodPressureMeasurement) measurement).getValue();

                    bloodPressureMeasurementDao.insert(sampleId, bloodPressureValue.getSystolic(), bloodPressureValue.getDiastolic(), bloodPressureValue.getMeanArterialPressure());
                    break;
                case Measurement.TYPE_BREATHING:
                    final BreathingSequenceMeasurement breathingSequenceMeasurement = (BreathingSequenceMeasurement) measurement;
                    int offset = 0;

                    for (BreathingValue i : breathingSequenceMeasurement.getValue()) {
                        breathingMeasurementDao.insert(sampleId, offset++, i.getType(), i.getValue());
                    }

                    break;
                case Measurement.TYPE_BREATHING_RATE:
                    final BreathingRateMeasurement breathingRateMeasurement = ((BreathingRateMeasurement) measurement);

                    breathingRateMeasurementDao.insert(sampleId, breathingRateMeasurement.getValue());
                    break;
                case Measurement.TYPE_CALORIES:
                    final CaloriesMeasurement caloriesMeasurement = (CaloriesMeasurement) measurement;

                    caloriesMeasurementDao.insert(sampleId, caloriesMeasurement.getValue());
                    break;
                case Measurement.TYPE_CALORIES_SNAPSHOT:
                    final CaloriesSnapshotMeasurement caloriesSnapshotMeasurement = (CaloriesSnapshotMeasurement) measurement;

                    caloriesSnapshotMeasurementDao.insert(sampleId, caloriesSnapshotMeasurement.getSnapshotType(), caloriesSnapshotMeasurement.getValue());
                    break;
                case Measurement.TYPE_DISTANCE:
                    final DistanceMeasurement distanceMeasurement = (DistanceMeasurement) measurement;
                    distanceMeasurementDao.insert(sampleId, distanceMeasurement.getValue());
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
                case Measurement.TYPE_STEPS:
                    final StepsMeasurement stepsMeasurement = (StepsMeasurement) measurement;
                    stepsMeasurementDao.insert(sampleId, stepsMeasurement.getValue());
                    break;
                case Measurement.TYPE_STEPS_SNAPSHOT:
                    final StepsSnapshotMeasurement stepsSnapshotMeasurement = (StepsSnapshotMeasurement) measurement;
                    stepsSnapshotMeasurementDao.insert(sampleId, stepsSnapshotMeasurement.getSnapshotType(), stepsSnapshotMeasurement.getValue());
                    break;
            }
        }

        return sampleId;
    }

    @Query("SELECT * FROM sample WHERE id = :sampleId")
    public abstract SampleRecord select(Long sampleId);

    @Query("SELECT DISTINCT (timestamp / 86400000 * 86400000) AS timestamp FROM sample WHERE timestamp >= :from AND timestamp < :to ORDER BY timestamp")
    @TypeConverters(EpochTypeConverter.class)
    public abstract List<LocalDate> select(LocalDate from, LocalDate to);

    @Query("SELECT COUNT(*) FROM sample WHERE timestamp >= :from AND timestamp < :to")
    @TypeConverters(EpochTypeConverter.class)
    public abstract LiveData<Integer> selectCountLiveData(LocalDate from, LocalDate to);

    @Query("SELECT timestamp FROM sample WHERE id == :sampleId")
    public abstract Long selectTimestampBasedOnId(Long sampleId);

}