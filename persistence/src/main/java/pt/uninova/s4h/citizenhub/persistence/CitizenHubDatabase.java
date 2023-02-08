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
import pt.uninova.s4h.citizenhub.persistence.dao.BreathingMeasurementDao;
import pt.uninova.s4h.citizenhub.persistence.dao.BreathingRateMeasurementDao;
import pt.uninova.s4h.citizenhub.persistence.dao.CaloriesMeasurementDao;
import pt.uninova.s4h.citizenhub.persistence.dao.CaloriesSnapshotMeasurementDao;
import pt.uninova.s4h.citizenhub.persistence.dao.DeviceDao;
import pt.uninova.s4h.citizenhub.persistence.dao.DistanceMeasurementDao;
import pt.uninova.s4h.citizenhub.persistence.dao.DistanceSnapshotMeasurementDao;
import pt.uninova.s4h.citizenhub.persistence.dao.HeartRateMeasurementDao;
import pt.uninova.s4h.citizenhub.persistence.dao.LumbarExtensionTrainingDao;
import pt.uninova.s4h.citizenhub.persistence.dao.PostureMeasurementDao;
import pt.uninova.s4h.citizenhub.persistence.dao.PulseRateMeasurementDao;
import pt.uninova.s4h.citizenhub.persistence.dao.ReportDao;
import pt.uninova.s4h.citizenhub.persistence.dao.SampleDao;
import pt.uninova.s4h.citizenhub.persistence.dao.SettingDao;
import pt.uninova.s4h.citizenhub.persistence.dao.Smart4HealthDailyReportDao;
import pt.uninova.s4h.citizenhub.persistence.dao.Smart4HealthMonthlyReportDao;
import pt.uninova.s4h.citizenhub.persistence.dao.Smart4HealthWeeklyReportDao;
import pt.uninova.s4h.citizenhub.persistence.dao.SmartBearDailyReportDao;
import pt.uninova.s4h.citizenhub.persistence.dao.StepsMeasurementDao;
import pt.uninova.s4h.citizenhub.persistence.dao.StepsSnapshotMeasurementDao;
import pt.uninova.s4h.citizenhub.persistence.dao.StreamDao;
import pt.uninova.s4h.citizenhub.persistence.dao.TagDao;
import pt.uninova.s4h.citizenhub.persistence.entity.BloodPressureMeasurementRecord;
import pt.uninova.s4h.citizenhub.persistence.entity.BreathingMeasurementRecord;
import pt.uninova.s4h.citizenhub.persistence.entity.BreathingRateMeasurementRecord;
import pt.uninova.s4h.citizenhub.persistence.entity.CaloriesMeasurementRecord;
import pt.uninova.s4h.citizenhub.persistence.entity.CaloriesSnapshotMeasurementRecord;
import pt.uninova.s4h.citizenhub.persistence.entity.DeviceRecord;
import pt.uninova.s4h.citizenhub.persistence.entity.DistanceMeasurementRecord;
import pt.uninova.s4h.citizenhub.persistence.entity.DistanceSnapshotMeasurementRecord;
import pt.uninova.s4h.citizenhub.persistence.entity.HeartRateMeasurementRecord;
import pt.uninova.s4h.citizenhub.persistence.entity.LumbarExtensionTrainingMeasurementRecord;
import pt.uninova.s4h.citizenhub.persistence.entity.PostureMeasurementRecord;
import pt.uninova.s4h.citizenhub.persistence.entity.PulseRateMeasurementRecord;
import pt.uninova.s4h.citizenhub.persistence.entity.SampleRecord;
import pt.uninova.s4h.citizenhub.persistence.entity.SettingRecord;
import pt.uninova.s4h.citizenhub.persistence.entity.Smart4HealthDailyReportRecord;
import pt.uninova.s4h.citizenhub.persistence.entity.Smart4HealthMonthlyReportRecord;
import pt.uninova.s4h.citizenhub.persistence.entity.Smart4HealthWeeklyReportRecord;
import pt.uninova.s4h.citizenhub.persistence.entity.SmartBearDailyReportRecord;
import pt.uninova.s4h.citizenhub.persistence.entity.StepsMeasurementRecord;
import pt.uninova.s4h.citizenhub.persistence.entity.StepsSnapshotMeasurementRecord;
import pt.uninova.s4h.citizenhub.persistence.entity.StreamRecord;
import pt.uninova.s4h.citizenhub.persistence.entity.TagRecord;

