package pt.uninova.s4h.citizenhub.persistence;

import android.content.Context;

import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.DeleteColumn;
import androidx.room.DeleteTable;
import androidx.room.RenameColumn;
import androidx.room.RenameTable;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.AutoMigrationSpec;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pt.uninova.s4h.citizenhub.persistence.dao.BloodPressureMeasurementDao;
import pt.uninova.s4h.citizenhub.persistence.dao.CaloriesMeasurementDao;
import pt.uninova.s4h.citizenhub.persistence.dao.CaloriesSnapshotMeasurementDao;
import pt.uninova.s4h.citizenhub.persistence.dao.DeviceDao;
import pt.uninova.s4h.citizenhub.persistence.dao.DistanceSnapshotMeasurementDao;
import pt.uninova.s4h.citizenhub.persistence.dao.EnabledMeasurementDao;
import pt.uninova.s4h.citizenhub.persistence.dao.HeartRateMeasurementDao;
import pt.uninova.s4h.citizenhub.persistence.dao.LumbarExtensionTrainingDao;
import pt.uninova.s4h.citizenhub.persistence.dao.PostureMeasurementDao;
import pt.uninova.s4h.citizenhub.persistence.dao.PulseRateMeasurementDao;
import pt.uninova.s4h.citizenhub.persistence.dao.SampleDao;
import pt.uninova.s4h.citizenhub.persistence.dao.SettingDao;
import pt.uninova.s4h.citizenhub.persistence.dao.SmartBearUploadDateDao;
import pt.uninova.s4h.citizenhub.persistence.dao.StepsSnapshotMeasurementDao;
import pt.uninova.s4h.citizenhub.persistence.dao.TagDao;
import pt.uninova.s4h.citizenhub.persistence.entity.BloodPressureMeasurementRecord;
import pt.uninova.s4h.citizenhub.persistence.entity.CaloriesMeasurementRecord;
import pt.uninova.s4h.citizenhub.persistence.entity.CaloriesSnapshotMeasurementRecord;
import pt.uninova.s4h.citizenhub.persistence.entity.DeviceRecord;
import pt.uninova.s4h.citizenhub.persistence.entity.DistanceSnapshotMeasurementRecord;
import pt.uninova.s4h.citizenhub.persistence.entity.EnabledMeasurementRecord;
import pt.uninova.s4h.citizenhub.persistence.entity.HeartRateMeasurementRecord;
import pt.uninova.s4h.citizenhub.persistence.entity.LumbarExtensionTrainingMeasurementRecord;
import pt.uninova.s4h.citizenhub.persistence.entity.PostureMeasurementRecord;
import pt.uninova.s4h.citizenhub.persistence.entity.PulseRateMeasurementRecord;
import pt.uninova.s4h.citizenhub.persistence.entity.SampleRecord;
import pt.uninova.s4h.citizenhub.persistence.entity.SettingRecord;
import pt.uninova.s4h.citizenhub.persistence.entity.SmartBearUploadDateRecord;
import pt.uninova.s4h.citizenhub.persistence.entity.StepsSnapshotMeasurementRecord;
import pt.uninova.s4h.citizenhub.persistence.entity.TagRecord;

@Database(
        autoMigrations = {
                @AutoMigration(from = 36, to = 37, spec = CitizenHubDatabase.AutoMigrationFrom36To37.class),
                @AutoMigration(from = 37, to = 100, spec = CitizenHubDatabase.AutoMigrationFrom37To100.class)
        },
        entities = {
                BloodPressureMeasurementRecord.class,
                CaloriesMeasurementRecord.class,
                CaloriesSnapshotMeasurementRecord.class,
                DeviceRecord.class,
                DistanceSnapshotMeasurementRecord.class,
                EnabledMeasurementRecord.class,
                HeartRateMeasurementRecord.class,
                LumbarExtensionTrainingMeasurementRecord.class,
                PostureMeasurementRecord.class,
                PulseRateMeasurementRecord.class,
                SampleRecord.class,
                SettingRecord.class,
                SmartBearUploadDateRecord.class,
                StepsSnapshotMeasurementRecord.class,
                TagRecord.class
        },
        version = 100)
public abstract class CitizenHubDatabase extends RoomDatabase {

    @RenameColumn(tableName = "lumbar_training", fromColumnName = "trainingLength", toColumnName = "duration")
    @RenameTable(fromTableName = "lumbar_training", toTableName = "lumbar_extension_training_measurement")
    static class AutoMigrationFrom36To37 implements AutoMigrationSpec {
    }

    @DeleteTable.Entries({
            @DeleteTable(tableName = "feature"),
            @DeleteTable(tableName = "measurement")
    })
    @DeleteColumn(tableName = "device", columnName = "state")
    @DeleteColumn(tableName = "lumbar_extension_training_measurement", columnName = "calories")
    @DeleteColumn(tableName = "lumbar_extension_training_measurement", columnName = "timestamp")
    @RenameColumn(tableName = "device", fromColumnName = "type", toColumnName = "agent")
    static class AutoMigrationFrom37To100 implements AutoMigrationSpec {
    }

    private static final int NUMBER_OF_THREADS = 4;
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    private static volatile CitizenHubDatabase INSTANCE;

    public static CitizenHubDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (CitizenHubDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), CitizenHubDatabase.class, "citizen_hub_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }

        return INSTANCE;
    }

    public static ExecutorService executorService() {
        return EXECUTOR_SERVICE;
    }

    public abstract BloodPressureMeasurementDao bloodPressureMeasurementDao();

    public abstract CaloriesMeasurementDao caloriesMeasurementDao();

    public abstract CaloriesSnapshotMeasurementDao caloriesSnapshotMeasurementDao();

    public abstract DeviceDao deviceDao();

    public abstract DistanceSnapshotMeasurementDao distanceSnapshotMeasurementDao();

    public abstract EnabledMeasurementDao enabledMeasurementDao();

    public abstract HeartRateMeasurementDao heartRateMeasurementDao();

    public abstract LumbarExtensionTrainingDao lumbarExtensionTrainingDao();

    public abstract PostureMeasurementDao postureMeasurementDao();

    public abstract PulseRateMeasurementDao pulseRateMeasurementDao();

    public abstract SampleDao sampleDao();

    public abstract SettingDao settingDao();

    public abstract SmartBearUploadDateDao smartBearUploadDateDao();

    public abstract StepsSnapshotMeasurementDao stepsSnapshotMeasurementDao();

    public abstract TagDao tagDao();

}