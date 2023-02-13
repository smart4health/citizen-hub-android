package pt.uninova.s4h.citizenhub.persistence.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.TypeConverters;

import java.time.LocalDate;
import java.util.List;

import pt.uninova.s4h.citizenhub.persistence.conversion.EpochTypeConverter;
import pt.uninova.s4h.citizenhub.persistence.entity.Smart4HealthWeeklyReportRecord;

@Dao
public interface Smart4HealthWeeklyReportDao {

    @Insert
    long insert(Smart4HealthWeeklyReportRecord record);

    @Query("INSERT OR REPLACE INTO smart4health_weekly_report (year, week, fhir) VALUES (:year, :week, :value);")
    long insertOrReplaceFhir(Integer year, Integer week, Boolean value);

    @Query("INSERT OR REPLACE INTO smart4health_weekly_report (year, week, pdf) VALUES (:year, :week, :value);")
    long insertOrReplacePdf(Integer year, Integer week, Boolean value);

    @Query("SELECT * FROM smart4health_weekly_report ORDER BY year DESC, week DESC LIMIT 1;")
    @TypeConverters(EpochTypeConverter.class)
    Smart4HealthWeeklyReportRecord selectLastWeekUploaded();

    @Query("SELECT DISTINCT (timestamp / 86400000) * 86400000 FROM sample EXCEPT SELECT date FROM smart4health_daily_report;")
    @TypeConverters(EpochTypeConverter.class)
    List<LocalDate> selectWeeksWithValues();
}