@Database(
        autoMigrations = {
                @AutoMigration(from = 36, to = 37, spec = CitizenHubDatabase.AutoMigrationFrom36To37.class),
                @AutoMigration(from = 37, to = 100, spec = CitizenHubDatabase.AutoMigrationFrom37To100.class),
                @AutoMigration(from = 100, to = 101, spec = CitizenHubDatabase.AutoMigrationFrom100To101.class),
                @AutoMigration(from = 101, to = 102, spec = CitizenHubDatabase.AutoMigrationFrom101To102.class),
                @AutoMigration(from = 102, to = 103, spec = CitizenHubDatabase.AutoMigrationFrom102To103.class),
                @AutoMigration(from = 103, to = 104, spec = CitizenHubDatabase.AutoMigrationFrom103To104.class),
                @AutoMigration(from = 104, to = 105, spec = CitizenHubDatabase.AutoMigrationFrom104To105.class)
        },
        entities = {
                BloodPressureMeasurementRecord.class,
                BreathingMeasurementRecord.class,
                BreathingRateMeasurementRecord.class,
                CaloriesMeasurementRecord.class,
                CaloriesSnapshotMeasurementRecord.class,
                DeviceRecord.class,
                DistanceMeasurementRecord.class,
                DistanceSnapshotMeasurementRecord.class,
                HeartRateMeasurementRecord.class,
                LumbarExtensionTrainingMeasurementRecord.class,
                PostureMeasurementRecord.class,
                PulseRateMeasurementRecord.class,
                SampleRecord.class,
                SettingRecord.class,
                Smart4HealthDailyReportRecord.class,
                Smart4HealthMonthlyReportRecord.class,
                Smart4HealthWeeklyReportRecord.class,
                SmartBearDailyReportRecord.class,
                StepsMeasurementRecord.class,
                StepsSnapshotMeasurementRecord.class,
                StreamRecord.class,
                TagRecord.class
        },
        version = 105)
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

    static class AutoMigrationFrom100To101 implements AutoMigrationSpec {
    }

    @RenameTable(fromTableName = "enabled_measurement", toTableName = "stream")
    @RenameTable(fromTableName = "smart_bear_upload_date", toTableName = "smart_bear_daily_report")
    @DeleteColumn(tableName = "tag", columnName = "id")
    static class AutoMigrationFrom101To102 implements AutoMigrationSpec {
    }

    static class AutoMigrationFrom102To103 implements AutoMigrationSpec {
    }

    static class AutoMigrationFrom103To104 implements AutoMigrationSpec {
    }

    static class AutoMigrationFrom104To105 implements AutoMigrationSpec {
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

    public abstract BreathingMeasurementDao breathingMeasurementDao();

    public abstract BreathingRateMeasurementDao breathingRateMeasurementDao();

    public abstract BloodPressureMeasurementDao bloodPressureMeasurementDao();

    public abstract CaloriesMeasurementDao caloriesMeasurementDao();

    public abstract CaloriesSnapshotMeasurementDao caloriesSnapshotMeasurementDao();

    public abstract DeviceDao deviceDao();

    public abstract DistanceMeasurementDao distanceMeasurementDao();

    public abstract DistanceSnapshotMeasurementDao distanceSnapshotMeasurementDao();

    public abstract HeartRateMeasurementDao heartRateMeasurementDao();

    public abstract LumbarExtensionTrainingDao lumbarExtensionTrainingDao();

    public abstract PostureMeasurementDao postureMeasurementDao();

    public abstract PulseRateMeasurementDao pulseRateMeasurementDao();

    public abstract ReportDao reportDao();

    public abstract SampleDao sampleDao();

    public abstract SettingDao settingDao();

    public abstract Smart4HealthDailyReportDao smart4HealthDailyReportDao();

    public abstract Smart4HealthWeeklyReportDao smart4HealthWeeklyReportDao();

    public abstract Smart4HealthMonthlyReportDao smart4HealthMonthlyReportDao();

    public abstract SmartBearDailyReportDao smartBearUploadDateDao();

    public abstract StepsMeasurementDao stepsMeasurementDao();

    public abstract StepsSnapshotMeasurementDao stepsSnapshotMeasurementDao();

    public abstract StreamDao enabledMeasurementDao();

    public abstract TagDao tagDao();

}